package tech.tongyu.bct.service.quantlib.common.numerics.black;

import org.apache.commons.math3.distribution.NormalDistribution;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;

import static java.lang.Math.*;

/**
 * @since 2016-10-25
 * quanto: fixed exchange rate foreign equity options
 * note: the underlying asset price and strike is priced in foreign currency
 * quanto vanilla European
 */
public class Quanto {
    private final static NormalDistribution N = new NormalDistribution();

//    private enum CalcType{
//        PRICE,
//        DELTA,
//        DUAL_DELTA,
//        GAMMA,
//        DUAL_GAMMA,
//        VEGA,
//        VEGA_E, // d Price / d vole
//        VEGA_RHO, // d Price / d rho
//        THETA
//    }
//
//    private enum Currency{
//        DOMESTIC,
//        FOREIGN
//    }

    /**
     * @param request       calculation type(price and Greeks: delta,gamma, dual_delta,dual_gamma, theta, vega)
     * @param optionType    option type CALL or PUT
     * @param currencyType  settlement currency: DOMESTIC or FOREIGN
     * @param spot          underlying asset price in foreign currency
     * @param strike        strike price in foreign currency
     * @param exchange      spot exchange rate (domestic / foreign)
     * @param rf            foreign interest rate
     * @param q             annually dividend payout rate of the underlying asset
     * @param vols          volatility of the underlying asset
     * @param r             domestic interest rate
     * @param Ep            predetermined exchange rate specified in units of domestic currency per unit of foreign currency
     * @param vole          volatility of the domestic exchange rate
     * @param rho           correlation between asset and domestic exchange rate
     * @param tau           time to maturity in years
     * @return the option price in domestic currency
     */
    @BctQuantApi(name = "qlQuantoOptionCalc",
            description = "Calculate the price or Greeks of the quanto option",
            argDescriptions = {"calculation type", "option type", "settlement currency: domestic or foreign",
                    "underlying asset price in foreign currency", "strike price in foreign currency",
                    "spot exchange rate (domestic / foreign)", "foreign interest rate", "annually dividend yield",
                    "volatility of the underlying asset", "domestic interest rate",
                    "predetermined exchange rate(domestic/foreign)", "volatility of the domestic exchange rate",
                    "correlation between asset and domestic exchange rate", "time to maturity in years"},
            argNames = {"request", "optionType", "currencyType", "spot", "strike",
                    "exchange", "rf", "q", "vols", "r", "Ep", "vole", "rho", "tau"},
            argTypes = {"String", "Enum", "String", "Double", "Double", "Double", "Double", "Double", "Double",
                    "Double", "Double", "Double", "Double", "Double"},
            retName = "", retDescription = "", retType = "Double")
    public static double calc(
            String request, OptionType optionType, String currencyType,
            double spot, double strike, double exchange, double rf, double q,
            double vols, double r, double Ep, double vole, double rho,
            double tau) {
        if (currencyType.equalsIgnoreCase("domestic"))
            return calcDomestic(request, optionType, spot, strike, rf, q, vols,
                    r, Ep, vole, rho, tau);
        else if (currencyType.equalsIgnoreCase("foreign"))
            return calcForeign(request, optionType, spot, strike, exchange, rf,
                    q, vols, r, Ep, vole, rho, tau);
        else
            return Double.NaN;
    }

    public static double calcDomestic(
            String request, OptionType optionType, double spot, double strike,
            double rf, double q, double vols, double r, double Ep, double vole,
            double rho, double tau) {
        double qD = q + r - rf + rho * vols * vole; // effective dividend in domestic currency
        double mu = r- qD;
        double d1 = (log(spot / strike)
                + (mu + 0.5 * vols * vols) * tau) / vols / sqrt(tau);
        double d2 = d1 - vols * sqrt(tau);
        if (optionType == OptionType.CALL) {
            if (request.equalsIgnoreCase("price"))
                return Ep * (spot * exp(-qD * tau) * N.cumulativeProbability(d1)
                        - strike * exp(-r * tau) * N.cumulativeProbability(d2));
            else if (request.equalsIgnoreCase("delta"))
                return Ep * exp(-qD * tau) * N.cumulativeProbability(d1);
            else if (request.equalsIgnoreCase("gamma"))
                return Ep * exp(-qD * tau) * N.density(d1) / (spot * vols * sqrt(tau));
            else if (request.equalsIgnoreCase("dual_delta"))
                return -Ep * exp(-r * tau) * N.cumulativeProbability(d2);
            else if (request.equalsIgnoreCase("dual_gamma"))
                return Ep * exp(-r * tau) * N.density(d2) / (strike * vols * sqrt(tau));
            else if (request.equalsIgnoreCase("theta"))
                return Ep * (-qD * spot * exp(-qD * tau) * N.cumulativeProbability(d1)
                        + r * strike * exp(-r * tau) * N.cumulativeProbability(d2)
                        + 0.5 * spot * exp(-qD * tau) * vols * N.density(d1) / sqrt(tau));
            else if (request.equalsIgnoreCase("vega"))
                return Ep * spot * exp(-qD * tau) * (sqrt(tau) * N.density(d1)
                        - rho * vole * tau * N.cumulativeProbability(d1));
            else if (request.equalsIgnoreCase("vega_exchange"))
                return -Ep * spot * exp(-qD * tau) * rho * vols * tau
                        * N.cumulativeProbability(d1);
            else if (request.equalsIgnoreCase("delta_correlation"))
                return -Ep * spot * exp(-qD * tau) * vole * vols * tau
                        * N.cumulativeProbability(d1);
            else
                return Double.NaN;
        } else {
            if (request.equalsIgnoreCase("price"))
                return Ep * (strike * exp(-r * tau) * N.cumulativeProbability(-d2)
                        - spot * exp(-qD * tau) * N.cumulativeProbability(-d1));
            else if (request.equalsIgnoreCase("delta"))
                return -Ep * exp(-qD * tau) * N.cumulativeProbability(-d1);
            else if (request.equalsIgnoreCase("gamma"))
                return Ep * exp(-qD * tau) * N.density(d1) / (spot * vols * sqrt(tau));
            else if (request.equalsIgnoreCase("dual_delta"))
                return Ep * exp(-r * tau) * N.cumulativeProbability(-d2);
            else if (request.equalsIgnoreCase("dual_gamma"))
                return Ep * exp(-r * tau) * N.density(d2) / (strike * vols * sqrt(tau));
            else if (request.equalsIgnoreCase("theta"))
                return Ep * (qD * spot * exp(-qD * tau) * N.cumulativeProbability(-d1)
                        - r * strike * exp(-r * tau) * N.cumulativeProbability(-d2)
                        + 0.5 * strike * exp(-r * tau) * vols * N.density(d2) / sqrt(tau));
            else if (request.equalsIgnoreCase("vega"))
                return Ep * spot * exp(-qD * tau) * (sqrt(tau) * N.density(d1)
                        + rho * vole * tau * N.cumulativeProbability(-d1));
            else if (request.equalsIgnoreCase("vega_exchange"))
                return Ep * spot * exp(-qD * tau) * rho * vols * tau
                        * N.cumulativeProbability(-d1);
            else if (request.equalsIgnoreCase("delta_correlation"))
                return Ep * spot * exp(-qD * tau) * vole * vols * tau
                        * N.cumulativeProbability(-d1);
            else
                return Double.NaN;
        }

    }

    public static double calcForeign(
            String request, OptionType optionType, double spot, double strike,
            double exchange, double rf, double q, double vols, double r, double Ep,
            double vole, double rho, double tau) {
        if (request.equalsIgnoreCase("delta_exchange"))
            return calcDomestic("price", optionType, spot, strike, rf, q,
                    vols, r, Ep, vole, rho, tau) / (-exchange * exchange);
        else if (request.equalsIgnoreCase("gamma_exchange"))
            return calcDomestic("price", optionType, spot, strike, rf, q,
                    vols, r, Ep, vole, rho, tau) * 2 / (exchange * exchange * exchange);
        else
            return calcDomestic(request, optionType, spot, strike, rf, q,
                    vols, r, Ep, vole, rho, tau) / exchange;
    }

}
