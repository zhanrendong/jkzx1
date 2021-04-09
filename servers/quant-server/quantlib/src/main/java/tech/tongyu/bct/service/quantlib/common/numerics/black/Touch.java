package tech.tongyu.bct.service.quantlib.common.numerics.black;

import org.apache.commons.math3.distribution.NormalDistribution;
import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.enums.RebateType;

import static java.lang.Math.*;

/**
 * Created by Liao Song on 16-9-1.
 * pricing formulas for binary barrier(one touch & no touch) Options
 * reference: The complete guide to option pricing formulas, page 176
 */
public class Touch {
    private final static NormalDistribution N = new NormalDistribution();

    private static double mu(double b, double vol) {
        return (b - 0.5 * vol * vol) / (vol * vol);
    }

    private static double lambda(double mu, double r, double vol) {
        return sqrt(mu * mu + 2 * r / (vol * vol));
    }

    private static double z(double spot, double barrier, double vol, double T, double lambda) {
        return log(barrier / spot) / (vol * sqrt(T)) + lambda * vol * sqrt(T);
    }

    private static double x_two(double spot, double barrier, double vol, double T, double mu) {
        return log(spot / barrier) / (vol * sqrt(T)) + (mu + 1) * vol * sqrt(T);
    }

    private static double y_two(double spot, double barrier, double vol, double T, double mu) {
        return log(barrier / spot) / (vol * sqrt(T)) + (mu + 1) * vol * sqrt(T);
    }

    private static double B_two(double rebate, double r, double T, double vol, double x2, double phi) {
        return rebate * exp(-r * T) * N.cumulativeProbability(phi * x2 - phi * vol * sqrt(T));
    }

    private static double B_four(double rebate, double spot, double barrier, double r, double T, double vol, double mu,
                                 double y2, double eta) {
        return rebate * exp(-r * T) * pow(barrier / spot, 2 * mu)
                * N.cumulativeProbability(eta * y2 - eta * vol * sqrt(T));
    }

    private static double A_five(double rebate, double spot, double barrier, double T, double vol, double mu,
                                 double lambda, double z, double eta) {
        return rebate * (pow(barrier / spot, mu + lambda) * N.cumulativeProbability(eta * z)
                + pow(barrier / spot, mu - lambda) * N.cumulativeProbability(eta * z - 2 * eta * lambda * vol * sqrt(T)));
    }

    /**
     * One touch option pays out a fixed amount (cash settled) if the spot hits the barrier. The payment is made
     * either when the barrier is breached or at expiry. The barrier is continuous monitored throughout the lifetime of
     * the option. The barrier direction is defined in the way that cash rebate is paid when the option is knocked out.
     *
     * @param rebate     rebate
     * @param barrier    barrier
     * @param spot       spot
     * @param vol        volatility
     * @param T          time to maturity in years
     * @param r          risk free interest rate
     * @param q          annually dividend yield
     * @param direction  barrier direction(DOWN_AND_IN or UP_AND_IN)
     * @param rebateType rebate payment type(PAY_WHEN_HIT or PAY_AT_EXPIRY)
     * @return option price
     */
    public static double oneTouch(double rebate, double barrier, double spot, double vol, double T, double r, double q,
                                  BarrierDirection direction, RebateType rebateType) {

        double forward = spot * exp((r - q) * T);
        if (direction == BarrierDirection.DOWN_AND_OUT && rebateType == RebateType.PAY_WHEN_HIT) {
            if (barrier == 0)
                return 0.0;

            if (spot <= barrier)
                return rebate;

            if (vol == 0) {
                if (forward <= barrier) {
                    double hit = log(barrier / spot) / (r - q);
                    return rebate * exp(-r * hit);
                } else
                    return 0;
            }

            if (T == 0) {
                if (spot <= barrier)
                    return rebate;
                else
                    return 0;
            }

            //if (spot > barrier)
            else {
                double eta = 1;
                double b = r - q;
                double mu = mu(b, vol);
                double lambda = lambda(mu, r, vol);
                double z = z(spot, barrier, vol, T, lambda);
                if (Double.isInfinite(pow(barrier / spot, mu + lambda)) ||
                        Double.isInfinite(pow(barrier / spot, mu - lambda)))
                    return oneTouch(rebate, barrier, spot, 0, T, r, q, direction, rebateType);
                else
                    return A_five(rebate, spot, barrier, T, vol, mu, lambda, z, eta);
            }
        } else if (direction == BarrierDirection.UP_AND_OUT && rebateType == RebateType.PAY_WHEN_HIT) {

            if (spot >= barrier)
                return rebate;

            if (vol == 0) {
                if (forward >= barrier) {
                    double hit = log(barrier / spot) / (r - q);
                    return rebate * exp(-r * hit);
                } else
                    return 0;
            }

            if (T == 0) {
                if (spot >= barrier)
                    return rebate;
                else
                    return 0;
            }

            //if (spot < barrier)
            else {
                double eta = -1;
                double b = r - q;
                double mu = mu(b, vol);
                double lambda = lambda(mu, r, vol);
                double z = z(spot, barrier, vol, T, lambda);
                if (Double.isInfinite(pow(barrier / spot, mu + lambda)) ||
                        Double.isInfinite(pow(barrier / spot, mu - lambda)))
                    return oneTouch(rebate, barrier, spot, 0, T, r, q, direction, rebateType);
                else
                    return A_five(rebate, spot, barrier, T, vol, mu, lambda, z, eta);
            }
        } else if (direction == BarrierDirection.DOWN_AND_OUT && rebateType == RebateType.PAY_AT_EXPIRY) {
            if (barrier == 0)
                return 0;

            if (spot <= barrier)
                return rebate * exp(-r * T);

            if (vol == 0) {
                if (forward <= barrier) {
                    return rebate * exp(-r * T);
                } else
                    return 0;
            }

            if (T == 0) {
                if (spot <= barrier)
                    return rebate;
                else
                    return 0;
            }

            //if (spot > barrier)
            else {
                double eta = 1;
                double phi = -1;
                double b = r - q;
                double mu = mu(b, vol);
                double x2 = x_two(spot, barrier, vol, T, mu);
                double y2 = y_two(spot, barrier, vol, T, mu);
                if (Double.isInfinite(pow(barrier / spot, 2 * mu)))
                    return oneTouch(rebate, barrier, spot, 0, T, r, q, direction, rebateType);
                else
                    return B_two(rebate, r, T, vol, x2, phi) + B_four(rebate, spot, barrier, r, T, vol, mu, y2, eta);
            }
        }
        //(direction==BarrierDirection.UP_AND_OUT && rebateType== RebateType.PAY_AT_EXPIRY)
        else {
            if (spot >= barrier)
                return rebate * exp(-r * T);

            if (vol == 0) {
                if (forward >= barrier) {
                    return rebate * exp(-r * T);
                } else
                    return 0;
            }

            if (T == 0) {
                if (spot >= barrier)
                    return rebate;
                else
                    return 0;
            }

            //if (spot < barrier)
            else {
                double eta = -1;
                double phi = 1;
                double b = r - q;
                double mu = mu(b, vol);
                double x2 = x_two(spot, barrier, vol, T, mu);
                double y2 = y_two(spot, barrier, vol, T, mu);
                if (Double.isInfinite(pow(barrier / spot, 2 * mu)))
                    return oneTouch(rebate, barrier, spot, 0, T, r, q, direction, rebateType);
                else
                    return B_two(rebate, r, T, vol, x2, phi) + B_four(rebate, spot, barrier, r, T, vol, mu, y2, eta);
            }
        }

    }

    /**
     * No-touch option pays a fixed amount (cash settled) only when the spot never hits the barrier.
     * The barrier is continuously monitored throughout the lifetime. The payment is made upon expiry.
     * The barrier direction is defined as nothing is paid if the option is knocked in during the lifetime.
     *
     * @param rebate    rebate
     * @param barrier   barrier
     * @param spot      spot
     * @param vol       volatility
     * @param T         time to maturity in years
     * @param r         risk free interest rate
     * @param q         annually dividend yield
     * @param direction barrier direction (DOWN_AND_OUT or UP_AND_OUT)
     * @return option price
     */
    public static double noTouch(double rebate, double barrier, double spot, double vol, double T, double r, double q,
                                 BarrierDirection direction) {
        double forward = spot * exp((r - q) * T);
        if (direction == BarrierDirection.DOWN_AND_OUT) {

            if (spot <= barrier)
                return 0;

            if (vol == 0 || T == 0) {
                if (forward <= barrier)
                    return 0;
                else
                    return rebate * exp(-r * T);
            }
            //if (spot > barrier)
            else {
                double eta = 1;
                double phi = 1;
                double b = r - q;
                double mu = mu(b, vol);
                double x2 = x_two(spot, barrier, vol, T, mu);
                double y2 = y_two(spot, barrier, vol, T, mu);
                if (Double.isInfinite(pow(barrier / spot, 2 * mu)))
                    return noTouch(rebate, barrier, spot, 0, T, r, q, direction);
                else
                    return B_two(rebate, r, T, vol, x2, phi) - B_four(rebate, spot, barrier, r, T, vol, mu, y2, eta);
            }
        } else {
            if (spot >= barrier)
                return 0;

            if (vol == 0 || T == 0) {
                if (forward >= barrier)
                    return 0;
                else
                    return rebate * exp(-r * T);
            }
            //if (spot < barrier)
            else {
                double eta = -1;
                double phi = -1;
                double b = r - q;
                double mu = mu(b, vol);
                double x2 = x_two(spot, barrier, vol, T, mu);
                double y2 = y_two(spot, barrier, vol, T, mu);
                if (Double.isInfinite(pow(barrier / spot, 2 * mu)))
                    return noTouch(rebate, barrier, spot, 0, T, r, q, direction);
                else
                    return B_two(rebate, r, T, vol, x2, phi) - B_four(rebate, spot, barrier, r, T, vol, mu, y2, eta);
            }
        }
    }


}
