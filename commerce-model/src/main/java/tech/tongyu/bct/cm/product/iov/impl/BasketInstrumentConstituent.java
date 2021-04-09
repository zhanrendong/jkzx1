package tech.tongyu.bct.cm.product.iov.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;

import java.math.BigDecimal;

public class BasketInstrumentConstituent
        implements tech.tongyu.bct.cm.product.iov.BasketInstrumentConstituent {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public InstrumentOfValue instrument;

    public BigDecimal weight;

    public BigDecimal multiplier;

    public BigDecimal initialSpot;

    public BasketInstrumentConstituent() {
    }

    public BasketInstrumentConstituent(InstrumentOfValue instrument, BigDecimal multiplier) {
        this.instrument = instrument;
        this.multiplier = multiplier;
        this.multiplier = null;
        this.initialSpot = null;
    }

    public BasketInstrumentConstituent(InstrumentOfValue instrument, Double multiplier) {
        this.instrument = instrument;
        this.multiplier = BigDecimal.valueOf(multiplier);
        this.multiplier = null;
        this.initialSpot = null;
    }

    public BasketInstrumentConstituent(InstrumentOfValue instrument, BigDecimal weight, BigDecimal multiplier,
                                       BigDecimal initialSpot) {
        this.instrument = instrument;
        this.weight = weight;
        this.multiplier = multiplier;
        this.initialSpot = initialSpot;
    }

    @Override
    public InstrumentOfValue instrument() {
        return instrument;
    }

    @Override
    public BigDecimal weight() {
        return weight;
    }

    @Override
    public BigDecimal multiplier() {
        return multiplier;
    }

    @Override
    public BigDecimal initialSpot() {
        return initialSpot;
    }
}
