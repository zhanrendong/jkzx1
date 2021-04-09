package tech.tongyu.bct.market.dto;

import tech.tongyu.bct.common.api.doc.BctField;

public class CorrelationDTO {
    @BctField(name = "instrumentId1", description = "标的物1", type = "String")
    private final String instrumentId1;
    @BctField(name = "instrumentId2", description = "标的物2", type = "String")
    private final String instrumentId2;
    @BctField(name = "correlation", description = "相关系数", type = "double")
    private final double correlation;

    public CorrelationDTO(String instrumentId1, String instrumentId2, double correlation) {
        this.instrumentId1 = instrumentId1;
        this.instrumentId2 = instrumentId2;
        this.correlation = correlation;
    }

    public String getInstrumentId1() {
        return instrumentId1;
    }

    public String getInstrumentId2() {
        return instrumentId2;
    }

    public double getCorrelation() {
        return correlation;
    }
}
