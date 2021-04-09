package tech.tongyu.bct.quant.library.numerics.mc;

import java.time.LocalDateTime;

public interface McEngineSingleAsset {
    /**
     * Generate paths
     * @param simDates Simulation times. Always assume that valuation date is the first sim date.
     * @param dfDates Dates on which discounting factors are needed. These are usually an instrument's payment dates.
     * @param n Number of paths to simulate
     * @param seed Random number generator seed
     */
    void genPaths(LocalDateTime[] simDates, LocalDateTime[] dfDates, int n, long seed);

    /**
     * Get nth path
     * @param n The path index
     * @return A simulated path
     */
    McPathSingleAsset getPath(int n);
}
