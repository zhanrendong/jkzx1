package tech.tongyu.bct.quant.library.priceable.common.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.HasSingleDelivery;
import tech.tongyu.bct.quant.library.priceable.feature.HasSingleStrike;
import tech.tongyu.bct.quant.library.priceable.feature.ImmediateExercise;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DigitalCash implements HasExpiry, HasSingleDelivery, HasSingleStrike, ImmediateExercise {
    protected final LocalDateTime expiry;
    protected final double strike;
    protected final OptionTypeEnum optionType;
    protected final double payment;
    protected final LocalDate deliveryDate;

    public DigitalCash(double strike, LocalDateTime expiry, OptionTypeEnum optionType,
                       double payment, LocalDate deliveryDate) {
        this.expiry = expiry;
        this.strike = strike;
        this.optionType = optionType;
        this.payment = payment;
        this.deliveryDate = deliveryDate;
    }

    public DigitalCash(double strike, LocalDateTime expiry, OptionTypeEnum optionType, double payment) {
        this.expiry = expiry;
        this.strike = strike;
        this.optionType = optionType;
        this.payment = payment;
        this.deliveryDate = expiry.toLocalDate();
    }

    @JsonIgnore
    @Override
    public LocalDate getExpirationDate() {
        return this.expiry.toLocalDate();
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        return this.optionType == OptionTypeEnum.CALL ?
                (underlyerPrice >= this.strike ? this.payment : 0.0) :
                (underlyerPrice <= this.strike ? this.payment : 0.0);
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

    public double getPayment() {
        return payment;
    }

    @Override
    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }
}
