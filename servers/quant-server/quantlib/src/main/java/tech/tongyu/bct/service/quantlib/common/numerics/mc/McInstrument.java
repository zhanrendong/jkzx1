package tech.tongyu.bct.service.quantlib.common.numerics.mc;

import tech.tongyu.bct.service.quantlib.common.utils.Constants;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public interface McInstrument {
    class PricerParams {
        public final boolean brownianBridgeAdj;

        public PricerParams(boolean brownianBridgeAdj) {
            this.brownianBridgeAdj = brownianBridgeAdj;
        }
    }

    class CashPayment {
        public final LocalDateTime paymentDate;
        public final double amount;

        public CashPayment(LocalDateTime paymentDate, double amount) {
            this.paymentDate = paymentDate;
            this.amount = amount;
        }
    }

    static List<LocalDateTime> genUniformSimDates(LocalDateTime start, LocalDateTime end, double stepSize) {
        long dt = (long) (stepSize * Constants.DAYS_IN_YEAR * 24 *  60 * 60);
        if (dt == 0)
            dt = 1;
        List<LocalDateTime> ts = new ArrayList<>();
        LocalDateTime t = start;
        while(!t.isAfter(end)) {
            ts.add(t);
            t = t.plus(dt, ChronoUnit.SECONDS);
        }
        LocalDateTime last = ts.get(ts.size() - 1);
        if (last.isBefore(end))
            ts.add(end);
        return ts;
    }

    LocalDateTime[] getSimDates(LocalDateTime val, double stepSize, boolean includeGridDates);
    List<CashPayment> exercise(McPathSingleAsset path, PricerParams params);

    /**
     * Get the instrument's must-have dates. For example, european option's expiry etc.
     * These dates must be on simulated paths so that the instrument can be priced.
     * This is useful when there are multiple instruments to be priced by a single MC engine.
     * Then we need to combine all instruments' important dates and merge with the uniform sim dates.
     * @return An array of instrument's import dates
     */
     LocalDateTime[] getImportantDates();
}
