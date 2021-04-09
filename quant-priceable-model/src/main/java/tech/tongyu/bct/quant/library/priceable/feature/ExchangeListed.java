package tech.tongyu.bct.quant.library.priceable.feature;

import tech.tongyu.bct.quant.library.priceable.Priceable;

public interface ExchangeListed extends Priceable {
    enum InstrumentTypeEnum {
        COMMODITY_SPOT, COMMODITY_FUTURES,
        EQUITY_STOCK, EQUITY_INDEX, EQUITY_INDEX_FUTURES,
        ANY
    }
    String getInstrumentId();
    InstrumentTypeEnum getInstrumentType();
}
