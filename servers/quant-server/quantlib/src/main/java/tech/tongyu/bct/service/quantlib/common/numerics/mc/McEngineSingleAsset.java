package tech.tongyu.bct.service.quantlib.common.numerics.mc;

import java.time.LocalDateTime;

public interface McEngineSingleAsset {
    /**
     * Generate paths
     * @param simDates Simulation times. Always assume that valuation date is the first sim date.
     * @param n Number of paths to simulate
     * @param seed Random number generator seed
     */
    void genPaths(LocalDateTime[] simDates, int n, long seed);

    /**
     * Get nth path
     * @param n The path index
     * @return A simulated path
     */
    McPathSingleAsset getPath(int n);
}
