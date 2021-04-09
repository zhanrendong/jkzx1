package tech.tongyu.bct.service.quantlib.common.enums;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantEnum;

/**
 * 1D interpolation types.
 * Given a set of points {(x_0, y_0), (x_1, y_1), ..., (x_n, y_n)}, interpolates any x between x_0 and y_0 for y.
 * For x outside [x_0, y_0], see extrapolation type {@link ExtrapType}
 * Linear interpolation: {@link #INTERP_1D_LINEAR}
 * Cubic-spline: {@link #INTERP_1D_CUBICSPLINE}.
 */
@BctQuantEnum
public enum InterpType {
    /**
     * Linear interpolation
     */
    INTERP_1D_LINEAR,
    /**
     * Cubic-spline interpolation
     */
    INTERP_1D_CUBICSPLINE
}