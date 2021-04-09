package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.enums.ExerciseType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.enums.RebateType;
import tech.tongyu.bct.service.quantlib.financial.instruments.Position;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * Single barrier knock-out option with continuous monitoring. The barrier can start after the option trade date and
 * end before option expiry, i.e. the option can be a window barrier. The underlying option currently can only be
 * European CALL or PUT. The underlying option is knocked out if the spot hits the barrier when the barrier is active.
 * A rebate can be paid if the underlying option is knocked out. The rebate can be paid either when the barrier is hit
 * or upon expiry.
 */
@BctQuantSerializable
public class KnockOutContinuous {
    @JsonProperty("class")
    private final String instrument = KnockOutContinuous.class.getSimpleName();

    private OptionType type; // Underlying option type (CALL/PUT)
    private double strike;
    private double barrier;
    private BarrierDirection barrierDirection;
    private LocalDateTime barrierStart;
    private LocalDateTime barrierEnd;
    private double rebateAmount;
    private RebateType rebateType;
    private LocalDateTime expiry;
    private LocalDateTime delivery;
    private ExerciseType exercise = ExerciseType.EUROPEAN;

    /**
     * Creates a single (window) barrier knock-out option.
     * @param type Underlying option type (CALL or PUT)
     * @param strike Underlying option strike
     * @param expiry Option expiry
     * @param barrier Barrier level
     * @param barrierDirection Up-and-out or down-and-out
     * @param barrierStart Barrier start date
     * @param barrierEnd Barrier end date
     * @param rebateAmount Rebate amount
     * @param rebateType Rebate type (whether paid when hit or at expiry)
     * @param exercise Exercise type (European or American)
     */
    @JsonCreator
    public KnockOutContinuous(
            @JsonProperty("type") OptionType type,
            @JsonProperty("strike") double strike,
            @JsonProperty("expiry") LocalDateTime expiry,
            @JsonProperty("delivery") LocalDateTime delivery,
            @JsonProperty("barrier") double barrier,
            @JsonProperty("barrierDirection") BarrierDirection barrierDirection,
            @JsonProperty("barrierStart") LocalDateTime barrierStart,
            @JsonProperty("barrierEnd") LocalDateTime barrierEnd,
            @JsonProperty("rebateAmount") double rebateAmount,
            @JsonProperty("rebateType") RebateType rebateType,
            @JsonProperty("exercise") ExerciseType exercise) {
        this.type = type;
        this.strike = strike;
        this.barrier = barrier;
        this.barrierDirection = barrierDirection;
        this.barrierStart = barrierStart;
        this.barrierEnd = barrierEnd;
        this.rebateAmount = rebateAmount;
        this.rebateType = rebateType;
        this.expiry = expiry;
        this.delivery = delivery;
        this.exercise = exercise;
    }

    /**
     * Split the option into a knock-out without rebate and a one-touch that captures the rebate.
     * If the original option does not pay a rebate, the one-touch option is then returned as null.
     * This function is useful when the rebate is priced separately from the KO part.
     * @return A pair consists of the knock-out without rebate and a one-touch
     */
    public List<Position<?>> split() {
        List<Position<?>> combinations = new ArrayList<>();

        KnockOutContinuous ko = new KnockOutContinuous(this.type, this.strike, this.expiry, this.delivery,
                this.barrier, this.barrierDirection, this.barrierStart, this.barrierEnd,
                0.0, RebateType.PAY_NONE, this.exercise);
        combinations.add(new Position<>(ko, 1.0));
        if (this.rebateType == RebateType.PAY_NONE) {
            return combinations;
        }
        BarrierDirection direction;
        if (this.barrierDirection == BarrierDirection.UP_AND_OUT) {
            direction = BarrierDirection.UP_AND_IN;
        } else if (this.barrierDirection == BarrierDirection.DOWN_AND_OUT) {
            direction = BarrierDirection.DOWN_AND_IN;
        } else
            throw new RuntimeException("Invalid barrier direction.");
        OneTouch oneTouch = new OneTouch(this.barrier, direction, this.expiry, this.delivery,
                this.barrierStart, this.barrierEnd, this.rebateAmount, this.rebateType);
        combinations.add(new Position<>(oneTouch, 1.0));
        return combinations;
    }

    public OptionType getType() {
        return type;
    }

    public double getStrike() {
        return strike;
    }

    public double getBarrier() {
        return barrier;
    }

    public BarrierDirection getBarrierDirection() { return barrierDirection; }

    public LocalDateTime getBarrierStart() {
        return barrierStart;
    }

    public LocalDateTime getBarrierEnd() {
        return barrierEnd;
    }

    public double getRebateAmount() {
        return rebateAmount;
    }

    public RebateType getRebateType() {
        return rebateType;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public LocalDateTime getDelivery() { return delivery; }

    public ExerciseType getExercise() {
        return exercise;
    }
}