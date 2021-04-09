package tech.tongyu.bct.service.quantlib.common.numerics.mc.instrument;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.enums.RebateType;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McInstrument;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McPathSingleAsset;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class McInstrumentKnockOut implements McInstrument {
    private final LocalDateTime expiry;
    private final DoubleUnaryOperator payoff;
    private final BarrierDirection direction;
    private final double rebate;
    private final RebateType rebateType;
    private final double barrier;

    public McInstrumentKnockOut(LocalDateTime expiry,
                                DoubleUnaryOperator payoff,
                                BarrierDirection direction,
                                double rebate,
                                RebateType rebateType,
                                double barrier) {
        this.expiry = expiry;
        this.payoff = payoff;
        this.direction = direction;
        this.rebate = rebate;
        this.rebateType = rebateType;
        this.barrier = barrier;
    }

    @Override
    public LocalDateTime[] getSimDates(LocalDateTime val, double stepSize, boolean includeGridDates) {
        return McInstrument.genUniformSimDates(val, expiry, stepSize).stream().toArray(LocalDateTime[]::new);
    }

    @Override
    public List<McInstrument.CashPayment> exercise(McPathSingleAsset path, PricerParams params) {
        ArrayList<McInstrument.CashPayment> cashflows = new ArrayList<>();
        LocalDateTime[] simDates = path.getSimDates();
        for (int i = 0; i < simDates.length; ++i) {
            LocalDateTime t = simDates[i];
            double spot = path.getSpot(t);
            // knocked out
            if (direction == BarrierDirection.UP_AND_OUT && spot >= barrier) {
                if (rebateType == RebateType.PAY_AT_EXPIRY)
                    cashflows.add(new McInstrument.CashPayment(expiry, rebate));
                else if (rebateType == RebateType.PAY_WHEN_HIT)
                    cashflows.add(new McInstrument.CashPayment(t, rebate));
                return cashflows;
            }
            if (direction == BarrierDirection.DOWN_AND_OUT && spot <= barrier) {
                if (rebateType == RebateType.PAY_AT_EXPIRY)
                    cashflows.add(new McInstrument.CashPayment(expiry, rebate));
                else if (rebateType == RebateType.PAY_WHEN_HIT)
                    cashflows.add(new McInstrument.CashPayment(t, rebate));
                return cashflows;
            }
        }
        // not knocked out
        //   adding brownian bridge adjustment
        //    account for the fact that there may be knockout between simulation dates
        if (params.brownianBridgeAdj) {
            // adj is really the probability that the underly has not knocked out up until current sim time
            double adj = 1.0;
            for (int i = 1; i < simDates.length; ++i) {
                LocalDateTime prev = simDates[i-1];
                LocalDateTime curr = simDates[i];
                // probability of ko between prev and curr
                double p = FastMath.exp(-2.0 * FastMath.log(path.getSpot(prev) / barrier)
                        * FastMath.log(path.getSpot(curr) / barrier)/path.getVariance(prev, curr));
                // handle the case when a rebate is paid when hit
                if (rebate != 0.0 && rebateType == RebateType.PAY_WHEN_HIT) {
                    cashflows.add(new CashPayment(curr, adj * p * rebate));
                }
                adj *= 1.0 - p;
            }
            // add the option part
            cashflows.add(new CashPayment(expiry, adj * payoff.applyAsDouble(path.getSpot(expiry))));
            // rebate
            //   pay at expiry
            if (rebate != 0.0 && rebateType == RebateType.PAY_AT_EXPIRY) {
                    cashflows.add(new CashPayment(expiry, (1-adj) * rebate));
            }
        } else {
            // without adjustment
            cashflows.add(new CashPayment(expiry, payoff.applyAsDouble(path.getSpot(expiry))));
        }

        return cashflows;
    }

    @Override
    public LocalDateTime[] getImportantDates() {
        return new LocalDateTime[] {expiry};
    }
}
