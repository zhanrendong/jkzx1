package tech.tongyu.bct.pricing.common.config.impl;

import org.springframework.lang.Nullable;
import tech.tongyu.bct.common.Locator;
import tech.tongyu.bct.model.dto.ModelLocator;
import tech.tongyu.bct.pricing.common.QuoteFieldLocator;
import tech.tongyu.bct.pricing.common.config.SingleAssetPricingConfig;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 现金定价仅支持：不折现，或者通过无风险利率进行折现。
 */
public class CashPricingConfig implements SingleAssetPricingConfig {
    /**
     * 是否折现
     */
    private final Boolean discounted;
    /**
     * 无风险利率曲线
     */
    @Nullable
    private final ModelLocator discountingCurve;

    public CashPricingConfig(Boolean discounted, @Nullable ModelLocator discountingCurve) {
        this.discounted = discounted;
        this.discountingCurve = discountingCurve;
    }

    public Boolean getDiscounted() {
        return discounted;
    }

    @Nullable
    public ModelLocator getDiscountingCurve() {
        return discountingCurve;
    }

    @Override
    public List<QuoteFieldLocator> quotes() {
        return new ArrayList<>();
    }

    @Override
    public List<ModelLocator> models() {
        List<ModelLocator> ret = new ArrayList<>();
        if (!Objects.isNull(discountingCurve)) {
            ret.add(discountingCurve);
        }
        return ret;
    }

    @Override
    public QuoteFieldLocator underlyerPrice() {
        return null;
    }

    @Override
    public Locator impliedVolSurface() {
        return null;
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
        return null;
    }
}
