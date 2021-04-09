package tech.tongyu.bct.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.workflow.process.ProcessConstants;

public class TaskInstanceDTO {

    @JsonProperty(ProcessConstants.PROCESS_INSTANCE)
    private ProcessInstanceDTO processInstanceDTO;

    private String taskId;
    private String taskName;

    public ProcessInstanceDTO getProcessInstanceDTO() {
        return processInstanceDTO;
    }

    public void setProcessInstanceDTO(ProcessInstanceDTO processInstanceDTO) {
        this.processInstanceDTO = processInstanceDTO;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}
