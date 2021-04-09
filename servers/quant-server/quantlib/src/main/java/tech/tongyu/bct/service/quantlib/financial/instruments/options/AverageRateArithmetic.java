package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.api.DateService;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.financial.dateservice.BusDayAdj;
import tech.tongyu.bct.service.quantlib.financial.dateservice.Roll;

import java.time.LocalDateTime;

/**
 * Asian option: arithmetic average rate.
 */
@BctQuantSerializable
public class AverageRateArithmetic {
    @JsonProperty("class")
    private final String instrument = AverageRateArithmetic.class.getSimpleName();

    private double strike;
    private OptionType type;
    private LocalDateTime expiry;
    private LocalDateTime delivery;
    private LocalDateTime[] schedule;
    private double[] weights;
    private double[] fixings;

    @JsonCreator
    public AverageRateArithmetic(
            @JsonProperty("type") OptionType type,
            @JsonProperty("strike") double strike,
            @JsonProperty("expiry") LocalDateTime expiry,
            @JsonProperty("delivery") LocalDateTime delivery,
            @JsonProperty("schedule") LocalDateTime[] schedule,
            @JsonProperty("weights") double[] weights,
            @JsonProperty("fixings") double[] fixings) {
        this.strike = strike;
        this.type = type;
        this.expiry = expiry;
        this.delivery = delivery;
        this.schedule = schedule;
        this.weights = weights;
        this.fixings = fixings;
    }

    /**
     * @param type              CALL or PUT
     * @param strike            strike price
     * @param expiry            expiry date
     * @param delivery          delivery date
     * @param scheduleStart     the scheduled observation start date , must no earlier than the valuation date.
     * @param scheduleLength    the scheduled observation tenor, for example 6M, 1Y etc.
     * @param scheduleFrequency the scheduled observation roll frequency , for example 1M, 3M, 6M, 1Y etc.
     * @param roll              the schedule roll direction
     */
    public AverageRateArithmetic(
            OptionType type,
            double strike,
            LocalDateTime expiry,
            LocalDateTime delivery,
            LocalDateTime scheduleStart,
            String scheduleLength,
            String scheduleFrequency,
            Roll roll) {
        String[] calendars = new String[]{"none"};
        LocalDateTime[] schedule = DateService.genSchedule(scheduleStart, scheduleLength, scheduleFrequency, roll,
                BusDayAdj.NONE, calendars);
        int N = schedule.length;
        double[] weights = new double[N];
        for (int i = 0; i < N; i++) {
            weights[i] = 1.0;
        }
        this.strike = strike;
        this.type = type;
        this.expiry = expiry;
        this.delivery = delivery;
        this.schedule = schedule;
        this.weights = weights;
        this.fixings = new double[]{};
    }


    public double getStrike() {
        return strike;
    }

    public OptionType getType() {
        return type;
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

    public double[] getWeights() {
        return weights;
    }

    public double[] getFixings() {
        return fixings;
    }
}