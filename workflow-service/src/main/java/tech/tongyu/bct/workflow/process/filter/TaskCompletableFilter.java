package tech.tongyu.bct.workflow.process.filter;

import org.activiti.engine.task.Task;
import tech.tongyu.bct.workflow.dto.process.ProcessData;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface TaskCompletableFilter extends Filter{

    /**
     * to show whether a user-defined task can be completed or not
     * @param userDTO user who try to complete the task
     * @param task the task will be completed
     * @param processData the params used to completed the task
     * @return true or false
     */
    Boolean canTaskComplete(UserDTO userDTO, Task task, ProcessData processData);
}
