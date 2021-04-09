package tech.tongyu.bct.quant.library.numerics.mc.impl.instrument;

import tech.tongyu.bct.quant.library.numerics.mc.McInstrument;
import tech.tongyu.bct.quant.library.numerics.mc.McPathSingleAsset;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class McInstrumentEuropean implements McInstrument {
    private LocalDateTime expiry;
    private LocalDateTime delivery;
    private final DoubleUnaryOperator payoff;

    public McInstrumentEuropean(LocalDateTime expiry, LocalDateTime delivery, DoubleUnaryOperator payoff) {
        this.expiry = expiry;
        this.delivery = delivery;
        this.payoff = payoff;
    }

    public McInstrumentEuropean(LocalDateTime expiry, DoubleUnaryOperator payoff) {
        this.expiry = expiry;
        this.delivery = expiry;
        this.payoff = payoff;
    }

    @Override
    public LocalDateTime[] getSimDates(LocalDateTime val, double stepSize, boolean includeGridDates) {
        if (includeGridDates)
            return McInstrument.genUniformSimDates(val, expiry, stepSize).toArray(new LocalDateTime[0]);
        return new LocalDateTime[] {val, expiry};
    }

    @Override
    public List<CashPayment> exercise(McPathSingleAsset path, PricerParams params) {
        ArrayList<CashPayment> cashflows = new ArrayList<>();
        cashflows.add(new CashPayment(delivery, payoff.applyAsDouble(path.getSpot(expiry))));
        return cashflows;
    }

    @Override
    public LocalDateTime[] getImportantDates() {
        return new LocalDateTime[] {expiry};
    }

    @Override
    public LocalDateTime[] getDfDates(LocalDateTime val) {
        if (delivery.isBefore(val))
            return new LocalDateTime[] {val};
        else
            return new LocalDateTime[] {val, delivery};
    }
}
