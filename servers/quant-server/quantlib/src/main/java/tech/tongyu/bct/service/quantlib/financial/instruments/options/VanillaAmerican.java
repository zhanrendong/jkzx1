package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;

import java.time.LocalDateTime;

/**
 * Vanilla American option. There are two types: {@link OptionType#CALL} and {@link OptionType#PUT}.
 * The option can be exercised at any time before its expiry.
 */
@BctQuantSerializable
public class VanillaAmerican {
    @JsonProperty("class")
    private final String instrument = VanillaAmerican.class.getSimpleName();

    private OptionType type;
    private LocalDateTime expiry;
    private double strike;

    @JsonCreator
    public VanillaAmerican(
            @JsonProperty("strike") double strike,
            @JsonProperty("type") OptionType type,
            @JsonProperty("expiry") LocalDateTime expiry) {
        this.strike = strike;
        this.type = type;
        this.expiry = expiry;
    }

    /**
     * Create an American CALL
     * @param strike Option strike
     * @param expiry Option exiry
     * @return An American CALL
     */
    public static VanillaAmerican call(double strike, LocalDateTime expiry) {
        return new VanillaAmerican(strike, OptionType.CALL, expiry);
    }

    /**
     * Create an American PUT
     * @param strike Option strike
     * @param expiry Option expiry
     * @return An American PUT
     */
    public static VanillaAmerican put(double strike, LocalDateTime expiry) {
        return new VanillaAmerican(strike, OptionType.PUT, expiry);
    }

    public OptionType getType() {
        return type;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public double getStrike() {
        return strike;
    }
}