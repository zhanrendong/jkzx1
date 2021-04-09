package tech.tongyu.bct.quant.library.numerics.black;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;

import static java.lang.Math.max;

public class BlackScholes {
    private final static NormalDistribution N = new NormalDistribution();

    /**
     * CALL price by Black-Scholes formula
     *
     * @param S   Underlying price
     * @param K   Strike
     * @param vol Volatility (annualized)
     * @param tau Time to expiry (in years)
     * @param r   Risk free rate (annualized)
     * @param q   Dividend yield/Borrow cost etc. combined (annualized)
     * @return CALL price discounted to valuation date
     */

    public static double call(double S, double K, double vol, double tau, double r, double q) {
        double var = vol * FastMath.sqrt(tau);
        if (DoubleUtils.smallEnough(S, DoubleUtils.SMALL_NUMBER)
                || DoubleUtils.smallEnough(var, DoubleUtils.SMALL_NUMBER))
            return max(S * FastMath.exp(-q * tau) - K * FastMath.exp(-r * tau), 0.0);
        double forward = S * FastMath.exp((r - q) * tau);
        double df = FastMath.exp(-r * tau);

        double lnMoneyness = 0.0;
        if (K < 0.0)
            lnMoneyness = 40.0;
        else
            lnMoneyness = FastMath.log(forward / K);
        double dp = lnMoneyness / var + 0.5 * var;
        double dm = dp - var;
        return df * (forward * N.cumulativeProbability(dp) - K * N.cumulativeProbability(dm));
    }

    /**
     * Calculate the implied vol of an option given its price
     *
     * @param price Option price
     * @param S     Underlying price
     * @param K     Option strike
     * @param tau   Time to expiry (in years)
     * @param r     Risk free rate (annualized)
     * @param q     Dividend yield/Borrow cost etc. combined (annualized)
     * @param type  Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return Implied Black-Scholes vol
     */
    public static double iv(double price, double S, double K, double tau,
                            double r, double q, OptionTypeEnum type) {
        double dfr = FastMath.exp(-r * tau);
        double dfq = FastMath.exp(-q * tau);
        double forward = S * dfq / dfr;
        // always use OTM option
        // convert using parity if necessary
        double priceToUse = price;
        OptionTypeEnum typeToUse = type;
        if (type == OptionTypeEnum.CALL && forward > K) {
            typeToUse = OptionTypeEnum.PUT;
            priceToUse = price - S * dfq + K * dfr;
        }
        if (type == OptionTypeEnum.PUT && forward < K) {
            typeToUse = OptionTypeEnum.CALL;
            priceToUse = price + S * dfq - K * dfr;
        }
        final double p = priceToUse;
        UnivariateFunction func = null;
        if (typeToUse == OptionTypeEnum.CALL) {
            func = new UnivariateFunction() {
                public double value(double x) {
                    return call(S, K, x, tau, r, q) - p;
                }
            };
        } else {
            func = new UnivariateFunction() {
                public double value(double x) {
                    return put(S, K, x, tau, r, q) - p;
                }
            };
        }
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        return solver.solve(100, func, 0.0001, 1.0);
    }

    /**
     * PUT price by Black-Scholes formula
     *
     * @param S   Underlying price
     * @param K   Strike
     * @param vol Volatility (annualized)
     * @param tau Time to expiry (in years)
     * @param r   Risk free rate (annualized)
     * @param q   Dividend yield/Borrow cost etc. combined (annualized)
     * @return put price discounted to valuation date
     */
    public static double put(double S, double K, double vol, double tau, double r, double q) {
        double var = vol * FastMath.sqrt(tau);
        if (DoubleUtils.smallEnough(S, DoubleUtils.SMALL_NUMBER)
                || DoubleUtils.smallEnough(var, DoubleUtils.SMALL_NUMBER))
            return max(S * FastMath.exp(-q * tau) - K * FastMath.exp(-r * tau), 0.0);
        double forward = S * FastMath.exp((r - q) * tau);
        double df = FastMath.exp(-r * tau);

        double lnMoneyness = FastMath.log(forward / K);
        double dp = lnMoneyness / var + 0.5 * var;
        double dm = dp - var;
        return df * (K * N.cumulativeProbability(-dm) - forward * N.cumulativeProbability(-dp));
    }

    /**
     * Option delta
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return Delta
     */
    public static double delta(double S, double K, double vol,
                               double tau, double r, double q, OptionTypeEnum type) {
        if (DoubleUtils.smallEnough(tau, DoubleUtils.SMALL_NUMBER)) {
            if (type == OptionTypeEnum.CALL)
                return S >= K ? 1.0 : 0.0;
            else
                return S >= K ? 0.0 : -1.0;
        }
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        double delta = N.cumulativeProbability(dp);
        if (type == OptionTypeEnum.PUT)
            delta = delta - 1;
        return delta * dfq;
    }

    /**
     * Option dual delta (dPrice/dK)
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return Dual Delta
     */
    public static double dualDelta(double S, double K, double vol,
                                   double tau, double r, double q, OptionTypeEnum type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dm = FastMath.log(forward / K) / var - 0.5 * var;
        double delta = -N.cumulativeProbability(dm);
        if (type == OptionTypeEnum.PUT)
            delta = delta + 1;
        return delta * dfr;
    }

    /**
     * Option gamma
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return Gamma
     */
    public static double gamma(double S, double K, double vol,
                               double tau, double r, double q, OptionTypeEnum type) {
        if (DoubleUtils.smallEnough(tau, DoubleUtils.SMALL_NUMBER)) {
            return 0.0;
        }
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        return dfq * N.density(dp) / S / var;
    }

    /**
     * Option dual gamma (d^2V/dK^2)
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return Dual Gamma
     */
    public static double dualGamma(double S, double K, double vol,
                                   double tau, double r, double q, OptionTypeEnum type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        return dfr * N.density(dp - var) / K / var;
    }

    /**
     * Option vega
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return Vega
     */
    public static double vega(double S, double K, double vol,
                              double tau, double r, double q, OptionTypeEnum type) {
        if (DoubleUtils.smallEnough(tau, DoubleUtils.SMALL_NUMBER)) {
            return 0.0;
        }
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        return dfq * N.density(dp) * S * FastMath.sqrt(tau);
    }

    /**
     * Option theta
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return theta
     */
    public static double theta(double S, double K, double vol,
                               double tau, double r, double q, OptionTypeEnum type) {
        if (DoubleUtils.smallEnough(tau, DoubleUtils.SMALL_NUMBER)) {
            return 0.0;
        }
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        double dm = dp - var;
        double ret = -0.5 * dfq * S * N.density(dp) * vol / FastMath.sqrt(tau);
        if (type == OptionTypeEnum.CALL) {
            ret -= r * K * dfr * N.cumulativeProbability(dm);
            ret += q * S * dfq * N.cumulativeProbability(dp);
        } else {
            ret += r * K * dfr * N.cumulativeProbability(-dm);
            ret -= q * S * dfq * N.cumulativeProbability(-dp);
        }
        return -ret;
    }

    /**
     * Option rho (with respect to the risk free rate)
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return Rho (with respect to the risk free rate)
     */
    public static double rho_r(double S, double K, double vol,
                               double tau, double r, double q, OptionTypeEnum type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        double dm = dp - var;
        switch (type) {
            case CALL:
                return K * tau * dfr * N.cumulativeProbability(dm);
            case PUT:
                return -K * tau * dfr * N.cumulativeProbability(-dm);
            default:
                return Double.NaN;
        }
    }

    /**
     * Option vera (with respect to the risk free rate: d^2V/d^VoldR)
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return Vera (with respect to the risk free rate)
     */
    public static double vera_r(double S, double K, double vol,
                                double tau, double r, double q, OptionTypeEnum type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        return -S * dfq * tau * dp * N.density(dp) / vol;
    }

    /**
     * Option rho (with respect to the dividend yield/borrow rate)
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return Rho (with respect to the risk free rate)
     */
    public static double rho_q(double S, double K, double vol,
                               double tau, double r, double q, OptionTypeEnum type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        if (type == OptionTypeEnum.CALL) {
            return -S * tau * dfq * N.cumulativeProbability(dp);
        } else {
            return S * tau * dfq * N.cumulativeProbability(-dp);
        }
    }

    /**
     * Option vera (with respect to dividend yield/borrow rate: d^2V/d^VoldQ)
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return Vera (with respect to the risk free rate)
     */
    public static double vera_q(double S, double K, double vol,
                                double tau, double r, double q, OptionTypeEnum type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        return -S * dfq * tau * N.density(dp) * (-dp / vol + FastMath.sqrt(tau));
    }

    /**
     * Option vanna (dV^2/dSdVol)
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return Vanna (with respect to spot and to vol)
     */
    public static double vanna(double S, double K, double vol,
                               double tau, double r, double q, OptionTypeEnum type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        double dm = dp - var;
        return -dfq * N.density(dp) * dm / vol;
    }

    /**
     * Option volga/vomma (dV^2/dVoldVol)
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return Volga (2nd order derivative with respect to vol)
     */
    public static double volga(double S, double K, double vol,
                               double tau, double r, double q, OptionTypeEnum type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        double dm = dp - var;
        return S * dfq * N.density(dp) * FastMath.sqrt(tau) * dm * dp / vol;
    }

    /**
     * Option veta (dV^2/dVoldTau)
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return Veta
     */
    public static double veta(double S, double K, double vol,
                              double tau, double r, double q, OptionTypeEnum type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        double dm = dp - var;
        return -S * dfq * N.density(dp) * FastMath.sqrt(tau) * (q + (r - q) * dp / var - 0.5 * (1.0 + dm * dp) / tau);
    }

    /**
     * Option charm (dV^2/dSdTau)
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return Vharm
     */
    public static double charm(double S, double K, double vol,
                               double tau, double r, double q, OptionTypeEnum type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        double dm = dp - var;
        double ret = -dfq * N.density(dp) * 0.5 * (2.0 * (r - q) * tau - dm * var) / (tau * var);
        switch (type) {
            case CALL:
                return -(q * dfq * N.cumulativeProbability(dp) + ret);
            case PUT:
                return -(-q * dfq * N.cumulativeProbability(-dp) + ret);
            default:
                return Double.NaN;
        }
    }

    /**
     * Option speed (d^3V/dS^3)
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return speed
     */
    public static double speed(double S, double K, double vol,
                               double tau, double r, double q, OptionTypeEnum type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        double dm = dp - var;
        return -dfq * N.density(dp) / (S * S * var) * (dp / var + 1.0);
    }

    /**
     * Option color (d^3V/dS^3dTau)
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return color
     */
    public static double color(double S, double K, double vol,
                               double tau, double r, double q, OptionTypeEnum type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        double dm = dp - var;
        return -dfq * N.density(dp) * 0.5 / (S * tau * var) *
                (2.0 * q * tau + 1.0 + (2.0 * (r - q) * tau - dm * var) * dp / var);
    }

    /**
     * Option ultima (d^3V/d^3Vol)
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return color
     */
    public static double ultima(double S, double K, double vol,
                                double tau, double r, double q, OptionTypeEnum type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        double dm = dp - var;
        return -vega(S, K, vol, tau, r, q, type) / (vol * vol) * (dp * dm * (1.0 - dp * dm) + dp * dp + dm * dm);
    }

    /**
     * Option zomma (d^3V/dS^2dVol)
     *
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return zomma
     */
    public static double zomma(double S, double K, double vol,
                               double tau, double r, double q, OptionTypeEnum type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        double dm = dp - var;
        return gamma(S, K, vol, tau, r, q, type) * (dp * dm - 1.0) / vol;
    }

    /**
     * Black-Scholes calculator
     * <p>
     * Note: THETA here is defined as the price sensitivity to <i>Time to Expiry</i>. This definition is
     * consistent with this calculator's input \tau. To get the sensitivity to <i>Expiry</i>, simply
     * negate the result returned by THETA. This holds for any time related derivative calculations.
     * For example VETA.
     * </p>
     *
     * @param req  Calculation request. See {@link CalcTypeEnum}
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return Requested calculation result
     */
    public static double calc(CalcTypeEnum req, double S, double K, double vol, double tau,
                              double r, double q, OptionTypeEnum type) {
        switch (req) {
            case INTRINSIC_VALUE:
                if (type == OptionTypeEnum.CALL) {
                    return S >= K ? S - K : 0.0;
                } else {
                    return S <= K ? K - S : 0.0;
                }
            case PRICE:
                switch (type) {
                    case CALL:
                        return call(S, K, vol, tau, r, q);
                    case PUT:
                        return put(S, K, vol, tau, r, q);
                    default:
                        throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                                "Black-Scholes公式仅支持CALL（看涨）和PUT（看跌）期权。输入为: " + type);
                }
            case DELTA:
                return delta(S, K, vol, tau, r, q, type);
            case DUAL_DELTA:
                return dualDelta(S, K, vol, tau, r, q, type);
            case GAMMA:
                return gamma(S, K, vol, tau, r, q, type);
            case DUAL_GAMMA:
                return dualGamma(S, K, vol, tau, r, q, type);
            case VEGA:
                return vega(S, K, vol, tau, r, q, type);
            case THETA:
                return theta(S, K, vol, tau, r, q, type);
            case RHO_R:
                return rho_r(S, K, vol, tau, r, q, type);
            case VERA_R:
                return vera_r(S, K, vol, tau, r, q, type);
            case RHO_Q:
                return rho_q(S, K, vol, tau, r, q, type);
            case VERA_Q:
                return vera_q(S, K, vol, tau, r, q, type);
            case VANNA:
                return vanna(S, K, vol, tau, r, q, type);
            case VOLGA:
                return volga(S, K, vol, tau, r, q, type);
            case VETA:
                return veta(S, K, vol, tau, r, q, type);
            case CHARM:
                return charm(S, K, vol, tau, r, q, type);
            case COLOR:
                return color(S, K, vol, tau, r, q, type);
            case SPEED:
                return speed(S, K, vol, tau, r, q, type);
            case ULTIMA:
                return ultima(S, K, vol, tau, r, q, type);
            case ZOMMA:
                return zomma(S, K, vol, tau, r, q, type);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID, "Black-Scholes不支持计算类型：" + req);
        }
    }

    /**
     * Forward pricer. A single linear product.
     *
     * @param req Calculation request. See {@link CalcTypeEnum}
     * @param S   Underlye price
     * @param K   Forward's strike
     * @param tau Time to delivery
     * @param r   Risk free rate
     * @param q   Dividend rate
     * @return Calculation result
     */
    public static double calcForward(CalcTypeEnum req, double S, double K, double tau,
                                     double r, double q) {
        switch (req) {
            case INTRINSIC_VALUE:
                return S - K;
            case PRICE:
                return S * FastMath.exp(-q * tau) - K * FastMath.exp(-r * tau);
            case DELTA:
                return FastMath.exp(-q * tau);
            case DUAL_DELTA:
                return -FastMath.exp(-r * tau);
            case GAMMA:
            case DUAL_GAMMA:
            case VEGA:
            case VERA_R:
            case VERA_Q:
            case VANNA:
            case VOLGA:
            case VETA:
            case COLOR:
            case SPEED:
            case ULTIMA:
            case ZOMMA:
                return 0.;
            case THETA:
                return -q * S * FastMath.exp(-q * tau) + r * K * FastMath.exp(-r * tau);
            case RHO_R:
                return tau * K * FastMath.exp(-r * tau);
            case RHO_Q:
                return -tau * S * FastMath.exp(-q * tau);
            case CHARM:
                return -q * FastMath.exp(-q * tau);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID, "Black-Scholes不支持计算类型：" + req);
        }
    }

    public static double calcCash(CalcTypeEnum req, double amount) {
        switch (req) {
            case INTRINSIC_VALUE:
            case PRICE:
                return amount;
            default:
                return 0.;
        }
    }

    public static double calcCash(CalcTypeEnum req, double amount, double tau, double r) {
        switch (req) {
            case INTRINSIC_VALUE:
                return amount;
            case PRICE:
                return amount * FastMath.exp(-r * tau);
            case RHO_R:
                return -tau * amount * FastMath.exp(-r * tau);
            case THETA:
                return r * amount * FastMath.exp(-r * tau);
            default:
                return 0.;
        }
    }
}
