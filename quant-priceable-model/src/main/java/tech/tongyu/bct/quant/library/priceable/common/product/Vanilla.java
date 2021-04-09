package tech.tongyu.bct.quant.library.priceable.common.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.common.flag.ExerciseTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.HasSingleStrike;
import tech.tongyu.bct.quant.library.priceable.feature.ImmediateExercise;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Vanilla implements HasExpiry, HasSingleStrike, ImmediateExercise {
    protected final LocalDateTime expiry;
    protected final double strike;
    protected final OptionTypeEnum optionType;
    protected final ExerciseTypeEnum exerciseType;

    public Vanilla(double strike, LocalDateTime expiry, OptionTypeEnum optionType, ExerciseTypeEnum exerciseType) {
        this.expiry = expiry;
        this.strike = strike;
        this.optionType = optionType;
        this.exerciseType = exerciseType;
    }

    @JsonIgnore
    @Override
    public LocalDate getExpirationDate() {
        return this.expiry.toLocalDate();
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        return this.optionType == OptionTypeEnum.CALL ?
                Math.max(underlyerPrice - this.strike, 0.0) :
                Math.max(this.strike - underlyerPrice, 0.0);
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

    public ExerciseTypeEnum getExerciseType() {
        return exerciseType;
    }
}
