package tech.tongyu.bct.service.quantlib.common.numerics.black;

import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.enums.RebateType;
import tech.tongyu.bct.service.quantlib.common.numerics.fd.FiniteDifference;

import static java.lang.Math.*;


/**
 * Created by Liao Song on 16-7-26.
 * reference: Pricing Double Barrier Options: An Analytic Approach by Antoon Pelsser (1997)
 */
public class DoubleKnockOut {


    private static double Lambda(double mu, int k, double vol, double l) {
        return 0.5 * (pow(mu / vol, 2) + pow(k * PI * vol / l, 2));
    }

    private static double Q_term(double a, double y, int n, double vol, double l, double mu, double tau, double x) {
        int k;
        double sum = 0;
        for (k = 1; k <= n; k++) {
            sum += 2 / l * exp(mu / pow(vol, 2) * (y - x) + a * y) * exp(-Lambda(mu, k, vol, l) * tau) * sin(k * PI * x / l) *
                    (((mu / pow(vol, 2) + a) * sin(k * PI * y / l) - k * PI / l * cos(k * PI * y / l)) /
                            (pow(mu / pow(vol, 2) + a, 2) + pow(k * PI / l, 2)));
        }
        return sum;
    }

    private static double rebateAmount(RebateType rebateType, double upperBarrier, double lowerBarrier, double upperRebate,
                                       double lowerRebate, double spot, double vol, double tau, double r, double q) {


        double mu = r - q - 0.5 * pow(vol, 2);
        double l = log(upperBarrier / lowerBarrier);
        double x = log(spot / lowerBarrier);
        double eps = 1e-10;
        //pay at expiry
        if (rebateType == RebateType.PAY_AT_EXPIRY) {


            if (abs(mu / vol / vol) > log(Double.MAX_VALUE) || vol == 0) {
                double forward = spot * exp((r - q) * tau);
                if (forward <= lowerBarrier)
                    return lowerRebate * exp(-r * tau);
                else if (forward >= upperBarrier)
                    return upperRebate * exp(-r * tau);
                else
                    return 0;
            }
            //normal case
            int steps;
            if ((-2 * log(eps) / tau - pow(mu / vol, 2)) < 0)
                steps = 1;
            else
                steps = (int) sqrt((-2 * log(eps) / tau - pow(mu / vol, 2)) / pow(PI * vol / l, 2)) + 1;
            //truncate to speed up
            if (steps > 10000)
                steps = 10000;

            double sum1 = 0;
            double sum2 = 0;

            for (int k = 1; k <= steps; k++) {
                sum1 += pow(vol / l, 2) * exp(-Lambda(mu, k, vol, l) * tau) /
                        Lambda(mu, k, vol, l) * k * PI * sin(k * PI * (l - x) / l);
                sum2 += pow(vol / l, 2) * exp(-Lambda(mu, k, vol, l) * tau) /
                        Lambda(mu, k, vol, l) * k * PI * sin(k * PI * x / l);
            }

            double P_plus = exp(mu * (l - x) / pow(vol, 2)) * (sinh(mu * x / pow(vol, 2)) / sinh(mu * l
                    / pow(vol, 2)) - sum1);
            double P_minus = exp(-mu * x / pow(vol, 2)) * (sinh(mu * (l - x) / pow(vol, 2)) / sinh(mu * l
                    / pow(vol, 2)) - sum2);
            return exp(-r * tau) * (upperRebate * P_plus + lowerRebate * P_minus);
        }
        //pay when hit
        else if (rebateType == RebateType.PAY_WHEN_HIT) {
            double mu_prime = sqrt(mu * mu + 2.0 * vol * vol * r);

            if (abs(mu_prime / vol / vol) > log(Double.MAX_VALUE) || vol == 0) {
                double forward = spot * exp((r - q) * tau);
                if (forward <= lowerBarrier) {
                    double hit = log(lowerBarrier / spot) / (r - q);
                    return lowerRebate * exp(-r * hit);
                } else if (forward >= upperBarrier) {
                    double hit = log(upperBarrier / spot) / (r - q);
                    return upperRebate * exp(-r * hit);
                } else
                    return 0;
            }
            int steps;
            if ((-2 * log(eps) / tau - pow(mu / vol, 2)) < 0)
                steps = 1;
            else
                steps = (int) sqrt((-2 * log(eps) / tau - pow(mu_prime / vol, 2)) / pow(PI * vol / l, 2)) + 1;
            //truncate to speed up
            if (steps > 10000)
                steps = 10000;

            double sum1 = 0;
            double sum2 = 0;

            for (int k = 1; k <= steps; k++) {
                sum1 += pow(vol / l, 2) * exp(-Lambda(mu_prime, k, vol, l) * tau) /
                        Lambda(mu_prime, k, vol, l) * k * PI * sin(k * PI * (l - x) / l);
                sum2 += pow(vol / l, 2) * exp(-Lambda(mu_prime, k, vol, l) * tau) /
                        Lambda(mu_prime, k, vol, l) * k * PI * sin(k * PI * x / l);
            }

            double P_plus = exp(mu * (l - x) / pow(vol, 2)) * (sinh(mu_prime * x /
                    pow(vol, 2)) / sinh(mu_prime * l
                    / pow(vol, 2)) - sum1);
            double P_minus = exp(-mu * x / pow(vol, 2)) * (sinh(mu_prime * (l - x) /
                    pow(vol, 2)) / sinh(mu_prime * l
                    / pow(vol, 2)) - sum2);
            return upperRebate * P_plus + lowerRebate * P_minus;

        }
        //pay nothing
        else
            return 0;

    }

    private static double price(OptionType type, RebateType rebateType,
                                double upperBarrier, double lowerBarrier, double upperRebate, double lowerRebate,
                                double spot, double strike, double vol, double tau, double r,
                                double q) {
        //Up-and-Out-Down-and-Out CALL
        //payoff: c=max(S-X,0) if L<S<U before T else rebate*exp(-r*Tau)
        //if tau =0 and not knocked out
        double value;
        if (tau == 0 && spot > lowerBarrier && spot < upperBarrier) {
            if (type == OptionType.CALL)
                return max(spot - strike, 0);
            else
                return max(strike - spot, 0);
        }
        //if already knocked out
        if (spot <= lowerBarrier) {
            if (rebateType == RebateType.PAY_AT_EXPIRY)
                return lowerRebate * exp(-r * tau);
            else
                return lowerRebate;
        } else if (spot >= upperBarrier) {
            if (rebateType == RebateType.PAY_AT_EXPIRY)
                return upperRebate * exp(-r * tau);
            else
                return upperRebate;
        }
        // when vol ~ 0 compared with r and q
        double forward = spot * exp((r - q) * tau);
        if (abs((r - q) / vol / vol) > log(Double.MAX_VALUE) || vol == 0) {
            if (forward <= lowerBarrier) {
                if (rebateType == RebateType.PAY_WHEN_HIT) {
                    double hit = log(lowerBarrier / spot) / (r - q);
                    return lowerRebate * exp(-r * hit);
                } else
                    return lowerRebate * exp(-r * tau);
            } else if (forward >= upperBarrier) {
                if (rebateType == RebateType.PAY_WHEN_HIT) {
                    double hit = log(upperBarrier / spot) / (r - q);
                    return upperRebate * exp(-r * hit);
                } else
                    return upperRebate * exp(-r * tau);
            } else {
                if (type == OptionType.CALL)
                    return max(spot * exp(-q * tau) - strike * exp(-r * tau), 0);
                else
                    return max(strike * exp(-r * tau) - spot * exp(-q * tau), 0);
            }
        }
        double Rebate = rebateAmount(rebateType, upperBarrier, lowerBarrier, upperRebate, lowerRebate, spot, vol, tau,
                r, q);

        //other normal conditions
        double mu = r - q - 0.5 * pow(vol, 2);
        double l = log(upperBarrier / lowerBarrier);
        double d = log(strike / lowerBarrier);
        double x = log(spot / lowerBarrier);
        double eps = 1e-10;

        int steps;
        if ((-2 * log(eps) / tau - pow(mu / vol, 2)) < 0)
            steps = 1;
        else
            steps = (int) sqrt((-2 * log(eps) / tau - pow(mu / vol, 2)) / pow(PI * vol / l, 2)) + 1;
        //truncate to speed up
        if (steps > 10000)
            steps = 10000;

        if (type == OptionType.CALL)
            value = exp(-r * tau) * (lowerBarrier * (Q_term(1, l, steps, vol, l, mu, tau, x)
                    - Q_term(1, d, steps, vol, l, mu, tau, x)) - strike * (Q_term(0, l, steps, vol, l, mu, tau, x)
                    - Q_term(0, d, steps, vol, l, mu, tau, x))) + Rebate;
        else
            value = exp(-r * tau) * (strike * (Q_term(0, d, steps, vol, l, mu, tau, x)
                    - Q_term(0, 0, steps, vol, l, mu, tau, x)) - lowerBarrier * (Q_term(1, d, steps, vol, l, mu, tau, x)
                    - Q_term(1, 0, steps, vol, l, mu, tau, x))) + Rebate;


        if (value / spot < 1e-15)
            value = 0;
        return value;
    }

    /**
     * @param request      PRICE or Greeks(DELTA, GAMMA,etc..), the Greeks are calculated by finite difference method
     * @param type         Option type (CALL or PUT)
     * @param rebateType   Rebate type (PAY_AT_EXPIRY or PAY_WHEN_HIT)
     * @param upperBarrier Upper barrier
     * @param lowerBarrier Lower barrier
     * @param upperRebate  rebate paid at expiry if the upper barrier is hit first during the option's lifetime
     * @param lowerRebate  rebate paid at expiry if the lower barrier is hit first during the option's lifetime
     * @param spot         current price of the underlying asset
     * @param strike       strike price
     * @param vol          volatility
     * @param tau          time to maturity in years
     * @param r            risk free interest rate
     * @param q            annually dividend yield
     * @return return the requested calculation result
     * @throws Exception if the calculation type is not supported
     */

    public static double calc(CalcType request, OptionType type, RebateType rebateType,
                              double upperBarrier, double lowerBarrier, double upperRebate, double lowerRebate,
                              double spot, double strike, double vol, double tau, double r, double q) throws Exception {
        double[] variables = new double[]{spot, strike, vol, tau, r, q};
        FiniteDifference calcFD = new FiniteDifference(
                u -> price(type, rebateType, upperBarrier, lowerBarrier, upperRebate, lowerRebate,
                        u[0], u[1], u[2], u[3], u[4], u[5]));
        double[] Gradients = calcFD.getGradient(variables);
        double[][] Hessian = calcFD.getHessian(variables);
        switch (request) {
            case PRICE: {
                double value = calcFD.getValue(variables);
                if (Double.isNaN(value) || Double.isInfinite(value))
                    throw new Exception("Argument error in analytic pricing formulas for double Knock-out options.");
                else
                    return value;
            }
            case DELTA:
                return Gradients[0];
            case DUAL_DELTA:
                return Gradients[1];
            case VEGA:
                return Gradients[2];
            case THETA:
                return Gradients[3];
            case RHO_R:
                return Gradients[4];
            case RHO_Q:
                return Gradients[5];
            case GAMMA:
                return Hessian[0][0];
            case DUAL_GAMMA:
                return Hessian[1][1];
            case VANNA:
                return Hessian[0][2];
            case CHARM:
                return Hessian[0][3];
            case VOLGA:
                return Hessian[2][2];
            case VETA:
                return Hessian[2][3];
            case VERA_R:
                return Hessian[2][4];
            case VERA_Q:
                return Hessian[2][5];
            case COLOR:
                break;
            case SPEED:
                break;
            case ULTIMA:
                break;
            case ZOMMA:
                break;
        }
        throw new Exception("Calculation type is not supported.");
    }


}
