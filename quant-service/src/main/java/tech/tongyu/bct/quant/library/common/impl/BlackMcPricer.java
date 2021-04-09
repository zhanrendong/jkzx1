package tech.tongyu.bct.quant.library.common.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;

public class BlackMcPricer implements QuantPricerSpec {
    private final String pricerName = "BlackMc";
    private final BlackMcPricerParams pricerParams;

    @JsonCreator
    public BlackMcPricer(BlackMcPricerParams pricerParams) {
        this.pricerParams = pricerParams;
    }

    @Override
    public String getPricerName() {
        return pricerName;
    }

    @Override
    public BlackMcPricerParams getPricerParams() {
        return pricerParams;
    }
}
