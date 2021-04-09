package tech.tongyu.bct.workflow.process.filter;

import org.activiti.engine.task.Task;
import tech.tongyu.bct.workflow.dto.process.ProcessData;
import tech.tongyu.bct.workflow.dto.UserDTO;

public interface TaskReadableFilter extends Filter{

    Boolean canTaskRead(UserDTO userDTO, Task task);
}
