package tech.tongyu.bct.service.quantlib.financial.instruments.linear;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/***
 * A forward contract exchanges a specific amount of cash for a unit of underlyer
 */
public class Forward {
    @JsonProperty("class")
    private final String instrument = Forward.class.getSimpleName();

    private final LocalDateTime deliveryDate;
    private final double strike;

    @JsonCreator
    public Forward(LocalDateTime deliveryDate, double strike) {
        this.deliveryDate = deliveryDate;
        this.strike = strike;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public double getStrike() {
        return strike;
    }
}
