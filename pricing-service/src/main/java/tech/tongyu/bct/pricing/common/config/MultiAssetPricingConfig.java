package tech.tongyu.bct.pricing.common.config;

import tech.tongyu.bct.model.dto.ModelLocator;
import tech.tongyu.bct.pricing.common.CorrelationMatrixLocator;
import tech.tongyu.bct.pricing.common.QuoteFieldLocator;

import java.util.List;

public interface MultiAssetPricingConfig extends PricingConfig {
    List<QuoteFieldLocator> underlyerPrices();
    List<ModelLocator> impliedVolSurfaces();
    ModelLocator discountingCurve();
    List<ModelLocator> dividendCurves();
    CorrelationMatrixLocator correlatinMatrix();
}
