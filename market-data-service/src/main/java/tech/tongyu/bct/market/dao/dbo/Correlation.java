package tech.tongyu.bct.market.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.market.service.MarketDataService;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = MarketDataService.SCHEMA,
        indexes = {@Index(columnList = "instrumentId1, instrumentId2", unique = true)})
public class Correlation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false)
    private String instrumentId1;

    @Column(nullable = false)
    private String instrumentId2;

    @Column(nullable = false)
    private Double correlation;

    @CreationTimestamp
    @Column
    private Instant createdAt;

    @UpdateTimestamp
    @Column
    private Instant updatedAt;

    public Correlation() {
    }

    public Correlation(String instrumentId1, String instrumentId2, Double correlation) {
        this.instrumentId1 = instrumentId1;
        this.instrumentId2 = instrumentId2;
        this.correlation = correlation;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getInstrumentId1() {
        return instrumentId1;
    }

    public void setInstrumentId1(String instrumentId1) {
        this.instrumentId1 = instrumentId1;
    }

    public String getInstrumentId2() {
        return instrumentId2;
    }

    public void setInstrumentId2(String instrumentId2) {
        this.instrumentId2 = instrumentId2;
    }

    public Double getCorrelation() {
        return correlation;
    }

    public void setCorrelation(Double correlation) {
        this.correlation = correlation;
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
