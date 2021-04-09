package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.VerticalSpread;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;

import java.time.LocalDateTime;

public class EquityVerticalSpread<U extends Equity & ExchangeListed>
        extends VerticalSpread
        implements Equity, Priceable, HasExpiry, HasUnderlyer {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    @JsonCreator
    public EquityVerticalSpread(U underlyer, double strikeLow, double strikeHigh,
                                     LocalDateTime expiry, OptionTypeEnum optionType) {
        super(strikeLow, strikeHigh, expiry, optionType);
        this.underlyer = underlyer;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_VERTICAL_SPREAD;
    }

    @Override
    public U getUnderlyer() {
        return underlyer;
    }
}
