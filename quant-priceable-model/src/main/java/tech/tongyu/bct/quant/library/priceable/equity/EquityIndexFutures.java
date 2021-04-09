package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

public class EquityIndexFutures implements Equity, ExchangeListed {
    private final String instrumentId;

    @JsonCreator
    public EquityIndexFutures(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_INDEX_FUTURES;
    }

    @Override
    public String getInstrumentId() {
        return instrumentId;
    }

    @JsonIgnore
    @Override
    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.EQUITY_INDEX_FUTURES;
    }
}
