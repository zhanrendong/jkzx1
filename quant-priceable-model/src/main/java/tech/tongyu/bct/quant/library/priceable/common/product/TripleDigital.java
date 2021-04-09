package tech.tongyu.bct.quant.library.priceable.common.product;

import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.ImmediateExercise;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class TripleDigital implements HasExpiry, ImmediateExercise {

    protected final LocalDateTime expiry;
    protected final OptionTypeEnum optionTypeEnum;

    protected final double firstStrike;
    protected final double firstPayment;

    protected final double secondStrike;
    protected final double secondPayment;

    protected final double thirdStrike;
    protected final double thirdPayment;


    public TripleDigital(LocalDateTime expiry, OptionTypeEnum optionTypeEnum, double firstStrike, double firstPayment, double secondStrike, double secondPayment, double thirdStrike, double thirdPayment) {
        this.expiry = expiry;
        this.optionTypeEnum = optionTypeEnum;
        this.firstStrike = firstStrike;
        this.firstPayment = firstPayment;
        this.secondStrike = secondStrike;
        this.secondPayment = secondPayment;
        this.thirdStrike = thirdStrike;
        this.thirdPayment = thirdPayment;
    }

    @Override
    public LocalDate getExpirationDate() {
        return expiry.toLocalDate();
    }

    @Override
    public LocalDateTime getExpiry() {
        return this.expiry;
    }

    public OptionTypeEnum getOptionTypeEnum() {
        return optionTypeEnum;
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

    public double getThirdStrike() {
        return thirdStrike;
    }

    public double getThirdPayment() {
        return thirdPayment;
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        if(Objects.equals(OptionTypeEnum.CALL, optionTypeEnum)){
            if(underlyerPrice >= firstStrike && underlyerPrice < secondStrike){
                return firstPayment;
            }
            if(underlyerPrice >= secondStrike && underlyerPrice < thirdStrike){
                return secondPayment;
            }
            if(underlyerPrice >= thirdStrike){
                return thirdPayment;
            }
            return 0;
        }
        else {
            if(underlyerPrice <= firstStrike && underlyerPrice > secondStrike){
                return firstPayment;
            }
            if(underlyerPrice <= secondStrike && underlyerPrice > thirdStrike){
                return secondPayment;
            }
            if(underlyerPrice <= thirdStrike){
                return thirdPayment;
            }
            return 0;
        }
    }
}
