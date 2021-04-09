package tech.tongyu.bct.service.quantlib.common.numerics.mc.lognormal;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McEngineSingleAsset;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.Path;
import tech.tongyu.bct.service.quantlib.market.curve.Discount;
import tech.tongyu.bct.service.quantlib.market.vol.AtmPwc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EngineLognormal implements McEngineSingleAsset {
    private final double spot;
    private final Discount ycDom;
    private final Discount ycFor;
    // specialized for lognormal model
    private final AtmPwc vs;

    private LocalDateTime[] simDates;
    private Map<LocalDateTime, Integer> tIndex;
    private double[][] paths;
    private double[][] pathVars;
    private double[] dfs;

    public EngineLognormal(double spot, Discount dfDom, Discount dfFor, AtmPwc vs) {
        this.spot = spot;
        this.ycDom = dfDom;
        this.ycFor = dfFor;
        this.vs = vs;
        this.tIndex = new HashMap<>();
    }

    // always assume val date is in simDates
    @Override
    public void genPaths(LocalDateTime[] simDates, int n, long seed) {
        this.simDates = simDates;
        tIndex.clear();
        for(int i=0; i<simDates.length; ++i) {
            tIndex.put(simDates[i], i);
        }
        double[] vars = Arrays.stream(simDates)
                .mapToDouble(vs::var)
                .toArray();
        double[] dfs = Arrays.stream(simDates)
                .mapToDouble(t -> ycFor.df(t)/ ycDom.df(t))
                .toArray();
        this.dfs = Arrays.stream(simDates)
                .mapToDouble(t -> ycDom.df(t))
                .toArray();
        paths = new double[n][simDates.length];
        pathVars = new double[n][simDates.length];
        RandomGenerator rng = new Well19937c(seed);
        for(int i=0; i<n; ++i) {
            paths[i][0] = spot;
            pathVars[i][0] = 0.0;
            for(int j=1; j<simDates.length; ++j) {
                double var = vars[j] - vars[j - 1];
                double df = dfs[j] / dfs[j - 1];
                paths[i][j] = paths[i][j-1]* FastMath.exp(-0.5*var+FastMath.sqrt(var)*rng.nextGaussian())*df;
                pathVars[i][j] = var;
            }
        }
    }

    @Override
    public Path getPath(int n) {
        return new Path(simDates, dfs, tIndex, paths[n], pathVars[n]);
    }
}
