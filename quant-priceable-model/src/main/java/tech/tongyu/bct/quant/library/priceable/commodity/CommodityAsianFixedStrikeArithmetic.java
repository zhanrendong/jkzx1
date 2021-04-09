package tech.tongyu.bct.quant.library.priceable.commodity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Commodity;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.AsianFixedStrikeArithmetic;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;
import java.util.List;

public class CommodityAsianFixedStrikeArithmetic<U extends Commodity & ExchangeListed>
        extends AsianFixedStrikeArithmetic
        implements Commodity, Priceable, HasUnderlyer {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public CommodityAsianFixedStrikeArithmetic(
            U underlyer, LocalDateTime expiry, OptionTypeEnum optionType, double strike,
            List<LocalDateTime> observationDates, List<Double> weights, List<Double> fixings, double daysInYear) {
        super(expiry, optionType, strike, observationDates, weights, fixings, daysInYear);
        this.underlyer = underlyer;
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.COMMODITY_ASIAN_FIXED_STRIKE_ARITHMETIC;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
