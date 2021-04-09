package tech.tongyu.bct.workflow.dto;

import tech.tongyu.bct.workflow.process.enums.ProcessInstanceStatusEnum;

public class ProcessInstanceDTO {

    public ProcessInstanceDTO(String processInstanceId, String processName, UserDTO initiator, String startTime, String processSequenceNum, String subject, ProcessInstanceStatusEnum processInstanceStatusEnum) {
        this.processInstanceId = processInstanceId;
        this.processName = processName;
        this.initiator = initiator;
        this.startTime = startTime;
        this.processSequenceNum = processSequenceNum;
        this.subject = subject;
        this.processInstanceStatusEnum = processInstanceStatusEnum;
    }

    private String processInstanceId;
    private String processName;
    private UserDTO initiator;
    private String startTime;
    private String processSequenceNum;
    private String subject;

    private ProcessInstanceStatusEnum processInstanceStatusEnum;

    public ProcessInstanceStatusEnum getProcessInstanceStatusEnum() {
        return processInstanceStatusEnum;
    }

    public void setProcessInstanceStatusEnum(ProcessInstanceStatusEnum processInstanceStatusEnum) {
        this.processInstanceStatusEnum = processInstanceStatusEnum;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public UserDTO getInitiator() {
        return initiator;
    }

    public void setInitiator(UserDTO initiator) {
        this.initiator = initiator;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

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
}
