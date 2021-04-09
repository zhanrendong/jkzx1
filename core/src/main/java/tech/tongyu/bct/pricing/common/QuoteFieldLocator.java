package tech.tongyu.bct.pricing.common;

import tech.tongyu.bct.common.Locator;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.market.dto.InstanceEnum;
import tech.tongyu.bct.market.dto.QuoteFieldEnum;
import tech.tongyu.bct.market.dto.QuoteLocator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

public class QuoteFieldLocator implements Locator {
    private String instrumentId;
    private InstanceEnum instance;
    private LocalDate valuationDate;
    private QuoteFieldEnum field;
    private LocalDateTime quoteTimestamp;
    private ZoneId quoteTimezone;

    public QuoteFieldLocator() {
    }

    public QuoteFieldLocator(String instrumentId, InstanceEnum instance,
                             LocalDate valuationDate, QuoteFieldEnum field, ZoneId quoteTimezone) {
        this.instrumentId = instrumentId;
        this.instance = instance;
        this.valuationDate = valuationDate;
        this.field = field;
        this.quoteTimestamp = DateTimeUtils.BCT_MAX_LOCALDATETIME;
        this.quoteTimezone = quoteTimezone;
    }

    public QuoteFieldLocator(String instrumentId, InstanceEnum instance, LocalDate valuationDate,
                             QuoteFieldEnum field, LocalDateTime quoteTimestamp, ZoneId quoteTimezone) {
        this.instrumentId = instrumentId;
        this.instance = instance;
        this.valuationDate = valuationDate;
        this.field = field;
        this.quoteTimestamp = quoteTimestamp;
        this.quoteTimezone = quoteTimezone;
    }

    public QuoteLocator toQuoteLocator() {
        return new QuoteLocator(instrumentId, instance,
                valuationDate, quoteTimestamp, quoteTimezone);
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

    public QuoteFieldEnum getField() {
        return field;
    }

    public void setField(QuoteFieldEnum field) {
        this.field = field;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuoteFieldLocator)) return false;
        QuoteFieldLocator that = (QuoteFieldLocator) o;
        return Objects.equals(getInstrumentId(), that.getInstrumentId()) &&
                getInstance() == that.getInstance() &&
                Objects.equals(getValuationDate(), that.getValuationDate()) &&
                getField() == that.getField() &&
                Objects.equals(getQuoteTimestamp(), that.getQuoteTimestamp()) &&
                Objects.equals(getQuoteTimezone(), that.getQuoteTimezone());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInstrumentId(), getInstance(), getValuationDate(),
                getField(), getQuoteTimestamp(), getQuoteTimezone());
    }
}
