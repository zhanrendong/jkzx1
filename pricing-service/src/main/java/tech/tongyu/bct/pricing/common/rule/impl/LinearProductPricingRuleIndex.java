package tech.tongyu.bct.pricing.common.rule.impl;

import tech.tongyu.bct.pricing.common.rule.PricingRuleIndex;
import tech.tongyu.bct.quant.library.priceable.AssetClass;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

import java.util.Objects;

public class LinearProductPricingRuleIndex implements PricingRuleIndex {
    private final AssetClass.AssetClassEnum assetClass;
    private final ExchangeListed.InstrumentTypeEnum instrumentType;

    public LinearProductPricingRuleIndex(AssetClass.AssetClassEnum assetClass,
                                         ExchangeListed.InstrumentTypeEnum instrumentType) {
        this.assetClass = assetClass;
        this.instrumentType = instrumentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinearProductPricingRuleIndex)) return false;
        LinearProductPricingRuleIndex that = (LinearProductPricingRuleIndex) o;
        return assetClass == that.assetClass &&
                instrumentType == that.instrumentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetClass, instrumentType);
    }
}
