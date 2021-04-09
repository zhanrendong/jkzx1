package tech.tongyu.bct.workflow.dto;

import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;

public class CurrentNodeDTO {

    public CurrentNodeDTO(String taskName, TaskTypeEnum taskType) {
        this.taskName = taskName;
        this.taskType = taskType;
    }

    private String taskName;

    private TaskTypeEnum taskType;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public TaskTypeEnum getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskTypeEnum taskType) {
        this.taskType = taskType;
    }
}
