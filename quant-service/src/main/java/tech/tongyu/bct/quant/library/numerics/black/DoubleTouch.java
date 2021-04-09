package tech.tongyu.bct.quant.library.numerics.black;

import io.vavr.Tuple2;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.numerics.fd.FiniteDifference;
import tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum;

import java.util.Arrays;

/**
 * Analytical pricing of double one-touch/double no-touch.
 * Reference: Pricing Double Barrier Options: An Analytic Approach by Antoon Pelsser (1997)
 */
public class DoubleTouch {
    private static final double pi2 = 9.869604401089358;
    private static final double eps = 1e-10;
    private static final double small = 0.001;

    /**
     *
     * @param request PRICE or Greeks(DELTA, GAMMA,etc..). See {@link CalcTypeEnum}.
     *               The Greeks are calculated by finite difference method
     * @param rebateType When rebate is paid. See {@link RebateTypeEnum}
     * @param lowerBarrier lower barrier
     * @param upperBarrier upper barrier
     * @param lowerRebate rebate if lower barrier is hit first
     * @param upperRebate rebate if upper rebate is hit first
     * @param noTouchRebate rebate if neither barrier is hit until maturity
     * @param spot spot price of underlying asset
     * @param vol volatility
     * @param t time to maturity in years
     * @param r risk free interest rate
     * @param q annually dividend yield
     * @return the requested value
     * @throws Exception if the request is not supported
     */
    public static double calc(CalcTypeEnum request, RebateTypeEnum rebateType, double lowerBarrier,
            double upperBarrier, double lowerRebate, double upperRebate, double noTouchRebate,
            double spot, double vol, double t, double r, double q) {
        double[] variables = new double[]{spot, vol, t, r, q};
        FiniteDifference calcFD = new FiniteDifference(
                u -> price(rebateType, lowerBarrier, upperBarrier, lowerRebate, upperRebate, noTouchRebate,
                        u[0], u[1], u[2], u[3], u[4]));
        double pv = calcFD.getValue(variables);
        if (Double.isNaN(pv) || Double.isInfinite(pv)) {
            throw new CustomException(ErrorCode.COMPUTATION_ERROR,
                    "双触碰/双不触碰定价公式返回NaN或Infinity。请检查输入是否合理。");
        }
        double[] gradients = new double[5];
        if (Arrays.asList(CalcTypeEnum.DELTA, CalcTypeEnum.VEGA, CalcTypeEnum.THETA,
                CalcTypeEnum.RHO_R, CalcTypeEnum.RHO_Q).contains(request)) {
            gradients = calcFD.getGradient(variables);
        }
        double[][] hessian = new double[5][5];
        if (Arrays.asList(CalcTypeEnum.GAMMA, CalcTypeEnum.VANNA, CalcTypeEnum.VOLGA, CalcTypeEnum.VETA,
                CalcTypeEnum.VERA_Q, CalcTypeEnum.VERA_R, CalcTypeEnum.CHARM).contains(request)) {
            hessian = calcFD.getHessian(variables);
        }
        switch (request) {
            case INTRINSIC_VALUE:
                return spot <= lowerBarrier ? lowerRebate
                        : (spot >= upperBarrier ? upperRebate
                        : noTouchRebate);
            case PRICE:
                return pv;
            case DELTA:
                return gradients[0];
            case GAMMA:
                return hessian[0][0];
            case VEGA:
                return gradients[1];
            case THETA:
                return gradients[2];
            case RHO_R:
                return gradients[3];
            case RHO_Q:
                return gradients[4];
            case VANNA:
                return hessian[0][1];
            case CHARM:
                return hessian[0][2];
            case VOLGA:
                return hessian[1][1];
            case VETA:
                return hessian[1][2];
            case VERA_R:
                return hessian[1][3];
            case VERA_Q:
                return hessian[1][4];
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        "双触碰/双不触碰定价公式不支持计算类型：" + request);
        }
    }

    private static double price(RebateTypeEnum rebateType, double lowerBarrier, double upperBarrier,
            double lowerRebate, double upperRebate, double noTouchRebate,
            double spot, double vol, double t, double r, double q) {
        if (spot <= lowerBarrier) {
            return lowerRebate;
        } else if (spot >= upperBarrier) {
            return upperRebate;
        }

        double mu = r - q - 0.5 * vol * vol;
        double l = FastMath.log(upperBarrier / lowerBarrier);
        double x = FastMath.log(spot / lowerBarrier);

        if (rebateType == RebateTypeEnum.PAY_AT_EXPIRY) {
            if (DoubleUtils.smallEnough(vol * vol, small * mu)) {
                double forward = spot * FastMath.exp((r - q) * t);
                return FastMath.exp(-r * t)
                        * (forward <= lowerBarrier ? lowerRebate
                        : (forward >= upperBarrier ? upperRebate
                        : noTouchRebate));
            }
            Tuple2<Double, Double> hitProb = hitProbatility(x, t, l, mu, vol);
            // real hit probability
            double upperProb = FastMath.exp(mu * (l - x) / vol / vol) * hitProb._1;
            double lowerProb = FastMath.exp(-mu * x / vol / vol) * hitProb._2;
            return FastMath.exp(-r * t) * (upperRebate * upperProb + lowerRebate * lowerProb
                    + noTouchRebate * (1 - upperProb - lowerProb));
        } else if (rebateType == RebateTypeEnum.PAY_WHEN_HIT) {
            double mu2 = FastMath.sqrt(mu * mu + 2 * vol * vol * r);
            if (DoubleUtils.smallEnough(vol * vol, small * mu2)) {
                double forward = spot * FastMath.exp((r - q) * t);
                return forward <= lowerBarrier
                        ? lowerRebate * FastMath.exp(-r * FastMath.log(lowerBarrier / spot) / (r - q))
                        : (forward >= upperBarrier
                        ? upperRebate * FastMath.exp(-r * FastMath.log(upperBarrier / spot) / (r - q))
                        : noTouchRebate * FastMath.exp(-r * t));
            }
            Tuple2<Double, Double> hitProb = hitProbatility(x, t, l , mu2, vol);
            // one touch value
            double pv = upperRebate * FastMath.exp(mu * (l-x) / vol / vol) * hitProb._1
                    + lowerRebate * FastMath.exp(-mu * x / vol / vol) * hitProb._2;
            if (noTouchRebate != 0) { // no touch value
                hitProb = hitProbatility(x, t, l, mu, vol);
                pv += noTouchRebate * FastMath.exp(-r * t) * (1 - hitProb._1 - hitProb._2);
            }
            return pv;
        } else { // PAY_NONE
            return 0;
        }
    }

    // Not exactly the probability of hitting one of the barrier.
    // The first of returned tuple for upper, second for lower.
    private static Tuple2<Double, Double> hitProbatility(double x, double t, double l, double mu, double vol) {
        double xU = FastMath.PI * (l - x) / l;
        double xL = FastMath.PI * x / l;
        double vol2 = vol * vol;
        int seriesNum = 1 + (int) FastMath.sqrt(FastMath.max(1.,
                (-2 * FastMath.log(eps) / t - mu * mu / vol2) * l * l / vol2 / pi2));
        // at most 100 terms
        seriesNum = FastMath.min(seriesNum, 100);
        double seriesU = 0;
        double seriesL = 0;
        for (int k = 1; k <= seriesNum; k++) {
            double lambdaK = 0.5 * (mu * mu / vol2 + pi2 * k * k * vol2 / l / l);
            double eLambdaK = FastMath.exp(-lambdaK * t) / lambdaK * k * FastMath.PI;
            seriesU += eLambdaK * FastMath.sin(k * xU);
            seriesL += eLambdaK * FastMath.sin(k * xL);
        }
        double probU = FastMath.sinh(mu * x / vol2) / FastMath.sinh(mu * l / vol2)
                - vol2 / l / l * seriesU;
        double probL = FastMath.sinh(mu * (l - x) / vol2) / FastMath.sinh(mu * l / vol2)
                - vol2 / l / l * seriesL;
        return new Tuple2<>(probU, probL);
    }
}
