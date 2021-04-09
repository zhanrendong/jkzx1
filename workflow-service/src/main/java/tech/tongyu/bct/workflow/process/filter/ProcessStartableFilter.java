package tech.tongyu.bct.workflow.process.filter;

import org.activiti.engine.repository.ProcessDefinition;
import tech.tongyu.bct.workflow.dto.process.ProcessData;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.process.Process;

public interface ProcessStartableFilter extends Filter{

    Boolean canProcessStart(UserDTO userDTO, Process process, ProcessData processData);

    Boolean canProcessStart(UserDTO userDTO, ProcessDefinition processDefinition, ProcessData processData);
}
