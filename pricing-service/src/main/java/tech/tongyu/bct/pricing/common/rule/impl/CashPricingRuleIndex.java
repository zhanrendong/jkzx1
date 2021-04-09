package tech.tongyu.bct.pricing.common.rule.impl;

import tech.tongyu.bct.pricing.common.rule.PricingRuleIndex;
import tech.tongyu.bct.quant.library.priceable.AssetClass;

import java.util.Objects;

public class CashPricingRuleIndex implements PricingRuleIndex {
    private final AssetClass.AssetClassEnum assetClass = AssetClass.AssetClassEnum.CASH;

    public CashPricingRuleIndex() {
    }

    public AssetClass.AssetClassEnum getAssetClass() {
        return assetClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CashPricingRuleIndex)) return false;
        CashPricingRuleIndex that = (CashPricingRuleIndex) o;
        return getAssetClass() == that.getAssetClass();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAssetClass());
    }
}
