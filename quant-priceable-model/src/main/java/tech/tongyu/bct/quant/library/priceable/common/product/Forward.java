package tech.tongyu.bct.quant.library.priceable.common.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.HasSingleStrike;
import tech.tongyu.bct.quant.library.priceable.feature.ImmediateExercise;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Forward implements HasExpiry, HasSingleStrike, ImmediateExercise {
    protected final LocalDateTime expiry;
    protected final LocalDate deliveryDate;
    protected final double strike;

    public Forward(LocalDateTime expiry, LocalDate deliveryDate, double strike) {
        this.expiry = expiry;
        this.deliveryDate = deliveryDate;
        this.strike = strike;
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

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    @Override
    public double getStrike() {
        return strike;
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        return underlyerPrice - this.strike;
    }
}
