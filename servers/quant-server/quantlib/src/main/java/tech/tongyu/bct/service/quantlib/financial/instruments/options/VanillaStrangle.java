package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.financial.instruments.Portfolio;
import tech.tongyu.bct.service.quantlib.financial.instruments.Position;

import java.time.LocalDateTime;

/**
 * Vanilla strangle. One put with lower strike and one call with higher.
 */
@BctQuantSerializable
public class VanillaStrangle {
    @JsonProperty("class")
    private final String instrument = VanillaStrangle.class.getSimpleName();

    private LocalDateTime expiry;
    private LocalDateTime delivery;
    private double strikeLow, strikeHigh;

    @JsonCreator
    public VanillaStrangle(
            @JsonProperty("strikeLow") double strikeLow,
            @JsonProperty("strikeHigh") double strikeHigh,
            @JsonProperty("expiry") LocalDateTime expiry,
            @JsonProperty("delivery") LocalDateTime delivery
    ) {
        this.strikeLow = strikeLow;
        this.strikeHigh = strikeHigh;
        this.expiry = expiry;
        this.delivery = delivery;
    }

    /**
     * Split a strangle into a put and a call
     * @return Portfolio of a put and a call
     */
    public Portfolio split() {
        Portfolio combinations = new Portfolio();
        // put
        VanillaEuropean put = new VanillaEuropean(OptionType.PUT, strikeLow, expiry, delivery);
        combinations.add(new Position<>(put, 1.0));
        // call
        VanillaEuropean call = new VanillaEuropean(OptionType.CALL, strikeHigh, expiry, delivery);
        combinations.add(new Position<>(call, 1.0));
        return combinations;
    }

    public String getInstrument() {
        return instrument;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public LocalDateTime getDelivery() {
        return delivery;
    }

    public double getStrikeLow() {
        return strikeLow;
    }

    public double getStrikeHigh() {
        return strikeHigh;
    }
}