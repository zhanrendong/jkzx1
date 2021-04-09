package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * Auto call. The barriers, observationDates, inPayments, outPayments
 * and deliveries must be consistent. Expiry is assumed to be the last
 * observationDates.
 */
@BctQuantSerializable
public class AutoCall {
    @JsonProperty("class")
    private final String instrument = AutoCall.class.getSimpleName();

    private final double[] deterministicPayments;
    private final LocalDateTime[] deterministicDeliveries;
    private final BarrierDirection barrierDirection;
    private final double[] barriers;
    private final LocalDateTime[] observationDates;
    private final double[] inPayments;
    private final double[] outPayments;
    private final LocalDateTime[] Deliveries;

    @JsonCreator
    public AutoCall(
            @JsonProperty("barrierDirection") BarrierDirection barrierDirection,
            @JsonProperty("barriers") double[] barriers,
            @JsonProperty("observationDates") LocalDateTime[] observationDates,
            @JsonProperty("inPayments") double[] inPayments,
            @JsonProperty("outPayments") double[] outPayments,
            @JsonProperty("delivery") LocalDateTime[] deliveries,
            @JsonProperty("deterministicPayments") double[] deterministicPayments,
            @JsonProperty("deterministicDeliveries") LocalDateTime[] deterministicDeliveries) {
        this.deterministicPayments = deterministicPayments;
        this.deterministicDeliveries = deterministicDeliveries;
        this.barrierDirection = barrierDirection;
        this.barriers = barriers;
        this.observationDates = observationDates;
        this.inPayments = inPayments;
        this.outPayments = outPayments;
        Deliveries = deliveries;
    }

    public String getInstrument() {
        return instrument;
    }

    public double[] getDeterministicPayments() {
        return deterministicPayments;
    }

    public LocalDateTime[] getDeterministicDeliveries() {
        return deterministicDeliveries;
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

    public double[] getInPayments() {
        return inPayments;
    }

    public double[] getOutPayments() {
        return outPayments;
    }

    public LocalDateTime[] getDeliveries() {
        return Deliveries;
    }

    public LocalDateTime getExpiry() {
        TreeSet<LocalDateTime> dates = new TreeSet<>();
        dates.addAll(Arrays.asList(observationDates));
        return dates.last();
    }
}
