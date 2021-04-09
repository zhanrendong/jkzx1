package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;

import java.time.LocalDateTime;
import java.util.TreeMap;

/**
 * Phoenix option on single asset.
 *
 * If knocked out, pay (knock out coupon + principal), no matter knocked in or not.
 *
 * If spot is above coupon barrier at some coupon observation date, pay coupon.
 * If not snowball, only coupon linked to the last observation period is paid,
 * otherwise all unpaid coupon is paid.
 *
 * If knocked in, at expiry pay
 * principal * min(underlying spot at expiry / initial underlying spot, 1) + coupon(if any)
 *
 * All input date arrays are assumed to be in natural order.
 */
@BctQuantSerializable
public class AutoCallPhoenix {
    @JsonProperty("class")
    private final String instrument = AutoCallPhoenix.class.getSimpleName();

    private final double principal;
    private final double initialSpot; // initial underlying spot

    private final LocalDateTime expiry;
    private final LocalDateTime delivery;

    // knock out
    private final LocalDateTime[] outObsDates;
    private final double[] outBarriers;
    private final LocalDateTime[] outDeliveries;

    // knock in
    private final LocalDateTime[] inObsDates;
    private final double[] inBarriers;
    private final double inStrike;

    // coupon
    private final LocalDateTime[] couponObsDates;
    private final double[] couponBarriers;
    private final LocalDateTime[] couponDeliveries;
    private final double[] coupons;
    private final boolean isSnowball;

    // history spots between start of the option and valuation
    private final TreeMap<LocalDateTime, Double> fixings;

    @JsonCreator
    public AutoCallPhoenix(
            @JsonProperty("principal") double principal,
            @JsonProperty("initialSpot") double initialSpot,
            @JsonProperty("expiry") LocalDateTime expiry,
            @JsonProperty("delivery") LocalDateTime delivery,
            @JsonProperty("outObsDates") LocalDateTime[] outObsDates,
            @JsonProperty("outBarriers") double[] outBarriers,
            @JsonProperty("outDeliveries") LocalDateTime[] outDeliveries,
            @JsonProperty("couponObsDates") LocalDateTime[] couponObsDates,
            @JsonProperty("couponBarriers") double[] couponBarriers,
            @JsonProperty("couponDeliveries") LocalDateTime[] couponDeliveries,
            @JsonProperty("coupons") double[] coupons,
            @JsonProperty("isSnowball") boolean isSnowball,
            @JsonProperty("inObsDates") LocalDateTime[] inObsDates,
            @JsonProperty("inBarriers") double[] inBarriers,
            @JsonProperty("inStrike") double inStrike,
            @JsonProperty("fixingDates") LocalDateTime[] fixingDates,
            @JsonProperty("fixings") double[] fixings) {
        this.principal = principal;
        this.initialSpot = initialSpot;
        this.expiry = expiry;
        this.delivery = delivery;
        this.outObsDates = outObsDates;
        this.outBarriers = outBarriers;
        this.outDeliveries = outDeliveries;
        this.couponObsDates = couponObsDates;
        this.couponBarriers = couponBarriers;
        this.couponDeliveries = couponDeliveries;
        this.coupons = coupons;
        this.isSnowball = isSnowball;
        // it is possible to have no knock in feature
        if (inObsDates == null && inBarriers == null) {
            this.inObsDates = new LocalDateTime[0];
            this.inBarriers = new double[0];
            this.inStrike = 0;
        }
        else {
            this.inObsDates = inObsDates;
            this.inBarriers = inBarriers;
            this.inStrike = inStrike;
        }
        this.fixings = new TreeMap<>();
        if (fixingDates != null && fixings != null) {
            for (int i = 0; i < fixingDates.length; i++)
                this.fixings.put(fixingDates[i], fixings[i]);
        }
    }

    public String getInstrument() {
        return instrument;
    }

    public double getPrincipal() {
        return principal;
    }

    public double getInitialSpot() {
        return initialSpot;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public LocalDateTime getDelivery() {
        return delivery;
    }

    public LocalDateTime[] getOutObsDates() {
        return outObsDates;
    }

    public double[] getOutBarriers() {
        return outBarriers;
    }

    public LocalDateTime[] getOutDeliveries() {
        return outDeliveries;
    }

    public LocalDateTime[] getCouponObsDates() {
        return couponObsDates;
    }

    public double[] getCouponBarriers() {
        return couponBarriers;
    }

    public LocalDateTime[] getCouponDeliveries() {
        return couponDeliveries;
    }

    public double[] getCoupons() {
        return coupons;
    }

    public boolean isSnowball() {
        return isSnowball;
    }

    public LocalDateTime[] getInObsDates() {
        return inObsDates;
    }

    public double[] getInBarriers() {
        return inBarriers;
    }

    public double getInStrike() {
        return inStrike;
    }

    public TreeMap<LocalDateTime, Double> getFixings() {
        return fixings;
    }
}
