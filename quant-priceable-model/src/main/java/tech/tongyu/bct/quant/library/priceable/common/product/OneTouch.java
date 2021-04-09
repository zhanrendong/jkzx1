package tech.tongyu.bct.quant.library.priceable.common.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.ObservationTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.ImmediateExercise;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class OneTouch implements HasExpiry, ImmediateExercise {
    protected final LocalDateTime expiry;
    protected final double barrier;
    protected final BarrierDirectionEnum barrierDirection;
    protected final double rebate;
    protected final RebateTypeEnum rebateType;
    protected final ObservationTypeEnum observationType;
    protected final double daysInYear;

    public OneTouch(LocalDateTime expiry, double barrier, BarrierDirectionEnum barrierDirection,
            double rebate, RebateTypeEnum rebateType, ObservationTypeEnum observationType, double daysInYear) {
        this.expiry = expiry;
        this.barrier = barrier;
        this.barrierDirection = barrierDirection;
        this.rebate = rebate;
        this.rebateType = rebateType;
        this.observationType = observationType;
        this.daysInYear = daysInYear;
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        return barrierDirection == BarrierDirectionEnum.UP ?
                (underlyerPrice >= barrier ? rebate : 0.0) :
                (underlyerPrice <= barrier ? rebate : 0.0);
    }

    public double effectiveBarrier(double vol) {
        if (observationType == ObservationTypeEnum.DAILY) {
            double idx = 0.58259715793901068 * vol * FastMath.sqrt(1. / daysInYear);
            return barrier * FastMath.exp(barrierDirection == BarrierDirectionEnum.UP ? idx : -idx);
        }
        return barrier;
    }

    @JsonIgnore
    @Override
    public LocalDate getExpirationDate() {
        return expiry.toLocalDate();
    }

    @Override
    public LocalDateTime getExpiry() {
        return expiry;
    }

    public double getBarrier() {
        return barrier;
    }

    public BarrierDirectionEnum getBarrierDirection() {
        return barrierDirection;
    }

    public double getRebate() {
        return rebate;
    }

    public RebateTypeEnum getRebateType() {
        return rebateType;
    }
}
