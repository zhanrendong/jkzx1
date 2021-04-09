package tech.tongyu.bct.quant.library.numerics.mc;

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
        long dt = (long) (stepSize * 365 * 24 *  60 * 60);
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

    /**
     * Get simulation dates. The general strategy is to use all important dates after valuation date
     * as simulation dates.
     * For certain vol structure, for example, lognormal, it is not necessary to add extra simulation dates
     * between important dates. The user can force extra simulation dates by including grid dates by
     * setting the flat includeGridDates to true.
     * @param val Valuation date
     * @param stepSize Simulation grid size
     * @param includeGridDates Whether to include grid dates into simulation
     * @return A list of simulation dates on which the mc engine will generate simulated paths.
     */
    LocalDateTime[] getSimDates(LocalDateTime val, double stepSize, boolean includeGridDates);

    List<CashPayment> exercise(McPathSingleAsset path, PricerParams params);

    /**
     * An instrument has specific payment dates. Only on these dates discounting factors are required.
     * These dates may differ from simulation dates. In fact there can be far fewer payment dates than
     * simulaiton dates, e.g. a european option has a single payment date, but can require thousands of
     * simulation dates to price.
     * @param val Valuation date. Past payment dates should be not be included, but valuation date must be included
     * @return A list of payment dates (when discounting is needed)
     */
    LocalDateTime[] getDfDates(LocalDateTime val);
    /**
     * Get the instrument's must-have dates. For example, european option's expiry etc.
     * These dates must be on simulated paths so that the instrument can be priced.
     * This is useful when there are multiple instruments to be priced by a single MC engine.
     * Then we need to combine all instruments' important dates and merge with the uniform sim dates.
     * @return An array of instrument's import dates
     */
    LocalDateTime[] getImportantDates();
}
