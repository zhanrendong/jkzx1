package tech.tongyu.bct.service.quantlib.api;

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi2;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApiArg;
import tech.tongyu.bct.service.quantlib.common.enums.ExtrapType;
import tech.tongyu.bct.service.quantlib.common.enums.InterpType;
import tech.tongyu.bct.service.quantlib.common.numerics.interp.Interpolator1D;
import tech.tongyu.bct.service.quantlib.common.numerics.interp.Interpolator1DCubicSpline;
import tech.tongyu.bct.service.quantlib.common.numerics.interp.Interpolator1DLinear;

public class Numerics {
    /*@BctQuantApi(
            name = "qlInterp1DCreate",
            description = "Create a 1D interpolator",
            argNames = {"type", "x", "y"},
            argDescriptions = {"Interpolation type", "Input x", "Input y"},
            argTypes = {"Enum", "ArrayDouble", "ArrayDouble"},
            retName = "interp",
            retType = "Handle",
            retDescription = "Interpolator"
    )
    public static UnivariateFunction interp1DCreate(InterpType type,
                                                    double[] x, double[] y) throws Exception {
        switch (type) {
            case INTERP_1D_LINEAR:
                return new LinearInterpolator().interpolate(x, y);
            case INTERP_1D_CUBICSPLINE:
                return new SplineInterpolator().interpolate(x, y);
            default:
                throw new Exception("Unknown interpolation type");
        }
    }*/

    @BctQuantApi2(
            name = "qlInterp1DCreate",
            description = "Create a 1D interpolator",
            retName = "interp",
            retType = "Handle",
            retDescription = "Newly created inteprolator",
            args = {
                    @BctQuantApiArg(name = "type", description = "Interpolator type", type = "Enum"),
                    @BctQuantApiArg(name = "x", description = "Input x", type = "ArrayDouble"),
                    @BctQuantApiArg(name = "y", description = "Input y", type = "ArrayDouble"),
                    @BctQuantApiArg(name = "extrapLow", description = "Extrap type before min x", type = "Enum"),
                    @BctQuantApiArg(name = "extrapHigh", description = "Extrap type after max x", type = "Enum")
            }
    )
    public static Interpolator1D interp1DCreate(InterpType type,
                                                 double[] x,
                                                 double[] y,
                                                 ExtrapType extrapLow,
                                                 ExtrapType extrapHigh) throws Exception {
        if (x.length != y.length)
            throw new Exception("Number of independent variables must be equal to that of dependent");
        if (x.length < 2)
            throw new Exception("Number of points to interpolate must be bigger than 1");
        switch (type) {
            case INTERP_1D_LINEAR:
                return new Interpolator1DLinear(x, y, extrapLow, extrapHigh);
            case INTERP_1D_CUBICSPLINE:
                return new Interpolator1DCubicSpline(x, y, extrapLow, extrapHigh);
            default:
                throw new Exception("Unknown interpolation type");
        }
    }

    /*@BctQuantApi(
            name = "qlInterp1DCalc",
            description = "Calculate interpolated function value given x",
            argNames = {"interpolator", "x"},
            argDescriptions = {"An interpolator", "x"},
            argTypes = {"Handle", "Double"},
            retDescription = "Interpolated function value",
            retType = "Double",
            retName = "y"
    )
    public static double interp1DCalc(UnivariateFunction interpolator, double x) {
        return interpolator.value(x);
    }*/

    @BctQuantApi2(
            name = "qlInterp1DCalc",
            description = "Calculate interpolated function value/derivative given x",
            retName = "value",
            retDescription = "Interpolated vlaue/derivative",
            retType = "ArrayDouble",
            args = {
                    @BctQuantApiArg(name = "interpolator", description = "Interpolator", type = "Handle"),
                    @BctQuantApiArg(name = "x", description = "x to be interpolated at", type =
                            "ArrayDouble"),
                    @BctQuantApiArg(name = "order", description = "derivative order", type = "Integer", required = false)
            }
    )
    public static double[] interp1DCalc(Interpolator1D interpolator, double[] x, int order) throws
            Exception {
        if (order < 0)
            throw new Exception("Order must be positive");
        double[] y = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            y[i] = interpolator.derivative(x[i], order);
        }
        return y;
    }

    @BctQuantApi(
            name = "qlInterp1DLoessFit",
            description = "Return LOESS smoothed y",
            argNames = {"x", "y", "weights", "bandwidth", "robustnesIters", "accuracy"},
            argTypes = {"ArrayDouble", "ArrayDouble", "ArrayDouble",
                            "Double", "Integer", "Double"},
            argDescriptions = {"x", "y", "weights", "Bandwidth: 0.3",
                            "Bobustness Iters: 2", "Accuracy: 1e-12"},
            retName = "y",
            retType = "ArrayDouble",
            retDescription = "Smoothed y"
    )
    public static double[] loessFit(double[] x, double[] y, double[] weights,
                                    double bandwidth, int robustnessIters, double accuracy) {
        LoessInterpolator loess = new LoessInterpolator(bandwidth, robustnessIters, accuracy);
        return loess.smooth(x, y, weights);
    }
}