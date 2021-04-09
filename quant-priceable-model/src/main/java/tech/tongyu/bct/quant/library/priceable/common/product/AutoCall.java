package tech.tongyu.bct.quant.library.priceable.common.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AutoCall implements HasExpiry {
    protected final LocalDateTime expiry;
    protected final List<LocalDate> observationDates;
    protected final BarrierDirectionEnum direction;
    protected final List<Double> barriers;
    protected final List<LocalDate> paymentDates;
    protected final List<Double> payments;
    protected final boolean finalPayFixed;
    protected final LocalDate finalPaymentDate;
    protected final double finalPayment;
    protected final OptionTypeEnum finalOptionType;
    protected final double finalOptionStrike;
    protected final boolean knockedOut;
    protected final double knockedOutPayment;
    protected final LocalDate knockedOutPaymentDate;

    public AutoCall(LocalDateTime expiry, List<LocalDate> observationDates,
                    BarrierDirectionEnum direction, List<Double> barriers,
                    List<LocalDate> paymentDates, List<Double> payments,
                    boolean finalPayFixed,
                    LocalDate finalPaymentDate,
                    double finalPayment,
                    OptionTypeEnum finalOptionType, double finalOptionStrike,
                    boolean knockedOut, double knockedOutPayment, LocalDate knockedOutPaymentDate) {
        this.expiry = expiry;
        this.observationDates = observationDates;
        this.direction = direction;
        this.barriers = barriers;
        this.paymentDates = paymentDates;
        this.payments = payments;
        this.finalPayFixed = finalPayFixed;
        this.finalPaymentDate = finalPaymentDate;
        this.finalPayment = finalPayment;
        this.finalOptionType = finalOptionType;
        this.finalOptionStrike = finalOptionStrike;
        this.knockedOut = knockedOut;
        this.knockedOutPayment = knockedOutPayment;
        this.knockedOutPaymentDate = knockedOutPaymentDate;
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

    public List<LocalDate> getObservationDates() {
        return observationDates;
    }

    public BarrierDirectionEnum getDirection() {
        return direction;
    }

    public List<Double> getBarriers() {
        return barriers;
    }

    public List<LocalDate> getPaymentDates() {
        return paymentDates;
    }

    public List<Double> getPayments() {
        return payments;
    }

    public boolean isFinalPayFixed() {
        return finalPayFixed;
    }

    public LocalDate getFinalPaymentDate() {
        return finalPaymentDate;
    }

    public double getFinalPayment() {
        return finalPayment;
    }

    public OptionTypeEnum getFinalOptionType() {
        return finalOptionType;
    }

    public double getFinalOptionStrike() {
        return finalOptionStrike;
    }

    public boolean knockedOut() {
        return knockedOut;
    }

    public double getKnockedOutPayment() {
        return knockedOutPayment;
    }

    public LocalDate getKnockedOutPaymentDate() {
        return knockedOutPaymentDate;
    }
}
