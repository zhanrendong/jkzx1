package tech.tongyu.bct.service.quantlib.common.numerics.mc.instrument;

import tech.tongyu.bct.service.quantlib.common.numerics.mc.McInstrument;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McPathSingleAsset;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.function.DoubleUnaryOperator;

public class McInstrumentAverageRateArithmatic implements McInstrument {
    private final LocalDateTime expiry;
    private final LocalDateTime[] schedule;
    private final double[] weights;
    private final double[] fixings;
    private final DoubleUnaryOperator payoff;

    public McInstrumentAverageRateArithmatic(
            LocalDateTime expiry,
            LocalDateTime[] schedule,
            double[] weights,
            double[] fixings,
            DoubleUnaryOperator payoff) {
        this.expiry = expiry;
        this.schedule = schedule;
        this.weights = weights;
        this.fixings = fixings;
        this.payoff = payoff;
    }

    @Override
    public LocalDateTime[] getSimDates(LocalDateTime val, double stepSize, boolean includeGridDates) {
        TreeSet<LocalDateTime> all = new TreeSet<>();
        all.add(val);
        for (LocalDateTime t : schedule) {
            if (t.isAfter(val))
                all.add(t);
        }
        all.add(expiry);
        if (includeGridDates)
            all.addAll(McInstrument.genUniformSimDates(val, expiry, stepSize));
        return all.toArray(new LocalDateTime[all.size()]);
    }

    @Override
    public LocalDateTime[] getImportantDates() {
        TreeSet<LocalDateTime> all = new TreeSet<>();
        all.addAll(Arrays.asList(schedule));
        all.add(expiry);
        return all.toArray(new LocalDateTime[all.size()]);
    }

    @Override
    public List<CashPayment> exercise(McPathSingleAsset path, PricerParams params) {
        LocalDateTime[] simDates = path.getSimDates();
        LocalDateTime val = simDates[0];
        double avg = 0.0;
        for (int i = 0; i < schedule.length; ++i) {
            if (schedule[i].isBefore(val)) {
                avg += weights[i] * fixings[i];
            } else {
                avg += weights[i] * path.getSpot(schedule[i]);
            }
        }
        avg /= schedule.length;
        ArrayList<CashPayment> cashflows = new ArrayList<>();
        cashflows.add(new CashPayment(expiry, payoff.applyAsDouble(avg)));
        return cashflows;
    }
}
