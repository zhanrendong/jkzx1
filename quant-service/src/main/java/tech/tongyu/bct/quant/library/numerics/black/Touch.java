package tech.tongyu.bct.quant.library.numerics.black;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.numerics.fd.FiniteDifference;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum;

import java.util.Arrays;

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
        return FastMath.sqrt(mu * mu + 2 * r / (vol * vol));
    }

    private static double z(double spot, double barrier, double vol, double T, double lambda) {
        return FastMath.log(barrier / spot) / (vol * FastMath.sqrt(T)) + lambda * vol * FastMath.sqrt(T);
    }

    private static double x_two(double spot, double barrier, double vol, double T, double mu) {
        return FastMath.log(spot / barrier) / (vol * FastMath.sqrt(T)) + (mu + 1) * vol * FastMath.sqrt(T);
    }

    private static double y_two(double spot, double barrier, double vol, double T, double mu) {
        return FastMath.log(barrier / spot) / (vol * FastMath.sqrt(T)) + (mu + 1) * vol * FastMath.sqrt(T);
    }

    private static double B_two(double rebate, double r, double T, double vol, double x2, double phi) {
        return rebate * FastMath.exp(-r * T) * N.cumulativeProbability(phi * x2 - phi * vol * FastMath.sqrt(T));
    }

    private static double B_four(double rebate, double spot, double barrier, double r, double T, double vol, double mu,
                                 double y2, double eta) {
        return rebate * FastMath.exp(-r * T) * FastMath.pow(barrier / spot, 2 * mu)
                * N.cumulativeProbability(eta * y2 - eta * vol * FastMath.sqrt(T));
    }

    private static double A_five(double rebate, double spot, double barrier, double T, double vol, double mu,
                                 double lambda, double z, double eta) {
        return rebate * (FastMath.pow(barrier / spot, mu + lambda) * N.cumulativeProbability(eta * z)
                + FastMath.pow(barrier / spot, mu - lambda)
                * N.cumulativeProbability(eta * z - 2 * eta * lambda * vol * FastMath.sqrt(T)));
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
                                  BarrierDirectionEnum direction, BarrierTypeEnum barrierType,
                                  RebateTypeEnum rebateType) {

        double forward = spot * FastMath.exp((r - q) * T);
        if (direction == BarrierDirectionEnum.DOWN
                && barrierType == BarrierTypeEnum.OUT
                && rebateType == RebateTypeEnum.PAY_WHEN_HIT) {
            if (barrier == 0)
                return 0.0;

            if (spot <= barrier)
                return rebate;

            if (vol == 0) {
                if (forward <= barrier) {
                    double hit = FastMath.log(barrier / spot) / (r - q);
                    return rebate * FastMath.exp(-r * hit);
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
                if (Double.isInfinite(FastMath.pow(barrier / spot, mu + lambda)) ||
                        Double.isInfinite(FastMath.pow(barrier / spot, mu - lambda)))
                    return oneTouch(rebate, barrier, spot, 0, T, r, q, direction, barrierType, rebateType);
                else
                    return A_five(rebate, spot, barrier, T, vol, mu, lambda, z, eta);
            }
        } else if (direction == BarrierDirectionEnum.UP
                && barrierType == BarrierTypeEnum.OUT
                && rebateType == RebateTypeEnum.PAY_WHEN_HIT) {

            if (spot >= barrier)
                return rebate;

            if (vol == 0) {
                if (forward >= barrier) {
                    double hit = FastMath.log(barrier / spot) / (r - q);
                    return rebate * FastMath.exp(-r * hit);
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
                if (Double.isInfinite(FastMath.pow(barrier / spot, mu + lambda)) ||
                        Double.isInfinite(FastMath.pow(barrier / spot, mu - lambda)))
                    return oneTouch(rebate, barrier, spot, 0, T, r, q, direction, barrierType, rebateType);
                else
                    return A_five(rebate, spot, barrier, T, vol, mu, lambda, z, eta);
            }
        } else if (direction == BarrierDirectionEnum.DOWN
                && barrierType == BarrierTypeEnum.OUT
                && rebateType == RebateTypeEnum.PAY_AT_EXPIRY) {
            if (barrier == 0)
                return 0;

            if (spot <= barrier)
                return rebate * FastMath.exp(-r * T);

            if (vol == 0) {
                if (forward <= barrier) {
                    return rebate * FastMath.exp(-r * T);
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
                if (Double.isInfinite(FastMath.pow(barrier / spot, 2 * mu)))
                    return oneTouch(rebate, barrier, spot, 0, T, r, q, direction, barrierType, rebateType);
                else
                    return B_two(rebate, r, T, vol, x2, phi) + B_four(rebate, spot, barrier, r, T, vol, mu, y2, eta);
            }
        }
        //(direction==BarrierDirection.UP_AND_OUT && rebateType== RebateType.PAY_AT_EXPIRY)
        else {
            if (spot >= barrier)
                return rebate * FastMath.exp(-r * T);

            if (vol == 0) {
                if (forward >= barrier) {
                    return rebate * FastMath.exp(-r * T);
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
                if (Double.isInfinite(FastMath.pow(barrier / spot, 2 * mu)))
                    return oneTouch(rebate, barrier, spot, 0, T, r, q, direction, barrierType, rebateType);
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
                                 BarrierDirectionEnum direction, BarrierTypeEnum barrierType) {
        double forward = spot * FastMath.exp((r - q) * T);
        if (direction == BarrierDirectionEnum.DOWN && barrierType == BarrierTypeEnum.OUT) {

            if (spot <= barrier)
                return 0;

            if (vol == 0 || T == 0) {
                if (forward <= barrier)
                    return 0;
                else
                    return rebate * FastMath.exp(-r * T);
            }
            //if (spot > barrier)
            else {
                double eta = 1;
                double phi = 1;
                double b = r - q;
                double mu = mu(b, vol);
                double x2 = x_two(spot, barrier, vol, T, mu);
                double y2 = y_two(spot, barrier, vol, T, mu);
                if (Double.isInfinite(FastMath.pow(barrier / spot, 2 * mu)))
                    return noTouch(rebate, barrier, spot, 0, T, r, q, direction, barrierType);
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
                    return rebate * FastMath.exp(-r * T);
            }
            //if (spot < barrier)
            else {
                double eta = -1;
                double phi = -1;
                double b = r - q;
                double mu = mu(b, vol);
                double x2 = x_two(spot, barrier, vol, T, mu);
                double y2 = y_two(spot, barrier, vol, T, mu);
                if (Double.isInfinite(FastMath.pow(barrier / spot, 2 * mu)))
                    return noTouch(rebate, barrier, spot, 0, T, r, q, direction, barrierType);
                else
                    return B_two(rebate, r, T, vol, x2, phi) - B_four(rebate, spot, barrier, r, T, vol, mu, y2, eta);
            }
        }
    }

    private static double price(double barrier, BarrierDirectionEnum barrierDirection, BarrierTypeEnum barrierType,
            double touchRebate, double noTouchRebate, RebateTypeEnum rebateType,
            double spot, double vol, double t, double r, double q) {
        double pv = 0;
        if (touchRebate != 0) {
            pv += oneTouch(touchRebate, barrier, spot, vol, t, r, q, barrierDirection, barrierType, rebateType);
        }
        if (noTouchRebate != 0) {
            pv += noTouch(noTouchRebate, barrier, spot, vol, t, r, q, barrierDirection, barrierType);
        }
        return pv;
    }

    /**
     *
     * @param request PRICE or Greeks(DELTA, GAMMA,etc..). See {@link CalcTypeEnum}.
     *                The Greeks are calculated by finite difference method
     * @param barrier barrier
     * @param barrierDirection barrier direction. UP or DOWN. See {@link BarrierDirectionEnum}
     * @param barrierType barrier type. OUT or IN. See {@link BarrierTypeEnum}
     * @param touchRebate rebate if barrier is hit
     * @param noTouchRebate rebate if barrier is not hit until maturity
     * @param rebateType rebate type. PAY_WHEN_HIT or PAY_AT_EXPIRY. See {@link RebateTypeEnum}.
     *                  Affects touch rebate only
     * @param spot spot price of underlying asset
     * @param vol volatility
     * @param tau time to maturity in year
     * @param r risk free interest rate
     * @param q annually dividend yield
     * @return the requested value
     * @throws Exception if the request is not supported or input parameters are not valid.
     */
    public static double calc(CalcTypeEnum request, double barrier, BarrierDirectionEnum barrierDirection,
            BarrierTypeEnum barrierType, double touchRebate, double noTouchRebate, RebateTypeEnum rebateType,
            double spot, double vol, double tau, double r, double q) {
        // already knocked out
        if ((barrierDirection == BarrierDirectionEnum.DOWN && spot <= barrier)
                || (barrierDirection == BarrierDirectionEnum.UP && spot >= barrier)) {
            switch (request) {
                case INTRINSIC_VALUE:
                    return rebateType == RebateTypeEnum.PAY_NONE ? 0 : touchRebate;
                case PRICE:
                    return rebateType == RebateTypeEnum.PAY_AT_EXPIRY ? FastMath.exp(-r * tau) * touchRebate
                            : (rebateType == RebateTypeEnum.PAY_WHEN_HIT ? touchRebate
                            : 0);
                case THETA:
                    return rebateType == RebateTypeEnum.PAY_AT_EXPIRY ? -r * FastMath.exp(-r * tau) * touchRebate
                            : 0;
                case RHO_R:
                    return rebateType == RebateTypeEnum.PAY_AT_EXPIRY ? -tau * FastMath.exp(-r * tau) * touchRebate
                            : 0;
                case DELTA:
                case VEGA:
                case RHO_Q:
                case DUAL_DELTA:
                case GAMMA:
                case DUAL_GAMMA:
                case VANNA:
                case CHARM:
                case VOLGA:
                case VETA:
                case VERA_R:
                case VERA_Q:
                    return 0;
                default:
                    throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                            "已敲出期权无法计算：：" + request);
            }
        }

        // active
        double[] variables = new double[]{spot, barrier, vol, tau, r, q};
        FiniteDifference calcFD = new FiniteDifference(
                u -> price(u[1], barrierDirection, barrierType, touchRebate, noTouchRebate, rebateType,
                        u[0], u[2], u[3], u[4], u[5]));
        double pv = calcFD.getValue(variables);
        if (Double.isNaN(pv) || Double.isInfinite(pv)) {
            throw new CustomException(ErrorCode.COMPUTATION_ERROR,
                    "一触即付/不触碰定价公式返回NaN或Infinity。请检查输入是否合理。");
        }
        double[] gradients = new double[6];
        if (Arrays.asList(CalcTypeEnum.DELTA, CalcTypeEnum.VEGA, CalcTypeEnum.THETA,
                CalcTypeEnum.RHO_R, CalcTypeEnum.RHO_Q, CalcTypeEnum.DUAL_DELTA).contains(request)) {
            gradients = calcFD.getGradient(variables);
        }
        double[][] hessian = new double[6][6];
        if (Arrays.asList(CalcTypeEnum.GAMMA, CalcTypeEnum.DUAL_GAMMA, CalcTypeEnum.VANNA, CalcTypeEnum.VOLGA,
                CalcTypeEnum.VETA, CalcTypeEnum.VERA_Q, CalcTypeEnum.VERA_R, CalcTypeEnum.CHARM).contains(request)) {
            hessian = calcFD.getHessian(variables);
        }
        switch (request) {
            case INTRINSIC_VALUE:
                return noTouchRebate;
            case PRICE:
                return pv;
            case DELTA:
                return gradients[0];
            case DUAL_DELTA:
                return gradients[1];
            case VEGA:
                return gradients[2];
            case THETA:
                if (DoubleUtils.smallEnough(tau, DoubleUtils.SMALL_NUMBER))
                    return 0.0;
                else
                    return gradients[3];
            case RHO_R:
                return gradients[4];
            case RHO_Q:
                return gradients[5];
            case GAMMA:
                return hessian[0][0];
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
                        "一触即付/不触碰定价公式不支持计算类型：" + request);
        }
    }
}
