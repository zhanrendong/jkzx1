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
 * Single barrier knock-in option with continuous monitoring. The barrier can start after the option trade date and
 * end before option expiry, i.e. the option can be a window barrier. The underlying option currently can only be
 * European CALL or PUT. The underlying option is triggered if the spot hits the barrier when the barrier is active.
 * A rebate can be paid if the barrier is never breached. The rebate is paid upon expiry.
 */
@BctQuantSerializable
public class KnockInContinuous {
    @JsonProperty("class")
    private final String instrument = KnockInContinuous.class.getSimpleName();

    private OptionType type; // Underlying option type (CALL/PUT)
    private double strike;
    private double barrier;
    private BarrierDirection barrierDirection;
    private LocalDateTime barrierStart;
    private LocalDateTime barrierEnd;
    private double rebateAmount;
    private LocalDateTime expiry;
    private LocalDateTime delivery;
    private ExerciseType exercise = ExerciseType.EUROPEAN;

    /**
     * Creates a single (window) barrier knock-in option.
     *
     * @param type             Underlying option type: CALL or PUT
     * @param strike           Underlying option strike
     * @param barrier          Barrier level
     * @param barrierDirection Barrier direction (Up-and-in or down-and-in)
     * @param barrierStart     Barrier start time
     * @param barrierEnd       Barrier end time
     * @param rebateAmount     Rebate amount if the barrier is never breached
     * @param expiry           Option expiry
     * @param exercise         Option exercise style (American or European)
     */
    @JsonCreator
    public KnockInContinuous(
            @JsonProperty("type") OptionType type,
            @JsonProperty("strike") double strike,
            @JsonProperty("expiry") LocalDateTime expiry,
            @JsonProperty("expiry") LocalDateTime delivery,
            @JsonProperty("barrier") double barrier,
            @JsonProperty("barrierDirection") BarrierDirection barrierDirection,
            @JsonProperty("barrierStart") LocalDateTime barrierStart,
            @JsonProperty("barrierEnd") LocalDateTime barrierEnd,
            @JsonProperty("rebateAmount") double rebateAmount,
            @JsonProperty("exercise") ExerciseType exercise) {
        this.type = type;
        this.strike = strike;
        this.barrier = barrier;
        this.barrierDirection = barrierDirection;
        this.barrierStart = barrierStart;
        this.barrierEnd = barrierEnd;
        this.rebateAmount = rebateAmount;
        this.expiry = expiry;
        this.delivery = delivery;
        this.exercise = exercise;
    }

    private NoTouch rebateToNoTouch() {
        BarrierDirection direction;
        if (this.barrierDirection == BarrierDirection.DOWN_AND_IN) {
            direction = BarrierDirection.DOWN_AND_OUT;
        } else if (this.barrierDirection == BarrierDirection.UP_AND_IN){
            direction = BarrierDirection.UP_AND_OUT;
        } else
            throw new RuntimeException("Invalid barrier direction.");
        return new NoTouch(this.barrier, direction, this.expiry, this.delivery,
                this.barrierStart, this.barrierEnd, this.rebateAmount);
    }

    /**
     * Split the option into a knock-in without rebate and a no-touch that captures the rebate.
     * If the original option does not pay a rebate, the no-touch option is then returned as null.
     * This function is useful when the rebate is priced separately from the KI part.
     *
     * @return A pair consists of the knock-out without rebate and a one-touch
     */
    public List<Position<?>> split() {
        List<Position<?>> combinations = new ArrayList<>();
        KnockInContinuous ki = new KnockInContinuous(this.type, this.strike, this.expiry, this.delivery,
                this.barrier, this.barrierDirection, this.barrierStart, this.barrierEnd, 0.0, this.exercise);
        combinations.add(new Position<>(ki, 1.0));
        if (this.rebateAmount != 0.0) {
            NoTouch noTouch = rebateToNoTouch();
            combinations.add(new Position<>(noTouch, 1.0));
        }
        return combinations;
    }

    /**
     * Split the KI option into a European and a KO by the parity KI + KO = European.
     * This split requires that the option does not pay any rebate. Otherwise call this function
     * after calling {@link KnockInContinuous#split} to remove the rebate first.
     * <em>Note</em>: the returned options do not include long/short flags. That means that to price a KI
     * the user needs to be aware that KI = European - KO and keep track of the signs.
     *
     * @return A knock out option and a European option
     */
    public List<Position<?>> paritySplit() throws Exception {
        List<Position<?>> combinations = new ArrayList<>();
        BarrierDirection direction;
        if (this.barrierDirection == BarrierDirection.UP_AND_IN) {
            direction = BarrierDirection.UP_AND_OUT;
        } else if (this.barrierDirection == BarrierDirection.DOWN_AND_IN) {
            direction = BarrierDirection.DOWN_AND_OUT;
        } else
            throw new RuntimeException("Invalid barrier direction.");
        KnockOutContinuous ko = new KnockOutContinuous(this.type, this.strike, this.expiry, this.delivery,
                this.barrier, direction, this.barrierStart, this.barrierEnd,
                0.0, RebateType.PAY_NONE, ExerciseType.EUROPEAN);
        combinations.add(new Position<>(ko, -1.0));
        VanillaEuropean european = new VanillaEuropean(this.type, this.strike, this.expiry, this.delivery);
        combinations.add(new Position<>(european, 1.0));
        if (this.rebateAmount != 0.0) {
            NoTouch noTouch = rebateToNoTouch();
            combinations.add(new Position<>(noTouch, 1.0));
        }
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

    public BarrierDirection getBarrierDirection() {
        return barrierDirection;
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