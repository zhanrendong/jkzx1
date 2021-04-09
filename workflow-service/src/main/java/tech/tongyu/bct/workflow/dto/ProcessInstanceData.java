package tech.tongyu.bct.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;
import tech.tongyu.bct.workflow.dto.process.ProcessData;
import tech.tongyu.bct.workflow.process.ProcessConstants;
import tech.tongyu.bct.workflow.process.enums.ProcessInstanceStatusEnum;

import java.util.List;

public class ProcessInstanceData {

    @JsonProperty(ProcessConstants.PROCESS_INSTANCE)
    private ProcessInstanceDTO processInstanceDTO;

    @JsonProperty(ProcessConstants.TASK_HISTORY)
    private List<TaskHistoryDTO> taskHistoryDTOList;

    @JsonProperty(ProcessConstants.PROCESS)
    private ProcessData processData;

    private CurrentNodeDTO currentNodeDTO;

    public ProcessInstanceDTO getProcessInstanceDTO() {
        return processInstanceDTO;
    }

    public void setProcessInstanceDTO(ProcessInstanceDTO processInstanceDTO) {
        this.processInstanceDTO = processInstanceDTO;
    }

    public List<TaskHistoryDTO> getTaskHistoryDTOList() {
        return taskHistoryDTOList;
    }

    public void setTaskHistoryDTOList(List<TaskHistoryDTO> taskHistoryDTOList) {
        this.taskHistoryDTOList = taskHistoryDTOList;
    }

    public ProcessData getProcessData() {
        return processData;
    }

    public void setProcessData(CommonProcessData processData) {
        this.processData = processData;
    }

    public CurrentNodeDTO getCurrentNodeDTO() {
        return currentNodeDTO;
    }

    public void setCurrentNodeDTO(CurrentNodeDTO currentNodeDTO) {
        this.currentNodeDTO = currentNodeDTO;
    }
}
