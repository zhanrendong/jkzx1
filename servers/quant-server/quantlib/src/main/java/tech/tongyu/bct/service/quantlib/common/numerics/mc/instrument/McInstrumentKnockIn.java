package tech.tongyu.bct.service.quantlib.common.numerics.mc.instrument;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McInstrument;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McPathSingleAsset;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class McInstrumentKnockIn implements McInstrument {
    private final LocalDateTime expiry;
    private final DoubleUnaryOperator payoff;
    private final BarrierDirection direction;
    private final double rebate;
    private final double barrier;

    public McInstrumentKnockIn(LocalDateTime expiry, DoubleUnaryOperator payoff, BarrierDirection direction, double rebate, double barrier) {
        this.expiry = expiry;
        this.payoff = payoff;
        this.direction = direction;
        this.rebate = rebate;
        this.barrier = barrier;
    }

    @Override
    public LocalDateTime[] getSimDates(LocalDateTime val, double stepSize, boolean includeGridDates) {
        return McInstrument.genUniformSimDates(val, expiry, stepSize).stream().toArray(LocalDateTime[]::new);
    }

    @Override
    public List<CashPayment> exercise(McPathSingleAsset path, PricerParams params) {
        ArrayList<CashPayment> cashflows = new ArrayList<>();
        LocalDateTime[] simDates = path.getSimDates();
        double finalSpot = path.getSpot(expiry);
        for (int i = 0; i < simDates.length; ++i) {
            LocalDateTime t = simDates[i];
            double spot = path.getSpot(t);
            // knocked in
            if ( (direction == BarrierDirection.UP_AND_IN && spot >= barrier) ||
                    (direction == BarrierDirection.DOWN_AND_IN && spot <= barrier)) {
                cashflows.add(new McInstrument.CashPayment(expiry, payoff.applyAsDouble(finalSpot)));
                return cashflows;
            }
        }
        // not knocked in
        //   adding brownian bridge adjustment
        //    account for the fact that there may be knockin between simulation dates
        if (params.brownianBridgeAdj) {
            // probability that the underly has not knocked in up until current sim time
            double pNotKnockedin = 1.0;
            for (int i = 1; i < simDates.length; ++i) {
                LocalDateTime prev = simDates[i-1];
                LocalDateTime curr = simDates[i];
                // probability of ki between prev and curr
                double p = FastMath.exp(-2.0 * FastMath.log(path.getSpot(prev) / barrier)
                        * FastMath.log(path.getSpot(curr) / barrier)/path.getVariance(prev, curr));
                cashflows.add(new CashPayment(expiry, pNotKnockedin * p * payoff.applyAsDouble(finalSpot)));
                pNotKnockedin *= 1.0 - p;
            }
            if (rebate != 0.0)
                cashflows.add(new CashPayment(expiry, pNotKnockedin * rebate));
        } else {
            cashflows.add(new CashPayment(expiry, rebate));
        }
        return cashflows;
    }

    @Override
    public LocalDateTime[] getImportantDates() {
        return new LocalDateTime[] {expiry};
    }
}
