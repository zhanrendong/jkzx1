package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.financial.instruments.Portfolio;
import tech.tongyu.bct.service.quantlib.financial.instruments.Position;

import java.time.LocalDateTime;


/**
 * Single barrier knock-out option with monitoring on expiry only. Essentially the option is path independent.
 * The option is equivalent to a portfolio of European options and can be priced accordingly.
 * The underlying option is knocked in if the spot hits the barrier upon expiry.
 * A rebate can be paid if the underlying option is not knocked out.
 */
@BctQuantSerializable
public class KnockOutTerminal {
    @JsonProperty("class")
    private final String instrument = KnockOutTerminal.class.getSimpleName();

    private OptionType type; // Underlying option type (CALL/PUT)
    private double strike;
    private double barrier;
    private BarrierDirection barrierDirection;
    private double rebateAmount;
    private LocalDateTime expiry;
    private LocalDateTime delivery;

    @JsonCreator
    public KnockOutTerminal(
            @JsonProperty("type") OptionType type,
            @JsonProperty("strike") double strike,
            @JsonProperty("expiry") LocalDateTime expiry,
            @JsonProperty("delivery") LocalDateTime delivery,
            @JsonProperty("barrier") double barrier,
            @JsonProperty("barrierDirection") BarrierDirection barrierDirection,
            @JsonProperty("rebateAmount") double rebateAmount) {
        this.type = type;
        this.strike = strike;
        this.barrier = barrier;
        this.barrierDirection = barrierDirection;
        this.rebateAmount = rebateAmount;
        this.expiry = expiry;
        this.delivery = delivery;
    }

    /**
     * Split the option into a combination of European options
     * <p>
     * Since there is only one observation date and that date is the option's expiry, a terminal knock-out option
     * is simply a combination of European options. This function splits the option into a portfolio of calls/puts
     * and digital calls/puts.
     * <p>
     * <p>
     * The decompositions are listed below: <br>
     * Up-and-out Call, B &gt; K: Call(K) - Call(B) + (B-K) * DigiCall(B) <br>
     * Down-and-out Call, B &gt; K: Call(B) + (B-K) * DigiCall(B) <br>
     * Up-and-out Put, B &lt; K: Put(B) + (K-B) * DigiPut(B) <br>
     * Down-and-out Put, B &lt; K: Put(K) - Put(B) - (K-B) * DigiPut(B) <br>
     * Note that other knockout types either not need any decomposition or simply have 0 value.
     * If there is any rebate, it is added as a cash settled digital option with strike the same as the barrier.
     *
     * @return portfolio of equivalent options
     */
    public Portfolio split() {
        Portfolio combinations = new Portfolio();
        if (type == OptionType.CALL && barrierDirection == BarrierDirection.UP_AND_OUT && barrier > strike) {
            VanillaEuropean call1 = new VanillaEuropean(type, strike, expiry, delivery);
            VanillaEuropean call2 = new VanillaEuropean(type, barrier, expiry, delivery);
            DigitalCash digi = new DigitalCash(type, barrier, barrier - strike, expiry, delivery);
            combinations.add(new Position<>(call1, 1.0));
            combinations.add(new Position<>(call2, -1.0));
            combinations.add(new Position<>(digi, -1.0));
            if (rebateAmount != 0.0) {
                DigitalCash digi2 = new DigitalCash(type, barrier, rebateAmount, expiry, delivery);
                combinations.add(new Position<>(digi2, 1.0));
            }
        } else if (type == OptionType.CALL && barrierDirection == BarrierDirection.UP_AND_OUT && barrier <= strike) {
            if (rebateAmount != 0.0) {
                DigitalCash digi = new DigitalCash(type, barrier, rebateAmount, expiry, delivery);
                combinations.add(new Position<>(digi, 1.0));
            }
        } else if (type == OptionType.CALL && barrierDirection == BarrierDirection.DOWN_AND_OUT && barrier > strike) {
            VanillaEuropean call1 = new VanillaEuropean(type, barrier, expiry, delivery);
            DigitalCash digi = new DigitalCash(type, barrier, barrier - strike, expiry, delivery);
            combinations.add(new Position<>(call1, 1.0));
            combinations.add(new Position<>(digi, 1.0));
            if (rebateAmount != 0.0) {
                DigitalCash digi2 = new DigitalCash(OptionType.PUT, barrier, rebateAmount, expiry, delivery);
                combinations.add(new Position<>(digi2, 1.0));
            }
        } else if (type == OptionType.CALL && barrierDirection == BarrierDirection.DOWN_AND_OUT && barrier <= strike) {
            VanillaEuropean optn = new VanillaEuropean(type, strike, expiry, delivery);
            combinations.add(new Position<>(optn, 1.0));
            if (rebateAmount != 0.0) {
                DigitalCash digi2 = new DigitalCash(OptionType.PUT, barrier, rebateAmount, expiry, delivery);
                combinations.add(new Position<>(digi2, 1.0));
            }
        } else if (type == OptionType.PUT && barrierDirection == BarrierDirection.UP_AND_OUT && barrier < strike) {
            VanillaEuropean put = new VanillaEuropean(type, barrier, expiry, delivery);
            DigitalCash digi = new DigitalCash(type, barrier, strike - barrier, expiry, delivery);
            combinations.add(new Position<>(put, 1.0));
            combinations.add(new Position<>(digi, 1.0));
            if (rebateAmount != 0.0) {
                DigitalCash digi2 = new DigitalCash(OptionType.CALL, barrier, rebateAmount, expiry, delivery);
                combinations.add(new Position<>(digi2, 1.0));
            }
        } else if (type == OptionType.PUT && barrierDirection == BarrierDirection.UP_AND_OUT && barrier >= strike) {
            VanillaEuropean optn = new VanillaEuropean(type, strike, expiry, delivery);
            combinations.add(new Position<>(optn, 1.0));
            if (rebateAmount != 0.0) {
                DigitalCash digi2 = new DigitalCash(OptionType.CALL, barrier, rebateAmount, expiry, delivery);
                combinations.add(new Position<>(digi2, 1.0));
            }
        } else if (type == OptionType.PUT && barrierDirection == BarrierDirection.DOWN_AND_OUT && barrier < strike) {
            VanillaEuropean put1 = new VanillaEuropean(type, strike, expiry, delivery);
            VanillaEuropean put2 = new VanillaEuropean(type, barrier, expiry, delivery);
            DigitalCash digi = new DigitalCash(type, barrier, strike - barrier, expiry, delivery);
            combinations.add(new Position<>(put1, 1.0));
            combinations.add(new Position<>(put2, -1.0));
            combinations.add(new Position<>(digi, -1.0));
            if (rebateAmount != 0.0) {
                DigitalCash digi2 = new DigitalCash(OptionType.PUT, barrier, rebateAmount, expiry, delivery);
                combinations.add(new Position<>(digi2, 1.0));
            }
        } else {
            // down-and-out put with barrier >= strike
            if (rebateAmount != 0.0) {
                DigitalCash digi2 = new DigitalCash(OptionType.PUT, barrier, rebateAmount, expiry, delivery);
                combinations.add(new Position<>(digi2, 1.0));
            }
        }
        return combinations;
    }

    public OptionType getType() {
        return type;
    }

    public BarrierDirection getBarrierDirection() {
        return barrierDirection;
    }

    public double getStrike() {
        return strike;
    }

    public double getBarrier() {
        return barrier;
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
}