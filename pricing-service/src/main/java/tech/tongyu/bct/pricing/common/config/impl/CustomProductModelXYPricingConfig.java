package tech.tongyu.bct.pricing.common.config.impl;

import tech.tongyu.bct.common.Locator;
import tech.tongyu.bct.model.dto.ModelLocator;
import tech.tongyu.bct.pricing.common.QuoteFieldLocator;
import tech.tongyu.bct.pricing.common.config.SingleAssetPricingConfig;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;
import tech.tongyu.bct.quant.library.common.impl.ModelXYPricer;

import java.util.Arrays;
import java.util.List;

public class CustomProductModelXYPricingConfig implements SingleAssetPricingConfig {
    private final QuoteFieldLocator underlyerPriceLocator;
    private final ModelLocator modelXYLocator;
    private final ModelXYPricer modelXYPricer;

    public CustomProductModelXYPricingConfig(QuoteFieldLocator underlyerPriceLocator,
                                             ModelLocator modelXYLocator, ModelXYPricer modelXYPricer) {
        this.underlyerPriceLocator = underlyerPriceLocator;
        this.modelXYLocator = modelXYLocator;
        this.modelXYPricer = modelXYPricer;
    }

    @Override
    public QuoteFieldLocator underlyerPrice() {
        return underlyerPriceLocator;
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
    public List<QuoteFieldLocator> quotes() {
        return Arrays.asList(underlyerPriceLocator);
    }

    @Override
    public List<ModelLocator> models() {
        return Arrays.asList(modelXYLocator);
    }

    @Override
    public QuantPricerSpec pricer() {
        return modelXYPricer;
    }

    public QuoteFieldLocator getUnderlyerPriceLocator() {
        return underlyerPriceLocator;
    }

    public ModelLocator getModelXYLocator() {
        return modelXYLocator;
    }

    public ModelXYPricer getModelXYPricer() {
        return modelXYPricer;
    }
}
