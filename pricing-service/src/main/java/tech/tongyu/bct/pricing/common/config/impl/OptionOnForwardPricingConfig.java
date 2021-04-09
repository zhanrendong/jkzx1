package tech.tongyu.bct.pricing.common.config.impl;

import tech.tongyu.bct.common.Locator;
import tech.tongyu.bct.model.dto.ModelLocator;
import tech.tongyu.bct.pricing.common.QuoteFieldLocator;
import tech.tongyu.bct.pricing.common.config.SingleAssetPricingConfig;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionOnForwardPricingConfig implements SingleAssetPricingConfig {
    private final QuoteFieldLocator underlyerPriceLocator;
    private final ModelLocator discountingCurve;
    private final ModelLocator volSurface;
    private final QuantPricerSpec pricer;

    public OptionOnForwardPricingConfig(QuoteFieldLocator underlyerPriceLocator,
                                        ModelLocator discountingCurve, ModelLocator volSurface,
                                        QuantPricerSpec pricer) {
        this.underlyerPriceLocator = underlyerPriceLocator;
        this.discountingCurve = discountingCurve;
        this.volSurface = volSurface;
        this.pricer = pricer;
    }

    public QuoteFieldLocator getUnderlyerPriceLocator() {
        return underlyerPriceLocator;
    }

    public ModelLocator getDiscountingCurve() {
        return discountingCurve;
    }

    public ModelLocator getVolSurface() {
        return volSurface;
    }

    public QuantPricerSpec getPricer() {
        return pricer;
    }

    @Override
    public List<QuoteFieldLocator> quotes() {
        List<QuoteFieldLocator> ret = new ArrayList<>();
        ret.add(underlyerPriceLocator);
        return ret;
    }

    @Override
    public List<ModelLocator> models() {
        return Arrays.asList(discountingCurve, volSurface);
    }

    @Override
    public QuoteFieldLocator underlyerPrice() {
        return underlyerPriceLocator;
    }

    @Override
    public Locator impliedVolSurface() {
        return volSurface;
    }

    @Override
    public Locator discountingCurve() {
        return discountingCurve;
    }

    @Override
    public Locator dividendCurve() {
        return null;
    }

    @Override
    public QuantPricerSpec pricer() {
        return pricer;
    }
}
