package tech.tongyu.bct.auth.service;

import tech.tongyu.bct.auth.dto.ErrorLogDTO;
import tech.tongyu.bct.common.api.response.RpcResponseListPaged;

public interface ErrorLogService {

    RpcResponseListPaged<ErrorLogDTO> authErrorLogList(Integer page, Integer pageSize, String errorType,
                                                       String requestMethod, String startDate, String endDate);

    void authCreateErrorLog(String errorType, String errorMessage, String requestMethod, String requestParams,
                            String errorStackTrace);

    void authDeleteErrorLog(String startDate);
}