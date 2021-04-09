package tech.tongyu.bct.reference.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.reference.service.MarginService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 保证金
 * @author hangzhi
 */
@Entity
@Table(schema = MarginService.SCHEMA)
public class Margin {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;


    @Column
    private UUID partyId;

    @Column(precision=19,scale=4)
    private BigDecimal maintenanceMargin;

    @Column
    @CreationTimestamp
    private Instant createdAt;

    @Column
    @UpdateTimestamp
    private Instant updatedAt;

    public Margin() {

    }

    public Margin(UUID partyId, BigDecimal maintenanceMargin) {
        this.partyId = partyId;
        this.maintenanceMargin = maintenanceMargin;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public BigDecimal getMaintenanceMargin() {
        return maintenanceMargin;
    }

    public void setMaintenanceMargin(BigDecimal maintenanceMargin) {
        this.maintenanceMargin = maintenanceMargin;
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

    public UUID getPartyId() {
        return partyId;
    }

    public void setPartyId(UUID partyId) {
        this.partyId = partyId;
    }
}
