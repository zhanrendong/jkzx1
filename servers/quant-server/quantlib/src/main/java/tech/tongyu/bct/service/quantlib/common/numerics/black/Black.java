package tech.tongyu.bct.service.quantlib.common.numerics.black;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;

import static java.lang.Math.max;

/**
 * Standard Black formulas for European calls and puts
 *
 * @author Lu Lu
 * @since 2016-06-15
 */
public class Black {
    private final static NormalDistribution N = new NormalDistribution();

    /**
     * Call price by Black formula
     *
     * @param S   Underlying price
     * @param K   Strike
     * @param vol Volatility (annualized)
     * @param tau Time to expiry (in years)
     * @param r   Risk free rate (annualized)
     * @param q   Dividend yield/Borrow cost etc. combined (annualized)
     * @return Call price discounted to valuation date
     */
    public static double call(double S, double K, double vol, double tau, double r, double q) {
        if (vol == 0.0 || tau == 0.0 || S < Constants.SMALL_NUMBER)
            return max(S * FastMath.exp(-q * tau) - K * FastMath.exp(-r * tau), 0.0);
        double forward = S * FastMath.exp((r - q) * tau);
        double df = FastMath.exp(-r * tau);
        double var = vol * FastMath.sqrt(tau);
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
     * @param type  Option type (CALL or PUT). See {@link OptionType}
     * @return Implied Black vol
     */
    @BctQuantApi(name = "qlBlackIV", description = "Get Black implied vol given option price",
            argDescriptions = {"Price", "Spot", "Strike", "Time to expiry", "Discount rate", "Dividend yield",
                    "Option Type (CALL/PUT)"},
            argNames = {"price", "S", "K", "tau", "r", "q", "type"},
            argTypes = {"Double", "Double", "Double", "Double", "Double", "Double", "Enum"},
            retName = "iv", retDescription = "Implied vol", retType = "Double")
    public static double iv(double price, double S, double K, double tau,
                            double r, double q, OptionType type) {
        double dfr = FastMath.exp(-r * tau);
        double dfq = FastMath.exp(-q * tau);
        double forward = S * dfq / dfr;
        // always use OTM option
        // convert using parity if necessary
        double priceToUse = price;
        OptionType typeToUse = type;
        if (type == OptionType.CALL && forward > K) {
            typeToUse = OptionType.PUT;
            priceToUse = price - S * dfq + K * dfr;
        }
        if (type == OptionType.PUT && forward < K) {
            typeToUse = OptionType.CALL;
            priceToUse = price + S * dfq - K * dfr;
        }
        final double p = priceToUse;
        UnivariateFunction func = null;
        if (typeToUse == OptionType.CALL) {
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
     * Put price by Black formula
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
        if (vol == 0.0 || tau == 0.0 || S < Constants.SMALL_NUMBER)
            return max(K * FastMath.exp(-r * tau) - S * FastMath.exp(-q * tau), 0.0);
        double forward = S * FastMath.exp((r - q) * tau);
        double df = FastMath.exp(-r * tau);
        double var = vol * FastMath.sqrt(tau);
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return Delta
     */
    public static double delta(double S, double K, double vol,
                               double tau, double r, double q, OptionType type) {
        if (tau < Constants.SMALL_NUMBER) {
            if (type == OptionType.CALL)
                return S>=K ? 1.0 : 0.0;
            else
                return S>=K ? 0.0 : -1.0;
        }
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        double delta = N.cumulativeProbability(dp);
        if (type == OptionType.PUT)
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return Dual Delta
     */
    public static double dualDelta(double S, double K, double vol,
                                   double tau, double r, double q, OptionType type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dm = FastMath.log(forward / K) / var - 0.5 * var;
        double delta = -N.cumulativeProbability(dm);
        if (type == OptionType.PUT)
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return Gamma
     */
    public static double gamma(double S, double K, double vol,
                               double tau, double r, double q, OptionType type) {
        if (tau < Constants.SMALL_NUMBER) {
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return Dual Gamma
     */
    public static double dualGamma(double S, double K, double vol,
                                   double tau, double r, double q, OptionType type) {
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return Vega
     */
    public static double vega(double S, double K, double vol,
                              double tau, double r, double q, OptionType type) {
        if (tau < Constants.SMALL_NUMBER) {
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return theta
     */
    public static double theta(double S, double K, double vol,
                               double tau, double r, double q, OptionType type) {
        if (tau < Constants.SMALL_NUMBER) {
            return 0.0;
        }
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        double dm = dp - var;
        double ret = -0.5 * dfq * S * N.density(dp) * vol / FastMath.sqrt(tau);
        if (type == OptionType.CALL) {
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return Rho (with respect to the risk free rate)
     */
    public static double rho_r(double S, double K, double vol,
                               double tau, double r, double q, OptionType type) {
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return Vera (with respect to the risk free rate)
     */
    public static double vera_r(double S, double K, double vol,
                                double tau, double r, double q, OptionType type) {
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return Rho (with respect to the risk free rate)
     */
    public static double rho_q(double S, double K, double vol,
                               double tau, double r, double q, OptionType type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        if (type == OptionType.CALL) {
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return Vera (with respect to the risk free rate)
     */
    public static double vera_q(double S, double K, double vol,
                                double tau, double r, double q, OptionType type) {
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return Vanna (with respect to spot and to vol)
     */
    public static double vanna(double S, double K, double vol,
                               double tau, double r, double q, OptionType type) {
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return Volga (2nd order derivative with respect to vol)
     */
    public static double volga(double S, double K, double vol,
                               double tau, double r, double q, OptionType type) {
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return Veta
     */
    public static double veta(double S, double K, double vol,
                              double tau, double r, double q, OptionType type) {
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return Vharm
     */
    public static double charm(double S, double K, double vol,
                               double tau, double r, double q, OptionType type) {
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return speed
     */
    public static double speed(double S, double K, double vol,
                               double tau, double r, double q, OptionType type) {
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return color
     */
    public static double color(double S, double K, double vol,
                               double tau, double r, double q, OptionType type) {
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return color
     */
    public static double ultima(double S, double K, double vol,
                                double tau, double r, double q, OptionType type) {
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
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return zomma
     */
    public static double zomma(double S, double K, double vol,
                               double tau, double r, double q, OptionType type) {
        double dfq = FastMath.exp(-q * tau);
        double dfr = FastMath.exp(-r * tau);
        double forward = S * dfq / dfr;
        double var = vol * FastMath.sqrt(tau);
        double dp = FastMath.log(forward / K) / var + 0.5 * var;
        double dm = dp - var;
        return gamma(S, K, vol, tau, r, q, type) * (dp * dm - 1.0) / vol;
    }

    /**
     * Black calculator
     * <p>
     * Note: THETA here is defined as the price sensitivity to <i>Time to Expiry</i>. This definition is
     * consistent with this calculator's input \tau. To get the sensitivity to <i>Expiry</i>, simply
     * negate the result returned by THETA. This holds for any time related derivative calculations.
     * For example VETA.
     * </p>
     *
     * @param req  Calculation request. See {@link CalcType}
     * @param S    Underlying price
     * @param K    Strike
     * @param vol  Volatility (annualized)
     * @param tau  Time to expiry (in years)
     * @param r    Risk free rate (annualized)
     * @param q    Dividend yield/Borrow cost etc. combined (annualized)
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return Requested calculation result
     */
    @BctQuantApi(name = "qlBlackCalc", description = "Black calculator",
            argNames = {"request", "spot", "strike", "vol", "tau", "r", "q", "type"},
            argTypes = {"Enum", "Double", "Double", "Double", "Double", "Double", "Double", "Enum"},
            argDescriptions = {"Request", "Spot", "Strike", "Volatility", "Time to expiry", "Yield", "Dividend",
                    "Option type"},
            retDescription = "result", retType = "Double", retName = "result")
    public static double calc(CalcType req, double S, double K, double vol, double tau,
                              double r, double q, OptionType type) {
        switch (req) {
            case PRICE:
                switch (type) {
                    case CALL:
                        return call(S, K, vol, tau, r, q);
                    case PUT:
                        return put(S, K, vol, tau, r, q);
                    default:
                        return Double.NaN;
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
                return Double.NaN;
        }
    }

    /**
     * Black76 formula that uses forward, vol, r, tau as independent variables. This formula is mainly
     * used to price European options on futures. Futures price, without considering rate/underlyer correlations,
     * is the same as forward price.
     * @param req Calculation request. See {@link CalcType}
     * @param forward Underlyer forward
     * @param K Strike
     * @param vol Volatility (annualized)
     * @param tau Time to expiry (in years)
     * @param r Risk free rate (continuous compounding)
     * @param type Option type (CALL or PUT). See {@link OptionType}
     * @return Requested calculation result
     */
    @BctQuantApi(name = "qlBlack76Calc", description = "Black76 calculator",
            argNames = {"request", "forward", "strike", "vol", "tau", "r", "type"},
            argTypes = {"Enum", "Double", "Double", "Double", "Double", "Double", "Enum"},
            argDescriptions = {"Request", "Forward", "Strike", "Volatility", "Time to expiry", "Risk-free rate",
                    "Option type"},
            retDescription = "result", retType = "Double", retName = "result")
    public static double black76(CalcType req, double forward, double K, double vol, double tau,
                                 double r, OptionType type) {
        switch (req) {
            case PRICE:
            case DELTA:
            case DUAL_DELTA:
            case GAMMA:
            case DUAL_GAMMA:
            case VEGA:
            case THETA:
                return calc(req, forward, K, vol, tau, r, r, type);
            case RHO_R:
                return calc(CalcType.RHO_R, forward, K, vol, tau, r, r, type)
                + calc(CalcType.RHO_Q, forward, K, vol, tau, r, r, type);
            default:
                return Double.NaN;
        }
    }
}