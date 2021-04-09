package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;

import java.time.LocalDateTime;

/**
 * @author Liao Song
 *         Range accrual with predetermined annual return rate
 *         Note: if no past fixing, set the fixing array to be [0].
 * @since 2016-10-18
 */
@BctQuantSerializable
public class RangeAccrual {
    @JsonProperty("class")
    private final String instrument = RangeAccrual.class.getSimpleName();

    private double payoff;
    private LocalDateTime expiry;
    private LocalDateTime delivery;
    private LocalDateTime[] schedule;
    private double[] fixings;
    private double min;
    private double max;
    private String cumulative;

    @JsonCreator
    public RangeAccrual(
            @JsonProperty("payoff") double payoff,
            @JsonProperty("expiry") LocalDateTime expiry,
            @JsonProperty("delivery") LocalDateTime delivery,
            @JsonProperty("schedule") LocalDateTime[] schedule,
            @JsonProperty("fixings") double[] fixings,
            @JsonProperty("min") double min,
            @JsonProperty("max") double max,
            @JsonProperty("cumulative") String cumulative) {
        this.cumulative = cumulative;
        this.payoff = payoff;
        this.expiry = expiry;
        this.delivery = delivery;
        this.schedule = schedule;
        this.fixings = fixings;
        this.min = min;
        this.max = max;
    }

    public double getPayoff() {
        return payoff;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public LocalDateTime getDelivery() {
        return delivery;
    }

    public LocalDateTime[] getSchedule() {
        return schedule;
    }

    public double[] getFixings() {
        return fixings;
    }

    public double getRangeMin() {
        return min;
    }

    public double getRangeMax() {
        return max;
    }

    public String getCumulative() {
        return cumulative;
    }
}
