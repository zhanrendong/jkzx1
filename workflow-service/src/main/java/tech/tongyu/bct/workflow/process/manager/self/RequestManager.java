package tech.tongyu.bct.workflow.process.manager.self;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.acl.utils.TokenUtils;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.manager.UserManager;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.rpc.json.http.client.util.HttpClient;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.repo.ProcessRepo;
import tech.tongyu.bct.workflow.process.repo.RequestRepo;
import tech.tongyu.bct.workflow.process.repo.entities.ProcessDbo;
import tech.tongyu.bct.workflow.process.repo.entities.RequestDbo;

import java.util.List;
import java.util.Map;

import static tech.tongyu.bct.workflow.process.ProcessConstants.*;
import static tech.tongyu.bct.workflow.process.ProcessConstants.TRADE;
import static tech.tongyu.bct.workflow.process.trigger.TriggerConstants.*;

@Component
public class RequestManager {

    private static Logger logger = LoggerFactory.getLogger(RequestManager.class);

    @Value("${settings.issuer}")
    private String issuer;

    @Value("${settings.secret}")
    private String secret;

    private HttpClient httpClient;
    private UserManager userManager;
    private RequestRepo requestRepo;
    private ProcessRepo processRepo;

    @Autowired
    public RequestManager(
            HttpClient httpClient
            , UserManager userManager
            , RequestRepo requestRepo
            , ProcessRepo processRepo) {
        this.httpClient = httpClient;
        this.userManager = userManager;
        this.requestRepo = requestRepo;
        this.processRepo = processRepo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveRequest(String processId, String service, String method) {
        RequestDbo requestDbo = new RequestDbo(processId, service, method);
        requestRepo.save(requestDbo);
    }

    public Object callService(String username, String processName, Map<String, Object> params) {
        UserDTO user = userManager.getUserByUserName(username);
        //TokenUtils 生成的token才会生效 -> 不能使用CommonUtils
        String token = TokenUtils.generateToken(user.getUsername(), issuer, 300, secret, false);

        ProcessDbo processDbo = processRepo.findValidProcessDboByProcessName(processName)
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.UNKNOWN_PROCESS_NAME, processName));
        RequestDbo dbo = requestRepo.findValidRequestDboByProcessId(processDbo.getId())
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.UNABLE_COMPLETE_TASK_REQUEST, processName));
        String router = "/api/rpc";
        Map<String, Object> data = Maps.newHashMap();
        data.put("method", dbo.getMethod());
        data.put("params", params);

        logger.info("\t=> 调用" + dbo.getService() + ": {}", params);
        logger.info("\t==> 参数为: {}", params);
        Object result = httpClient.postTemplateNoneServiceInfo(dbo.getService(), router, JsonUtils.toJson(data), token);
        logger.info("\t==> 返回结果为: {}", result);
        if (StringUtils.equals(processName, TRADE_APPROVAL)) {
            return createAccountTasks(params, token);
        } else {
            return result;
        }
    }

    private Object createAccountTasks(Map<String, Object> param, String token) {
        String router = "/api/rpc";
        Map<String, Object> trade = (Map<String, Object>) param.get(TRADE);
        List<Map<String, Object>> positions = (List<Map<String, Object>>) trade.get(POSITIONS);
        Map<String, Object> map = positions.get(0);
        String counterPartyCode = (String) map.get(COUNTER_PARTY_CODE);
        String tradeId = (String) trade.get(TRADE_ID);

        Map<String, Object> data = Maps.newHashMap();
        Map<String, Object> params = Maps.newHashMap();

        params.put(TRADE_ID, tradeId);
        params.put(LEGAL_NAME, counterPartyCode);

        data.put("method", "cliTasksGenerateByTradeId");
        data.put("params", params);

        return httpClient.postTemplateNoneServiceInfo("reference-data-service", router, JsonUtils.toJson(data), token);
    }
}