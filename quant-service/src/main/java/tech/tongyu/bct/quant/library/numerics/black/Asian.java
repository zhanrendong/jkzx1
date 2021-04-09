package tech.tongyu.bct.quant.library.numerics.black;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.numerics.fd.FiniteDifference;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;

import java.util.Arrays;
import java.util.List;

public class Asian {
    private final static NormalDistribution Norm = new NormalDistribution();

    private static double price(CalcTypeEnum request, OptionTypeEnum type,
                                double[] fixings, double[] scheduleTimeToExpiry, double[] weights,
                                double spot, double strike, double vol, double tau, double r, double q) {
        int N = scheduleTimeToExpiry.length - 1;
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
            ti[i] = tau - scheduleTimeToExpiry[i];
            n += weights[i];
        }

        for (int i = 0; i <= M; i++) {
            sa += weights[i] * fixings[i];
        }

        for (int i = M + 1; i <= N; i++) {
            E1 += spot / (n + 1) * weights[i] * FastMath.exp(b * ti[i]);
            E1R += spot / (n + 1) * weights[i] * ti[i] * FastMath.exp(b * ti[i]);
            for (int j = M + 1; j <= N; j++) {
                E2 += FastMath.pow(spot, 2) /
                        FastMath.pow(n + 1, 2) * weights[i] * weights[j] * FastMath.exp(b * (ti[i] + ti[j])
                        + FastMath.pow(vol, 2) * FastMath.min(ti[i], ti[j]));
                E2V += FastMath.pow(spot, 2) /
                        FastMath.pow(n + 1, 2) * 2 * vol * FastMath.min(ti[i], ti[j]) * weights[i] * weights[j] *
                        FastMath.exp(b * (ti[i] + ti[j]) + FastMath.pow(vol, 2) * FastMath.min(ti[i], ti[j]));
                E2R += FastMath.pow(spot, 2) /
                        FastMath.pow(n + 1, 2) * weights[i] * weights[j] * (ti[i] + ti[j]) *
                        FastMath.exp(b * (ti[i] + ti[j]) + FastMath.pow(vol, 2) * FastMath.min(ti[i], ti[j]));
            }
        }

        double K = strike - sa / (n + 1);
        double df = FastMath.exp(-r * tau);

        // special case 1: all fixings are known
        if (M == N) {
            switch (request) {
                case PRICE:
                    return df * (OptionTypeEnum.CALL == type ? FastMath.max(-K, 0.0) : FastMath.max(K, 0.0));
                case THETA:
                    return r * df * (OptionTypeEnum.CALL == type ? FastMath.max(-K, 0.0) : FastMath.max(K, 0.0));
                case DELTA:
                case GAMMA:
                case VEGA:
                    return 0.0;
                case RHO_R:
                    return -tau * df * (OptionTypeEnum.CALL == type ? FastMath.max(-K, 0.0) : FastMath.max(K, 0.0));
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
                    if (type == OptionTypeEnum.CALL) return df * (E1 - K);
                    else return 0;
                }

                case DELTA: {
                    if (type == OptionTypeEnum.CALL) return df * E1 / spot;
                    else return 0;
                }

                case GAMMA:
                    return 0;

                case THETA: {
                    if (type == OptionTypeEnum.CALL) return r * df * (E1 - K) - b * df * E1;
                    else return 0;
                }

                case VEGA:

                    return 0;

                case RHO_R: {
                    if (type == OptionTypeEnum.CALL) return -tau * df * (E1 - K) + df * E1R;
                    else return 0;
                }

                case RHO_Q: {
                    if (type == OptionTypeEnum.CALL) return -df * E1R;
                    else return 0;
                }

                default:
                    return Double.NaN;
            }
        }


        double v = FastMath.sqrt(FastMath.log(E2) - 2.0 * FastMath.log(E1));
        double d1 = (0.5 * FastMath.log(E2) - FastMath.log(K)) / v;
        double d2 = d1 - v;


        switch (request) {

            case PRICE: {
                if (type == OptionTypeEnum.CALL)
                    return df * (E1 * Norm.cumulativeProbability(d1) - K * Norm.cumulativeProbability(d2));
                else
                    return df * (K * Norm.cumulativeProbability(-d2) - E1 * Norm.cumulativeProbability(-d1));
            }

            case DELTA: {
                if (type == OptionTypeEnum.CALL)
                    return df * E1 * Norm.cumulativeProbability(d1) / spot;
                else
                    return -df * E1 * Norm.cumulativeProbability(-d1) / spot;
            }

            case GAMMA: {
                return df * E1 * Norm.density(d1) / (v * FastMath.pow(spot, 2));
            }

            case THETA: {
                if (type == OptionTypeEnum.CALL)
                    return r * df * (E1 * Norm.cumulativeProbability(d1) - K * Norm.cumulativeProbability(d2))
                            - df * E1 * (b * Norm.cumulativeProbability(d1)
                            + 0.5 * FastMath.pow(vol, 2) / v * Norm.density(d1));
                else
                    return r * df * (K * Norm.cumulativeProbability(-d2) - E1 * Norm.cumulativeProbability(-d1))
                            - df * E1 * (0.5 * FastMath.pow(vol, 2) / v * Norm.density(d1)
                            - b * Norm.cumulativeProbability(-d1));
            }
            case VEGA: {
                return df * 0.5 * E1 * E2V * Norm.density(d1) / v / E2;
            }

            case RHO_R: {
                if (type == OptionTypeEnum.CALL)
                    return -tau * df * (E1 * Norm.cumulativeProbability(d1) - K * Norm.cumulativeProbability(d2))
                            + df * (E1R * Norm.cumulativeProbability(d1) + 0.5 / v * E1 * Norm.density(d1)
                            * (E2R / E2 - 2 * E1R / E1));
                else
                    return -tau * df * (K * Norm.cumulativeProbability(-d2) - E1 * Norm.cumulativeProbability(-d1))
                            + df * (-E1R * Norm.cumulativeProbability(-d1) + 0.5 / v * E1 * Norm.density(d1)
                            * (E2R / E2 - 2 * E1R / E1));
            }
            case RHO_Q: {
                if (type == OptionTypeEnum.CALL)
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
     * @param fixings    past fixings
     * @param scheduleTimeToExpiry   time periods from observation dates to expiry in year
     * @param weights    averaging weights
     * @param spot       current price of the underlying asset
     * @param strike     strike price
     * @param tau        time to maturity in years
     * @param r          risk free interest rate
     * @param q          annually dividend yield
     * @param vol        volatility
     * @return return the requested calculation result
     * @throws Exception if the calculation type is not supported
     */
    public static double calc(CalcTypeEnum request, OptionTypeEnum type, double strike,
                              List<Double> fixings, List<Double> scheduleTimeToExpiry, List<Double> weights,
                              double spot, double vol, double tau, double r, double q) {
        double[] times = scheduleTimeToExpiry.stream().mapToDouble(Double::doubleValue).toArray();
        double[] ws = weights.stream().mapToDouble(Double::doubleValue).toArray();
        double[] fixs = fixings.stream().mapToDouble(Double::doubleValue).toArray();
        double[] variables = new double[]{spot, strike, vol, tau, r, q};
        FiniteDifference calcFD = new FiniteDifference(u -> price(
                CalcTypeEnum.PRICE, type, fixs, times, ws,
                u[0], u[1], u[2], u[3], u[4], u[5]));

        double[] gradients = new double[6];
        if (request == CalcTypeEnum.DUAL_DELTA) {
            gradients = calcFD.getGradient(variables);
        }
        double[][] hessian = new double[6][6];
        if (Arrays.asList(CalcTypeEnum.DUAL_GAMMA, CalcTypeEnum.VANNA, CalcTypeEnum.CHARM, CalcTypeEnum.VOLGA,
                CalcTypeEnum.VETA, CalcTypeEnum.VERA_R, CalcTypeEnum.VERA_Q).contains(request)) {
            hessian = calcFD.getHessian(variables);
        }

        switch (request) {
            case INTRINSIC_VALUE:
                double sum = 0;
                double totalWeight = 0;
                for (int i = 0; i < fixs.length; i++) {
                    sum += fixs[i] * ws[i];
                    totalWeight += ws[i];
                }
                return type == OptionTypeEnum.CALL ? Math.max(sum / totalWeight - strike, 0)
                        : Math.max(strike - sum / totalWeight, 0);
            case PRICE:
                return calcFD.getValue(variables);
            case DELTA:
                return price(CalcTypeEnum.DELTA, type, fixs, times, ws,
                        spot, strike, vol, tau, r, q);
            case DUAL_DELTA:
                return gradients[1];
            case VEGA:
                return price(CalcTypeEnum.VEGA, type, fixs, times, ws,
                        spot, strike, vol, tau, r, q);
            // BlackPricer expect THETA calculated with respect to the time to expiry.
            case THETA:
                return -price(CalcTypeEnum.THETA, type, fixs, times, ws,
                        spot, strike, vol, tau, r, q);
            case RHO_R:
                return price(CalcTypeEnum.RHO_R, type, fixs, times, ws,
                        spot, strike, vol, tau, r, q);
            case RHO_Q:
                return price(CalcTypeEnum.RHO_Q, type, fixs, times, ws,
                        spot, strike, vol, tau, r, q);
            case GAMMA:
                return price(CalcTypeEnum.GAMMA, type, fixs, times, ws,
                        spot, strike, vol, tau, r, q);
            case DUAL_GAMMA:
                return hessian[1][1];
            case VANNA:
                return hessian[0][2];
            case CHARM:
                return hessian[0][3];
            case VOLGA:
                return hessian[2][2];
            case VETA:
             return hessian[2][3];
            case VERA_R:
                return hessian[2][4];
            case VERA_Q:
                return hessian[2][5];
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        "亚式定价公式不支持计算类型：" + request.toString());
        }
    }
}
