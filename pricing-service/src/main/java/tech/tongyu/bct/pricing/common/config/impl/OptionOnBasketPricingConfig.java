package tech.tongyu.bct.pricing.common.config.impl;

import org.springframework.lang.Nullable;
import tech.tongyu.bct.model.dto.ModelLocator;
import tech.tongyu.bct.pricing.common.CorrelationMatrixLocator;
import tech.tongyu.bct.pricing.common.QuoteFieldLocator;
import tech.tongyu.bct.pricing.common.config.MultiAssetPricingConfig;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OptionOnBasketPricingConfig implements MultiAssetPricingConfig {
    private final List<QuoteFieldLocator> underlyerPrices;
    private final List<ModelLocator> impliedVolSurfaces;
    private final ModelLocator discountingCurve;
    @Nullable
    private final List<ModelLocator> dividendCurves;
    private final CorrelationMatrixLocator correlationMatrix;
    private final QuantPricerSpec pricer;

    public OptionOnBasketPricingConfig(List<QuoteFieldLocator> underlyerPrices,
                                       List<ModelLocator> impliedVolSurfaces, ModelLocator discountingCurve,
                                       List<ModelLocator> dividendCurves, QuantPricerSpec pricer) {
        this.underlyerPrices = underlyerPrices;
        this.impliedVolSurfaces = impliedVolSurfaces;
        this.discountingCurve = discountingCurve;
        this.dividendCurves = dividendCurves;
        this.correlationMatrix = new CorrelationMatrixLocator(underlyerPrices.stream()
                .map(QuoteFieldLocator::getInstrumentId)
                .collect(Collectors.toList()));
        this.pricer = pricer;
    }

    @Override
    public List<QuoteFieldLocator> underlyerPrices() {
        return underlyerPrices;
    }

    @Override
    public List<ModelLocator> impliedVolSurfaces() {
        return impliedVolSurfaces;
    }

    @Override
    public ModelLocator discountingCurve() {
        return discountingCurve;
    }

    @Override
    public List<ModelLocator> dividendCurves() {
        return dividendCurves;
    }

    @Override
    public CorrelationMatrixLocator correlatinMatrix() {
        return this.correlationMatrix;
    }

    @Override
    public List<QuoteFieldLocator> quotes() {
        return this.underlyerPrices;
    }

    @Override
    public List<ModelLocator> models() {
        List<ModelLocator> all = new ArrayList<>(impliedVolSurfaces);
        all.add(discountingCurve);
        if(!Objects.isNull(dividendCurves)) {
            all.addAll(dividendCurves);
        }
        return all;
    }

    @Override
    public QuantPricerSpec pricer() {
        return pricer;
    }

    public List<QuoteFieldLocator> getUnderlyerPrices() {
        return underlyerPrices;
    }

    public List<ModelLocator> getImpliedVolSurfaces() {
        return impliedVolSurfaces;
    }

    public ModelLocator getDiscountingCurve() {
        return discountingCurve;
    }

    public List<ModelLocator> getDividendCurves() {
        return dividendCurves;
    }

    public CorrelationMatrixLocator getCorrelationMatrix() {
        return correlationMatrix;
    }

    public QuantPricerSpec getPricer() {
        return pricer;
    }
}
