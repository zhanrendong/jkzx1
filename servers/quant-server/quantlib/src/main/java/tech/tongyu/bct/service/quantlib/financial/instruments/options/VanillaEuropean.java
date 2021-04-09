package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;

import java.time.LocalDateTime;

/**
 * Vanilla option. There are two types: {@link OptionType#CALL} and {@link OptionType#PUT}.
 */
@BctQuantSerializable
public class VanillaEuropean {
    @JsonProperty("class")
    private final String instrument = VanillaEuropean.class.getSimpleName();

    private OptionType type;
    private LocalDateTime expiry;
    private LocalDateTime delivery;
    private double strike;

    @JsonCreator
    public VanillaEuropean(
            @JsonProperty("type") OptionType type,
            @JsonProperty("strike") double strike,
            @JsonProperty("expiry") LocalDateTime expiry,
            @JsonProperty("delivery") LocalDateTime delivery
    ) {
        this.strike = strike;
        this.type = type;
        this.expiry = expiry;
        this.delivery = delivery;
    }

    /**
     * Construct a European option (CALL or PUT)
     * @param strike Option strike
     * @param expiry Option expiry
     * @param delivery Option delivery
     * @return A European CALL
     */
    public static VanillaEuropean call(double strike, LocalDateTime expiry, LocalDateTime delivery) {
        return new VanillaEuropean(OptionType.CALL, strike, expiry, delivery);
    }

    /**
     * Construct a European option (CALL or PUT)
     * @param strike Option strike
     * @param expiry Option expiry
     * @param delivery Option delivery
     * @return A European PUT
     */
    public static VanillaEuropean put(double strike, LocalDateTime expiry, LocalDateTime delivery) {
        return new VanillaEuropean(OptionType.PUT, strike, expiry, delivery);
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public LocalDateTime getDelivery() {
        return delivery;
    }

    public OptionType getType() {
        return type;
    }

    public double getStrike() {
        return strike;
    }

    public String getInstrument() {
        return instrument;
    }
}