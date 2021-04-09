package tech.tongyu.bct.pricing.common.rule.impl;

import org.springframework.lang.Nullable;
import tech.tongyu.bct.pricing.common.rule.PricingRuleIndex;
import tech.tongyu.bct.quant.library.priceable.AssetClass;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

import java.util.Objects;

public class SingleAssetOptionPricingRuleIndex implements PricingRuleIndex {
    private final AssetClass.AssetClassEnum assetClass;
    private final Priceable.PriceableTypeEnum productType;
    private final ExchangeListed.InstrumentTypeEnum underlyerType;
    @Nullable
    private final String positionId;

    public SingleAssetOptionPricingRuleIndex(AssetClass.AssetClassEnum assetClass,
                                             Priceable.PriceableTypeEnum productType,
                                             ExchangeListed.InstrumentTypeEnum underlyerType,
                                             @Nullable String positionId) {
        this.assetClass = assetClass;
        this.productType = productType;
        this.underlyerType = underlyerType;
        this.positionId = positionId;
    }

    public AssetClass.AssetClassEnum getAssetClass() {
        return assetClass;
    }

    public Priceable.PriceableTypeEnum getProductType() {
        return productType;
    }

    public ExchangeListed.InstrumentTypeEnum getUnderlyerType() {
        return underlyerType;
    }

    public String getPositionId() {
        return positionId;
    }

    public SingleAssetOptionPricingRuleIndex withoutPositionId() {
        return new SingleAssetOptionPricingRuleIndex(assetClass, productType, underlyerType, null);
    }

    public SingleAssetOptionPricingRuleIndex toGenericOptionType() {
        return new SingleAssetOptionPricingRuleIndex(assetClass,
                Priceable.PriceableTypeEnum.GENERIC_SINGLE_ASSET_OPTION, underlyerType, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SingleAssetOptionPricingRuleIndex)) return false;
        SingleAssetOptionPricingRuleIndex that = (SingleAssetOptionPricingRuleIndex) o;
        return getAssetClass() == that.getAssetClass() &&
                getProductType() == that.getProductType() &&
                getUnderlyerType() == that.getUnderlyerType() &&
                Objects.equals(getPositionId(), that.getPositionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAssetClass(), getProductType(), getUnderlyerType(), getPositionId());
    }
}
