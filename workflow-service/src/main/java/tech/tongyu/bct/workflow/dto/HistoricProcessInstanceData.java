package tech.tongyu.bct.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;
import tech.tongyu.bct.workflow.dto.process.ProcessData;
import tech.tongyu.bct.workflow.process.ProcessConstants;

import java.util.List;

public class HistoricProcessInstanceData {

    @JsonProperty(ProcessConstants.PROCESS_INSTANCE)
    private HistoricProcessInstanceDTO historicProcessInstanceDTO;

    @JsonProperty(ProcessConstants.TASK_HISTORY)
    private List<TaskHistoryDTO> taskHistoryDTOList;

    @JsonProperty(ProcessConstants.PROCESS)
    private ProcessData processData;

    public HistoricProcessInstanceDTO getHistoricProcessInstanceDTO() {
        return historicProcessInstanceDTO;
    }

    public void setHistoricProcessInstanceDTO(HistoricProcessInstanceDTO historicProcessInstanceDTO) {
        this.historicProcessInstanceDTO = historicProcessInstanceDTO;
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
}
