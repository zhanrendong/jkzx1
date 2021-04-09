package tech.tongyu.bct.service.quantlib.common.numerics.mc.instrument;

import tech.tongyu.bct.service.quantlib.common.numerics.mc.McInstrument;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McPathSingleAsset;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class McInstrumentForward implements McInstrument {
    private LocalDateTime deliveryDate;
    private double strike;

    public McInstrumentForward(LocalDateTime deliveryDate, double strike) {
        this.deliveryDate = deliveryDate;
        this.strike = strike;
    }

    @Override
    public LocalDateTime[] getSimDates(LocalDateTime val, double stepSize, boolean includeGridDates) {
        if (includeGridDates)
            return McInstrument.genUniformSimDates(val, deliveryDate, stepSize).stream().toArray(LocalDateTime[]::new);
        return new LocalDateTime[] {val, deliveryDate};
    }

    @Override
    public List<CashPayment> exercise(McPathSingleAsset path, PricerParams params) {
        ArrayList<CashPayment> cashflows = new ArrayList<>();
        cashflows.add(new CashPayment(deliveryDate, path.getSpot(deliveryDate) - strike));
        return cashflows;
    }

    @Override
    public LocalDateTime[] getImportantDates() {
        return new LocalDateTime[] {deliveryDate};
    }
}
