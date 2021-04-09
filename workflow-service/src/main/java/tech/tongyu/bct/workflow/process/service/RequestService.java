package tech.tongyu.bct.workflow.process.service;

import tech.tongyu.bct.workflow.dto.UserDTO;

import java.util.Map;

public interface RequestService {

    Object callService(UserDTO user, String processName, Map<String, Object> params);

}
