package tech.tongyu.bct.quant.library.priceable.common.product;

import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.ImmediateExercise;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DigitalConvex implements HasExpiry, ImmediateExercise {

    protected final LocalDateTime expiry;
    protected final double lowStrike;
    protected final double highStrike;
    protected final double payment;

    public DigitalConvex(LocalDateTime expiry, double lowStrike, double highStrike, double payment){
        this.expiry = expiry;
        this.lowStrike = lowStrike;
        this.highStrike = highStrike;
        this.payment = payment;
    }

    @Override
    public LocalDate getExpirationDate() {
        return expiry.toLocalDate();
    }

    @Override
    public LocalDateTime getExpiry() {
        return this.expiry;
    }

    public double getLowStrike() {
        return lowStrike;
    }

    public double getHighStrike() {
        return highStrike;
    }

    public double getPayment() {
        return payment;
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        if(underlyerPrice < highStrike && underlyerPrice > lowStrike)
            return payment;
        return 0.;
    }
}
