package tech.tongyu.bct.quant.library.numerics.black;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.numerics.fd.FiniteDifference;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;

public class American {
    private static NormalDistribution Norm = new NormalDistribution();

    private static double d1(double spot, double strike, double tau, double r, double q, double vol) {
        double b = r - q;

        return (FastMath.log(spot / strike) +
                (b + 0.5 * FastMath.pow(vol, 2)) * tau) / (vol * FastMath.sqrt(tau));
    }

    private static double GS(OptionTypeEnum type, double spot, double strike,
                             double tau, double r, double q, double vol) {
        double b = r - q;
        double M = 2 * r / FastMath.pow(vol, 2);
        double N = 2 * b / FastMath.pow(vol, 2);

        double K = 1 - FastMath.exp(-r * tau);
        double q1 = 0.5 * (-N + 1 - FastMath.sqrt(FastMath.pow(N - 1, 2) + 4 * M / K));
        double q2 = 0.5 * (-N + 1 + FastMath.sqrt(FastMath.pow(N - 1, 2) + 4 * M / K));
        if (type == OptionTypeEnum.CALL)
            return spot - strike - BlackScholes.calc(CalcTypeEnum.PRICE, spot, strike, vol, tau, r, q, type) -
                    (1 - FastMath.exp((b - r) * tau)
                            * Norm.cumulativeProbability(d1(spot, strike, tau, r, q, vol))) * spot / q2;
        else
            return strike - spot - BlackScholes.calc(CalcTypeEnum.PRICE, spot, strike, vol, tau, r, q, type) +
                    (1 - FastMath.exp((b - r) * tau)
                            * Norm.cumulativeProbability(-d1(spot, strike, tau, r, q, vol))) * spot / q1;
    }

    private static double GSP(OptionTypeEnum type, double spot,
                              double strike, double tau, double r, double q, double vol) {
        double b = r - q;
        double M = 2 * r / FastMath.pow(vol, 2);
        double N = 2 * b / FastMath.pow(vol, 2);

        double K = 1 - FastMath.exp(-r * tau);
        double q1 = 0.5 * (-N + 1 - FastMath.sqrt(FastMath.pow(N - 1, 2) + 4 * M / K));
        double q2 = 0.5 * (-N + 1 + FastMath.sqrt(FastMath.pow(N - 1, 2) + 4 * M / K));
        if (type == OptionTypeEnum.CALL)
            return (1 - FastMath.exp((b - r) * tau) * Norm.cumulativeProbability(d1(spot, strike, tau, r, q, vol)))
                    * (1 - 1 / q2) + FastMath.exp((b - r) * tau) * Norm.density(d1(spot, strike, tau, r, q, vol))
                    / (vol * FastMath.sqrt(tau)) / q2;
        else
            return (1 - FastMath.exp((b - r) * tau) * Norm.cumulativeProbability(-d1(spot, strike, tau, r, q, vol)))
                    * (1 / q1 - 1) + FastMath.exp((b - r) * tau) * Norm.density(-d1(spot, strike, tau, r, q, vol))
                    / (vol * FastMath.sqrt(tau)) / q1;
    }

    private static double CriticalSpot(OptionTypeEnum type,
                                       double strike, double tau, double r, double q, double vol) {
        double eps = 1e-5;
        double spot;
        double b = r - q;
        double M = 2 * r / FastMath.pow(vol, 2);
        double N = 2 * b / FastMath.pow(vol, 2);
        if (type == OptionTypeEnum.CALL) {
            double q2_inf = 0.5 * (-N + 1 + FastMath.sqrt(FastMath.pow(N - 1, 2) + 4 * M));
            double s_inf = strike / (1 - 1 / q2_inf);
            double h2 = -(b * tau + 2 * vol * FastMath.sqrt(tau)) * (strike / (s_inf - strike));
            spot = strike + (s_inf - strike) * (1 - FastMath.exp(h2));
            int count = 0;
            while (FastMath.abs(GS(type, spot, strike, tau, r, q, vol)) / strike > eps && spot > 0) {
                spot = spot - GS(type, spot, strike, tau, r, q, vol) / GSP(type, spot, strike, tau, r, q, vol);
                if(++count > 50) {
                    throw new CustomException(ErrorCode.MAX_ITERATION_REACHED, "美式期权定价：未在50次迭代内收敛到临界价格");
                }
            }
        } else {
            double q1_inf = 0.5 * (-N + 1 - FastMath.sqrt(FastMath.pow(N - 1, 2) + 4 * M));
            double s_inf = strike / (1 - 1 / q1_inf);
            double h1 = (b * tau - 2 * vol * FastMath.sqrt(tau)) * (strike / (strike - s_inf));
            spot = s_inf + (strike - s_inf) * FastMath.exp(h1);
            int count = 0;
            while (FastMath.abs(GS(type, spot, strike, tau, r, q, vol)) / strike > eps && spot > 0) {
                spot = spot - GS(type, spot, strike, tau, r, q, vol) / GSP(type, spot, strike, tau, r, q, vol);
                if(++count > 50) {
                    throw new CustomException(ErrorCode.MAX_ITERATION_REACHED, "美式期权定价：未在50次迭代内收敛到临界价格");
                }
            }
        }
        return spot;
    }

    private static double price(OptionTypeEnum optionType,
                                double spot, double strike, double vol, double tau, double r, double q) {


        double b = r - q;
        double M = 2 * r / FastMath.pow(vol, 2);
        double N = 2 * b / FastMath.pow(vol, 2);

        double K = 1 - FastMath.exp(-r * tau);

        // temporary fix:
        // to handle the case r = 0 => K = 0 => M / K becomes undefined
        // use small r expansion
        double mOverK = 0.0;
        if (FastMath.abs(K)<1e-14)
            mOverK = 2.0/(tau * vol * vol);
        else
            mOverK = M / K;

        double q1 = 0.5 * (-N + 1 - FastMath.sqrt(FastMath.pow(N - 1, 2) + 4 * mOverK));
        double q2 = 0.5 * (-N + 1 + FastMath.sqrt(FastMath.pow(N - 1, 2) + 4 * mOverK));


        double s_critical;
        if (optionType == OptionTypeEnum.CALL) {
            if (b < 0 || q > 0) {
                try {
                    s_critical = CriticalSpot(optionType, strike, tau, r, q, vol);
                } catch (CustomException e) {
                    if (e.getErrorCode() == ErrorCode.MAX_ITERATION_REACHED) {
                        return BlackScholes.calc(CalcTypeEnum.PRICE, spot, strike, vol, tau, r, q, optionType);
                    } else {
                        throw e;
                    }
                }
                double A2 = (s_critical / q2) * (1 - FastMath.exp((b - r) * tau)
                        * Norm.cumulativeProbability(d1(s_critical, strike, tau, r, q, vol)));
                if (spot < s_critical)
                    return BlackScholes.calc(CalcTypeEnum.PRICE, spot, strike, vol, tau, r, q, optionType)
                            + A2 * FastMath.pow(spot / s_critical, q2);
                else
                    return spot - strike;
            } else
                return BlackScholes.calc(CalcTypeEnum.PRICE, spot, strike, vol, tau, r, q, optionType);
        } else {
            if (b > 0 || q > 0) {
                try {
                    s_critical = CriticalSpot(optionType, strike, tau, r, q, vol);
                } catch (CustomException e) {
                    if (e.getErrorCode() == ErrorCode.MAX_ITERATION_REACHED) {
                        return BlackScholes.calc(CalcTypeEnum.PRICE, spot, strike, vol, tau, r, q, optionType);
                    } else {
                        throw e;
                    }
                }
                if (s_critical <= 0) {  // transform put to call
                    return price(OptionTypeEnum.CALL, strike, spot, vol, tau, q, r);
                }
                double A1 = -(s_critical / q1) * (1 - FastMath.exp((b - r) * tau)
                        * Norm.cumulativeProbability(-d1(s_critical, strike, tau, r, q, vol)));
                if (spot > s_critical)
                    return BlackScholes.calc(CalcTypeEnum.PRICE, spot, strike, vol, tau, r, q, optionType)
                            + A1 * FastMath.pow(spot / s_critical, q1);
                else
                    return strike - spot;

            } else {
                return BlackScholes.calc(CalcTypeEnum.PRICE, spot, strike, vol, tau, r, q, optionType);
            }
        }
    }

    /**
     * calculates price and Greeks of American options by B-A Whaley approximation methods
     *
     * @param request PRICE or Greeks
     * @param type    option type CALL or PUT
     * @param spot    current price of the underlying asset
     * @param strike  strike
     * @param vol     volatility
     * @param tau     time to maturity in years
     * @param r       risk free interest rate
     * @param q       volatility
     * @return requested calculation result
     * @throws Exception unsupported calculation type
     */
    public static double calc(CalcTypeEnum request, OptionTypeEnum type,
                              double spot, double strike, double vol, double tau, double r, double q) {
        double[] variables = new double[]{spot, strike, vol, tau, r, q};
        FiniteDifference calcFD = new FiniteDifference(u -> price(type, u[0], u[1], u[2], u[3], u[4], u[5]));
        double[] Gradients = calcFD.getGradient(variables);
        double[][] Hessian = calcFD.getHessian(variables);
        switch (request) {
            case INTRINSIC_VALUE:
                if (type == OptionTypeEnum.CALL) {
                    return spot >= strike ? spot - strike : 0.0;
                } else {
                    return spot <= strike ? strike - spot : 0.0;
                }
            case PRICE:
                return calcFD.getValue(variables);
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
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        "未支持计算类型：" + request);
        }
    }
}
