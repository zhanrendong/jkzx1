package tech.tongyu.bct.service.quantlib.pricer.PricerPDE;

import java.util.function.BiFunction;

/**
 * @author Liao Song
 * @since 2016-8-23
 * constructs the Black-Scholes partial differential equations:
 * df/dt + A df/dx + B d^2f/dx^2 +C f + D = 0 :
 * where A = (r-q)s, B= 1/2 vol^2s^s, C= -r and
 * r,q ,s vol are all constant
 */
class BSPDEConstructor {
    static BiFunction<Double, Double, Double> getFirstOrderDerivFunction(double r, double q, double vol) {
        return (s, t) -> (r - q) * s;

    }

    static BiFunction<Double, Double, Double> getSecondOrderDerivFunction(double r, double q, double vol) {
        return (s, t) -> 0.5 * vol * vol * s * s;
    }

    static BiFunction<Double, Double, Double> getFirstOrderFunction(double r, double q, double vol) {
        return (s, t) -> -r;
    }

    static BiFunction<Double, Double, Double> getConstFunction(double r, double q, double vol) {
        return (s, t) -> 0.0;
    }
}
