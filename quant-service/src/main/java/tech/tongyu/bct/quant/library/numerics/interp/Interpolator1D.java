package tech.tongyu.bct.quant.library.numerics.interp;

public interface Interpolator1D {
    double value(double x);
    double derivative(double x, int order);
}
