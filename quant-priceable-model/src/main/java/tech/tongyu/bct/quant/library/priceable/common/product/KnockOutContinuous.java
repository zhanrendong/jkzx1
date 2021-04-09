package tech.tongyu.bct.quant.library.priceable.common.product;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.quant.library.priceable.common.flag.*;

import java.time.LocalDateTime;

import static tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum.DOWN;
import static tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum.UP;

public class KnockOutContinuous extends AbstractBarrierContinuous {
    protected final ObservationTypeEnum observationType;
    protected final double daysInYear;

    public KnockOutContinuous(LocalDateTime expiry, double strike, OptionTypeEnum optionType,
                              double barrier, BarrierDirectionEnum barrierDirection,
                              double rebate, RebateTypeEnum rebateType, ObservationTypeEnum observationType,
                              double daysInYear) {
        super(expiry, strike, optionType, barrier, barrierDirection, BarrierTypeEnum.OUT, rebate, rebateType);
        this.observationType = observationType;
        this.daysInYear = daysInYear;
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        if ((this.barrierDirection == UP && underlyerPrice >= this.barrier) ||
                (this.barrierDirection == DOWN && underlyerPrice <= this.barrier)) {
            return this.rebate;
        }
        return this.optionType == OptionTypeEnum.CALL ?
                Math.max(underlyerPrice - this.strike, 0.0) :
                Math.max(this.strike - underlyerPrice, 0.0);
    }

    public ObservationTypeEnum getObservationType() {
        return observationType;
    }

    public double getDaysInYear() {
        return daysInYear;
    }
}
