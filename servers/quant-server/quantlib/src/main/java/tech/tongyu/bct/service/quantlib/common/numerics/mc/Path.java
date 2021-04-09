package tech.tongyu.bct.service.quantlib.common.numerics.mc;

import java.time.LocalDateTime;
import java.util.Map;

public class Path implements McPathSingleAsset {
    private final LocalDateTime[] simDates;
    private final Map<LocalDateTime, Integer> tIdx;
    private final double[] path;
    private final double[] dfs;
    private final double[] vars;

    public Path(LocalDateTime[] simDates, double[] dfs, Map<LocalDateTime, Integer> tIdx,
                double[] path, double[] vars) {
        this.simDates = simDates;
        this.tIdx = tIdx;
        this.path = path;
        this.dfs = dfs;
        this.vars = vars;
    }

    @Override
    public double getSpot(LocalDateTime t) {
        return path[tIdx.get(t)];
    }

    // var between t and previous t
    @Override
    public double getVariance(LocalDateTime start, LocalDateTime end) {return vars[tIdx.get(end)];}

    @Override
    public double df(LocalDateTime t) {
        return dfs[tIdx.get(t)];
    }

    @Override
    public LocalDateTime[] getSimDates() {
        return simDates;
    }
}
