package tech.tongyu.bct.pricing.common.config;

import tech.tongyu.bct.common.Locator;
import tech.tongyu.bct.pricing.common.QuoteFieldLocator;

public interface SingleAssetPricingConfig extends PricingConfig{
    QuoteFieldLocator underlyerPrice();
    Locator impliedVolSurface();
    Locator discountingCurve();
    Locator dividendCurve();
}
