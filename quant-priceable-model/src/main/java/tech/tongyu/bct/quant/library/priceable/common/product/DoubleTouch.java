package tech.tongyu.bct.quant.library.priceable.common.product;

import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum;

import java.time.LocalDateTime;

public class DoubleTouch extends AbstractDoubleBarrierContinuous {
    private final double noTouchRebate;

    public DoubleTouch(
            LocalDateTime expiry, RebateTypeEnum rebateType, double lowBarrier, double highBarrier,
            double lowRebate, double highRebate, double noTouchRebate) {
        super(expiry, lowBarrier, highBarrier, BarrierTypeEnum.OUT, lowRebate, highRebate, rebateType);
        this.noTouchRebate = noTouchRebate;
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        return underlyerPrice <= lowBarrier ? lowRebate
                : (underlyerPrice >= highBarrier ? highRebate
                : noTouchRebate);
    }

    public double getNoTouchRebate() {
        return noTouchRebate;
    }
}
