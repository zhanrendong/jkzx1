package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;

import java.time.LocalDateTime;

/**
 * Digital option (cash settled)
 */
@BctQuantSerializable
public class DigitalCash {
    @JsonProperty("class")
    private final String instrument = DigitalCash.class.getSimpleName();

    private OptionType type;
    private LocalDateTime expiry;
    private LocalDateTime delivery;
    private double strike;
    private double payment;

    /**
     * Creates a cash settled digital option (European exercise)
     * @param type CALL or PUT
     * @param strike Option strike
     * @param payment Payment
     * @param expiry Expiry
     * @param delivery Delivery date
     */
    @JsonCreator
    public DigitalCash(
            @JsonProperty("type") OptionType type,
            @JsonProperty("strike") double strike,
            @JsonProperty("payment") double payment,
            @JsonProperty("expiry") LocalDateTime expiry,
            @JsonProperty("delivery") LocalDateTime delivery) {
        this.type = type;
        this.expiry = expiry;
        this.delivery = delivery;
        this.strike = strike;
        this.payment = payment;
    }

    public OptionType getType() {
        return type;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public LocalDateTime getDelivery() {
        return delivery;
    }

    public double getStrike() {
        return strike;
    }

    public double getPayment() {
        return payment;
    }

    public String getInstrument() {
        return instrument;
    }
}