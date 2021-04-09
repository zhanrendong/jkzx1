package tech.tongyu.bct.pricing.common.config;

import tech.tongyu.bct.model.dto.ModelLocator;
import tech.tongyu.bct.pricing.common.QuoteFieldLocator;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;

import java.util.List;

public interface PricingConfig {
    List<QuoteFieldLocator> quotes();
    List<ModelLocator> models();
    QuantPricerSpec pricer();
}
