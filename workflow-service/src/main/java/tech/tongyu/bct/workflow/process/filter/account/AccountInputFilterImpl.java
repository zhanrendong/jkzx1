package tech.tongyu.bct.workflow.process.filter.account;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.process.filter.TaskReadableFilter;
import tech.tongyu.bct.workflow.process.manager.ProcessInstanceManager;
/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Component
public class AccountInputFilterImpl implements TaskReadableFilter {

    private ProcessInstanceManager processInstanceManager;

    @Autowired
    public AccountInputFilterImpl(ProcessInstanceManager processInstanceManager){
        this.processInstanceManager = processInstanceManager;
    }

    @Override
    public Boolean canTaskRead(UserDTO userDTO, Task task) {
        ProcessInstance processInstance = processInstanceManager.getProcessInstanceByTask(task);
        return userDTO.getUserName().equals(processInstance.getStartUserId());
    }
}
