package tech.tongyu.bct.quant.library.priceable.common.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.HasSingleStrike;
import tech.tongyu.bct.quant.library.priceable.feature.ImmediateExercise;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class AbstractBarrierContinuous implements HasExpiry, HasSingleStrike, ImmediateExercise {
    protected final LocalDateTime expiry;
    protected final double strike;
    protected final OptionTypeEnum optionType;
    protected final double barrier;
    protected final BarrierDirectionEnum barrierDirection;
    protected final BarrierTypeEnum barrierType;
    protected final double rebate;
    protected final RebateTypeEnum rebateType;

    public AbstractBarrierContinuous(LocalDateTime expiry, double strike, OptionTypeEnum optionType,
                                     double barrier, BarrierDirectionEnum barrierDirection, BarrierTypeEnum barrierType,
                                     double rebate, RebateTypeEnum rebateType) {
        this.expiry = expiry;
        this.strike = strike;
        this.optionType = optionType;
        this.barrier = barrier;
        this.barrierDirection = barrierDirection;
        this.barrierType = barrierType;
        this.rebate = rebate;
        this.rebateType = rebateType;
    }

    @JsonIgnore
    @Override
    public LocalDate getExpirationDate() {
        return this.expiry.toLocalDate();
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

    public BarrierTypeEnum getBarrierType() {
        return barrierType;
    }

    public double getRebate() {
        return rebate;
    }

    public RebateTypeEnum getRebateType() {
        return rebateType;
    }
}
