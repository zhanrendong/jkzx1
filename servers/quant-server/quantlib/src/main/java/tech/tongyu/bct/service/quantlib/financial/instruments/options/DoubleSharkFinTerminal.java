package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.financial.instruments.Portfolio;
import tech.tongyu.bct.service.quantlib.financial.instruments.Position;

import java.time.LocalDateTime;

@BctQuantSerializable
public class DoubleSharkFinTerminal {
    @JsonProperty("class")
    private final String instrument = DoubleSharkFinTerminal.class.getSimpleName();

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
    public DoubleSharkFinTerminal(
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
     * Split a double-shark-fin-terminal option into several European options for pricing
     *
     * @return A portfolio of European options that's equivalent to the original double-shark-fin-terminal option
     */
    public Portfolio split() {
        Portfolio combination = new Portfolio();
        // vanilla European call with upperStrike as strike
        VanillaEuropean call1 = new VanillaEuropean(OptionType.CALL, upperStrike, expiry, delivery);
        combination.add(new Position<>(call1, 1.0));
        // vanilla European call with upperBarrier as strike
        VanillaEuropean call2 = new VanillaEuropean(OptionType.CALL, upperBarrier, expiry, delivery);
        combination.add(new Position<>(call2, -1.0));
        // digital cash call with  upperBarrier as the strike
        DigitalCash digitalCall = new DigitalCash(OptionType.CALL, upperBarrier, 1.0, expiry, delivery);
        combination.add(new Position<>(digitalCall, upperStrike - upperBarrier));
        // vanilla European put with lowerStrike as strike
        VanillaEuropean put1 = new VanillaEuropean(OptionType.PUT, lowerStrike, expiry, delivery);
        combination.add(new Position<>(put1, 1.0));
        // vanilla European put with lowerBarrier as strike
        VanillaEuropean put2 = new VanillaEuropean(OptionType.PUT, lowerBarrier, expiry, delivery);
        combination.add(new Position<>(put2, -1.0));
        //digital cash put with  the lowerBarrier as the strike
        DigitalCash digitalPut = new DigitalCash(OptionType.PUT, lowerBarrier, 1.0, expiry, delivery);
        combination.add(new Position<>(digitalPut, lowerBarrier - lowerStrike));
        //two more digital
        combination.add(new Position<>(digitalCall, upperRebate));
        combination.add(new Position<>(digitalPut, lowerRebate));
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