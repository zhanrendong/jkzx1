package tech.tongyu.bct.quant.library.common.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import tech.tongyu.bct.quant.library.common.QuantPricerParams;

public class ModelXYPricerParams implements QuantPricerParams {
    private final String modelId;

    @JsonCreator
    public ModelXYPricerParams(String modelId) {
        this.modelId = modelId;
    }

    public String getModelId() {
        return modelId;
    }
}
