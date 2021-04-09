package tech.tongyu.bct.workflow.process.repo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(schema = EntityConstants.SCHEMA, name = EntityConstants.TABLE_NAME_$ATTACHMENT)
public class AttachmentDbo {

    public AttachmentDbo(String processInstanceId, String attachmentName, String attachmentPath
            , Date createTime, Date updateTime, String createdBy) {
        this.processInstanceId = processInstanceId;
        this.attachmentName = attachmentName;
        this.attachmentPath = attachmentPath;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.createdBy = createdBy;
    }

    public AttachmentDbo() {
    }

    @Id
    @Column(name = "id", unique = true)
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @GeneratedValue(generator = "system-uuid")
    protected String id;

    @Column
    private String processInstanceId;

    @Column
    private String attachmentName;

    @Column
    private String attachmentPath;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time")
    @CreationTimestamp
    @JsonIgnore
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time")
    @UpdateTimestamp
    @JsonIgnore
    private Date updateTime;

    @Column
    private String createdBy;

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
