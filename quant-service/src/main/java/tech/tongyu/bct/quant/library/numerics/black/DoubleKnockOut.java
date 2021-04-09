package tech.tongyu.bct.quant.library.numerics.black;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.numerics.fd.FiniteDifference;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum;

/**
 * Created by Liao Song on 16-7-26.
 * reference: Pricing Double Barrier Options: An Analytic Approach by Antoon Pelsser (1997)
 */
public class DoubleKnockOut {
    private static double Lambda(double mu, int k, double vol, double l) {
        return 0.5 * (FastMath.pow(mu / vol, 2) + FastMath.pow(k * FastMath.PI * vol / l, 2));
    }

    private static double Q_term(double a, double y, int n, double vol, double l, double mu, double tau, double x) {
        int k;
        double sum = 0;
        for (k = 1; k <= n; k++) {
            sum += 2 / l * FastMath.exp(mu / FastMath.pow(vol, 2) * (y - x) + a * y)
                    * FastMath.exp(-Lambda(mu, k, vol, l) * tau) * FastMath.sin(k * FastMath.PI * x / l) *
                    (((mu / FastMath.pow(vol, 2) + a) * FastMath.sin(k * FastMath.PI * y / l)
                            - k * FastMath.PI / l * FastMath.cos(k * FastMath.PI * y / l)) /
                            (FastMath.pow(mu / FastMath.pow(vol, 2) + a, 2)
                                    + FastMath.pow(k * FastMath.PI / l, 2)));
        }
        return sum;
    }

    private static double rebateAmount(RebateTypeEnum rebateType, double upperBarrier, double lowerBarrier, double upperRebate,
                                       double lowerRebate, double spot, double vol, double tau, double r, double q) {


        double mu = r - q - 0.5 * FastMath.pow(vol, 2);
        double l = FastMath.log(upperBarrier / lowerBarrier);
        double x = FastMath.log(spot / lowerBarrier);
        double eps = 1e-10;
        //pay at expiry
        if (rebateType == RebateTypeEnum.PAY_AT_EXPIRY) {


            if (FastMath.abs(mu / vol / vol) > FastMath.log(Double.MAX_VALUE) || vol == 0) {
                double forward = spot * FastMath.exp((r - q) * tau);
                if (forward <= lowerBarrier)
                    return lowerRebate * FastMath.exp(-r * tau);
                else if (forward >= upperBarrier)
                    return upperRebate * FastMath.exp(-r * tau);
                else
                    return 0;
            }
            //normal case
            int steps;
            if ((-2 * FastMath.log(eps) / tau - FastMath.pow(mu / vol, 2)) < 0)
                steps = 1;
            else
                steps = (int) FastMath.sqrt((-2 * FastMath.log(eps) / tau
                        - FastMath.pow(mu / vol, 2)) / FastMath.pow(FastMath.PI * vol / l, 2)) + 1;
            //truncate to speed up
            if (steps > 10000)
                steps = 10000;

            double sum1 = 0;
            double sum2 = 0;

            for (int k = 1; k <= steps; k++) {
                sum1 += FastMath.pow(vol / l, 2) * FastMath.exp(-Lambda(mu, k, vol, l) * tau) /
                        Lambda(mu, k, vol, l) * k * FastMath.PI * FastMath.sin(k * FastMath.PI * (l - x) / l);
                sum2 += FastMath.pow(vol / l, 2) * FastMath.exp(-Lambda(mu, k, vol, l) * tau) /
                        Lambda(mu, k, vol, l) * k * FastMath.PI * FastMath.sin(k * FastMath.PI * x / l);
            }

            double P_plus = FastMath.exp(mu * (l - x) / FastMath.pow(vol, 2))
                    * (FastMath.sinh(mu * x / FastMath.pow(vol, 2)) / FastMath.sinh(mu * l
                    / FastMath.pow(vol, 2)) - sum1);
            double P_minus = FastMath.exp(-mu * x / FastMath.pow(vol, 2))
                    * (FastMath.sinh(mu * (l - x) / FastMath.pow(vol, 2)) / FastMath.sinh(mu * l
                    / FastMath.pow(vol, 2)) - sum2);
            return FastMath.exp(-r * tau) * (upperRebate * P_plus + lowerRebate * P_minus);
        }
        //pay when hit
        else if (rebateType == RebateTypeEnum.PAY_WHEN_HIT) {
            double mu_prime = FastMath.sqrt(mu * mu + 2.0 * vol * vol * r);

            if (FastMath.abs(mu_prime / vol / vol) > FastMath.log(Double.MAX_VALUE) || vol == 0) {
                double forward = spot * FastMath.exp((r - q) * tau);
                if (forward <= lowerBarrier) {
                    double hit = FastMath.log(lowerBarrier / spot) / (r - q);
                    return lowerRebate * FastMath.exp(-r * hit);
                } else if (forward >= upperBarrier) {
                    double hit = FastMath.log(upperBarrier / spot) / (r - q);
                    return upperRebate * FastMath.exp(-r * hit);
                } else
                    return 0;
            }
            int steps;
            if ((-2 * FastMath.log(eps) / tau - FastMath.pow(mu / vol, 2)) < 0)
                steps = 1;
            else
                steps = (int) FastMath.sqrt((-2 * FastMath.log(eps) / tau - FastMath.pow(mu_prime / vol, 2))
                        / FastMath.pow(FastMath.PI * vol / l, 2)) + 1;
            //truncate to speed up
            if (steps > 10000)
                steps = 10000;

            double sum1 = 0;
            double sum2 = 0;

            for (int k = 1; k <= steps; k++) {
                sum1 += FastMath.pow(vol / l, 2) * FastMath.exp(-Lambda(mu_prime, k, vol, l) * tau) /
                        Lambda(mu_prime, k, vol, l) * k * FastMath.PI * FastMath.sin(k * FastMath.PI * (l - x) / l);
                sum2 += FastMath.pow(vol / l, 2) * FastMath.exp(-Lambda(mu_prime, k, vol, l) * tau) /
                        Lambda(mu_prime, k, vol, l) * k * FastMath.PI * FastMath.sin(k * FastMath.PI * x / l);
            }

            double P_plus = FastMath.exp(mu * (l - x) / FastMath.pow(vol, 2)) * (FastMath.sinh(mu_prime * x /
                    FastMath.pow(vol, 2)) / FastMath.sinh(mu_prime * l
                    / FastMath.pow(vol, 2)) - sum1);
            double P_minus = FastMath.exp(-mu * x / FastMath.pow(vol, 2)) * (FastMath.sinh(mu_prime * (l - x) /
                    FastMath.pow(vol, 2)) / FastMath.sinh(mu_prime * l
                    / FastMath.pow(vol, 2)) - sum2);
            return upperRebate * P_plus + lowerRebate * P_minus;

        }
        //pay nothing
        else
            return 0;

    }

    private static double price(OptionTypeEnum type, RebateTypeEnum rebateType,
                                double upperBarrier, double lowerBarrier, double upperRebate, double lowerRebate,
                                double spot, double strike, double vol, double tau, double r,
                                double q) {
        //Up-and-Out-Down-and-Out CALL
        //payoff: c=max(S-X,0) if L<S<U before T else rebate*exp(-r*Tau)
        //if tau =0 and not knocked out
        double value;
        if (tau == 0 && spot > lowerBarrier && spot < upperBarrier) {
            if (type == OptionTypeEnum.CALL)
                return FastMath.max(spot - strike, 0);
            else
                return FastMath.max(strike - spot, 0);
        }
        //if already knocked out
        if (spot <= lowerBarrier) {
            if (rebateType == RebateTypeEnum.PAY_AT_EXPIRY)
                return lowerRebate * FastMath.exp(-r * tau);
            else
                return lowerRebate;
        } else if (spot >= upperBarrier) {
            if (rebateType == RebateTypeEnum.PAY_AT_EXPIRY)
                return upperRebate * FastMath.exp(-r * tau);
            else
                return upperRebate;
        }
        // when vol ~ 0 compared with r and q
        double forward = spot * FastMath.exp((r - q) * tau);
        if (FastMath.abs((r - q) / vol / vol) > FastMath.log(Double.MAX_VALUE) || vol == 0) {
            if (forward <= lowerBarrier) {
                if (rebateType == RebateTypeEnum.PAY_WHEN_HIT) {
                    double hit = FastMath.log(lowerBarrier / spot) / (r - q);
                    return lowerRebate * FastMath.exp(-r * hit);
                } else
                    return lowerRebate * FastMath.exp(-r * tau);
            } else if (forward >= upperBarrier) {
                if (rebateType == RebateTypeEnum.PAY_WHEN_HIT) {
                    double hit = FastMath.log(upperBarrier / spot) / (r - q);
                    return upperRebate * FastMath.exp(-r * hit);
                } else
                    return upperRebate * FastMath.exp(-r * tau);
            } else {
                if (type == OptionTypeEnum.CALL)
                    return FastMath.max(spot * FastMath.exp(-q * tau) - strike * FastMath.exp(-r * tau), 0);
                else
                    return FastMath.max(strike * FastMath.exp(-r * tau) - spot * FastMath.exp(-q * tau), 0);
            }
        }
        double Rebate = rebateAmount(rebateType, upperBarrier, lowerBarrier, upperRebate, lowerRebate, spot, vol, tau,
                r, q);

        //other normal conditions
        double mu = r - q - 0.5 * FastMath.pow(vol, 2);
        double l = FastMath.log(upperBarrier / lowerBarrier);
        double d = FastMath.log(strike / lowerBarrier);
        double x = FastMath.log(spot / lowerBarrier);
        double eps = 1e-10;

        int steps;
        if ((-2 * FastMath.log(eps) / tau - FastMath.pow(mu / vol, 2)) < 0)
            steps = 1;
        else
            steps = (int) FastMath.sqrt((-2 * FastMath.log(eps) / tau - FastMath.pow(mu / vol, 2))
                    / FastMath.pow(FastMath.PI * vol / l, 2)) + 1;
        //truncate to speed up
        if (steps > 10000)
            steps = 10000;

        if (type == OptionTypeEnum.CALL)
            value = FastMath.exp(-r * tau) * (lowerBarrier * (Q_term(1, l, steps, vol, l, mu, tau, x)
                    - Q_term(1, d, steps, vol, l, mu, tau, x)) - strike * (Q_term(0, l, steps, vol, l, mu, tau, x)
                    - Q_term(0, d, steps, vol, l, mu, tau, x))) + Rebate;
        else
            value = FastMath.exp(-r * tau) * (strike * (Q_term(0, d, steps, vol, l, mu, tau, x)
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

    public static double calc(CalcTypeEnum request, OptionTypeEnum type, RebateTypeEnum rebateType,
                              double upperBarrier, double lowerBarrier, double upperRebate, double lowerRebate,
                              double spot, double strike, double vol, double tau, double r, double q) {
        double[] variables = new double[]{spot, strike, vol, tau, r, q};
        FiniteDifference calcFD = new FiniteDifference(
                u -> price(type, rebateType, upperBarrier, lowerBarrier, upperRebate, lowerRebate,
                        u[0], u[1], u[2], u[3], u[4], u[5]));
        double[] Gradients = calcFD.getGradient(variables);
        double[][] Hessian = calcFD.getHessian(variables);
        switch (request) {
            case INTRINSIC_VALUE:
                if (spot >= upperBarrier || spot <= lowerBarrier) {
                    throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                            "根据输入标的物价格判断，期权已敲出，不存在内在价值。");
                }
                if (type == OptionTypeEnum.CALL) {
                    return spot >= strike ? spot - strike : 0.0;
                } else {
                    return spot <=strike ? strike - spot : 0.0;
                }
            case PRICE: {
                double value = calcFD.getValue(variables);
                if (Double.isNaN(value) || Double.isInfinite(value))
                    throw new CustomException(ErrorCode.COMPUTATION_ERROR,
                            "Black-Scholes双敲出公式返回NaN或Infinity。请检查输入是否合理。");
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
            case SPEED:
            case ULTIMA:
            case ZOMMA:
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        "Black-Scholes双敲出公式不支持计算类型：" + request);
        }
    }
}
