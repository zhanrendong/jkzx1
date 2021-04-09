package tech.tongyu.bct.quant.library.numerics.interp;

/**
 * Given a set of points {(x_0, y_0), (x_1, y_1), ..., (x_n, y_n)}, extrapolates for x outside [x_0, y_0]
 * We allow different extrapolation types at two end points. For example, flat for x less than x_0 and linear
 * for x larger than x_n
 * Linear extrapolation {@link #EXTRAP_1D_LINEAR}
 * Flat extrapolation {@link #EXTRAP_1D_FLAT}
 */
public enum ExtrapTypeEnum {
    /**
     * Linear extrapolation
     */
    EXTRAP_1D_LINEAR,
    /**
     * Flat extrapolation
     */
    EXTRAP_1D_FLAT
}
