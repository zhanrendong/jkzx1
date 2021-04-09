package tech.tongyu.bct.workflow.process.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.process.manager.self.RequestManager;
import tech.tongyu.bct.workflow.process.service.RequestService;

import java.util.Map;

@Service
public class RequestServiceImpl implements RequestService {

    private RequestManager requestManager;

    @Autowired
    public RequestServiceImpl(RequestManager requestManager) {
        this.requestManager = requestManager;
    }


    @Override
    public Object callService(UserDTO user, String processName, Map<String, Object> params) {
        return requestManager.callService(user.getUserName(), processName, params);
    }
}
