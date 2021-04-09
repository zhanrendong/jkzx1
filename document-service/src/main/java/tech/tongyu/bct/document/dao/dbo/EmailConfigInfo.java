package tech.tongyu.bct.document.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.document.service.DocumentService;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = DocumentService.SCHEMA)
public class EmailConfigInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column
    private String emailAddress;

    @Column
    private String emailPassword;

    @Column
    private String emailServerHost;

    @Column
    private String emailServerPort;

    @CreationTimestamp
    @Column
    private Instant createdAt;

    @UpdateTimestamp
    @Column
    private Instant updatedAt;

    @Column
    private String createdBy;

    public EmailConfigInfo() {
    }

    public EmailConfigInfo(String emailAddress, String emailPassword, String emailServerHost, String emailServerPort) {
        this.emailAddress = emailAddress;
        this.emailPassword = emailPassword;
        this.emailServerHost = emailServerHost;
        this.emailServerPort = emailServerPort;
    }

    public String getEmailServerHost() {
        return emailServerHost;
    }

    public void setEmailServerHost(String emailServerHost) {
        this.emailServerHost = emailServerHost;
    }

    public String getEmailServerPort() {
        return emailServerPort;
    }

    public void setEmailServerPort(String emailServerPort) {
        this.emailServerPort = emailServerPort;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
