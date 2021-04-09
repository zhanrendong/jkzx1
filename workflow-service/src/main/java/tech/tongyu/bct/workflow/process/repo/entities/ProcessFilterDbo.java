package tech.tongyu.bct.workflow.process.repo.entities;

import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;
import tech.tongyu.bct.workflow.process.repo.common.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author yangyiwei
 *  - mailto: yangyiwei@tongyu.tech
 */
@Entity
@Table(schema = EntityConstants.SCHEMA, name = EntityConstants.TABlE_NAME_$PROCESS_FILTER)
public class ProcessFilterDbo extends BaseEntity {

    public ProcessFilterDbo(){}

    public ProcessFilterDbo(String processId, String filterId, TaskTypeEnum taskType) {
        this.processId = processId;
        this.filterId = filterId;
        this.taskType = taskType;
    }

    @Column
    private String processId;

    @Column
    private String filterId;

    @Column
    private TaskTypeEnum taskType;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getFilterId() {
        return filterId;
    }

    public void setFilterId(String filterId) {
        this.filterId = filterId;
    }

    public TaskTypeEnum getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskTypeEnum taskType) {
        this.taskType = taskType;
    }
}
