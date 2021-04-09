package tech.tongyu.bct.service.quantlib.common.numerics.mc.instrument;

import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McInstrument;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McPathSingleAsset;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.NoTouchDiscrete;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

public class McInstrumentNoTouchDiscrete implements McInstrument {
    private final NoTouchDiscrete noTouchDiscrete;

    public McInstrumentNoTouchDiscrete(NoTouchDiscrete noTouchDiscrete) {
        this.noTouchDiscrete = noTouchDiscrete;
    }

    @Override
    public LocalDateTime[] getSimDates(LocalDateTime val, double stepSize, boolean includeGridDates) {
        TreeSet<LocalDateTime> dates = new TreeSet<>();
        dates.add(val);
        dates.addAll(Arrays.asList(noTouchDiscrete.getObservationDates()));
        dates.addAll(Arrays.asList(noTouchDiscrete.getRebatePaymentDates()));
        dates.add(noTouchDiscrete.getPaymentDate());
        LocalDateTime lastObs = noTouchDiscrete.getObservationDates()[noTouchDiscrete.getObservationDates().length - 1];
        if (includeGridDates)
            dates.addAll(McInstrument.genUniformSimDates(val, lastObs, stepSize));
        return dates.toArray(new LocalDateTime[dates.size()]);
    }

    @Override
    public List<CashPayment> exercise(McPathSingleAsset path, PricerParams params) {
        ArrayList<CashPayment> cashflows = new ArrayList<>();
        BarrierDirection direction = noTouchDiscrete.getBarrierDirection();
        LocalDateTime[] observationDates = noTouchDiscrete.getObservationDates();
        double[] barriers = noTouchDiscrete.getBarriers();
        double[] rebates = noTouchDiscrete.getRebates();
        LocalDateTime[] rebatePaymentDates = noTouchDiscrete.getRebatePaymentDates();
        for (int i = 0; i < observationDates.length; ++i) {
            LocalDateTime t = observationDates[i];
            double spot = path.getSpot(t);
            // knock out
            if ( (direction == BarrierDirection.DOWN_AND_OUT && spot <= barriers[i]) ||
                    (direction == BarrierDirection.UP_AND_OUT && spot >= barriers[i])) {
                cashflows.add(new CashPayment(rebatePaymentDates[i], rebates[i]));
                return cashflows;
            }
        }
        // no touch
        cashflows.add(new CashPayment(noTouchDiscrete.getPaymentDate(), noTouchDiscrete.getPayment()));
        return cashflows;
    }

    @Override
    public LocalDateTime[] getImportantDates() {
        return noTouchDiscrete.getObservationDates();
    }
}
