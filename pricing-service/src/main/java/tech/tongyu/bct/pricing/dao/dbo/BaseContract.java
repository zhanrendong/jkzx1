package tech.tongyu.bct.pricing.dao.dbo;

import tech.tongyu.bct.pricing.service.PricingService;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = PricingService.SCHEMA, indexes = {@Index(columnList = "positionId, baseContractId", unique = true)})
public class BaseContract {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false)
    private String positionId;

    @Column(nullable = false)
    private String instrumentId;

    @Column(nullable = false)
    private LocalDate expiry;

    @Column(nullable = false)
    private String baseContractId;

    @Column(nullable = false)
    private String hedgingContractId;

    @Column(nullable = false)
    private LocalDate baseContractMaturity;

    @Column(nullable = false)
    private LocalDate baseContractValidStart;

    public BaseContract() {
    }

    public BaseContract(String positionId, String instrumentId, LocalDate expiry,
                        String baseContractId, LocalDate baseContractMaturity, LocalDate baseContractValidStart,
                        String hedgingContractId) {
        this.positionId = positionId;
        this.instrumentId = instrumentId;
        this.expiry = expiry;
        this.baseContractId = baseContractId;
        this.baseContractMaturity = baseContractMaturity;
        this.baseContractValidStart = baseContractValidStart;
        this.hedgingContractId = hedgingContractId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public LocalDate getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDate expiry) {
        this.expiry = expiry;
    }

    public String getBaseContractId() {
        return baseContractId;
    }

    public void setBaseContractId(String baseContractId) {
        this.baseContractId = baseContractId;
    }

    public LocalDate getBaseContractMaturity() {
        return baseContractMaturity;
    }

    public void setBaseContractMaturity(LocalDate baseContractMaturity) {
        this.baseContractMaturity = baseContractMaturity;
    }

    public LocalDate getBaseContractValidStart() {
        return baseContractValidStart;
    }

    public void setBaseContractValidStart(LocalDate baseContractValidStart) {
        this.baseContractValidStart = baseContractValidStart;
    }

    public String getHedgingContractId() {
        return hedgingContractId;
    }

    public void setHedgingContractId(String hedgingContractId) {
        this.hedgingContractId = hedgingContractId;
    }
}
