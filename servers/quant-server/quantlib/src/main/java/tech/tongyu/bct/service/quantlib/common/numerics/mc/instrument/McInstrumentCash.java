package tech.tongyu.bct.service.quantlib.common.numerics.mc.instrument;

import tech.tongyu.bct.service.quantlib.common.numerics.mc.McInstrument;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McPathSingleAsset;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class McInstrumentCash implements McInstrument {
    private final LocalDateTime paymentDate;
    private final double amount;

    public McInstrumentCash(LocalDateTime paymentDate, double amount) {
        this.paymentDate = paymentDate;
        this.amount = amount;
    }

    @Override
    public LocalDateTime[] getSimDates(LocalDateTime val, double stepSize, boolean includeGridDates) {
        if (includeGridDates)
            return McInstrument.genUniformSimDates(val, paymentDate, stepSize).stream().toArray(LocalDateTime[]::new);
        return new LocalDateTime[] {val, paymentDate};
    }

    @Override
    public List<CashPayment> exercise(McPathSingleAsset path, PricerParams params) {
        ArrayList<CashPayment> cashflows = new ArrayList<>();
        cashflows.add(new CashPayment(paymentDate, amount));
        return cashflows;
    }

    @Override
    public LocalDateTime[] getImportantDates() {
        return new LocalDateTime[] {paymentDate};
    }
}
