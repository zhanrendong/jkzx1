package tech.tongyu.bct.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;

import java.util.Collection;

import static tech.tongyu.bct.workflow.process.ProcessConstants.PROCESS;

/**
 * @author david.yang
 *  - mailto: yangyiwei.tongyu.tech
 */
public class TaskNodeDTO implements TaskNode{

    private String processName;

    private String processId;

    private String taskName;

    private TaskTypeEnum taskType;

    private Integer sequence;

    private String actionClass;

    @Override
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Override
    public TaskTypeEnum getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskTypeEnum taskType) {
        this.taskType = taskType;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    @Override
    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    @Override
    public String getActionClass() {
        return actionClass;
    }

    public void setActionClass(String actionClass) {
        this.actionClass = actionClass;
    }

    @Override
    public Collection<String> getApproveGroupList() {
        return Lists.newArrayList();
    }
}
