package tech.tongyu.bct.workflow.process.repo.entities;

import tech.tongyu.bct.workflow.process.repo.common.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Entity
@Table(schema = EntityConstants.SCHEMA, name = EntityConstants.TABLE_NAME_$TASK_APPROVE_GROUP)
public class TaskApproveGroupDbo extends BaseEntity {

    public TaskApproveGroupDbo() {
    }

    public TaskApproveGroupDbo(String taskNodeId, String approveGroupId) {
        this.taskNodeId = taskNodeId;
        this.approveGroupId = approveGroupId;
    }

    @Column
    private String taskNodeId;

    @Column
    private String approveGroupId;

    public String getTaskNodeId() {
        return taskNodeId;
    }

    public void setTaskNodeId(String taskNodeId) {
        this.taskNodeId = taskNodeId;
    }

    public String getApproveGroupId() {
        return approveGroupId;
    }

    public void setApproveGroupId(String approveGroupId) {
        this.approveGroupId = approveGroupId;
    }
}
