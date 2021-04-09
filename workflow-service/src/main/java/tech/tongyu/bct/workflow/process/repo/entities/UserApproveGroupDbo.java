package tech.tongyu.bct.workflow.process.repo.entities;

import tech.tongyu.bct.workflow.process.repo.common.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(schema = EntityConstants.SCHEMA, name = EntityConstants.TABLE_NAME_$USER_APPROVE_GROUP)
public class UserApproveGroupDbo extends BaseEntity {

    public UserApproveGroupDbo() {
    }

    public UserApproveGroupDbo(String approveGroupId, String username) {
        this.approveGroupId = approveGroupId;
        this.username = username;
    }

    @Column
    private String approveGroupId;

    @Column
    private String username;

    public String getApproveGroupId() {
        return approveGroupId;
    }

    public void setApproveGroupId(String approveGroupId) {
        this.approveGroupId = approveGroupId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
