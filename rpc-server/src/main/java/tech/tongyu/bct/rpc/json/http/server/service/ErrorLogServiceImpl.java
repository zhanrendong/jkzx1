package tech.tongyu.bct.rpc.json.http.server.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.dto.ErrorLogDTO;
import tech.tongyu.bct.auth.service.ErrorLogService;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.api.response.RpcResponseListPaged;
import tech.tongyu.bct.rpc.json.http.server.manager.ErrorLogManager;
import tech.tongyu.bct.rpc.json.http.server.util.ApiParamConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Service
public class ErrorLogServiceImpl implements ErrorLogService {

    private ErrorLogManager errorLogManager;

    @Autowired
    public ErrorLogServiceImpl(ErrorLogManager errorLogManager) {
        this.errorLogManager = errorLogManager;
    }

    @Override
    @BctMethodInfo(
            description = "根据条件查询错误日志列表",
            retName = "RpcResponseListPaged<ErrorLogDTO>",
            retDescription = "错误日志列表",
            returnClass = ErrorLogDTO.class,
            service = "auth-service"
    )
    @Transactional(rollbackFor = Exception.class)
    public RpcResponseListPaged<ErrorLogDTO> authErrorLogList(
            @BctMethodArg(description = "页码") Integer page,
            @BctMethodArg(description = "页距") Integer pageSize,
            @BctMethodArg(name = ApiParamConstants.ERROR_TYPE, description = "操作人", required = false) String username,
            @BctMethodArg(name = ApiParamConstants.REQUEST_METHOD, description = "请求方法", required = false) String requestMethod,
            @BctMethodArg(description = "开始时间") String startDate,
            @BctMethodArg(description = "结束时间") String endDate
    ) {
        return errorLogManager.getErrorLogList(page, pageSize, username,
                requestMethod, StringUtils.isBlank(startDate) ? null : LocalDate.parse(startDate),
                StringUtils.isBlank(endDate) ? null : LocalDate.parse(endDate));
    }

    @Override
    @BctMethodInfo(
            description = "创建错误日志",
            service = "auth-service"
    )
    @Transactional(rollbackFor = Exception.class)
    public void authCreateErrorLog(
            @BctMethodArg(description = "操作人") String username,
            @BctMethodArg(description = "错误信息") String errorMessage,
            @BctMethodArg(description = "请求方法") String requestMethod,
            @BctMethodArg(description = "请求参数") String requestParams,
            @BctMethodArg(description = "具体信息") String errorStackTrace
    ) {
        ErrorLogDTO errorLogDTO = new ErrorLogDTO(username, errorMessage, requestMethod, requestParams, errorStackTrace);
        errorLogManager.save(errorLogDTO);
    }

    @Override
    @BctMethodInfo(
            description = "根据日期删除日期以前的错误日志列表",
            service = "auth-service"
    )
    @Transactional(rollbackFor = Exception.class)
    public void authDeleteErrorLog(
            @BctMethodArg(description = "开始时间") String startDate
    ) {
        errorLogManager.clearErrorLogByCreateTime(StringUtils.isBlank(startDate) ? null : LocalDateTime.parse(startDate));
    }

}
