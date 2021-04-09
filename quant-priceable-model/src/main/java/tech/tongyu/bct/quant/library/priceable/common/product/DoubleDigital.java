package tech.tongyu.bct.quant.library.priceable.common.product;

import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.ImmediateExercise;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class DoubleDigital implements HasExpiry, ImmediateExercise {

    protected final LocalDateTime expiry;

    protected final OptionTypeEnum optionTypeEnum;

    protected final double firstStrike;
    protected final double firstPayment;

    protected final double secondStrike;
    protected final double secondPayment;

    public DoubleDigital(LocalDateTime expiry, OptionTypeEnum optionTypeEnum, double firstStrike, double firstPayment, double secondStrike, double secondPayment) {
        this.expiry = expiry;
        this.optionTypeEnum = optionTypeEnum;
        this.firstStrike = firstStrike;
        this.firstPayment = firstPayment;
        this.secondStrike = secondStrike;
        this.secondPayment = secondPayment;
    }

    @Override
    public LocalDate getExpirationDate() {
        return expiry.toLocalDate();
    }

    @Override
    public LocalDateTime getExpiry() {
        return expiry;
    }

    public double getFirstStrike() {
        return firstStrike;
    }

    public double getFirstPayment() {
        return firstPayment;
    }

    public double getSecondStrike() {
        return secondStrike;
    }

    public double getSecondPayment() {
        return secondPayment;
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        if(Objects.equals(optionTypeEnum, OptionTypeEnum.CALL)) {
            if (underlyerPrice >= firstStrike && underlyerPrice < secondStrike)
                return firstPayment;
            if (underlyerPrice >= secondStrike)
                return secondPayment;
            return 0.;
        }
        else{
            if(underlyerPrice > secondStrike && underlyerPrice <= firstStrike)
                return firstPayment;
            if(underlyerPrice <= secondStrike)
                return secondPayment;
            return 0.;
        }
    }
}
