package tech.tongyu.bct.quant.library.numerics.mc.impl.single;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.quant.library.market.curve.DiscountingCurve;
import tech.tongyu.bct.quant.library.market.vol.ImpliedVolSurface;
import tech.tongyu.bct.quant.library.market.vol.impl.AtmPwcVolSurface;
import tech.tongyu.bct.quant.library.numerics.mc.McEngineSingleAsset;
import tech.tongyu.bct.quant.library.numerics.mc.McPathSingleAsset;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class McEngineSingleAssetLognormal implements McEngineSingleAsset {
    private final double spot;
    private final DiscountingCurve ycDom;
    private final DiscountingCurve ycFor;
    // specialized for lognormal model
    // strictly speaking it is meaningless to ask for a variance if the vol surface is
    // lognormal with term structure. thus we approximate by taking atm vols from
    // the surface (whether it has smile/skew or not)
    // for simplicity we also take only atm spot vol
    // when the surface is lognormal this gives back the correct variances
    private final ImpliedVolSurface vs;

    private LocalDateTime[] simDates;
    private Map<LocalDateTime, Integer> tIndex;
    private double[][] paths;
    private double[][] pathVars;
    private Map<LocalDateTime, Double> dfs;

    public McEngineSingleAssetLognormal(double spot,
                                        DiscountingCurve dfDom, DiscountingCurve dfFor,
                                        ImpliedVolSurface vs) {
        this.spot = spot;
        this.ycDom = dfDom;
        this.ycFor = dfFor;
        this.vs = vs;
        this.tIndex = new HashMap<>();
    }

    // always assume val date is in simDates
    @Override
    public void genPaths(LocalDateTime[] simDates, LocalDateTime dfDates[], int n, long seed) {
        this.simDates = simDates;
        tIndex.clear();
        for(int i=0; i<simDates.length; ++i) {
            tIndex.put(simDates[i], i);
        }
        double[] vars = Arrays.stream(simDates)
                .mapToDouble(t -> vs.variance(spot, spot, t))
                .toArray();
        double[] dfRatios = Arrays.stream(simDates)
                .mapToDouble(t -> ycFor.df(t)/ ycDom.df(t))
                .toArray();
        this.dfs = Arrays.stream(dfDates)
                .collect(Collectors.toMap(t -> t, t -> ycDom.df(t)));
        paths = new double[n][simDates.length];
        pathVars = new double[n][simDates.length];
        RandomGenerator rng = new Well19937c(seed);
        for(int i=0; i<n; ++i) {
            paths[i][0] = spot;
            pathVars[i][0] = 0.0;
            for(int j=1; j<simDates.length; ++j) {
                double var = vars[j] - vars[j - 1];
                double df = dfRatios[j] / dfRatios[j - 1];
                paths[i][j] = paths[i][j-1]* FastMath.exp(-0.5*var+FastMath.sqrt(var)*rng.nextGaussian())*df;
                pathVars[i][j] = var;
            }
        }
    }

    @Override
    public McPathSingleAsset getPath(int n) {
        return new McPathLognormal(simDates, dfs, tIndex, paths[n], pathVars[n]);
    }
}
