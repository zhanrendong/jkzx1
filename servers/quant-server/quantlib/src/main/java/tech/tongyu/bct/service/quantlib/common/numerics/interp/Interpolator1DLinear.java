package tech.tongyu.bct.service.quantlib.common.numerics.interp;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import tech.tongyu.bct.service.quantlib.common.enums.ExtrapType;

public class Interpolator1DLinear implements Interpolator1D {
    private final PolynomialSplineFunction interp;
    private final double xMin, xMax, yMin, yMax, slopeMin, slopeMax;

    public Interpolator1DLinear(double[] xs, double[] ys, ExtrapType extrapMin, ExtrapType extrapMax) {
        this.interp = new LinearInterpolator().interpolate(xs, ys);
        this.xMin = xs[0];
        this.yMin = ys[0];
        this.xMax = xs[xs.length - 1];
        this.yMax = ys[ys.length - 1];
        if (extrapMin == ExtrapType.EXTRAP_1D_LINEAR)
            this.slopeMin = (ys[1] - ys[0]) / (xs[1] - xs[0]);
        else
            this.slopeMin = 0.0;
        if (extrapMax == ExtrapType.EXTRAP_1D_LINEAR)
            this.slopeMax = (ys[ys.length - 1] - ys[ys.length - 2]) / (xs[xs.length - 1] - xs[xs.length - 2]);
        else
            this.slopeMax = 0.0;
    }

    @Override
    public double value(double x) {
        if (x >= xMin && x <= xMax)
            return interp.value(x);
        else if (x < xMin)
            return yMin + slopeMin * (x - xMin);
        else
            return yMax + slopeMax * (x - xMax);
    }

    @Override
    public double derivative(double x, int order) {
        if (order == 0)
            return value(x);
        if (order > 1)
            return 0.0;
        if (x >= xMin && x <= xMax) {
            return interp.polynomialSplineDerivative().value(x);
        }
        else if (x <= xMin)
            return slopeMin;
        else
            return slopeMax;
    }
}
