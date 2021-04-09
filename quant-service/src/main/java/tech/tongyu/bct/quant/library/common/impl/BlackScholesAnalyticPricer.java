package tech.tongyu.bct.quant.library.common.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;

public class BlackScholesAnalyticPricer implements QuantPricerSpec {
    private final String pricerName = "BlackAnalytic";
    private final BlackAnalyticPricerParams pricerParams;

    @JsonCreator
    public BlackScholesAnalyticPricer(BlackAnalyticPricerParams pricerParams) {
        this.pricerParams = pricerParams;
    }

    @Override
    public String getPricerName() {
        return this.pricerName;
    }

    @Override
    public BlackAnalyticPricerParams getPricerParams() {
        return pricerParams;
    }
}
