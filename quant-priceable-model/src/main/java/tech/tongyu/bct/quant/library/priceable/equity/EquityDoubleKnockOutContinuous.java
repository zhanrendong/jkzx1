package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.DoubleKnockOutContinuous;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;

public class EquityDoubleKnockOutContinuous<U extends Equity & ExchangeListed>
        extends DoubleKnockOutContinuous
        implements Equity, Priceable, HasUnderlyer {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public EquityDoubleKnockOutContinuous(
            U underlyer, double strike, OptionTypeEnum optionType, LocalDateTime expiry, double lowBarrier,
            double highBarrier, BarrierTypeEnum barrierType, double lowRebate, double highRebate,
            RebateTypeEnum rebateType) {
        super(strike, optionType, expiry, lowBarrier, highBarrier, barrierType, lowRebate, highRebate, rebateType);
        this.underlyer = underlyer;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_DOUBLE_KNOCK_OUT_CONTINUOUS;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
