package tech.tongyu.bct.workflow.process.func;

import tech.tongyu.bct.workflow.dto.process.ProcessData;
import tech.tongyu.bct.workflow.dto.UserDTO;

public interface Modifier {

    ProcessData modify(UserDTO userDTO, ProcessData processData);
}
