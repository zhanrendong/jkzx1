package tech.tongyu.bct.workflow.process.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.workflow.auth.AuthenticationService;
import tech.tongyu.bct.workflow.dto.HistoricProcessInstanceDTO;
import tech.tongyu.bct.workflow.dto.HistoricProcessInstanceData;
import tech.tongyu.bct.workflow.process.service.HistoricProcessInstanceService;

import java.util.Collection;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 * @author yongbin
 */
@Component
public class HistoricProcessInstanceApi {

    private HistoricProcessInstanceService historicProcessInstanceService;
    private AuthenticationService authenticationService;

    @Autowired
    public HistoricProcessInstanceApi(
            HistoricProcessInstanceService historicProcessInstanceService
            , AuthenticationService authenticationService){
        this.historicProcessInstanceService = historicProcessInstanceService;
        this.authenticationService = authenticationService;
    }

    /**
     * list historic process instance
     * @return collection of historic process instance data
     */
    @BctMethodInfo
    public Collection<HistoricProcessInstanceDTO> wkHistoricProcessInstanceList(){
        return historicProcessInstanceService.listHistoricProcessInstanceByUser(authenticationService.authenticateCurrentUser());
    }

    /**
     * get the data of historic process instance (completed)
     * 1. historic data of workflow (activity node, etc)
     * 2. form data of process instance
     * @param processInstanceId process instance's id
     * @return historic process instance's form data
     */
    @BctMethodInfo
    public HistoricProcessInstanceData wkHistoricProcessInstanceFormGet(
            @BctMethodArg String processInstanceId){
        return historicProcessInstanceService.getHistoricProcessInstanceDataByProcessInstanceId(processInstanceId);
    }
}
