package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.enums.RebateType;

import java.time.LocalDateTime;

/**
 * One touch option pays out a fixed amount (cash settled) if the spot hits the barrier. The payment is made
 * either when the barrier is breached or at expiry. The barrier is allowed to start after
 * the trade date or before the expiry. Monitoring is continuous.
 */
@BctQuantSerializable
public class OneTouch {
    @JsonProperty("class")
    private final String instrument = OneTouch.class.getSimpleName();

    private double barrier;
    private BarrierDirection barrierDirection;
    private LocalDateTime barrierStart;
    private LocalDateTime barrierEnd;
    private double rebateAmount;
    private RebateType rebateType;
    private LocalDateTime expiry;
    private LocalDateTime delivery;

    /**
     * Creates a one-touch option with continuous monitoring
     * @param barrier Barrier level
     * @param barrierDirection Barrier direction (Up-and-in or Down-and-in)
     * @param barrierStart Barrier start date
     * @param barrierEnd Barrier end date
     * @param rebateAmount Payment amount when the barrier is hit
     * @param rebateType When to pay (when hit or at expiry)
     * @param expiry Option expiry
     */
    @JsonCreator
    public OneTouch(
            @JsonProperty("barrier") double barrier,
            @JsonProperty("barrierDirection") BarrierDirection barrierDirection,
            @JsonProperty("expiry") LocalDateTime expiry,
            @JsonProperty("delivery") LocalDateTime delivery,
            @JsonProperty("barrierStart") LocalDateTime barrierStart,
            @JsonProperty("barrierEnd") LocalDateTime barrierEnd,
            @JsonProperty("rebateAmount") double rebateAmount,
            @JsonProperty("rebateType") RebateType rebateType) {
        this.barrier = barrier;
        this.barrierDirection = barrierDirection;
        this.barrierStart = barrierStart;
        this.barrierEnd = barrierEnd;
        this.rebateAmount = rebateAmount;
        this.rebateType = rebateType;
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

    public double getRebateAmount() {
        return rebateAmount;
    }

    public RebateType getRebateType() {
        return rebateType;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public LocalDateTime getDelivery() { return delivery; }
}