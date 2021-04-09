package tech.tongyu.bct.market.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.common.jpa.HasUuid;
import tech.tongyu.bct.market.service.MarketDataService;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = MarketDataService.SCHEMA)
public class InstrumentWhitelist implements HasUuid {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;


    @Column(nullable = false)
    private String venueCode;

    @Column(nullable = false, unique = true)
    private String instrumentId;

    @Column
    private Double notionalLimit;

    @CreationTimestamp
    @Column
    private Instant createdAt;

    @UpdateTimestamp
    @Column
    private Instant updatedAt;

    public InstrumentWhitelist() {
    }

    public InstrumentWhitelist(String venueCode, String instrumentId, Double notionalLimit) {
        this.venueCode = venueCode;
        this.instrumentId = instrumentId;
        this.notionalLimit = notionalLimit;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Double getNotionalLimit() {
        return notionalLimit;
    }

    public void setNotionalLimit(Double notionalLimit) {
        this.notionalLimit = notionalLimit;
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

    public String getVenueCode() {
        return venueCode;
    }

    public void setVenueCode(String venueCode) {
        this.venueCode = venueCode;
    }
}
