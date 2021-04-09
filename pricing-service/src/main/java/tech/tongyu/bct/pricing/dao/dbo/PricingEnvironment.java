package tech.tongyu.bct.pricing.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.pricing.service.PricingService;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = PricingService.SCHEMA)
public class PricingEnvironment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String pricingEnvironmentId;

    @Column
    private String description;

    @CreationTimestamp
    @Column
    private Instant createdAt;

    @UpdateTimestamp
    @Column
    private Instant updatedAt;

    public PricingEnvironment() {
    }

    public PricingEnvironment(String pricingEnvironmentId, String description) {
        this.pricingEnvironmentId = pricingEnvironmentId;
        this.description = description;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPricingEnvironmentId() {
        return pricingEnvironmentId;
    }

    public void setPricingEnvironmentId(String pricingEnvironmentId) {
        this.pricingEnvironmentId = pricingEnvironmentId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
