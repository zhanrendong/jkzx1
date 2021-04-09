package tech.tongyu.bct.service.quantlib.common.numerics.mc.instrument;

import tech.tongyu.bct.service.quantlib.common.numerics.mc.McInstrument;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McPathSingleAsset;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class McInstrumentEuropean implements McInstrument {
    private LocalDateTime expiry;
    private final DoubleUnaryOperator payoff;

    public McInstrumentEuropean(LocalDateTime expiry, DoubleUnaryOperator payoff) {
        this.expiry = expiry;
        this.payoff = payoff;
    }

    @Override
    public LocalDateTime[] getSimDates(LocalDateTime val, double stepSize, boolean includeGridDates) {
        if (includeGridDates)
            return McInstrument.genUniformSimDates(val, expiry, stepSize).stream().toArray(LocalDateTime[]::new);
        return new LocalDateTime[] {val, expiry};
    }

    @Override
    public List<CashPayment> exercise(McPathSingleAsset path, PricerParams params) {
        ArrayList<CashPayment> cashflows = new ArrayList<>();
        cashflows.add(new CashPayment(expiry, payoff.applyAsDouble(path.getSpot(expiry))));
        return cashflows;
    }

    @Override
    public LocalDateTime[] getImportantDates() {
        return new LocalDateTime[] {expiry};
    }
}
