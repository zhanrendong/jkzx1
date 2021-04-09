package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;

import java.time.LocalDateTime;

/**
 * No-touch with discrete observations
 */
@BctQuantSerializable
public class NoTouchDiscrete {
    @JsonProperty("class")
    private final String instrument = NoTouchDiscrete.class.getSimpleName();

    private final BarrierDirection barrierDirection;
    private final double[] barriers;
    private final LocalDateTime[] observationDates;

    private final double[] rebates;
    private final LocalDateTime[] rebatePaymentDates;

    private final double payment;
    private final LocalDateTime paymentDate;

    @JsonCreator
    public NoTouchDiscrete(
            @JsonProperty("barrierDirection") BarrierDirection barrierDirection,
            @JsonProperty("barriers") double[] barriers,
            @JsonProperty("observationDates") LocalDateTime[] observationDates,
            @JsonProperty("rebates") double[] rebates,
            @JsonProperty("rebatePaymentDates") LocalDateTime[] rebatePaymentDates,
            @JsonProperty("payment") double payment,
            @JsonProperty("paymentDate") LocalDateTime paymentDate) {
        this.barrierDirection = barrierDirection;
        this.barriers = barriers;
        this.observationDates = observationDates;
        this.rebates = rebates;
        this.rebatePaymentDates = rebatePaymentDates;
        this.payment = payment;
        this.paymentDate = paymentDate;
    }

    public String getInstrument() {
        return instrument;
    }

    public BarrierDirection getBarrierDirection() {
        return barrierDirection;
    }

    public double[] getBarriers() {
        return barriers;
    }

    public LocalDateTime[] getObservationDates() {
        return observationDates;
    }

    public double[] getRebates() {
        return rebates;
    }

    public LocalDateTime[] getRebatePaymentDates() {
        return rebatePaymentDates;
    }

    public double getPayment() {
        return payment;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }
}
