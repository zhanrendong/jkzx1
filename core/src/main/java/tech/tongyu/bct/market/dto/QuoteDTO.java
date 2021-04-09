package tech.tongyu.bct.market.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

public class QuoteDTO {
    @BctField(name = "instrumentId", description = "标的物ID", type = "String")
    private String instrumentId;
    @BctField(name = "instance", description = "行情种类", type = "InstanceEnum")
    private InstanceEnum instance;
    @BctField(name = "valuationDate", description = "行情日期", type = "LocalDate")
    private LocalDate valuationDate;
    @BctField(name = "quoteTimestamp", description = "行情时间戳", type = "LocalDateTime")
    private LocalDateTime quoteTimestamp;
    @BctField(name = "quoteTimezone", description = "时区", type = "ZoneId")
    private ZoneId quoteTimezone;
    @BctField(name = "fields", description = "行情", type = "Map<QuoteFieldEnum, Double>")
    private Map<QuoteFieldEnum, Double> fields;

    public QuoteDTO() {
    }

    public QuoteDTO(String instrumentId, InstanceEnum instance,
                    LocalDate valuationDate, LocalDateTime quoteTimestamp, ZoneId quoteTimezone,
                    Map<QuoteFieldEnum, Double> fields) {
        this.instrumentId = instrumentId;
        this.instance = instance;
        this.valuationDate = valuationDate;
        this.quoteTimestamp = quoteTimestamp;
        this.quoteTimezone = quoteTimezone;
        this.fields = fields;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public InstanceEnum getInstance() {
        return instance;
    }

    public void setInstance(InstanceEnum instance) {
        this.instance = instance;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
    }

    public LocalDateTime getQuoteTimestamp() {
        return quoteTimestamp;
    }

    public void setQuoteTimestamp(LocalDateTime quoteTimestamp) {
        this.quoteTimestamp = quoteTimestamp;
    }

    public ZoneId getQuoteTimezone() {
        return quoteTimezone;
    }

    public void setQuoteTimezone(ZoneId quoteTimezone) {
        this.quoteTimezone = quoteTimezone;
    }

    public Map<QuoteFieldEnum, Double> getFields() {
        return fields;
    }

    public void setFields(Map<QuoteFieldEnum, Double> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "QuoteDTO{" +
                "instrumentId='" + instrumentId + '\'' +
                ", instance=" + instance +
                ", valuationDate=" + valuationDate +
                ", quoteTimestamp=" + quoteTimestamp +
                ", quoteTimezone=" + quoteTimezone +
                ", fields=" + fields +
                '}';
    }
}
