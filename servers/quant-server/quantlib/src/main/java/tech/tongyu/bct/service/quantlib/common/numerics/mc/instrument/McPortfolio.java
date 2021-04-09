package tech.tongyu.bct.service.quantlib.common.numerics.mc.instrument;

import tech.tongyu.bct.service.quantlib.common.numerics.mc.McInstrument;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McPathSingleAsset;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class McPortfolio implements McInstrument {
    private final McInstrument[] instruments;
    private final double[] weights;

    public McPortfolio(McInstrument[] instruments, double[] weights) {
        this.instruments = instruments;
        this.weights = weights;
    }

    @Override
    public List<CashPayment> exercise(McPathSingleAsset path, PricerParams params) {
        List<CashPayment> all = new ArrayList<>();
        for (int i = 0; i < instruments.length; ++i) {
            List<CashPayment> payments = instruments[i].exercise(path, params);
            final int j = i;
            List<CashPayment> weighted = payments.stream()
                    .map(p -> new CashPayment(p.paymentDate, weights[j] * p.amount))
                    .collect(Collectors.toList());
            all.addAll(weighted);
        }
        return all;
    }

    @Override
    public LocalDateTime[] getSimDates(LocalDateTime val, double stepSize, boolean includeGridDates) {
        SortedSet<LocalDateTime> all = new TreeSet<>(Arrays.asList(getImportantDates()));
        List<LocalDateTime> uniform = McInstrument.genUniformSimDates(val, all.last(), stepSize);
        all.addAll(uniform);
        return all.toArray(new LocalDateTime[all.size()]);
    }

    @Override
    public LocalDateTime[] getImportantDates() {
        SortedSet<LocalDateTime> all = new TreeSet<>();
        Arrays.stream(instruments)
                .forEach(i -> all.addAll(Arrays.asList(i.getImportantDates())));
        return all.toArray(new LocalDateTime[all.size()]);
    }
}
