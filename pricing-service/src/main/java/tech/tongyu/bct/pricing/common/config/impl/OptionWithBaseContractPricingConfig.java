package tech.tongyu.bct.pricing.common.config.impl;

import tech.tongyu.bct.common.Locator;
import tech.tongyu.bct.model.dto.ModelLocator;
import tech.tongyu.bct.pricing.common.QuoteFieldLocator;
import tech.tongyu.bct.pricing.common.config.SingleAssetPricingConfig;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class OptionWithBaseContractPricingConfig implements SingleAssetPricingConfig {
    private final QuoteFieldLocator underlyerPriceLocator;
    private final QuoteFieldLocator baseContractPriceLocator;
    private final LocalDate baseContractMaturity;
    private final String hedgingContract;
    private final ModelLocator discountingCurve;
    private final ModelLocator volSurface;
    private final QuantPricerSpec pricer;

    public OptionWithBaseContractPricingConfig(QuoteFieldLocator underlyerPriceLocator,
                                               QuoteFieldLocator baseContractPriceLocator,
                                               LocalDate baseContractMaturity, String hedgingContract,
                                               ModelLocator discountingCurve, ModelLocator volSurface,
                                               QuantPricerSpec pricer) {
        this.underlyerPriceLocator = underlyerPriceLocator;
        this.baseContractPriceLocator = baseContractPriceLocator;
        this.baseContractMaturity = baseContractMaturity;
        this.hedgingContract = hedgingContract;
        this.discountingCurve = discountingCurve;
        this.volSurface = volSurface;
        this.pricer = pricer;
    }

    public QuoteFieldLocator getUnderlyerPriceLocator() {
        return underlyerPriceLocator;
    }

    public QuoteFieldLocator getBaseContractPriceLocator() {
        return baseContractPriceLocator;
    }

    public LocalDate getBaseContractMaturity() {
        return baseContractMaturity;
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

    public String getHedgingContract() {
        return hedgingContract;
    }

    @Override
    public List<QuoteFieldLocator> quotes() {
        return Arrays.asList(underlyerPriceLocator, baseContractPriceLocator);
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
