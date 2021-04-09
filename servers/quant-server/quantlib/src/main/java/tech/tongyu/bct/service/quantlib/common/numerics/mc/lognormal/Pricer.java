package tech.tongyu.bct.service.quantlib.common.numerics.mc.lognormal;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McInstrument;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.Path;
import tech.tongyu.bct.service.quantlib.market.curve.Discount;
import tech.tongyu.bct.service.quantlib.market.vol.AtmPwc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pricer {
    public static class McParams {
        public final long seed;
        public final int numPaths;
        public final double stepSize;
        // some instruments, for example, european options, are not path dependent
        // and can be priced by simulating only the spots on expiry dates, which is easy to do in black model
        // for testing purposes we allow the simulation to include all grid dates generated from the step size param
        public final boolean includeGridDates;
        // pricer params
        public final boolean brownianBridgeAdj;

        public McParams(long seed, int numPaths, double stepSize, boolean includeGridDates,
                        boolean brownianBridgeAdj) {
            this.seed = seed;
            this.numPaths = numPaths;
            this.stepSize = stepSize;
            this.includeGridDates = includeGridDates;
            // pricer params
            this.brownianBridgeAdj = brownianBridgeAdj;
        }
    }

    public static Map<String, Double> pv(
            McInstrument optn,
            LocalDateTime val,
            double spot,
            Discount discountYc,
            Discount dividendYc,
            AtmPwc vs,
            McParams params) {
        EngineLognormal engine = new EngineLognormal(spot, discountYc, dividendYc, vs);
        LocalDateTime[] simDates = optn.getSimDates(val, params.stepSize, params.includeGridDates);
        engine.genPaths(simDates, params.numPaths, params.seed);
        // generate pricer params
        McInstrument.PricerParams pricerParams = new McInstrument.PricerParams(params.brownianBridgeAdj);
        double npv = 0.0, npv2 = 0.0; // mean and mean of square
        for(int i = 0; i < params.numPaths; ++i) {
            Path path = engine.getPath(i);
            List<McInstrument.CashPayment> cashflows = optn.exercise(path, pricerParams);
            double pathPv = 0.0, pathPv2 = 0.0;
            for (McInstrument.CashPayment p : cashflows) {
                pathPv += path.df(p.paymentDate) * p.amount;
            }
            pathPv2 = pathPv * pathPv;
            npv = (npv * i + pathPv) / (i + 1);
            npv2 = (npv2 * i + pathPv2) / (i + 1);
        }
        HashMap<String, Double> ret = new HashMap<>();
        ret.put("price", npv);
        ret.put("stddev", FastMath.sqrt((npv2 - npv * npv) / (params.numPaths - 1)));
        return ret;
    }
}
