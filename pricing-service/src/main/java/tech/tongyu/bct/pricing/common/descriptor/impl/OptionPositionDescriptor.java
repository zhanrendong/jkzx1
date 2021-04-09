package tech.tongyu.bct.pricing.common.descriptor.impl;

import tech.tongyu.bct.pricing.common.descriptor.PositionDescriptor;
import tech.tongyu.bct.pricing.common.rule.PricingRuleIndex;
import tech.tongyu.bct.pricing.common.rule.impl.SingleAssetOptionPricingRuleIndex;
import tech.tongyu.bct.quant.library.priceable.AssetClass;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

public class OptionPositionDescriptor implements PositionDescriptor {
    private final AssetClass.AssetClassEnum assetClass;
    private final Priceable.PriceableTypeEnum productType;
    private final ExchangeListed.InstrumentTypeEnum underlyerType;
    private final String underlyerInstrumentId;
    private final String positionId;

    public OptionPositionDescriptor(AssetClass.AssetClassEnum assetClass, Priceable.PriceableTypeEnum productType,
                                    ExchangeListed.InstrumentTypeEnum underlyerType,
                                    String underlyerInstrumentId, String positionId) {
        this.assetClass = assetClass;
        this.productType = productType;
        this.underlyerType = underlyerType;
        this.underlyerInstrumentId = underlyerInstrumentId;
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

    public String getUnderlyerInstrumentId() {
        return underlyerInstrumentId;
    }

    public String getPositionId() {
        return positionId;
    }

    @Override
    public PricingRuleIndex getRuleIndex() {
        return new SingleAssetOptionPricingRuleIndex(assetClass, productType, underlyerType, positionId);
    }
}
