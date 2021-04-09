package tech.tongyu.bct.model.ao;

import tech.tongyu.bct.market.dto.InstanceEnum;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DividendCurveBuilderConfigAO {
    private String modelName;
    private LocalDate valuationDate;
    private ZoneId timezone;
    private InstanceEnum instance;
    private String underlyer;
    private List<CurveInstrumentAO> instruments;

    public DividendCurveBuilderConfigAO() {
    }

    public DividendCurveBuilderConfigAO(String modelName, LocalDate valuationDate, ZoneId timezone,
                                        InstanceEnum instance, String underlyer,
                                        List<CurveInstrumentAO> instruments) {
        this.modelName = modelName;
        this.valuationDate = valuationDate;
        this.timezone = timezone;
        this.instance = instance;
        this.underlyer = underlyer;
        this.instruments = instruments;
    }

    public String getModelId() {
        return modelName + "|DIVIDEND_CURVE|" + instance + "|" + underlyer + "|"
                + valuationDate.format(DateTimeFormatter.BASIC_ISO_DATE) + "|"
                + timezone;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
    }

    public ZoneId getTimezone() {
        return timezone;
    }

    public void setTimezone(ZoneId timezone) {
        this.timezone = timezone;
    }

    public InstanceEnum getInstance() {
        return instance;
    }

    public void setInstance(InstanceEnum instance) {
        this.instance = instance;
    }

    public String getUnderlyer() {
        return underlyer;
    }

    public void setUnderlyer(String underlyer) {
        this.underlyer = underlyer;
    }

    public List<CurveInstrumentAO> getInstruments() {
        return instruments;
    }

    public void setInstruments(List<CurveInstrumentAO> instruments) {
        this.instruments = instruments;
    }
}
