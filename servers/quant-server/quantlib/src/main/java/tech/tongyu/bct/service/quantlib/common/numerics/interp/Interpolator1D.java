package tech.tongyu.bct.service.quantlib.common.numerics.interp;

public interface Interpolator1D {
    double value(double x);
    double derivative(double x, int order);
}
