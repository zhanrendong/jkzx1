package tech.tongyu.bct.auth.service;

import tech.tongyu.bct.auth.dto.SysLogDTO;
import tech.tongyu.bct.common.api.response.RpcResponseListPaged;

public interface SysLogService {

    RpcResponseListPaged<SysLogDTO> authSysLogList(Integer page, Integer pageSize, String username,
                                                   String operation, String startDate, String endDate);

}
