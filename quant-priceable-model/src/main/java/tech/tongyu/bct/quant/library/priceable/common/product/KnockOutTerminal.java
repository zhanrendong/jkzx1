package tech.tongyu.bct.quant.library.priceable.common.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.HasSingleStrike;
import tech.tongyu.bct.quant.library.priceable.feature.ImmediateExercise;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum.DOWN;
import static tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum.UP;

public class KnockOutTerminal implements HasExpiry, HasSingleStrike, ImmediateExercise {
    protected final LocalDateTime expiry;
    protected final double strike;
    protected final OptionTypeEnum optionType;
    protected final double barrier;
    protected final BarrierDirectionEnum barrierDirection;
    protected final double rebate;

    public KnockOutTerminal(LocalDateTime expiry, double strike, OptionTypeEnum optionType,
            double barrier, BarrierDirectionEnum barrierDirection, double rebate) {
        this.expiry = expiry;
        this.strike = strike;
        this.optionType = optionType;
        this.barrier = barrier;
        this.barrierDirection = barrierDirection;
        this.rebate = rebate;
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

    @JsonIgnore
    @Override
    public LocalDate getExpirationDate() {
        return expiry.toLocalDate();
    }

    @Override
    public LocalDateTime getExpiry() {
        return expiry;
    }

    @Override
    public double getStrike() {
        return strike;
    }

    public OptionTypeEnum getOptionType() {
        return optionType;
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
}
