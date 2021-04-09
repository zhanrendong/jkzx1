package tech.tongyu.bct.workflow.process.func;

import tech.tongyu.bct.workflow.dto.process.CommonProcessData;
import tech.tongyu.bct.workflow.dto.process.ProcessData;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.Task;

public interface Action {

    CommonProcessData execute(Process process, Task task, UserDTO userDTO, CommonProcessData elderProcessData, CommonProcessData processData, String starter);
    
}
