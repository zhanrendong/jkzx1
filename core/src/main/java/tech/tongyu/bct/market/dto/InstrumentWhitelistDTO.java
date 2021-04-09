package tech.tongyu.bct.market.dto;

import tech.tongyu.bct.common.api.doc.BctField;

public class InstrumentWhitelistDTO {
    @BctField(name = "venueCode", description = "交易所代码", type = "String")
    private String venueCode;
    @BctField(name = "instrumentId", description = "标的物ID", type = "String")
    private String instrumentId;
    @BctField(name = "notionalLimit", description = "本金限制", type = "Double")
    private Double notionalLimit;

    public InstrumentWhitelistDTO(String venueCode, String instrumentId, Double notionalLimit) {
        this.venueCode = venueCode;
        this.instrumentId = instrumentId;
        this.notionalLimit = notionalLimit;
    }

    public InstrumentWhitelistDTO() {
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

    public String getVenueCode() {
        return venueCode;
    }

    public void setVenueCode(String venueCode) {
        this.venueCode = venueCode;
    }
}
