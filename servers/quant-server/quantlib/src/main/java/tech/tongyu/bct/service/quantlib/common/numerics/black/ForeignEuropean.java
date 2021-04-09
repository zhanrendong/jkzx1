package tech.tongyu.bct.service.quantlib.common.numerics.black;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi2;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApiArg;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;

/**
 * Vanilla European option on asset in foreign currency.
 */
public class ForeignEuropean {

    @BctQuantApi2(
            name = "qlForeignEuropeanCalc",
            description = "Calulate the price or greeks of vanilla European" +
                    "option on asset in foreign currency.",
            args = {@BctQuantApiArg(name = "request", type = "String",
                    description = "calulation type"),
                    @BctQuantApiArg(name = "optionType", type = "Enum",
                    description = "option type: call or put"),
                    @BctQuantApiArg(name = "currencyType", type = "String",
                    description = "settlement currency: domestic or foreign"),
                    @BctQuantApiArg(name = "spot", type = "Double",
                    description = "asset spot in foreign currency"),
                    @BctQuantApiArg(name = "strike", type = "Double",
                    description = "strike price in foreign currency"),
                    @BctQuantApiArg(name = "exchange", type = "Double",
                    description = "exchange rate (domestic / foreign currency)"),
                    @BctQuantApiArg(name = "vol", type = "Double",
                    description = "volatility of underlying asset"),
                    @BctQuantApiArg(name = "r", type = "Double",
                    description = "foreign interest rate"),
                    @BctQuantApiArg(name = "q", type = "Double",
                    description = "annually dividend yeild"),
                    @BctQuantApiArg(name = "tau", type = "Double",
                    description = "time to expiry (in year)")
            },
            retName = "",
            retDescription = "calculated price or greeks",
            retType = "Double")
    public static double calc(
            String request, OptionType optionType, String currencyType,
            double spot, double strike, double exchange, double vol, double r,
            double q, double tau) {
        if (currencyType.equalsIgnoreCase("foreign"))
            return calcForeign(request, optionType, spot, strike, vol, r, q,
                    tau);
        else if (currencyType.equalsIgnoreCase("domestic"))
            return calcDomestic(request, optionType, spot, strike, exchange, vol,
                    r, q, tau);
        else
            return Double.NaN;
    }

    public static double calcForeign(
            String request, OptionType optionType, double spot, double strike,
            double vol, double r, double q, double tau) {
        if (request.equalsIgnoreCase("price"))
            return Black.calc(CalcType.PRICE, spot, strike, vol, tau, r,
                    q, optionType);
        else if (request.equalsIgnoreCase("delta"))
            return Black.calc(CalcType.DELTA, spot, strike, vol, tau, r,
                    q, optionType);
        else if (request.equalsIgnoreCase("dual_delta"))
            return Black.calc(CalcType.DUAL_DELTA, spot, strike, vol,
                    tau, r, q, optionType);
        else if (request.equalsIgnoreCase("gamma"))
            return Black.calc(CalcType.GAMMA, spot, strike, vol, tau, r,
                    q, optionType);
        else if (request.equalsIgnoreCase("dual_gamma"))
            return Black.calc(CalcType.DUAL_GAMMA, spot, strike, vol,
                    tau, r, q, optionType);
        else if (request.equalsIgnoreCase("theta"))
            return Black.calc(CalcType.THETA, spot, strike, vol, tau, r,
                    q, optionType);
        else if (request.equalsIgnoreCase("vega"))
            return Black.calc(CalcType.VEGA, spot, strike, vol, tau, r,
                    q, optionType);
        else
            return Double.NaN;
    }

    public static double calcDomestic(
            String request, OptionType optionType, double spot, double strike,
            double exchange, double vol, double r, double q, double tau) {
        if (request.equalsIgnoreCase("price"))
            return exchange * Black.calc(CalcType.PRICE, spot, strike, vol, tau,
                    r, q, optionType);
        else if (request.equalsIgnoreCase("delta"))
            return exchange * Black.calc(CalcType.DELTA, spot, strike, vol, tau,
                    r, q, optionType);
        else if (request.equalsIgnoreCase("dual_delta"))
            return exchange * Black.calc(CalcType.DUAL_DELTA, spot, strike, vol,
                    tau, r, q, optionType);
        else if (request.equalsIgnoreCase("gamma"))
            return exchange * Black.calc(CalcType.GAMMA, spot, strike, vol, tau,
                    r, q, optionType);
        else if (request.equalsIgnoreCase("dual_gamma"))
            return exchange * Black.calc(CalcType.DUAL_GAMMA, spot, strike, vol,
                    tau, r, q, optionType);
        else if (request.equalsIgnoreCase("theta"))
            return exchange * Black.calc(CalcType.THETA, spot, strike, vol, tau,
                    r, q, optionType);
        else if (request.equalsIgnoreCase("vega"))
            return exchange * Black.calc(CalcType.VEGA, spot, strike, vol, tau,
                    r, q, optionType);
        else if (request.equalsIgnoreCase("delta_exchange"))
            return Black.calc(CalcType.PRICE, spot, strike, vol, tau, r, q,
                    optionType);
        else
            return Double.NaN;
    }
}
