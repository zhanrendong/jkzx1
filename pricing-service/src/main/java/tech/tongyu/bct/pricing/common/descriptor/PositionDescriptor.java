package tech.tongyu.bct.pricing.common.descriptor;

import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.pricing.common.descriptor.impl.*;
import tech.tongyu.bct.pricing.common.rule.PricingRuleIndex;
import tech.tongyu.bct.quant.library.priceable.AssetClass;
import tech.tongyu.bct.quant.library.priceable.Cash;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.product.CustomProductModelXY;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasBasketUnderlyer;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.util.ArrayList;
import java.util.List;


public interface PositionDescriptor {
    PricingRuleIndex getRuleIndex();

    static PositionDescriptor getPositionDescriptor(Position position ) {
        String positionId = position.getPositionId();
        Priceable priceable = position.getPriceable();
        // custom model xy
        if (priceable instanceof CustomProductModelXY) {
            return new CustomProductModelXYPositionDescriptor(
                    positionId,
                    ((CustomProductModelXY) priceable).getUnderlyerInstrumentId());
        }
        // option on basket/multi-asset
        if (priceable instanceof HasBasketUnderlyer) {
            List<Priceable> underlyers = ((HasBasketUnderlyer) priceable).underlyers();
            List<String> underlyerInstruments = new ArrayList<>();
            for (Priceable p : underlyers) {
                if (!(p instanceof ExchangeListed)) {
                    throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                            "篮子期权标的物必须为交易所可交易品种，比如大宗期货，股票等");
                }
                underlyerInstruments.add(((ExchangeListed)p).getInstrumentId());
            }
            return new OptionOnBasketPositionDescriptor(underlyerInstruments);
        }
        // cash
        if (priceable instanceof Cash) {
            return new CashPositionDescriptor(positionId);
        }
        // linear products
        if (priceable instanceof ExchangeListed) {
            ExchangeListed exchangeListed = (ExchangeListed) priceable;
            return new LinearProductPositionDescriptor(((AssetClass)exchangeListed).getAssetClassEnum(),
                    exchangeListed.getInstrumentType(), exchangeListed.getInstrumentId(), positionId);
        }
        // options
        if (priceable instanceof HasUnderlyer) {
            Priceable underlyer = ((HasUnderlyer) priceable).getUnderlyer();
            if (!(underlyer instanceof ExchangeListed)) {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID, "期权标的物必须为交易所可交易品种，比如大宗期货，股票等");
            }
            return new OptionPositionDescriptor(((AssetClass)priceable).getAssetClassEnum(),
                    priceable.getPriceableTypeEnum(),
                    ((ExchangeListed) underlyer).getInstrumentType(),
                    ((ExchangeListed) underlyer).getInstrumentId(),
                    positionId);
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "期权产品必须有标的物");
        }
    }
}
