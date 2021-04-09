package tech.tongyu.bct.workflow.process.filter.cap;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.workflow.auth.AuthenticationService;
import tech.tongyu.bct.workflow.dto.process.ProcessData;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.filter.ProcessStartableFilter;
import tech.tongyu.bct.workflow.process.filter.TaskCompletableFilter;
import tech.tongyu.bct.workflow.process.filter.TaskReadableFilter;
import tech.tongyu.bct.workflow.process.manager.ProcessInstanceManager;
import tech.tongyu.bct.workflow.process.manager.ProcessManager;
import tech.tongyu.bct.workflow.process.manager.TaskNodeManager;
import tech.tongyu.bct.workflow.process.manager.self.ProcessConfigManager;

import java.util.Objects;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Component
public class FundInputReviewFilterImpl implements TaskReadableFilter, TaskCompletableFilter, ProcessStartableFilter {

    private AuthenticationService authenticationService;
    private ProcessInstanceManager processInstanceManager;
    private TaskNodeManager taskNodeManager;
    private ProcessConfigManager processConfigManager;
    private ProcessManager processManager;

    @Autowired
    public FundInputReviewFilterImpl(
            AuthenticationService authenticationService
            , ProcessInstanceManager processInstanceManager
            , TaskNodeManager taskNodeManager
            , ProcessManager processManager
            , ProcessConfigManager processConfigManager){
        this.authenticationService = authenticationService;
        this.processInstanceManager = processInstanceManager;
        this.taskNodeManager = taskNodeManager;
        this.processManager = processManager;
        this.processConfigManager = processConfigManager;
    }

    @Override
    public Boolean canProcessStart(UserDTO userDTO, Process process, ProcessData processData) {
        return taskNodeManager.isInTaskApproveGroup(process.getInputTask(), userDTO.getUserName());
    }

    @Override
    public Boolean canProcessStart(UserDTO userDTO, ProcessDefinition processDefinition, ProcessData processData) {
        return true;
    }

    @Override
    public Boolean canTaskComplete(UserDTO userDTO, Task task, ProcessData processData) {
        return isNotStartedBySelfIfConfigEnabled(userDTO, task);
    }

    @Override
    public Boolean canTaskRead(UserDTO userDTO, Task task) {
        return isNotStartedBySelfIfConfigEnabled(userDTO, task);
    }

    private Boolean isNotStartedBySelfIfConfigEnabled(UserDTO userDTO, Task task){
        Process process = processManager.getProcessByProcessDefinitionId(task.getProcessDefinitionId());
        if (processConfigManager.getProcessConfigStatus(process.getProcessId(), "can_start_by_self")){
            ProcessInstance processInstance = processInstanceManager.getProcessInstanceByTask(task);
            String self = processInstance.getStartUserId();
            UserDTO starter = authenticationService.authenticateByUsername(self);
            return !Objects.equals(starter.getUserName(), userDTO.getUserName());
        }
        return true;
    }
}
