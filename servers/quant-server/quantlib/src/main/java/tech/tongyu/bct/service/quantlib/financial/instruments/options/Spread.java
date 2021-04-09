package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * @since 2016-10-19
 * Option that exchange an asset for another
 * the strike price can be positive or negative
 */
public class Spread {
    @JsonProperty("class")
    private final String instrument = Spread.class.getSimpleName();
    private double strike;
    private LocalDateTime expiry;
    private LocalDateTime delivery;


    @JsonCreator
    public Spread(
            @JsonProperty("strike") double strike,
            @JsonProperty("expiry") LocalDateTime expiry,
            @JsonProperty("delivery") LocalDateTime delivery) {
        this.strike = strike;
        this.expiry = expiry;
        this.delivery = delivery;
    }

    public double getStrike() {
        return strike;
    }


    public LocalDateTime getExpiry() {
        return expiry;
    }

    public LocalDateTime getDelivery() {
        return delivery;
    }


}
