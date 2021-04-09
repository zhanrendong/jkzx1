package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.AsianFixedStrikeArithmetic;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;
import java.util.List;

public class EquityAsianFixedStrikeArithmetic<U extends Equity & ExchangeListed>
        extends AsianFixedStrikeArithmetic
        implements Equity, Priceable, HasUnderlyer {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public EquityAsianFixedStrikeArithmetic(
            U underlyer, LocalDateTime expiry, OptionTypeEnum optionType, double strike,
            List<LocalDateTime> observationDates, List<Double> weights, List<Double> fixings, double daysInYear) {
        super(expiry, optionType, strike, observationDates, weights, fixings, daysInYear);
        this.underlyer = underlyer;
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_ASIAN_FIXED_STRIKE_ARITHMETIC;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
