package tech.tongyu.bct.pricing.common.descriptor.impl;

import tech.tongyu.bct.pricing.common.descriptor.PositionDescriptor;
import tech.tongyu.bct.pricing.common.rule.impl.SingleAssetOptionPricingRuleIndex;
import tech.tongyu.bct.quant.library.priceable.AssetClass;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

public class CustomProductModelXYPositionDescriptor implements PositionDescriptor {
    private final String positionId;
    private final String underlyerInstrumentId;

    public CustomProductModelXYPositionDescriptor(String positionId, String underlyerInstrumentId) {
        this.positionId = positionId;
        this.underlyerInstrumentId = underlyerInstrumentId;
    }

    @Override
    public SingleAssetOptionPricingRuleIndex getRuleIndex() {
        return new SingleAssetOptionPricingRuleIndex(AssetClass.AssetClassEnum.ANY,
                Priceable.PriceableTypeEnum.CUSTOM_MODEL_XY, ExchangeListed.InstrumentTypeEnum.ANY, null);
    }

    public String getUnderlyerInstrumentId() {
        return underlyerInstrumentId;
    }

    public String getPositionId() {
        return positionId;
    }
}
