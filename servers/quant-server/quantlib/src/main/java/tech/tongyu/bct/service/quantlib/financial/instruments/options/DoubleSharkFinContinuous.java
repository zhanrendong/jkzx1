package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.ExerciseType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.enums.RebateType;
import tech.tongyu.bct.service.quantlib.financial.instruments.Portfolio;
import tech.tongyu.bct.service.quantlib.financial.instruments.Position;

import java.time.LocalDateTime;

@BctQuantSerializable
public class DoubleSharkFinContinuous {
    @JsonProperty("class")
    private final String instrument = DoubleSharkFinContinuous.class.getSimpleName();

    private double lowerStrike;
    private double lowerBarrier;
    private double lowerRebate;
    private double upperStrike;
    private double upperBarrier;
    private double upperRebate;
    private LocalDateTime barrierStart;
    private LocalDateTime barrierEnd;
    private LocalDateTime expiry;
    private LocalDateTime delivery;

    @JsonCreator
    public DoubleSharkFinContinuous(
            @JsonProperty("lowerStrike") double lowerStrike,
            @JsonProperty("lowerBarrier") double lowerBarrier,
            @JsonProperty("lowerRebate") double lowerRebate,
            @JsonProperty("upperStrike") double upperStrike,
            @JsonProperty("upperBarrier") double upperBarrier,
            @JsonProperty("upperRebate") double upperRebate,
            @JsonProperty("barrierStart") LocalDateTime barrierStart,
            @JsonProperty("barrierEnd") LocalDateTime barrierEnd,
            @JsonProperty("expiry") LocalDateTime expiry,
            @JsonProperty("delivery") LocalDateTime delivery) {
        this.lowerStrike = lowerStrike;
        this.lowerBarrier = lowerBarrier;
        this.lowerRebate = lowerRebate;
        this.upperStrike = upperStrike;
        this.upperBarrier = upperBarrier;
        this.upperRebate = upperRebate;
        this.barrierStart = barrierStart;
        this.barrierEnd = barrierEnd;
        this.expiry = expiry;
        this.delivery = delivery;
    }

    /**
     * Split a double-shark-fin option into one double-knock-out call and one double-knock-out put for pricing
     * @return A portfolio of two double-knock-out options that's equivalent to the original double-shark-fin option
     */
    public Portfolio split() {
        Portfolio combination = new Portfolio();
        // the double-knock-out call
        DoubleKnockOutContinuous call = new DoubleKnockOutContinuous(
                OptionType.CALL, upperStrike,
                lowerBarrier, 0.0,
                upperBarrier, upperRebate,
                barrierStart, barrierEnd,
                expiry, delivery,
                ExerciseType.EUROPEAN,
                RebateType.PAY_AT_EXPIRY
        );
        combination.add(new Position<>(call, 1.0));
        DoubleKnockOutContinuous put = new DoubleKnockOutContinuous(
                OptionType.PUT, lowerStrike,
                lowerBarrier, lowerRebate,
                upperBarrier, 0.0,
                barrierStart, barrierEnd,
                expiry, delivery,
                ExerciseType.EUROPEAN,
                RebateType.PAY_AT_EXPIRY
        );
        combination.add(new Position<>(put, 1.0));
        return combination;
    }

    public String getInstrument() {
        return instrument;
    }

    public double getLowerStrike() {
        return lowerStrike;
    }

    public double getLowerBarrier() {
        return lowerBarrier;
    }

    public double getLowerRebate() {
        return lowerRebate;
    }

    public double getUpperStrike() {
        return upperStrike;
    }

    public double getUpperBarrier() {
        return upperBarrier;
    }

    public double getUpperRebate() {
        return upperRebate;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public LocalDateTime getDelivery() {
        return delivery;
    }

    public LocalDateTime getBarrierStart() {
        return barrierStart;
    }

    public LocalDateTime getBarrierEnd() {
        return barrierEnd;
    }
}