package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.ExerciseType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.enums.RebateType;

import java.time.LocalDateTime;

/**
 * Double-barrier knock-out option with continuous monitoring. The barriers can start after the option trade date and
 * end before option expiry, i.e. the option can have a window barrier. The underlying option currently can only be
 * European CALL or PUT. The underlying option is triggered if the spot hits either of the barriers when they are active.
 * A rebate can be paid if either barrier is breached. The rebate can be paid when hit or upon expiry.
 */
@BctQuantSerializable
public class DoubleKnockOutContinuous {
    @JsonProperty("class")
    private final String instrument = DoubleKnockOutContinuous.class.getSimpleName();

    private OptionType type; // Underlying option type (CALL/PUT)
    private double strike;
    private double lowerBarrier;
    private double lowerRebate;
    private double upperBarrier;
    private double upperRebate;
    private LocalDateTime barrierStart;
    private LocalDateTime barrierEnd;
    private LocalDateTime expiry;
    private LocalDateTime delivery;
    private ExerciseType exercise = ExerciseType.EUROPEAN;
    private RebateType rebateType;

    @JsonCreator
    public DoubleKnockOutContinuous(
            @JsonProperty("type") OptionType type,
            @JsonProperty("strike") double strike,
            @JsonProperty("lowerBarrier") double lowerBarrier,
            @JsonProperty("lowerRebate") double lowerRebate,
            @JsonProperty("upperBarrier") double upperBarrier,
            @JsonProperty("upperRebate") double upperRebate,
            @JsonProperty("barrierStart") LocalDateTime barrierStart,
            @JsonProperty("barrierEnd") LocalDateTime barrierEnd,
            @JsonProperty("expiry") LocalDateTime expiry,
            @JsonProperty("delivery") LocalDateTime delivery,
            @JsonProperty("exercise") ExerciseType exercise,
            @JsonProperty("rebateType") RebateType rebateType)
    {
        this.type = type;
        this.strike = strike;
        this.lowerBarrier = lowerBarrier;
        this.lowerRebate = lowerRebate;
        this.upperBarrier = upperBarrier;
        this.upperRebate = upperRebate;
        this.barrierStart = barrierStart;
        this.barrierEnd = barrierEnd;
        this.expiry = expiry;
        this.delivery = delivery;
        this.exercise = exercise;
        this.rebateType = rebateType;
    }

    public String getInstrument() {
        return instrument;
    }

    public OptionType getType() {
        return type;
    }

    public double getStrike() {
        return strike;
    }

    public double getLowerBarrier() {
        return lowerBarrier;
    }

    public double getLowerRebate() {
        return lowerRebate;
    }

    public double getUpperBarrier() {
        return upperBarrier;
    }

    public double getUpperRebate() {
        return upperRebate;
    }

    public LocalDateTime getBarrierStart() {
        return barrierStart;
    }

    public LocalDateTime getBarrierEnd() {
        return barrierEnd;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public LocalDateTime getDelivery() {
        return delivery;
    }

    public ExerciseType getExercise() {
        return exercise;
    }

    public RebateType getRebateType() {
        return rebateType;
    }
}