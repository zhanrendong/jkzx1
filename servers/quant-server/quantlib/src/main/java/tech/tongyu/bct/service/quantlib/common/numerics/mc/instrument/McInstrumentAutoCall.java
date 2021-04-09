package tech.tongyu.bct.service.quantlib.common.numerics.mc.instrument;

import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McInstrument;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McPathSingleAsset;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.AutoCall;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 *
 */
public class McInstrumentAutoCall implements McInstrument{
    private final AutoCall autoCall;

    public McInstrumentAutoCall(AutoCall autoCall) {
        this.autoCall = autoCall;
    }

    @Override
    public LocalDateTime[] getSimDates(LocalDateTime val, double stepSize,
                                       boolean includeGridDates) {
        TreeSet<LocalDateTime> dates = new TreeSet<>();
        dates.add(val);
        dates.addAll(Arrays.asList(autoCall.getObservationDates()));
        dates.addAll(Arrays.asList(autoCall.getDeliveries()));
        dates.addAll(Arrays.asList(autoCall.getDeterministicDeliveries()));
        if (includeGridDates)
            dates.addAll(McInstrument.genUniformSimDates(val, dates.last(),
                    stepSize));
        return dates.toArray(new LocalDateTime[dates.size()]);
    }

    /**
     * given spot and barrier, return if knocked out
     */
    private boolean knockedOut(double spot, double barrier) {
        boolean knockedOut;
        if (autoCall.getBarrierDirection() == BarrierDirection.DOWN_AND_OUT)
            knockedOut = spot <= barrier;
        else
            knockedOut = spot >= barrier;
        return knockedOut;
    }

    @Override
    public List<CashPayment> exercise(McPathSingleAsset path, PricerParams params) {
        ArrayList<CashPayment> cashflows = new ArrayList<>();
        // deterministic payment
        for (int i = 0; i < autoCall.getDeterministicPayments().length; i++)
            cashflows.add(new CashPayment(autoCall.getDeterministicDeliveries()[i],
                    autoCall.getDeterministicPayments()[i]));
        // barrier monitoring
        double[] barriers = autoCall.getBarriers();
        LocalDateTime[] obsDates = autoCall.getObservationDates();
        LocalDateTime[] delDates = autoCall.getDeliveries();
        double[] inPayments = autoCall.getInPayments();
        double[] outPayments = autoCall.getOutPayments();
        for (int i = 0; i < obsDates.length; i++) {
            double spot = path.getSpot(obsDates[i]);
            if (knockedOut(spot, barriers[i])) { // knock out
                cashflows.add(new CashPayment(delDates[i], outPayments[i]));
                return cashflows;
            } else // not knocked out
                cashflows.add(new CashPayment(delDates[i], inPayments[i]));
        }
        return cashflows;
    }

    @Override
    public LocalDateTime[] getImportantDates() {
        TreeSet<LocalDateTime> dates = new TreeSet<>();
        dates.addAll(Arrays.asList(autoCall.getObservationDates()));
        dates.addAll(Arrays.asList(autoCall.getDeliveries()));
        dates.addAll(Arrays.asList(autoCall.getDeterministicDeliveries()));
        return dates.toArray(new LocalDateTime[dates.size()]);
    }
}
