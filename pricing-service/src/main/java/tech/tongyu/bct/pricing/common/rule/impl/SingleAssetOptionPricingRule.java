package tech.tongyu.bct.pricing.common.rule.impl;

import org.springframework.lang.Nullable;
import tech.tongyu.bct.market.dto.InstanceEnum;
import tech.tongyu.bct.market.dto.QuoteFieldEnum;
import tech.tongyu.bct.pricing.common.rule.PricingRule;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;

public class SingleAssetOptionPricingRule implements PricingRule {
    private final InstanceEnum underlyerInstance;
    private final QuoteFieldEnum underlyerField;
    private final String discountingCurveName;
    private final InstanceEnum discountingCurveInstance;
    private final String volSurfaceName;
    private final InstanceEnum volSurfaceInstance;
    @Nullable
    private final String dividendCurveName;
    @Nullable
    private final InstanceEnum dividendCurveInstance;
    private final QuantPricerSpec pricer;

    public SingleAssetOptionPricingRule(InstanceEnum underlyerInstance, QuoteFieldEnum underlyerField,
                                        String discountingCurveName, InstanceEnum discountingCurveInstance,
                                        String volSurfaceName, InstanceEnum volSurfaceInstance,
                                        @Nullable String dividendCurveName,
                                        @Nullable InstanceEnum dividendCurveInstance,
                                        QuantPricerSpec pricer) {
        this.underlyerInstance = underlyerInstance;
        this.underlyerField = underlyerField;
        this.discountingCurveName = discountingCurveName;
        this.discountingCurveInstance = discountingCurveInstance;
        this.volSurfaceName = volSurfaceName;
        this.volSurfaceInstance = volSurfaceInstance;
        this.dividendCurveName = dividendCurveName;
        this.dividendCurveInstance = dividendCurveInstance;
        this.pricer = pricer;
    }

    public InstanceEnum getUnderlyerInstance() {
        return underlyerInstance;
    }

    public QuoteFieldEnum getUnderlyerField() {
        return underlyerField;
    }

    public String getDiscountingCurveName() {
        return discountingCurveName;
    }

    public InstanceEnum getDiscountingCurveInstance() {
        return discountingCurveInstance;
    }

    public String getVolSurfaceName() {
        return volSurfaceName;
    }

    public InstanceEnum getVolSurfaceInstance() {
        return volSurfaceInstance;
    }

    @Nullable
    public String getDividendCurveName() {
        return dividendCurveName;
    }

    @Nullable
    public InstanceEnum getDividendCurveInstance() {
        return dividendCurveInstance;
    }

    public QuantPricerSpec getPricer() {
        return pricer;
    }
}
