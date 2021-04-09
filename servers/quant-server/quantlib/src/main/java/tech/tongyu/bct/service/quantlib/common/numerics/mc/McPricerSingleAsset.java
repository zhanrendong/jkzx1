package tech.tongyu.bct.service.quantlib.common.numerics.mc;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.enums.PricerType;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.localvol.EngineLocalVol;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.lognormal.EngineLognormal;
import tech.tongyu.bct.service.quantlib.market.curve.Discount;
import tech.tongyu.bct.service.quantlib.market.vol.AtmPwc;
import tech.tongyu.bct.service.quantlib.market.vol.ImpliedVolSurface;
import tech.tongyu.bct.service.quantlib.market.vol.InterpolatedVolSurface;
import tech.tongyu.bct.service.quantlib.market.vol.LocalVolSurface;

import java.time.LocalDateTime;
import java.util.*;

public class McPricerSingleAsset {
    public static class Params {
        private final PricerType pricerType;
        private final long seed;
        private final int numPaths;
        private final double stepSize;
        // some instruments, for example, european options, are not path dependent
        // and can be priced by simulating only the spots on expiry dates, which is easy to do in black model
        // for testing purposes we allow the simulation to include all grid dates generated from the step size param
        // !! this flag is valid only if the pricer is BLACK_MC
        private final boolean includeGridDates;
        // local vol can jump on vol expiries due to interpolation
        private final boolean addVolSurfaceDates;
        // pricer params
        private final boolean brownianBridgeAdj;

        public Params(PricerType pricerType, long seed, int numPaths, double stepSize,
                      boolean includeGridDates, boolean addVolSurfaceDates, boolean brownianBridgeAdj) {
            this.pricerType = pricerType;
            this.seed = seed;
            this.numPaths = numPaths;
            this.stepSize = stepSize;
            this.includeGridDates = pricerType == PricerType.BLACK_MC ? includeGridDates : true;
            this.addVolSurfaceDates = addVolSurfaceDates;
            // pricer params
            this.brownianBridgeAdj = brownianBridgeAdj;
        }

        public PricerType getPricerType() {
            return pricerType;
        }

        public long getSeed() {
            return seed;
        }

        public int getNumPaths() {
            return numPaths;
        }

        public double getStepSize() {
            return stepSize;
        }

        public boolean isIncludeGridDates() {
            return includeGridDates;
        }

        public boolean isBrownianBridgeAdj() {
            return brownianBridgeAdj;
        }
    }

    private static McEngineSingleAsset from(PricerType pricerType,
                                            double spot,
                                            Object volSurface,
                                            Discount domYc,
                                            Discount forYc) {
        if (pricerType == PricerType.BLACK_MC) {
            return new EngineLognormal(spot, domYc, forYc, (AtmPwc)volSurface);
        } else {
            return new EngineLocalVol(spot, domYc, forYc, (LocalVolSurface)volSurface);
        }
    }

    public static Map<String, Double> pv(
            McInstrument optn,
            LocalDateTime val,
            double spot,
            ImpliedVolSurface vs,
            Discount discountYc,
            Discount dividendYc,
            Params params) {
        McEngineSingleAsset engine = from(params.getPricerType(), spot, vs, discountYc, dividendYc);
        LocalDateTime[] simDates = optn.getSimDates(val, params.stepSize, params.includeGridDates);
        LocalDateTime last = simDates[simDates.length - 1];
        if (params.addVolSurfaceDates) {
            if (vs instanceof InterpolatedVolSurface) {
                Set<LocalDateTime> all = new TreeSet<>(Arrays.asList(simDates));
                all.addAll(((InterpolatedVolSurface)vs).getVarInterp().keySet());
                all.removeIf(t -> t.isAfter(last));
                simDates = all.toArray(new LocalDateTime[all.size()]);
            }
        }
        engine.genPaths(simDates, params.numPaths, params.seed);
        // generate pricer params
        McInstrument.PricerParams pricerParams = new McInstrument.PricerParams(params.brownianBridgeAdj);
        double npv = 0.0, npv2 = 0.0; // mean and mean of square
        for(int i = 0; i < params.numPaths; ++i) {
            McPathSingleAsset path = engine.getPath(i);
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
