package tech.tongyu.bct.quant.library.priceable.common.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.ImmediateExercise;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class VerticalSpread implements HasExpiry, ImmediateExercise {
    protected final LocalDateTime expiry;
    protected final double strikeLow;
    protected final double strikeHigh;
    protected final OptionTypeEnum optionType;

    public VerticalSpread(double strikeLow, double strikeHigh, LocalDateTime expiry, OptionTypeEnum optionType) {
        this.expiry = expiry;
        this.strikeLow = strikeLow;
        this.strikeHigh = strikeHigh;
        this.optionType = optionType;
    }

    @JsonIgnore
    @Override
    public LocalDate getExpirationDate() {
        return this.expiry.toLocalDate();
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        return this.optionType == OptionTypeEnum.CALL ?
                (underlyerPrice < this.strikeLow ?
                        0.0 :
                        (underlyerPrice >= this.strikeHigh ?
                                this.strikeHigh - this.strikeLow :
                                underlyerPrice - this.strikeLow)) :
                (underlyerPrice < this.strikeLow ?
                        this.strikeHigh - this.strikeLow :
                        (underlyerPrice >= this.strikeHigh ?
                                0.0 :
                                this.strikeHigh - underlyerPrice));
    }

    @Override
    public LocalDateTime getExpiry() {
        return expiry;
    }

    public double getStrikeLow() {
        return strikeLow;
    }

    public double getStrikeHigh() {
        return strikeHigh;
    }

    public OptionTypeEnum getOptionType() {
        return optionType;
    }
}
