package tech.tongyu.bct.pricing.common.descriptor.impl;

import tech.tongyu.bct.pricing.common.descriptor.PositionDescriptor;
import tech.tongyu.bct.pricing.common.rule.PricingRuleIndex;
import tech.tongyu.bct.pricing.common.rule.impl.SingleAssetOptionPricingRuleIndex;
import tech.tongyu.bct.quant.library.priceable.AssetClass;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

import java.util.List;

public class OptionOnBasketPositionDescriptor implements PositionDescriptor {
    private final List<String> underlyerInstrumentIds;

    public OptionOnBasketPositionDescriptor(List<String> underlyerInstrumentIds) {
        this.underlyerInstrumentIds = underlyerInstrumentIds;
    }

    public List<String> getUnderlyerInstrumentIds() {
        return underlyerInstrumentIds;
    }

    @Override
    public SingleAssetOptionPricingRuleIndex getRuleIndex() {
        // we are using the same rule for all multi-asset options
        // this is why all the parameters are just placeholders
        return new SingleAssetOptionPricingRuleIndex(AssetClass.AssetClassEnum.ANY,
                Priceable.PriceableTypeEnum.GENERIC_MULTI_ASSET_OPTION,
                ExchangeListed.InstrumentTypeEnum.ANY, null);
    }
}
