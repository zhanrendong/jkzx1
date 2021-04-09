package tech.tongyu.bct.quant.library.priceable.common.product;

import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasSingleStrike;

import java.time.LocalDateTime;

public class DoubleKnockOutContinuous extends AbstractDoubleBarrierContinuous
        implements HasSingleStrike {
    private final double strike;
    private final OptionTypeEnum optionType;

    public DoubleKnockOutContinuous(
            double strike, OptionTypeEnum optionType, LocalDateTime expiry, double lowBarrier, double highBarrier,
            BarrierTypeEnum barrierType, double lowRebate, double highRebate, RebateTypeEnum rebateType) {
        super(expiry, lowBarrier, highBarrier, barrierType, lowRebate, highRebate, rebateType);
        this.strike = strike;
        this.optionType = optionType;
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        if (underlyerPrice >= highBarrier) {
            return highRebate;
        }
        if (underlyerPrice <= lowBarrier) {
            return lowRebate;
        }
        return optionType == OptionTypeEnum.CALL ?
                Math.max(underlyerPrice - strike, 0) :
                Math.max(strike - underlyerPrice, 0);
    }

    @Override
    public double getStrike() {
        return strike;
    }

    public OptionTypeEnum getOptionType() {
        return optionType;
    }
}
