package tech.tongyu.bct.quant.library.priceable.common.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AutoCallPhoenix implements HasExpiry {
    protected final LocalDateTime expiry;
    protected final List<LocalDate> observationDates;
    protected final List<Double> observed;
    protected final BarrierDirectionEnum direction;
    protected final List<Double> barriers;
    protected final List<Double> couponBarriers;
    protected final List<LocalDate> paymentDates;
    protected final List<Double> coupons; // for each period
    protected final List<LocalDate> knockInObservationDates;
    protected final List<Double> knockInBarriers;
    protected final boolean knockedIn;
    protected final OptionTypeEnum knockedInOptionType;
    protected final double knockedInOptionStrike;
    protected final LocalDate finalPaymentDate;

    public AutoCallPhoenix(LocalDateTime expiry,
                           List<LocalDate> observationDates, List<Double> observed,
                           BarrierDirectionEnum direction, List<Double> barriers, List<Double> couponBarriers,
                           List<LocalDate> paymentDates, List<Double> coupons,
                           List<LocalDate> knockInObservationDates,
                           List<Double> knockInBarriers,
                           boolean knockedIn,
                           OptionTypeEnum knockedInOptionType, double knockedInOptionStrike,
                           LocalDate finalPaymentDate) {
        this.expiry = expiry;
        this.observationDates = observationDates;
        this.observed = observed;
        this.direction = direction;
        this.barriers = barriers;
        this.couponBarriers = couponBarriers;
        this.paymentDates = paymentDates;
        this.coupons = coupons;
        this.knockedIn = knockedIn;
        this.knockInObservationDates = knockInObservationDates;
        this.knockInBarriers = knockInBarriers;
        this.knockedInOptionType = knockedInOptionType;
        this.knockedInOptionStrike = knockedInOptionStrike;
        this.finalPaymentDate = finalPaymentDate;
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

    public List<Double> getObserved() {
        return observed;
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

    public List<Double> getCoupons() {
        return coupons;
    }

    public List<LocalDate> getKnockInObservationDates() {
        return knockInObservationDates;
    }

    public boolean isKnockedIn() {
        return knockedIn;
    }

    public OptionTypeEnum getKnockedInOptionType() {
        return knockedInOptionType;
    }

    public double getKnockedInOptionStrike() {
        return knockedInOptionStrike;
    }

    public List<Double> getCouponBarriers() {
        return couponBarriers;
    }

    public LocalDate getFinalPaymentDate() {
        return finalPaymentDate;
    }

    public List<Double> getKnockInBarriers() {
        return knockInBarriers;
    }
}
