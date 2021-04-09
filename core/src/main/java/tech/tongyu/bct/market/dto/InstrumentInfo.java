package tech.tongyu.bct.market.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CommoditySpotInfo.class),
        @JsonSubTypes.Type(value = CommodityFuturesInfo.class),
        @JsonSubTypes.Type(value = CommodityFuturesOptionInfo.class),
        @JsonSubTypes.Type(value = EquityIndexInfo.class),
        @JsonSubTypes.Type(value = EquityIndexOptionInfo.class),
        @JsonSubTypes.Type(value = EquityIndexFuturesInfo.class),
        @JsonSubTypes.Type(value = EquityStockInfo.class),
        @JsonSubTypes.Type(value = EquityStockOptionInfo.class)
})
public interface InstrumentInfo {

    Integer multiplier();
}
