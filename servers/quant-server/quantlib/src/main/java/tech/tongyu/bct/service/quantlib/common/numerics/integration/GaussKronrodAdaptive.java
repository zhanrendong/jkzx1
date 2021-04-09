package tech.tongyu.bct.service.quantlib.common.numerics.integration;

import java.util.function.DoubleUnaryOperator;

public class GaussKronrodAdaptive {
    // weights for 7-point Gauss-Legendre integration
    // (only 4 values out of 7 are given as they are symmetric)
    private static double g7w[] = {
            0.417959183673469,
            0.381830050505119,
            0.279705391489277,
            0.129484966168870};
    // weights for 15-point Gauss-Kronrod integration
    private static double k15w[] = {
            0.209482141084728,
            0.204432940075298,
            0.190350578064785,
            0.169004726639267,
            0.140653259715525,
            0.104790010322250,
            0.063092092629979,
            0.022935322010529};
    // abscissae (evaluation points)
    // for 15-point Gauss-Kronrod integration
    private static double k15t[] = {
            0.000000000000000,
            0.207784955007898,
            0.405845151377397,
            0.586087235467691,
            0.741531185599394,
            0.864864423359769,
            0.949107912342758,
            0.991455371120813};
    int numIntervals;
    private double absTol;
    private int maxIte;

    public GaussKronrodAdaptive(double absTol, int maxIte) {
        this.absTol = absTol;
        this.maxIte = maxIte;
        this.numIntervals = 0;
    }

    private double oneStep(double a, double b, DoubleUnaryOperator f, double tol) throws Exception {
        double half = (b - a) / 2.;
        double center = (a + b) / 2.;
        double fc = f.applyAsDouble(center);
        double g7 = fc * g7w[0];
        double k15 = fc * k15w[0];

        double t, fsum;
        int j, j2;
        for (j = 1, j2 = 2; j < 4; j++, j2 += 2) {
            t = half * k15t[j2];
            fsum = f.applyAsDouble(center - t) + f.applyAsDouble(center + t);
            g7 += fsum * g7w[j];
            k15 += fsum * k15w[j2];
        }
        for (j2 = 1; j2 < 8; j++, j2 += 2) {
            t = half * k15t[j2];
            fsum = f.applyAsDouble(center - t) + f.applyAsDouble(center + t);
            k15 += fsum * k15w[j2];
        }
        g7 = half * g7;
        k15 = half * k15;

        numIntervals += 15;
        if (Math.abs(k15 - g7) < tol)
            return k15;
        else {
            if (numIntervals > maxIte)
                throw new Exception("maximum number of function valuation reached"
                        + " while the integration has not converged.");
            else
                return oneStep(a, center, f, tol / 2.) + oneStep(center, b, f, tol / 2.);
        }
    }

    public double integrate(final double a, final double b, DoubleUnaryOperator f) {
        numIntervals = 0;
        double ret = 0.0;
        DoubleUnaryOperator func;
        if (a == Double.NEGATIVE_INFINITY && b == Double.POSITIVE_INFINITY) {
            func = t -> (f.applyAsDouble((1. - t) / t) + f.applyAsDouble((t - 1.) / t)) / (t * t);
        } else if (b == Double.POSITIVE_INFINITY) {
            func = t -> f.applyAsDouble((a + (1. - t) / t)) / (t * t);
        } else if (a == Double.NEGATIVE_INFINITY) {
            func = t -> f.applyAsDouble(b + (t - 1.) / t) / (t * t);
        } else
            func = t -> (b - a) * f.applyAsDouble((b - a) * t + a);

        try {
            ret = oneStep(0., 1., func, absTol);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return ret;
    }
}