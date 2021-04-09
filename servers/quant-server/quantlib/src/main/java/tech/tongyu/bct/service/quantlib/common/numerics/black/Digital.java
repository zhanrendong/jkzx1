package tech.tongyu.bct.service.quantlib.common.numerics.black;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.enums.PayoffType;
import tech.tongyu.bct.service.quantlib.common.numerics.ad.Backwarder;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;

/**
 * Black formulas for European digital calls and puts. Depending on whether the option pays cash or asset, there
 * are two types of digital options: cash-or-nothing and asset-or-nothing. Upon expiry, if the spot is above
 * the strike, a cash-or-nothing call pays 1 unit of cash while an asset-or-nothing call pays 1 unit of underlying.
 *
 * @author Lu Lu
 * @since 2016-07-04
 */
public class Digital {
    private final static NormalDistribution N = new NormalDistribution();


    private static double calcTau0(CalcType request,
                            double S, double K,
                            OptionType type, PayoffType cashOrAsset) throws Exception {
        if (cashOrAsset == PayoffType.CASH) {
            switch (request) {
                case PRICE:
                    if (type == OptionType.CALL)
                        return S>=K ? 1.0 : 0.0;
                    else
                        return S<K ? 1.0 : 0.0;
                case DELTA:
                    return 0.0;
                case GAMMA:
                    return 0.0;
                case VEGA:
                    return 0.0;
                case THETA:
                    return 0.0;
                default:
                    return Double.NaN;
            }
        } else {
            switch (request) {
                case PRICE:
                    if (type == OptionType.CALL)
                        return S>=K ? S : 0.0;
                    else
                        return S<K ? S : 0.0;
                case DELTA:
                    return 1.0;
                case GAMMA:
                    return 0.0;
                case VEGA:
                    return 0.0;
                case THETA:
                    return 0.0;
                default:
                    return Double.NaN;
            }
        }
    }
    /**
     * Price a European digital option. The payoff is 1 unit of cash or asset.
     *
     * @param request     Calculation request. See {@link CalcType}
     * @param S           Underlying spot
     * @param K           Strike
     * @param vol         Volatility (annualized)
     * @param tau         Time to expiry (in years)
     * @param r           Risk free rate (annualized)
     * @param q           Dividend yield/Borrow cost etc. combined (annualized)
     * @param type        Option type (CALL or PUT). See {@link OptionType}
     * @param cashOrAsset Pays 1 unit of cash ({@link PayoffType#CASH}) or asset ({@link PayoffType#ASSET})
     * @return requested calculation result
     */
    @BctQuantApi(
            name = "qlBlackDigitalCalc",
            description = "Calculates the price and greeks of a European digital option",
            argNames = {"request", "spot", "strike", "vol", "tau", "r", "q", "type", "cashOrAsset"},
            argDescriptions = {"Request", "Spot", "Strike", "Volatility", "Time to expiry", "Yield", "Dividend",
                    "Option type", "Cash or asset?"},
            argTypes = {"Enum", "Double", "Double", "Double", "Double", "Double", "Double", "Enum", "Enum"},
            retDescription = "result", retType = "Double", retName = "result"
    )
    public static double calc(CalcType request,
                              double S, double K, double vol, double tau, double r, double q,
                              OptionType type, PayoffType cashOrAsset) throws Exception {
        if (tau < Constants.SMALL_NUMBER)
            return calcTau0(request, S, K, type, cashOrAsset);
        Backwarder calculator = new Backwarder(new String[]{"S", "K", "vol", "tau", "r", "q"},
                new double[]{S, K, vol, tau, r, q}, 2);
        String dfr = calculator.exp(calculator.mul("tau", calculator.scale("r", -1.0)));
        String dfq = calculator.exp(calculator.mul("tau", calculator.scale("q", -1.0)));
        String fwd = calculator.div(calculator.mul("S", dfq), dfr);
        String lnMoneyness = calculator.ln(calculator.div(fwd, "K"));
        String sqrtVar = calculator.sqrt(calculator.mul(calculator.power("vol", 2), "tau"));
        String dp = calculator.sum(calculator.div(lnMoneyness, sqrtVar), calculator.scale(sqrtVar, 0.5));
        String dm = calculator.sub(dp, sqrtVar);
        String price;
        if (cashOrAsset == PayoffType.CASH) {
            if (type == OptionType.CALL) {
                price = calculator.mul(dfr, calculator.ncdf(dm));
            } else {
                price = calculator.mul(dfr, calculator.sum(calculator.scale(calculator.ncdf(dm), -1.0), 1.0));
            }
        } else {
            if (type == OptionType.CALL) {
                price = calculator.mul(dfq, calculator.mul("S", calculator.ncdf(dp)));
            } else {
                price = calculator.mul(dfq, calculator.mul("S", calculator.ncdf(calculator.scale(dp, -1.0))));
            }
        }
        calculator.rollBack();
        switch (request) {
            case PRICE:
                return calculator.getValue(price);
            case DELTA:
                return calculator.getDeriv("S");
            case DUAL_DELTA:
                return calculator.getDeriv("K");
            case GAMMA:
                return calculator.getDeriv("S", "S");
            case DUAL_GAMMA:
                return calculator.getDeriv("K", "K");
            case VEGA:
                return calculator.getDeriv("vol");
            case THETA:
                return calculator.getDeriv("tau");
            case RHO_R:
                return calculator.getDeriv("r");
            case VERA_R:
                return calculator.getDeriv("r", "vol");
            case RHO_Q:
                return calculator.getDeriv("q");
            case VERA_Q:
                return calculator.getDeriv("q", "vol");
            case VANNA:
                return calculator.getDeriv("S", "vol");
            case VOLGA:
                return calculator.getDeriv("vol", "vol");
            case VETA:
                return calculator.getDeriv("tau", "vol");
            case CHARM:
            case COLOR:
            case SPEED:
            case ULTIMA:
            case ZOMMA:
            default:
                return Double.NaN;
        }
    }

    public static double calcPrice(OptionType type, double S, double K,
                                      double vol, double r, double q, double tau)
    {
        double var = vol * FastMath.sqrt(tau);
        double f = S * FastMath.exp((r - q) * tau);
        double df = FastMath.exp(-r * tau);
        double lnM = FastMath.log(f / K);
        double d2 = lnM / var - 0.5 * var;
        if (type == OptionType.CALL)
            return df * N.cumulativeProbability(d2);
        else
            return df * (1 - N.cumulativeProbability(d2));
    }

    public static double calcDelta(OptionType type, double S, double K,
                                   double vol, double r, double q, double tau)
    {
        double var = vol * FastMath.sqrt(tau);
        double df = FastMath.exp(-r * tau);
        double lnM = FastMath.log( S * FastMath.exp((r - q) * tau) / K);
        double d2 = lnM / var - 0.5 * var;
        if (type == OptionType.CALL)
            return df * N.density(d2) / (S * var);
        else
            return - df * N.density(d2) / (S * var);
    }

    public static double calcGamma(OptionType type, double S, double K,
                                   double vol, double r, double q, double tau)
    {
        double var = vol * FastMath.sqrt(tau);
        double df = FastMath.exp(-r * tau);
        double lnM = FastMath.log( S * FastMath.exp((r - q) * tau) / K);
        double d1 = lnM / var + 0.5 * var;
        double d2 = d1 - var;
        if (type == OptionType.CALL)
            return -df * d1* N.density(d2) / (S * S * var * var);
        else
            return df * d1* N.density(d2) / (S * S * var * var);
    }

    public static double calcVega(OptionType type, double S, double K,
                                   double vol, double r, double q, double tau)
    {
        double var = vol * FastMath.sqrt(tau);
        double df = FastMath.exp(-r * tau);
        double lnM = FastMath.log( S * FastMath.exp((r - q) * tau) / K);
        double d1 = lnM / var + 0.5 * var;
        double d2 = d1 - var;
        if (type == OptionType.CALL)
            return -df * N.density(d2) * (d2/vol + FastMath.sqrt(tau));
        else
            return df * N.density(d2) * (d2/vol + FastMath.sqrt(tau));
    }

    public static double calcTheta(OptionType type, double S, double K,
                                  double vol, double r, double q, double tau)
    {
        double var = vol * FastMath.sqrt(tau);
        double df = FastMath.exp(-r * tau);
        double lnM = FastMath.log( S * FastMath.exp((r - q) * tau) / K);
        double d1 = lnM / var + 0.5 * var;
        double d2 = d1 - var;
        double rP = r * df * N.cumulativeProbability(d2);
        double rQ = r * df *(1- N.cumulativeProbability(d2));
        if (type == OptionType.CALL)
            return -rP - df * N.density(d2) * (d1 / (2 * tau) - (r - q) / var);
        else
            return -rQ + df * N.density(d2) * (d1 / (2 * tau) - (r - q) / var);
    }

    public static double calcRhoR(OptionType type, double S, double K,
                                  double vol, double r, double q, double tau)
    {
        double var = vol * FastMath.sqrt(tau);
        double df = FastMath.exp(-r * tau);
        double lnM = FastMath.log( S * FastMath.exp((r - q) * tau) / K);
        double d1 = lnM / var + 0.5 * var;
        double d2 = d1 - var;
        if (type == OptionType.CALL)
            return df *(-tau* N.cumulativeProbability(d2) + FastMath.sqrt(tau) / vol * N.density(d2));
        else
            return df *(-tau * (1-N.cumulativeProbability(d2)) - FastMath.sqrt(tau) / vol * N.density(d2));
    }

    public static double calcRhoQ(OptionType type, double S, double K,
                                  double vol, double r, double q, double tau)
    {
        double var = vol * FastMath.sqrt(tau);
        double df = FastMath.exp(-r * tau);
        double lnM = FastMath.log( S * FastMath.exp((r - q) * tau) / K);
        double d1 = lnM / var + 0.5 * var;
        double d2 = d1 - var;
        if (type == OptionType.CALL)
            return - df * FastMath.sqrt(tau) / vol * N.density(d2);
        else
            return df * FastMath.sqrt(tau) / vol * N.density(d2);
    }
}