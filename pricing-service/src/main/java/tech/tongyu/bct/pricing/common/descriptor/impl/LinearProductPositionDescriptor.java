package tech.tongyu.bct.pricing.common.descriptor.impl;

import tech.tongyu.bct.pricing.common.descriptor.PositionDescriptor;
import tech.tongyu.bct.pricing.common.rule.PricingRuleIndex;
import tech.tongyu.bct.pricing.common.rule.impl.LinearProductPricingRuleIndex;
import tech.tongyu.bct.quant.library.priceable.AssetClass;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

public class LinearProductPositionDescriptor implements PositionDescriptor {
    private final AssetClass.AssetClassEnum assetClass;
    private final ExchangeListed.InstrumentTypeEnum instrumentType;
    private final String instrumentId;
    private final String positionId;

    public LinearProductPositionDescriptor(AssetClass.AssetClassEnum assetClass,
                                           ExchangeListed.InstrumentTypeEnum instrumentType,
                                           String instrumentId, String positionId) {
        this.assetClass = assetClass;
        this.instrumentType = instrumentType;
        this.instrumentId = instrumentId;
        this.positionId = positionId;
    }

    public AssetClass.AssetClassEnum getAssetClass() {
        return assetClass;
    }

    public ExchangeListed.InstrumentTypeEnum getInstrumentType() {
        return instrumentType;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public String getPositionId() {
        return positionId;
    }

    @Override
    public PricingRuleIndex getRuleIndex() {
        return new LinearProductPricingRuleIndex(assetClass, instrumentType);
    }
}
