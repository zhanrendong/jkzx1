package tech.tongyu.bct.workflow.process.repo.entities;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.workflow.dto.TaskNode;
import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;
import tech.tongyu.bct.workflow.process.repo.common.BaseEntity;

import javax.persistence.*;
import java.util.Collection;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 * @author yongbin
 */
@Entity
@Table(schema = EntityConstants.SCHEMA, name = EntityConstants.TABLE_NAME_$TASK_NODE)
public class TaskNodeDbo extends BaseEntity implements TaskNode {

    public TaskNodeDbo() {
    }

    public TaskNodeDbo(String processId, String taskName
            , TaskTypeEnum taskType, Integer sequence, String actionClass) {
        this.processId = processId;
        this.taskName = taskName;
        this.taskType = taskType;
        this.sequence = sequence;
        this.actionClass = actionClass;
    }

    @Column
    private String processId;

    @Column
    private String taskName;

    @Column
    @Enumerated(EnumType.STRING)
    private TaskTypeEnum taskType;

    @Column
    private Integer sequence;

    @Column
    private String actionClass;

    public String getActTaskId(){
        switch (taskType){
            case INPUT_DATA:
                return taskName + "_-1";
            case MODIFY_DATA:
                return taskName + "_-2";
            case REVIEW_DATA:
                return taskName + "_" + sequence;
            case COUNTER_SIGN_DATA:
                return taskName + "_" + sequence;
            default:
                throw new UnsupportedOperationException("unknown taskType");
        }
    }

    public String getCandidateGroup(String processName){
        assert StringUtils.isNotBlank(processName);
        return processName + "_" + getActTaskId();
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

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
