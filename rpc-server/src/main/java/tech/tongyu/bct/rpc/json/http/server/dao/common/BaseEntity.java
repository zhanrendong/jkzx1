package tech.tongyu.bct.rpc.json.http.server.dao.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
public class BaseEntity {
    public BaseEntity() {
        createTime = new Date();
        updateTime = new Date();
        revoked = false;
        revokeTime = null;
    }

    @Id
    @Column(name = "id", unique = true)
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @GeneratedValue(generator = "system-uuid")
    protected String id;

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

    @Column(name = "revoked")
    @JsonIgnore
    protected boolean revoked;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "revoke_time")
    @JsonIgnore
    private Date revokeTime;

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

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public Date getRevokeTime() {
        return revokeTime;
    }

    public void setRevokeTime(Date revokeTime) {
        this.revokeTime = revokeTime;
    }
}
