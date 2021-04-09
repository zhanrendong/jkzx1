package tech.tongyu.bct.quant.library.common;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface QuantPricerSpec {
    enum QuantlibPricerEnum {
        BLACKSHOLES_ANALYTIC,
        BLACKSCHOLES_MC,
        BLACK76_ANALYTIC,
        BLACK76_MC,
        MODEL_XY
    }
    String getPricerName();
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    QuantPricerParams getPricerParams();
}
