package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.ExerciseType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;

import java.time.LocalDateTime;

/**
 * Double-barrier knock-in option with continuous monitoring. The barriers can start after the option trade date and
 * end before option expiry, i.e. the option can be a window barrier. The underlying option currently can only be
 * European CALL or PUT. The underlying option is triggered if the spot hits either of the barriers when they are active.
 * A rebate can be paid if the barrier is never breached. The rebate is paid upon expiry.
 */
@BctQuantSerializable
public class DoubleKnockInContinuous {
    @JsonProperty("class")
    private final String instrument = DoubleKnockInContinuous.class.getSimpleName();

    private OptionType type; // Underlying option type (CALL/PUT)
    private double strike;
    private double upperBarrier;
    private double lowerBarrier;
    private LocalDateTime barrierStart;
    private LocalDateTime barrierEnd;
    private double rebateAmount;
    private LocalDateTime expiry;
    private LocalDateTime delivery;
    private ExerciseType exercise = ExerciseType.EUROPEAN;

    @JsonCreator
    public DoubleKnockInContinuous(
            @JsonProperty("type") OptionType type,
            @JsonProperty("strike") double strike,
            @JsonProperty("upperBarrier") double upperBarrier,
            @JsonProperty("lowerBarrier") double lowerBarrier,
            @JsonProperty("barrierStart") LocalDateTime barrierStart,
            @JsonProperty("barrierEnd") LocalDateTime barrierEnd,
            @JsonProperty("rebateAmount") double rebateAmount,
            @JsonProperty("expiry") LocalDateTime expiry,
            @JsonProperty("delivery") LocalDateTime delivery,
            @JsonProperty("exercise") ExerciseType exercise) {
        this.type = type;
        this.strike = strike;
        this.upperBarrier = upperBarrier;
        this.lowerBarrier = lowerBarrier;
        this.barrierStart = barrierStart;
        this.barrierEnd = barrierEnd;
        this.rebateAmount = rebateAmount;
        this.expiry = expiry;
        this.delivery = delivery;
        this.exercise = exercise;
    }

    public OptionType getType() {
        return type;
    }

    public double getStrike() {
        return strike;
    }

    public double getUpperBarrier() {
        return upperBarrier;
    }

    public double getLowerBarrier() {
        return lowerBarrier;
    }

    public LocalDateTime getBarrierStart() {
        return barrierStart;
    }

    public LocalDateTime getBarrierEnd() {
        return barrierEnd;
    }

    public double getRebateAmount() {
        return rebateAmount;
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
}