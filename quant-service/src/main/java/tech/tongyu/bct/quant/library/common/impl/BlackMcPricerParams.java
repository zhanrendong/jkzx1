package tech.tongyu.bct.quant.library.common.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import tech.tongyu.bct.quant.library.common.QuantPricerParams;

public class BlackMcPricerParams implements QuantPricerParams {
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

    @JsonCreator
    public BlackMcPricerParams(long seed, int numPaths,
                               double stepSize, boolean includeGridDates,
                               boolean addVolSurfaceDates, boolean brownianBridgeAdj) {
        this.seed = seed;
        this.numPaths = numPaths;
        this.stepSize = stepSize;
        this.includeGridDates = includeGridDates;
        this.addVolSurfaceDates = addVolSurfaceDates;
        this.brownianBridgeAdj = brownianBridgeAdj;
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

    public boolean isAddVolSurfaceDates() {
        return addVolSurfaceDates;
    }

    public boolean isBrownianBridgeAdj() {
        return brownianBridgeAdj;
    }
}
