package tech.tongyu.bct.pricing.common.descriptor.impl;

import tech.tongyu.bct.pricing.common.descriptor.PositionDescriptor;
import tech.tongyu.bct.pricing.common.rule.PricingRuleIndex;
import tech.tongyu.bct.pricing.common.rule.impl.CashPricingRuleIndex;
import tech.tongyu.bct.quant.library.priceable.AssetClass;
import tech.tongyu.bct.quant.library.priceable.Priceable;

public class CashPositionDescriptor implements PositionDescriptor {
    private final String positionId;
    private final AssetClass.AssetClassEnum assetClass = AssetClass.AssetClassEnum.CASH;
    private final Priceable.PriceableTypeEnum productType;

    public CashPositionDescriptor(String positionId, Priceable.PriceableTypeEnum productType) {
        this.positionId = positionId;
        this.productType = productType;
    }

    public CashPositionDescriptor(String positionId) {
        this.positionId = positionId;
        this.productType = Priceable.PriceableTypeEnum.CASH_PAYMENT;
    }

    public String getPositionId() {
        return positionId;
    }

    public AssetClass.AssetClassEnum getAssetClass() {
        return assetClass;
    }

    public Priceable.PriceableTypeEnum getProductType() {
        return productType;
    }

    @Override
    public PricingRuleIndex getRuleIndex() {
        return new CashPricingRuleIndex();
    }
}
