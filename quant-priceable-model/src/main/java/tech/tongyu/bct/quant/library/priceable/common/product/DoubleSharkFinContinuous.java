package tech.tongyu.bct.quant.library.priceable.common.product;

import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum;

import java.time.LocalDateTime;

public class DoubleSharkFinContinuous extends AbstractDoubleBarrierContinuous {
    protected final double lowStrike;
    protected final double highStrike;
    protected final double lowParticipationRate;
    protected final double highParticipationRate;

    public DoubleSharkFinContinuous(
            double lowStrike, double highStrike, LocalDateTime expiry, double lowBarrier, double highBarrier,
            BarrierTypeEnum barrierType, double lowRebate, double highRebate, RebateTypeEnum rebateType,
            double lowParticipationRate, double highParticipationRate) {
        super(expiry, lowBarrier, highBarrier, barrierType, lowRebate, highRebate, rebateType);
        this.lowStrike = lowStrike;
        this.highStrike = highStrike;
        this.lowParticipationRate = lowParticipationRate;
        this.highParticipationRate = highParticipationRate;
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        if (underlyerPrice >= highBarrier) {
            return highRebate;
        }
        if (underlyerPrice <= lowBarrier) {
            return lowRebate;
        }
        return Math.max(lowStrike - underlyerPrice, 0) + Math.max(underlyerPrice - highStrike, 0);
    }
}
