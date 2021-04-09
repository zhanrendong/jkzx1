package tech.tongyu.bct.workflow.process.repo.entities;

import tech.tongyu.bct.workflow.process.repo.common.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(schema = EntityConstants.SCHEMA, name = EntityConstants.TABLE_NAME_$APPROVE_GROUP)
public class ApproveGroupDbo extends BaseEntity {

    public ApproveGroupDbo() {
    }

    public ApproveGroupDbo(String approveGroupName, String description) {
        this.approveGroupName = approveGroupName;
        this.description = description;
    }

    @Column
    private String approveGroupName;

    @Column
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApproveGroupName() {
        return approveGroupName;
    }

    public void setApproveGroupName(String approveGroupName) {
        this.approveGroupName = approveGroupName;
    }
}
