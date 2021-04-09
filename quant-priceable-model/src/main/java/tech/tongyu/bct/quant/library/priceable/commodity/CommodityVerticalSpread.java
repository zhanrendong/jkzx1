package tech.tongyu.bct.quant.library.priceable.commodity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Commodity;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.VerticalSpread;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;

public class CommodityVerticalSpread<U extends Commodity & ExchangeListed>
        extends VerticalSpread
        implements Commodity, Priceable, HasUnderlyer {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public CommodityVerticalSpread(U underlyer, double strikeLow, double strikeHigh,
            LocalDateTime expiry, OptionTypeEnum optionType) {
        super(strikeLow, strikeHigh, expiry, optionType);
        this.underlyer = underlyer;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.COMMODITY_VERTICAL_SPREAD;
    }

    @Override
    public U getUnderlyer() {
        return underlyer;
    }
}
