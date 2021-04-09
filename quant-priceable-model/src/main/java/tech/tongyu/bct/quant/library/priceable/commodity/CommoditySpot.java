package tech.tongyu.bct.quant.library.priceable.commodity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.Commodity;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

public class CommoditySpot implements Commodity, ExchangeListed {
    private final String instrumentId;

    @JsonCreator
    public CommoditySpot(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.COMMODITY_SPOT;
    }

    @Override
    public String getInstrumentId() {
        return instrumentId;
    }

    @JsonIgnore
    @Override
    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.COMMODITY_SPOT;
    }
}
