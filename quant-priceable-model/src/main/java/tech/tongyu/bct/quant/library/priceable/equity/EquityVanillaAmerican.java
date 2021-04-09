package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.Vanilla;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.common.flag.ExerciseTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

import java.time.LocalDateTime;

public class EquityVanillaAmerican<U extends Equity & ExchangeListed>
        extends Vanilla
        implements Equity, Priceable, HasUnderlyer {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    @JsonCreator
    public EquityVanillaAmerican(U underlyer, double strike, LocalDateTime expiry, OptionTypeEnum optionType) {
        super(strike, expiry, optionType, ExerciseTypeEnum.AMERICAN);
        this.underlyer = underlyer;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_VANILLA_AMERICAN;
    }

    @Override
    public U getUnderlyer() {
        return underlyer;
    }
}
