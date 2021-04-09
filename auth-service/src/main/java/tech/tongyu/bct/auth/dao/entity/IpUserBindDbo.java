package tech.tongyu.bct.auth.dao.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(schema = EntityConstants.AUTH_SERVICE,
        name = EntityConstants.IP_USER_BIND,
        indexes = {@Index(name = "ip_username_index", columnList = "ip,username")})
public class IpUserBindDbo{

    @Id
    @Column(name = "id", unique = true)
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @GeneratedValue(generator = "system-uuid")
    protected String id;

    @Column
    private String ip;

    @Column
    private String username;

    @Column
    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    @Column
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
