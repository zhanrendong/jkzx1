package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

public class EquityIndex implements Equity, ExchangeListed {
    private final String instrumentId;

    @JsonCreator
    public EquityIndex(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_INDEX;
    }

    @Override
    public String getInstrumentId() {
        return instrumentId;
    }

    @JsonIgnore
    @Override
    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.EQUITY_INDEX;
    }
}
