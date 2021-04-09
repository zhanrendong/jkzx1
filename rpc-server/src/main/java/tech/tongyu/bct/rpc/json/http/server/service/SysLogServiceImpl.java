package tech.tongyu.bct.rpc.json.http.server.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.dto.SysLogDTO;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.manager.UserManager;
import tech.tongyu.bct.auth.service.SysLogService;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.api.response.RpcResponseListPaged;
import tech.tongyu.bct.rpc.json.http.server.manager.SysLogManager;
import tech.tongyu.bct.rpc.json.http.server.util.ApiParamConstants;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

@Service
public class SysLogServiceImpl implements SysLogService {

    private SysLogManager sysLogManager;
    private UserManager userManager;

    @Autowired
    public SysLogServiceImpl(SysLogManager sysLogManager, UserManager userManager) {
        this.sysLogManager = sysLogManager;
        this.userManager = userManager;
    }

    @Override
    @BctMethodInfo(
            description = "根据条件查询日志列表",
            retName = "Collection<SysLogDTO>",
            retDescription = "日志列表",
            returnClass = SysLogDTO.class,
            service = "auth-service"
    )
    @Transactional
    public RpcResponseListPaged<SysLogDTO> authSysLogList(
            @BctMethodArg(description = "页码") Integer page,
            @BctMethodArg(description = "页距") Integer pageSize,
            @BctMethodArg(name = ApiParamConstants.USERNAME, description = "用户名", required = false) String username,
            @BctMethodArg(name = ApiParamConstants.OPERATION, description = "操作", required = false) String operation,
            @BctMethodArg(description = "开始时间") String startDate,
            @BctMethodArg(description = "结束时间") String endDate
    ) {
        return sysLogManager.getSysLogList(page, pageSize, username,
                operation, StringUtils.isBlank(startDate) ? null : LocalDate.parse(startDate),
                StringUtils.isBlank(endDate) ? null : LocalDate.parse(endDate));
    }

    @BctMethodInfo(
            description = "保存系统日志",
            retName = "Boolean",
            retDescription = "true or false",
            returnClass = Boolean.class,
            service = "auth-service"
    )
    public Boolean authSysLogSave(
            @BctMethodArg(name = ApiParamConstants.USERNAME, description = "用户名", required = false) String username,
            @BctMethodArg(name = ApiParamConstants.OPERATION, description = "用户操作") String operation,
            @BctMethodArg(description = "服务名") String service,
            @BctMethodArg(description = "请求方法") String method,
            @BctMethodArg(description = "请求参数", required = false) Map<String, Object> params,
            @BctMethodArg(description = "执行时长(毫秒)", required = false) String executionTimeInMillis
    ) {
        if (StringUtils.isEmpty(username)){
            UserDTO currentUser = userManager.getCurrentUser();
            if (!Objects.isNull(currentUser)){
                username = currentUser.getUsername();
            }
        }
        sysLogManager.save(new SysLogDTO(username, operation, service, method, params,
                StringUtils.isEmpty(executionTimeInMillis) ? 0 : Long.parseLong(executionTimeInMillis)));
        return true;
    }
}
