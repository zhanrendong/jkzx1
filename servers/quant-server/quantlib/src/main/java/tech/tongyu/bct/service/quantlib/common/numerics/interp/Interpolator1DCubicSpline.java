package tech.tongyu.bct.service.quantlib.common.numerics.interp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import tech.tongyu.bct.service.quantlib.common.enums.ExtrapType;

public class Interpolator1DCubicSpline implements Interpolator1D {
    private final PolynomialSplineFunction interp;
    private final double xMin, xMax, yMin, yMax, slopeMin, slopeMax;

    private final double[] xs, ys;
    private final ExtrapType extrapMin, extrapMax;

    @JsonCreator
    public Interpolator1DCubicSpline(
            @JsonProperty("xs") double[] xs,
            @JsonProperty("ys") double[] ys,
            @JsonProperty("extrapMin") ExtrapType extrapMin,
            @JsonProperty("extrapMax") ExtrapType extrapMax) {
        this.xs = xs;
        this.ys = ys;
        this.extrapMax = extrapMax;
        this.extrapMin = extrapMin;

        this.interp = new SplineInterpolator().interpolate(xs, ys);
        this.xMin = xs[0];
        this.yMin = ys[0];
        this.xMax = xs[xs.length - 1];
        this.yMax = ys[ys.length - 1];
        if (extrapMin == ExtrapType.EXTRAP_1D_LINEAR) {
            this.slopeMin = interp.polynomialSplineDerivative().value((this.xMin));
        } else
            this.slopeMin = 0.0;
        if (extrapMax == ExtrapType.EXTRAP_1D_LINEAR) {
            this.slopeMax = interp.polynomialSplineDerivative().value(this.xMax);
        } else
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
        else if (order == 1) {
            if (x >= xMin && x <= xMax)
                return interp.polynomialSplineDerivative().value(x);
            else if (x < xMin)
                return slopeMin;
            else
                return slopeMax;
        } else if (order == 2) {
            if (x >= xMin && x <= xMax) {
                return interp.polynomialSplineDerivative().polynomialSplineDerivative().value(x);
            } else
                return 0.0;
        } else
            return 0.0;
    }

    public double[] getXs() {
        return xs;
    }

    public double[] getYs() {
        return ys;
    }

    public ExtrapType getExtrapMin() {
        return extrapMin;
    }

    public ExtrapType getExtrapMax() {
        return extrapMax;
    }
}
