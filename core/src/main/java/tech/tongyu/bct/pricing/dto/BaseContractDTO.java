package tech.tongyu.bct.pricing.dto;

import java.time.LocalDate;
import java.util.UUID;

public class BaseContractDTO {
    private UUID uuid;
    private String positionId;
    private String instrumentId;
    private LocalDate expiry;
    private String baseContractId;
    private String hedgingContractId;
    private LocalDate baseContractMaturity;
    private LocalDate baseContractValidStart;

    public BaseContractDTO() {
    }

    public BaseContractDTO(UUID uuid, String positionId, String instrumentId, LocalDate expiry,
                           String baseContractId, LocalDate baseContractMaturity, LocalDate baseContractValidStart,
                           String hedgingContractId) {
        this.uuid = uuid;
        this.positionId = positionId;
        this.instrumentId = instrumentId;
        this.expiry = expiry;
        this.baseContractId = baseContractId;
        this.baseContractMaturity = baseContractMaturity;
        this.baseContractValidStart = baseContractValidStart;
        this.hedgingContractId = hedgingContractId;
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

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
