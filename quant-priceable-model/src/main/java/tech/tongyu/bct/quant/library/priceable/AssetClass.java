package tech.tongyu.bct.quant.library.priceable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Cash.class),
        @JsonSubTypes.Type(value = Equity.class),
        @JsonSubTypes.Type(value = Commodity.class)
})
public interface AssetClass{
    enum AssetClassEnum {
        CASH, EQUITY, COMMODITY, INTEREST_RATE, ANY
    }
    AssetClassEnum getAssetClassEnum();
}
