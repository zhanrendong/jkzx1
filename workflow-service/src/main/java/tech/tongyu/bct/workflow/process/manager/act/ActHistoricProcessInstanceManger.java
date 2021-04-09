package tech.tongyu.bct.workflow.process.manager.act;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;

import java.util.Collection;
import java.util.List;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 * @author yongbin
 */
@Component
public class ActHistoricProcessInstanceManger {
    private HistoryService historyService;

    @Autowired
    public ActHistoricProcessInstanceManger(HistoryService historyService) {
        this.historyService = historyService;
    }

    /**
     * get historic process instance, throws exception if not found or multiple results found.
     * @param processInstanceId -> id of historic process instance
     * @return historic process instance
     */
    public HistoricProcessInstance getHistoricByProcessInstanceId(String processInstanceId){
        return  checkProcessInstanceSingleResult(
                historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .includeProcessVariables()
                        .list()
                , processInstanceId);
    }

    /**
     * 获取流程中已经执行的节点，按照执行先后顺序排序
     * @param processInstanceId -> id of process instance
     * @return -> list of historic activity of process instance
     */
    public List<HistoricActivityInstance> getHistoricActivityNode(String processInstanceId){
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceId()
                .asc()
                .list();
    }

    private static HistoricProcessInstance checkProcessInstanceSingleResult(Collection<HistoricProcessInstance> processInstances, String processInstanceId){
        if(CollectionUtils.isEmpty(processInstances)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.UNKNOWN_PROCESS_INSTANCE, processInstanceId);
        }
        if(processInstances.size() > 1) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.MULTI_PROCESS_INSTANCE_BYID, processInstanceId);
        }
        return (HistoricProcessInstance) processInstances.toArray()[0];
    }

    public List<HistoricProcessInstance> listHistoricProcessInstanceByStarter(String username){
        return historyService.createHistoricProcessInstanceQuery()
                .startedBy(username)
                .includeProcessVariables()
                .finished()
                .list();
    }

    public List<HistoricProcessInstance> listHistoricProcessInstanceByInvolvedUser(String username){
        return historyService.createHistoricProcessInstanceQuery()
                .involvedUser(username)
                .includeProcessVariables()
                .finished()
                .list();
    }

}
