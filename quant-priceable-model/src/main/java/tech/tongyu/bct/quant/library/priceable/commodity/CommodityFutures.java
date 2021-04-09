package tech.tongyu.bct.quant.library.priceable.commodity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.Commodity;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

public class CommodityFutures implements Commodity, ExchangeListed {
    private final String instrumentId;

    @JsonCreator
    public CommodityFutures(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.COMMODITY_FUTURES;
    }

    @Override
    public String getInstrumentId() {
        return instrumentId;
    }

    @JsonIgnore
    @Override
    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.COMMODITY_FUTURES;
    }
}
