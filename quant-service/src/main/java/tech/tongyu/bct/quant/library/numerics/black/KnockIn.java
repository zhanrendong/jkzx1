package tech.tongyu.bct.quant.library.numerics.black;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.numerics.fd.FiniteDifference;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;

public class KnockIn {
    private final static NormalDistribution N = new NormalDistribution();

    private static double mu(double b, double v) {
        return (b - v * v / 2) / (v * v);
    }

    private static double x1(double S, double X, double T, double v, double b) {
        return FastMath.log(S / X) / (v * Math.sqrt(T)) + (1 + mu(b, v)) * v * Math.sqrt(T);
    }

    private static double x2(double S, double H, double T, double v, double b) {
        return FastMath.log(S / H) / (v * Math.sqrt(T)) + (1 + mu(b, v)) * v * Math.sqrt(T);
    }

    private static double y1(double S, double X, double H, double T, double v, double b) {
        return FastMath.log(H * H / (S * X)) / (v * Math.sqrt(T)) + (1 + mu(b, v)) * v * Math.sqrt(T);
    }

    private static double y2(double S, double H, double T, double v, double b) {
        return FastMath.log(H / S) / (v * Math.sqrt(T)) + (1 + mu(b, v)) * v * Math.sqrt(T);
    }

    private static double A(double S, double X, double T, double r, double b, double v, double phi) {
        return phi * S * FastMath.exp((b - r) * T) * N.cumulativeProbability(phi * x1(S, X, T, v, b))
                - phi * X * FastMath.exp(-r * T) * N.cumulativeProbability(phi * x1(S, X, T, v, b)
                - phi * v * Math.sqrt(T));
    }

    private static double B(double S, double X, double H, double T, double r, double b, double v, double phi) {
        return phi * S * FastMath.exp((b - r) * T) * N.cumulativeProbability(phi * x2(S, H, T, v, b))
                - phi * X * FastMath.exp(-r * T) * N.cumulativeProbability(phi * x2(S, H, T, v, b)
                - phi * v * Math.sqrt(T));
    }

    private static double C(double S, double X, double H, double T, double r, double b, double v, double phi,
                            double eta) {
        return phi * S * FastMath.exp((b - r) * T) * Math.pow(H / S, 2 * (mu(b, v) + 1))
                * N.cumulativeProbability(eta * y1(S, X, H, T, v, b))
                - phi * X * FastMath.exp(-r * T) * Math.pow(H / S, 2 * mu(b, v))
                * N.cumulativeProbability(eta * y1(S, X, H, T, v, b) - eta * v * Math.sqrt(T));
    }

    private static double D(double S, double X, double H, double T, double r, double b, double v, double phi,
                            double eta) {
        return phi * S * FastMath.exp((b - r) * T) * Math.pow(H / S, 2 * (mu(b, v) + 1))
                * N.cumulativeProbability(eta * y2(S, H, T, v, b))
                - phi * X * FastMath.exp(-r * T) * Math.pow(H / S, 2 * mu(b, v))
                * N.cumulativeProbability(eta * y2(S, H, T, v, b) - eta * v * Math.sqrt(T));
    }


    private static double price(OptionTypeEnum type,
                                BarrierDirectionEnum direction, BarrierTypeEnum barrierType,
                                double barrier, double rebate,
                                double spot, double strike, double vol, double tau, double r, double q) {
        double b;
        b = r - q;
        double eta, phi;
        double forward = spot * FastMath.exp((r - q) * tau);
        double rebateAmount = Touch.noTouch(rebate, barrier, spot, vol, tau, r, q, direction, BarrierTypeEnum.OUT);


        if (direction == BarrierDirectionEnum.DOWN
                && barrierType == BarrierTypeEnum.IN
                && type == OptionTypeEnum.CALL) {
            if (spot <= barrier)
                return BlackScholes.calc(CalcTypeEnum.PRICE, spot, strike, vol, tau, r, q, type) + rebateAmount;

            eta = 1;
            phi = 1;
            if (vol == 0. || tau == 0.) {
                if (forward <= barrier)
                    return FastMath.max(spot * FastMath.exp(-q * tau) - strike * FastMath.exp(-r * tau), 0);
                else
                    return rebateAmount;
            } else if (strike > barrier) {
                if (Double.isInfinite(FastMath.pow(barrier / spot, 2 * mu(b, vol))))
                    return price(type, direction, barrierType, barrier, 0, spot, strike, 0, tau, r, q) + rebateAmount;
                else
                    return C(spot, strike, barrier, tau, r, b, vol, phi, eta) + rebateAmount;
            } else {
                if (Double.isInfinite(FastMath.pow(barrier / spot, 2 * mu(b, vol))))
                    return price(type, direction, barrierType, barrier, 0, spot, strike, 0, tau, r, q) + rebateAmount;
                else
                    return A(spot, strike, tau, r, b, vol, phi) - B(spot, strike, barrier, tau, r, b, vol, phi)
                            + D(spot, strike, barrier, tau, r, b, vol, phi, eta) + rebateAmount;
            }
        } else if (direction == BarrierDirectionEnum.UP
                && barrierType == BarrierTypeEnum.IN
                && type == OptionTypeEnum.CALL) {
            if (spot >= barrier)
                return BlackScholes.calc(CalcTypeEnum.PRICE, spot, strike, vol, tau, r, q, type) + rebateAmount;

            eta = -1;
            phi = 1;
            if (vol == 0 || tau == 0) {
                if (forward >= barrier)
                    return FastMath.max(spot * FastMath.exp(-q * tau) - strike * FastMath.exp(-r * tau), 0);
                else
                    return rebateAmount;
            } else if (strike > barrier)
                return A(spot, strike, tau, r, b, vol, phi) + rebateAmount;
            else {
                if (Double.isInfinite(FastMath.pow(barrier / spot, 2 * mu(b, vol))))
                    return price(type, direction, barrierType, barrier, 0, spot, strike, 0, tau, r, q) + rebateAmount;
                else
                    return B(spot, strike, barrier, tau, r, b, vol, phi) - C(spot, strike, barrier, tau, r, b, vol,
                            phi, eta) + D(spot, strike, barrier, tau, r, b, vol, phi, eta) + rebateAmount;
            }
        } else if (direction == BarrierDirectionEnum.DOWN
                && barrierType == BarrierTypeEnum.IN
                && type == OptionTypeEnum.PUT) {
            if (spot <= barrier)
                return BlackScholes.calc(CalcTypeEnum.PRICE, spot, strike, vol, tau, r, q, type) + rebateAmount;

            eta = 1;
            phi = -1;
            if (vol == 0 || tau == 0) {
                if (forward <= barrier)
                    return FastMath.max(strike * FastMath.exp(-r * tau) - spot * FastMath.exp(-q * tau), 0);
                else
                    return rebateAmount;
            } else if (strike > barrier) {
                if (Double.isInfinite(FastMath.pow(barrier / spot, 2 * mu(b, vol))))
                    return price(type, direction, barrierType, barrier, 0, spot, strike, 0, tau, r, q) + rebateAmount;
                else
                    return B(spot, strike, barrier, tau, r, b, vol, phi) - C(spot, strike, barrier, tau, r, b, vol,
                            phi, eta) + D(spot, strike, barrier, tau, r, b, vol, phi, eta) + rebateAmount;
            } else
                return A(spot, strike, tau, r, b, vol, phi) + rebateAmount;
        }
        //if (direction == BarrierDirection.UP_AND_IN && type == OptionType.PUT)
        else {
            if (spot >= barrier)
                return BlackScholes.calc(CalcTypeEnum.PRICE, spot, strike, vol, tau, r, q, type) + rebateAmount;

            eta = -1;
            phi = -1;
            if (vol == 0. || tau == 0.) {
                if (forward >= barrier)
                    return FastMath.max(strike * FastMath.exp(-r * tau) - spot * FastMath.exp(-q * tau), 0);
                else
                    return rebateAmount;
            } else if (strike > barrier) {
                if (Double.isInfinite(FastMath.pow(barrier / spot, 2 * mu(b, vol))))
                    return price(type, direction, barrierType, barrier, 0, spot, strike, 0, tau, r, q) + rebateAmount;
                else
                    return A(spot, strike, tau, r, b, vol, phi) - B(spot, strike, barrier, tau, r, b, vol, phi)
                            + D(spot, strike, barrier, tau, r, b, vol, phi, eta) + rebateAmount;
            } else {
                if (Double.isInfinite(FastMath.pow(barrier / spot, 2 * mu(b, vol))))
                    return price(type, direction, barrierType, barrier, 0, spot, strike, 0, tau, r, q) + rebateAmount;
                else
                    return C(spot, strike, barrier, tau, r, b, vol, phi, eta) + rebateAmount;
            }
        }
    }

    /**
     * calculates the requested calculation type (PRICE or Greeks) for knock in options(barrier is continuously monitored
     * throughout the option's lifetime). The rebate is paid at expiry when the option is not knocked in till the expiry.
     *
     * @param request   calculation type (PRICE or Greeks)
     * @param type      option type( CALL or PUT)
     * @param direction barrier direction(for knock in options: DOWN_AND_IN or UP_AND_IN)
     * @param barrier   barrier
     * @param rebate    rebate
     * @param spot      spot
     * @param strike    strike
     * @param vol       volatility
     * @param tau       time to maturity in years
     * @param r         risk free interest rate
     * @param q         annually dividend yield
     * @return requested calculation result
     */
    public static double calc(CalcTypeEnum request, OptionTypeEnum type,
                              BarrierDirectionEnum direction, BarrierTypeEnum barrierType,
                              double barrier, double rebate, double spot, double strike,
                              double vol, double tau, double r, double q) {
        if (barrierType != BarrierTypeEnum.IN) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "输入非敲入期权");
        }
        double[] variables = new double[]{spot, strike, vol, tau, r, q};
        FiniteDifference calcFD = new FiniteDifference(u -> price(type, direction, barrierType, barrier, rebate,
                u[0], u[1], u[2], u[3], u[4], u[5]));
        double[] Gradients = calcFD.getGradient(variables);
        double[][] Hessian = calcFD.getHessian(variables);
        switch (request) {
            case INTRINSIC_VALUE:
                if (direction == BarrierDirectionEnum.UP) {
                    if (spot < barrier) {
                        return 0.0;
                    } else {
                        return type == OptionTypeEnum.CALL ? FastMath.max(spot - strike, 0.0)
                                : FastMath.max(strike - spot, 0.0);
                    }
                } else  {
                    if (spot > barrier) {
                        return 0.0;
                    } else {
                        return type == OptionTypeEnum.CALL ? FastMath.max(spot - strike, 0.0)
                                : FastMath.max(strike - spot, 0.0);
                    }

                }
            case PRICE: {
                double value = calcFD.getValue(variables);
                if (Double.isInfinite(value) || Double.isNaN(value))
                    throw new CustomException(ErrorCode.COMPUTATION_ERROR,
                            "Black-Scholes敲入公式返回NaN或Infinity。请检查输入是否合理。");
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
                        "Black-Scholes敲入公式不支持计算类型：" + request);
        }
    }
}
