package tech.tongyu.bct.quant.library.priceable.common.product;

import io.vavr.Tuple2;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierTypeEnum;

import java.time.LocalDateTime;

public class DoubleSharkFinTerminal extends AbstractDoubleBarrierTerminal {
    protected final double lowStrike;
    protected final double highStrike;
    protected final double lowParticipationRate;
    protected final double highParticipationRate;

    public DoubleSharkFinTerminal(
            double lowStrike, double highStrike, LocalDateTime expiry,
            double lowBarrier, double highBarrier, BarrierTypeEnum barrierType, double lowRebate,
            double highRebate, double lowParticipationRate, double highParticipationRate) {
        super(expiry, lowBarrier, highBarrier, barrierType, lowRebate, highRebate);
        this.lowStrike = lowStrike;
        this.highStrike = highStrike;
        this.lowParticipationRate = lowParticipationRate;
        this.highParticipationRate = highParticipationRate;
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        if (underlyerPrice <= lowBarrier) {
            return lowRebate;
        } else if (underlyerPrice >= highBarrier) {
            return highRebate;
        } else {
            return Math.max(lowStrike - underlyerPrice, 0) + Math.max(underlyerPrice - highStrike, 0);
        }
    }
}
