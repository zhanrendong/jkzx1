package tech.tongyu.bct.workflow.dto;

public class TaskHistoryDTO {

    public TaskHistoryDTO(String taskName, String userName, String operation, String operateTime, String comment) {
        this.taskName = taskName;
        this.userName = userName;
        this.operation = operation;
        this.operateTime = operateTime;
        this.comment = comment;
    }

    private String taskName;
    private String userName;
    private String operation;
    private String operateTime;

    private String comment;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(String operateTime) {
        this.operateTime = operateTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
