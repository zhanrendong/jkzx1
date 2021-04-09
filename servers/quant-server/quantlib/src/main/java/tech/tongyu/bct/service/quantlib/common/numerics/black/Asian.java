package tech.tongyu.bct.service.quantlib.common.numerics.black;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.numerics.fd.FiniteDifference;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.lang.Math.*;

/**
 * Created by Liao Song on 16-7-19.
 * Asian option pricing formulas by moment matching method
 * reference: Pricing European average rate currency options by Edmond Levy (1992)
 */
public class Asian {
    private final static NormalDistribution Norm = new NormalDistribution();

    private static double price(CalcType request, OptionType type, LocalDateTime t,
                                double[] fixings, LocalDateTime[] schedule, double[] weights, double daysInYear,
                                double spot, double strike, double vol, double tau, double r, double q) {
        int N = schedule.length - 1;
        double n = -1;
        int M = fixings.length - 1;


        /*if ((M == 0) && (fixings[0] == 0)) {
            M = -1;
        }*/

        double[] ti = new double[N + 1];
        double E1 = 0;
        double E2 = 0;
        double E2V = 0;
        double E1R = 0;
        double E2R = 0;
        double sa = 0;
        double b = r - q;

        for (int i = 0; i <= N; i++) {
            ti[i] = t.until(schedule[i], ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
            n += weights[i];
        }


        for (int i = 0; i <= M; i++) {
            sa += weights[i] * fixings[i];
        }


        for (int i = M + 1; i <= N; i++) {
            E1 += spot / (n + 1) * weights[i] * exp(b * ti[i]);
            E1R += spot / (n + 1) * weights[i] * ti[i] * exp(b * ti[i]);
            for (int j = M + 1; j <= N; j++) {
                E2 += pow(spot, 2) / pow(n + 1, 2) * weights[i] * weights[j] * exp(b * (ti[i] + ti[j])
                        + pow(vol, 2) * min(ti[i], ti[j]));
                E2V += pow(spot, 2) / pow(n + 1, 2) * 2 * vol * min(ti[i], ti[j]) * weights[i] * weights[j] *
                        exp(b * (ti[i] + ti[j]) + pow(vol, 2) * min(ti[i], ti[j]));
                E2R += pow(spot, 2) / pow(n + 1, 2) * weights[i] * weights[j] * (ti[i] + ti[j]) *
                        exp(b * (ti[i] + ti[j]) + pow(vol, 2) * min(ti[i], ti[j]));
            }
        }

        double K = strike - sa / (n + 1);
        double df = exp(-r * tau);

        // special case 1: all fixings are known
        if (M == N) {
            switch (request) {
                case PRICE:
                    return df * (OptionType.CALL == type ? FastMath.max(-K, 0.0) : FastMath.max(K, 0.0));
                case DELTA:
                case GAMMA:
                case VEGA:
                    return 0.0;
                case RHO_R:
                    return -tau * df * (OptionType.CALL == type ? FastMath.max(-K, 0.0) : FastMath.max(K, 0.0));
                case RHO_Q:
                    return 0.0;
                default:
                    return Double.NaN;
            }
        }
        // special case 2: effective strike is negative
        if (K <= 0.0) {
            switch (request) {
                case PRICE: {
                    if (type == OptionType.CALL) return df * (E1 - K);
                    else return 0;
                }

                case DELTA: {
                    if (type == OptionType.CALL) return df * E1 / spot;
                    else return 0;
                }

                case GAMMA:
                    return 0;

                case THETA: {
                    if (type == OptionType.CALL) return r * df * (E1 - K) - b * df * E1;
                    else return 0;
                }

                case VEGA:

                    return 0;

                case RHO_R: {
                    if (type == OptionType.CALL) return -tau * df * (E1 - K) + df * E1R;
                    else return 0;
                }

                case RHO_Q: {
                    if (type == OptionType.CALL) return -df * E1R;
                    else return 0;
                }

                default:
                    return Double.NaN;
            }
        }


        double v = sqrt(log(E2) - 2.0 * log(E1));
        double d1 = (0.5 * log(E2) - log(K)) / v;
        double d2 = d1 - v;


        switch (request) {

            case PRICE: {
                if (type == OptionType.CALL)
                    return df * (E1 * Norm.cumulativeProbability(d1) - K * Norm.cumulativeProbability(d2));
                else
                    return df * (K * Norm.cumulativeProbability(-d2) - E1 * Norm.cumulativeProbability(-d1));
            }

            case DELTA: {
                if (type == OptionType.CALL)
                    return df * E1 * Norm.cumulativeProbability(d1) / spot;
                else
                    return -df * E1 * Norm.cumulativeProbability(-d1) / spot;
            }

            case GAMMA: {
                return df * E1 * Norm.density(d1) / (v * pow(spot, 2));
            }

            case THETA: {
                if (type == OptionType.CALL)
                    return r * df * (E1 * Norm.cumulativeProbability(d1) - K * Norm.cumulativeProbability(d2))
                            - df * E1 * (b * Norm.cumulativeProbability(d1) + 0.5 * pow(vol, 2) / v * Norm.density(d1));
                else
                    return r * df * (K * Norm.cumulativeProbability(-d2) - E1 * Norm.cumulativeProbability(-d1))
                            - df * E1 * (0.5 * pow(vol, 2) / v * Norm.density(d1) - b * Norm.cumulativeProbability(-d1));
            }
            case VEGA: {
                return df * 0.5 * E1 * E2V * Norm.density(d1) / v / E2;
            }

            case RHO_R: {
                if (type == OptionType.CALL)
                    return -tau * df * (E1 * Norm.cumulativeProbability(d1) - K * Norm.cumulativeProbability(d2))
                            + df * (E1R * Norm.cumulativeProbability(d1) + 0.5 / v * E1 * Norm.density(d1)
                            * (E2R / E2 - 2 * E1R / E1));
                else
                    return -tau * df * (K * Norm.cumulativeProbability(-d2) - E1 * Norm.cumulativeProbability(-d1))
                            + df * (-E1R * Norm.cumulativeProbability(-d1) + 0.5 / v * E1 * Norm.density(d1)
                            * (E2R / E2 - 2 * E1R / E1));
            }
            case RHO_Q: {
                if (type == OptionType.CALL)
                    return df * (-E1R * Norm.cumulativeProbability(d1) + 0.5 / v * E1 * Norm.density(d1)
                            * (-E2R / E2 + 2 * E1R / E1));
                else
                    return df * (E1R * Norm.cumulativeProbability(-d1) + 0.5 / v * E1 * Norm.density(d1)
                            * (-E2R / E2 + 2 * E1R / E1));
            }
            //other greeks will be updated later
            default:
                return Double.NaN;
        }


    }


    /**
     * @param request    PRICE or Greeks(DELTA, GAMMA,etc..)
     * @param type       Option type (CALL or PUT)
     * @param t          valuation date
     * @param fixings    past fixings
     * @param schedule   scheduled period for calculating the average spot
     * @param weights    averaging weights
     * @param daysInYear pre-specified number of days in a year
     * @param spot       current price of the underlying asset
     * @param strike     strike price
     * @param tau        time to maturity in years
     * @param r          risk free interest rate
     * @param q          annually dividend yield
     * @param vol        volatility
     * @return return the requested calculation result
     * @throws Exception if the calculation type is not supported
     */
    public static double calc(CalcType request, OptionType type, LocalDateTime t,
                              double[] fixings, LocalDateTime[] schedule, double[] weights, double daysInYear,
                              double spot, double strike, double vol, double tau, double r, double q) throws Exception {
        double[] variables = new double[]{spot, strike, vol, tau, r, q};
        FiniteDifference calcFD = new FiniteDifference(u -> price(CalcType.PRICE, type, t, fixings, schedule, weights,
                daysInYear, u[0], u[1], u[2], u[3], u[4], u[5]));

        double[] Gradients = calcFD.getGradient(variables);
        double[][] Hessian = calcFD.getHessian(variables);

        switch (request) {
            case PRICE:
                return calcFD.getValue(variables);
            case DELTA:
                return price(CalcType.DELTA, type, t, fixings, schedule, weights, daysInYear, spot, strike, vol, tau, r, q);
            case DUAL_DELTA:
                return Gradients[1];
            case VEGA:
                return price(CalcType.VEGA, type, t, fixings, schedule, weights, daysInYear, spot, strike, vol, tau, r, q);
            // THETA of Asian option should be calculated with respect to the valuation time not the time to expiry.
            case THETA:
                return price(CalcType.THETA, type, t, fixings, schedule, weights, daysInYear, spot, strike, vol, tau, r, q);
            case RHO_R:
                return price(CalcType.RHO_R, type, t, fixings, schedule, weights, daysInYear, spot, strike, vol, tau, r, q);
            case RHO_Q:
                return price(CalcType.RHO_Q, type, t, fixings, schedule, weights, daysInYear, spot, strike, vol, tau, r, q);
            case GAMMA:
                return price(CalcType.GAMMA, type, t, fixings, schedule, weights, daysInYear, spot, strike, vol, tau, r, q);
            case DUAL_GAMMA:
                return Hessian[1][1];
            case VANNA:
                return Hessian[0][2];
            // case CHARM:
            // return Hessian[0][3];
            case VOLGA:
                return Hessian[2][2];
            //case VETA:
            // return Hessian[2][3];
            case VERA_R:
                return Hessian[2][4];
            case VERA_Q:
                return Hessian[2][5];
            default:
                throw new Exception("Calculation type is not supported.");
        }
    }


}




