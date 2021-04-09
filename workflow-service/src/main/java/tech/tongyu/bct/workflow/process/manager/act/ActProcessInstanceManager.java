package tech.tongyu.bct.workflow.process.manager.act;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;

import java.util.Collection;
import java.util.List;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Component
public class ActProcessInstanceManager {

    private static Logger logger = LoggerFactory.getLogger(ActProcessInstanceManager.class);

    private RuntimeService runtimeService;

    @Autowired
    public ActProcessInstanceManager(RuntimeService runtimeService){
        this.runtimeService = runtimeService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearAllProcessInstance(){
        List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
        for(ProcessInstance processInstance : processInstances){
            logger.info("\t=> clear all process instance: {}", processInstance.getId());
            runtimeService.deleteProcessInstance(processInstance.getId(), "reset system");
        }
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ProcessInstance getProcessInstanceByTask(Task task){
        String processInstanceId = task.getProcessInstanceId();
        Collection<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
                .active()
                .includeProcessVariables()
                .processInstanceId(processInstanceId)
                .list();

        return checkProcessInstanceSingleResult(processInstances, processInstanceId);
    }

    /**
     * try to figure out whether there are one or more process instances having not been finished.
     * @param processName process's name
     * @return true or false
     */
    public Boolean hasOngoingProcessInstance(String processName){
        return runtimeService.createProcessInstanceQuery()
                .active()
                .processDefinitionName(processName)
                .list().size() > 0;
    }

    private static ProcessInstance checkProcessInstanceSingleResult(Collection<ProcessInstance> processInstances, String processInstanceId){
        // same with code in ActHistoricProcessInstanceManager class
        if(CollectionUtils.isEmpty(processInstances)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.UNKNOWN_PROCESS_INSTANCE, processInstanceId);
        }
        if(processInstances.size() > 1) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.MULTI_PROCESS_INSTANCE_BYID, processInstanceId);
        }

        return (ProcessInstance) processInstances.toArray()[0];
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ProcessInstance getProcessInstanceByProcessInstanceId(String processInstanceId){
        return checkProcessInstanceSingleResult(runtimeService.createProcessInstanceQuery()
                .active()
                .includeProcessVariables()
                .processInstanceId(processInstanceId)
                .list(), processInstanceId);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public Collection<ProcessInstance> listProcessInstanceByStarter(String username){
        return runtimeService.createProcessInstanceQuery()
                .active()
                .includeProcessVariables()
                .startedBy(username)
                .list();
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public Collection<ProcessInstance> listProcessInstanceByInvolvedUser(String username){
        return runtimeService.createProcessInstanceQuery()
                .active()
                .includeProcessVariables()
                .involvedUser(username)
                .list();
    }

    @Transactional(rollbackFor = Exception.class)
    public void terminateProcessInstanceByProcessInstanceId(String processInstanceId){
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public Collection<ProcessInstance> listProcessInstanceByProcessName(String processName){
        return runtimeService.createProcessInstanceQuery()
                .active()
                .includeProcessVariables()
                .processDefinitionName(processName)
                .list();
    }

}
