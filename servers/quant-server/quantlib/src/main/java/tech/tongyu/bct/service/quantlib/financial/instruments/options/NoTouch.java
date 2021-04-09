package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;

import java.time.LocalDateTime;

/**
 * No-touch option. The option pays a fixed amount (cash settled) only when the spot never hits the barrier.
 * The barrier can start after trade date and before the expiry. The payment is made upon expiry.
 */
@BctQuantSerializable
public class NoTouch {
    @JsonProperty("class")
    private final String instrument = NoTouch.class.getSimpleName();

    private double barrier;
    private BarrierDirection barrierDirection;
    private LocalDateTime barrierStart;
    private LocalDateTime barrierEnd;
    private double payment;
    private LocalDateTime expiry;
    private LocalDateTime delivery;

    /**
     * Creates a no-touch option
     * @param barrier Barrier level
     * @param barrierDirection Up-and-out or down-and-out
     * @param expiry Option expiry
     * @param delivery Option delivery
     * @param barrierStart Barrier start date
     * @param barrierEnd Barrier end date
     * @param payment Payment amount if the barrier is never hit
     */
    @JsonCreator
    public NoTouch(
            @JsonProperty("barrier") double barrier,
            @JsonProperty("barrierDirection") BarrierDirection barrierDirection,
            @JsonProperty("expiry") LocalDateTime expiry,
            @JsonProperty("delivery") LocalDateTime delivery,
            @JsonProperty("barrierStart") LocalDateTime barrierStart,
            @JsonProperty("barrierEnd") LocalDateTime barrierEnd,
            @JsonProperty("payment") double payment) {
        this.barrier = barrier;
        this.barrierDirection = barrierDirection;
        this.barrierStart = barrierStart;
        this.barrierEnd = barrierEnd;
        this.payment = payment;
        this.expiry = expiry;
        this.delivery = delivery;
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

    public double getPayment() {
        return payment;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public LocalDateTime getDelivery() { return delivery; }
}