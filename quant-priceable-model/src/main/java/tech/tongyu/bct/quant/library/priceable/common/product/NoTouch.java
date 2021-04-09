package tech.tongyu.bct.quant.library.priceable.common.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.ImmediateExercise;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class NoTouch implements HasExpiry, ImmediateExercise {
    protected final LocalDateTime expiry;
    protected final double barrier;
    protected final BarrierDirectionEnum barrierDirection;
    protected final double payment;

    public NoTouch(LocalDateTime expiry, double barrier, BarrierDirectionEnum barrierDirection, double payment) {
        this.expiry = expiry;
        this.barrier = barrier;
        this.barrierDirection = barrierDirection;
        this.payment = payment;
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        return barrierDirection == BarrierDirectionEnum.UP ?
                (underlyerPrice >= barrier ? 0.0 : payment) :
                (underlyerPrice <= barrier ? 0.0 : payment);
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

    public double getPayment() {
        return payment;
    }
}
