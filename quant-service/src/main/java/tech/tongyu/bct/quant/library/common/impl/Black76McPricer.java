package tech.tongyu.bct.quant.library.common.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;

public class Black76McPricer implements QuantPricerSpec {
    private final String pricerName = "Black76Mc";
    private final BlackMcPricerParams pricerParams;

    @JsonCreator
    public Black76McPricer(BlackMcPricerParams pricerParams) {
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
