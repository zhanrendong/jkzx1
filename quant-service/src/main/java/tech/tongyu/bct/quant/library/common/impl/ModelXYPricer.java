package tech.tongyu.bct.quant.library.common.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;

public class ModelXYPricer implements QuantPricerSpec {
    private final String pricerName = "MODEL_XY";
    private final ModelXYPricerParams pricerParams;

    @JsonCreator
    public ModelXYPricer(ModelXYPricerParams pricerParams) {
        this.pricerParams = pricerParams;
    }

    @Override
    public String getPricerName() {
        return this.pricerName;
    }

    @Override
    public ModelXYPricerParams getPricerParams() {
        return this.pricerParams;
    }
}
