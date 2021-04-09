package tech.tongyu.bct.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.workflow.process.ProcessConstants;

import java.time.LocalDateTime;

public class HistoricProcessInstanceDTO {

    public HistoricProcessInstanceDTO() { }

    public HistoricProcessInstanceDTO(UserDTO operator, String startTime, String endTime,
                                      String processName, String processInstanceId, String processSequenceNum,
                                      String subject) {
        this.operator = operator;
        this.startTime = startTime;
        this.endTime = endTime;
        this.processName = processName;
        this.processInstanceId = processInstanceId;
        this.processSequenceNum = processSequenceNum;
        this.subject = subject;
    }

    @JsonProperty(ProcessConstants.OPERATOR)
    private UserDTO operator;
    private String startTime;
    private String endTime;
    private String deleteReason;
    private String processName;
    private String processInstanceId;
    private String processSequenceNum;
    private String subject;

    public String getProcessSequenceNum() {
        return processSequenceNum;
    }

    public void setProcessSequenceNum(String processSequenceNum) {
        this.processSequenceNum = processSequenceNum;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public UserDTO getOperator() {
        return operator;
    }

    public void setOperator(UserDTO operator) {
        this.operator = operator;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDeleteReason() {
        return deleteReason;
    }

    public void setDeleteReason(String deleteReason) {
        this.deleteReason = deleteReason;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
}
