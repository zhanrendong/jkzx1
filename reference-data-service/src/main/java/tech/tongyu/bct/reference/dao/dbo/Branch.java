package tech.tongyu.bct.reference.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.reference.service.SalesService;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = SalesService.SCHEMA, indexes = {@Index(columnList = "subsidiaryId, branchName", unique = true)})
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false)
    private UUID subsidiaryId;
    @Column(nullable = false)
    private String branchName;
    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column
    private Instant updatedAt;

    public Branch() {
    }

    public Branch(UUID subsidiaryId, String branchName) {
        this.subsidiaryId = subsidiaryId;
        this.branchName = branchName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getSubsidiaryId() {
        return subsidiaryId;
    }

    public void setSubsidiaryId(UUID subsidiaryId) {
        this.subsidiaryId = subsidiaryId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
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
