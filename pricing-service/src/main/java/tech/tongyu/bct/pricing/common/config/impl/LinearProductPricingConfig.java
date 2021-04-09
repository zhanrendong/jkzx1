package tech.tongyu.bct.pricing.common.config.impl;

import tech.tongyu.bct.common.Locator;
import tech.tongyu.bct.model.dto.ModelLocator;
import tech.tongyu.bct.pricing.common.QuoteFieldLocator;
import tech.tongyu.bct.pricing.common.config.SingleAssetPricingConfig;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;

import java.util.ArrayList;
import java.util.List;

public class LinearProductPricingConfig implements SingleAssetPricingConfig {
    private final QuoteFieldLocator locator;

    public LinearProductPricingConfig(QuoteFieldLocator locator) {
        this.locator = locator;
    }

    public QuoteFieldLocator getLocator() {
        return locator;
    }

    @Override
    public List<QuoteFieldLocator> quotes() {
        List<QuoteFieldLocator> ret = new ArrayList<>();
        ret.add(locator);
        return ret;
    }

    @Override
    public List<ModelLocator> models() {
        return new ArrayList<>();
    }

    @Override
    public QuoteFieldLocator underlyerPrice() {
        return locator;
    }

    @Override
    public Locator impliedVolSurface() {
        return null;
    }

    @Override
    public Locator discountingCurve() {
        return null;
    }

    @Override
    public Locator dividendCurve() {
        return null;
    }

    @Override
    public QuantPricerSpec pricer() {
        return null;
    }
}
