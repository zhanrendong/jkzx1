package tech.tongyu.bct.quant.library.common.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;

public class Black76AnalyticPricer implements QuantPricerSpec {
    private final String pricerName = "Black76Analytic";
    private final BlackAnalyticPricerParams pricerParams;

    @JsonCreator
    public Black76AnalyticPricer(BlackAnalyticPricerParams pricerParams) {
        this.pricerParams = pricerParams;
    }

    @Override
    public String getPricerName() {
        return pricerName;
    }

    @Override
    public BlackAnalyticPricerParams getPricerParams() {
        return pricerParams;
    }
}
