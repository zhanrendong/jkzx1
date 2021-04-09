package tech.tongyu.bct.service.quantlib.api;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi2;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApiArg;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.PricerType;
import tech.tongyu.bct.service.quantlib.common.numerics.fd.FiniteDifference;
import tech.tongyu.bct.service.quantlib.financial.instruments.Portfolio;
import tech.tongyu.bct.service.quantlib.financial.instruments.linear.Cash;
import tech.tongyu.bct.service.quantlib.financial.instruments.linear.Forward;
import tech.tongyu.bct.service.quantlib.financial.instruments.linear.Spot;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.*;
import tech.tongyu.bct.service.quantlib.pricer.PricerBlack;

import java.time.LocalDateTime;
import java.util.Map;

public class OptionPricerBlack {
    @BctQuantApi(
            name = "qlOptionCalcBlack",
            description = "Computes option price and greeks by Black formulas",
            argNames = {"request", "instrument", "valDate", "spot", "vol", "r", "q"},
            argTypes = {"Enum", "Handle", "DateTime", "Double", "Double", "Double", "Double"},
            argDescriptions = {"Request", "The instrument to price", "Valuation date", "Spot",
                    "vol", "r", "q"},
            retName = "", retDescription = "", retType = "Double"
    )
    public static double calc(CalcType request, Object instrument,
                              LocalDateTime val, double spot, double vol, double r, double q) throws Exception {
        double daysInYear = 365;
        String optnType = instrument.getClass().getSimpleName();

        //Vanilla European
        if (instrument instanceof VanillaEuropean) {
            return PricerBlack.calc(request, (VanillaEuropean) instrument, val, spot, vol, r, q, daysInYear);
        }
        //Digital Cash
        else if (instrument instanceof DigitalCash) {
            return PricerBlack.calc(request, (DigitalCash) instrument, val, spot, vol, r, q, daysInYear);
        }
        //Knock Out Terminal
        else if (instrument instanceof KnockOutTerminal) {
            Portfolio combinations = ((KnockOutTerminal) instrument).split();
            return PortfolioTools.portfolioCalcBlack(request, combinations, val, spot, vol, r, q);
        }
        //knock In terminal
        else if (instrument instanceof KnockInTerminal) {
            Portfolio combinations = ((KnockInTerminal) instrument).split();
            return PortfolioTools.portfolioCalcBlack(request, combinations, val, spot, vol, r, q);
        }
        //Average Rate Arithmetic
        else if (instrument instanceof AverageRateArithmetic) {
            return PricerBlack.calc(request, (AverageRateArithmetic) instrument, val, spot, vol, r, q, daysInYear);
        }
        //Knock In Continuous
        else if (instrument instanceof KnockInContinuous) {
            return PricerBlack.calc(request, (KnockInContinuous) instrument, val, spot, vol, r, q, daysInYear);
        }
        //Knock Out Continuous
        else if (instrument instanceof KnockOutContinuous) {
            return PricerBlack.calc(request, (KnockOutContinuous) instrument, val, spot, vol, r, q, daysInYear);
        }
        //Double Knock Out Continuous
        else if (instrument instanceof DoubleKnockOutContinuous) {
            return PricerBlack.calc(request, (DoubleKnockOutContinuous) instrument, val, spot, vol, r, q, daysInYear);
        }
        //Vanilla American
        else if (instrument instanceof VanillaAmerican) {
            return PricerBlack.calc(request, (VanillaAmerican) instrument, val, spot, vol, r, q, daysInYear);
        }
        //Double Shark Fin Continuous
        else if (instrument instanceof DoubleSharkFinContinuous) {
            Portfolio combinations = ((DoubleSharkFinContinuous) instrument).split();
            return PortfolioTools.portfolioCalcBlack(request, combinations, val, spot, vol, r, q);
        }
        //Double Shark Fin Terminal
        else if (instrument instanceof DoubleSharkFinTerminal) {
            Portfolio combinations = ((DoubleSharkFinTerminal) instrument).split();
            return PortfolioTools.portfolioCalcBlack(request, combinations, val, spot, vol, r, q);
        }
        //vertical spread
        else if (instrument instanceof VerticalSpread) {
            Portfolio combinations = ((VerticalSpread) instrument).split();
            return PortfolioTools.portfolioCalcBlack(request, combinations, val, spot, vol, r, q);
        }
        //range accrual
        else if (instrument instanceof RangeAccrual){
            return PricerBlack.calc(request,(RangeAccrual)instrument,val,spot,vol,r,q,daysInYear);
        }
        //strangle
        else if (instrument instanceof VanillaStrangle) {
            Portfolio combinations = ((VanillaStrangle) instrument).split();
            return PortfolioTools.portfolioCalcBlack(request, combinations, val, spot, vol, r, q);
        }
        // cash
        else if (instrument instanceof Cash) {
            return PricerBlack.calc(request, (Cash) instrument, val, spot, vol, r, q, daysInYear);
        }
        // spot
        else if (instrument instanceof Spot) {
            return PricerBlack.calc(request, (Spot) instrument, val, spot, vol, r, q, daysInYear);
        }
        // no touch continuous
        else if (instrument instanceof NoTouch) {
            return PricerBlack.calc(request, (NoTouch) instrument, val, spot, vol, r, q, daysInYear);
        }
        else
            throw new Exception("Option type " + optnType + " is not supported by Black model");
    }

    public static LocalDateTime getExpiry(Object instrument) throws Exception {
        String optnType = instrument.getClass().getSimpleName();
        //Vanilla European
        if (instrument instanceof VanillaEuropean) {
            return ((VanillaEuropean) instrument).getExpiry();
        }
        //Digital Cash
        else if (instrument instanceof DigitalCash) {
            return ((DigitalCash) instrument).getExpiry();
        }
        //Knock Out Terminal
        else if (instrument instanceof KnockOutTerminal) {
            return ((KnockOutTerminal) instrument).getExpiry();
        }
        //knock In terminal
        else if (instrument instanceof KnockInTerminal) {
            return ((KnockInTerminal) instrument).getExpiry();
        }
        //Average Rate Arithmetic
        else if (instrument instanceof AverageRateArithmetic) {
            return ((AverageRateArithmetic) instrument).getExpiry();
        }
        //Knock In Continuous
        else if (instrument instanceof KnockInContinuous) {
            return ((KnockInContinuous) instrument).getExpiry();
        }
        //Knock Out Continuous
        else if (instrument instanceof KnockOutContinuous) {
            return ((KnockOutContinuous) instrument).getExpiry();
        }
        //Double Knock Out Continuous
        else if (instrument instanceof DoubleKnockOutContinuous) {
            return ((DoubleKnockOutContinuous) instrument).getExpiry();
        }
        //Vanilla American
        else if (instrument instanceof VanillaAmerican) {
            return ((VanillaAmerican) instrument).getExpiry();
        }
        //Double Shark Fin Continuous
        else if (instrument instanceof DoubleSharkFinContinuous) {
            return ((DoubleSharkFinContinuous) instrument).getExpiry();
        }
        //Double Shark Fin Terminal
        else if (instrument instanceof DoubleSharkFinTerminal) {
           return ((DoubleSharkFinTerminal) instrument).getExpiry();
        }
        //Vertical Spread
        else if (instrument instanceof VerticalSpread) {
            return ((VerticalSpread) instrument).getExpiry();
        }
        //range accrual
        else if (instrument instanceof RangeAccrual){
            return ((RangeAccrual)instrument).getExpiry();
        }
        //spread
        else if (instrument instanceof Spread) {
            return ((Spread) instrument).getExpiry();
        }
        //strangle
        else if (instrument instanceof VanillaStrangle) {
            return ((VanillaStrangle) instrument).getExpiry();
        }
        // cash
        else if (instrument instanceof Cash) {
            return ((Cash) instrument).getPayDate();
        }
        // spot
        else if (instrument instanceof Spot) {
            return LocalDateTime.of(3000, 1, 1,0, 0);
        }
        // forward
        else if (instrument instanceof Forward) {
            return ((Forward) instrument).getDeliveryDate();
        }
        else if (instrument instanceof AutoCall) {
            return ((AutoCall) instrument).getExpiry();
        }
        else if (instrument instanceof AutoCallPhoenix) {
            return ((AutoCallPhoenix) instrument).getExpiry();
        }
        else
            throw new Exception("Option type " + optnType + " is not supported");
    }

    // TODO （http://10.1.2.16:8080/browse/OTMS-232）: merge with PricerBlackWithRisks.calcPDE
    @BctQuantApi2(
            name = "qlOptionCalcBlackScholes",
            description = "Compute option price and greeks under Black-Schole's model",
            retType = "Double",
            retName = "result",
            retDescription = "Option price or greeks",
            args = {
                    @BctQuantApiArg(name = "request", type = "Enum", description = "Calculation request"),
                    @BctQuantApiArg(name = "instrument", type = "Handle", description = "The option to be priced"),
                    @BctQuantApiArg(name = "val", type = "DateTime", description = "Valuation date"),
                    @BctQuantApiArg(name = "spot", type = "Double", description = "Underlyer spot"),
                    @BctQuantApiArg(name = "vol", type = "Double", description = "Volatility"),
                    @BctQuantApiArg(name = "r", type = "Double", description = "Risk free rate"),
                    @BctQuantApiArg(name = "q", type = "Double", description = "Dividend yield"),
                    @BctQuantApiArg(name = "pricer", type = "Enum", description = "Pricer: BLACK_ANALYTIC, BLACK_PDE"),
                    @BctQuantApiArg(name = "params", type = "Json", description = "Pricer parameters", required = false)
            }
    )
    public static double calcBlackScholes(CalcType request, Object instrument,
                                          LocalDateTime val, double spot, double vol, double r, double q,
                                          PricerType pricer,
                                          Map<String, Object> params) throws Exception {
        switch (pricer) {
            case BLACK_ANALYTIC:
                double result = calc(request, instrument, val, spot, vol, r, q);
                if (request == CalcType.THETA)
                    result = -result;
                return result;
            case BLACK_PDE:
                //   defaults
                double eps = 0.0001;
                double alpha = 0.5;
                // parse params inputs
                if (params != null) {
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        if (entry.getKey().toLowerCase().equals("alpha"))
                            alpha = (double)entry.getValue();
                        if (entry.getKey().toLowerCase().equals("eps"))
                            eps = (double)entry.getValue();
                    }
                }
                if (request == CalcType.PRICE)
                    return OptionPricerPDE.calc(instrument, val, spot, r, q, vol,
                            eps, alpha, params);
                // theta
                if (request == CalcType.THETA) {
                    double price = OptionPricerPDE.calc(instrument, val, spot, r, q, vol,
                            eps, alpha, params);
                    LocalDateTime valPlusOne = val.plusDays(1);
                    double pricePlusOne = OptionPricerPDE.calc(instrument, valPlusOne,
                            spot, r, q, vol, eps, alpha, params);
                    return (pricePlusOne - price) * 365;
                }
                // the rest of greeks
                double[] variables = new double[]{spot, vol, r, q};
                double epsToUse = eps;
                double alphaToUse = alpha;
                FiniteDifference calcFD = new FiniteDifference(u -> {
                    try {
                        return OptionPricerPDE.calc(
                                instrument, val, u[0], u[2], u[3], u[1],
                                epsToUse, alphaToUse, params);
                    } catch (Exception e) {
                        return 0.0;
                    }
                });
                double[] gradients = calcFD.getGradient(variables);
                double[][] hessian = calcFD.getHessian(variables);
                switch (request) {
                    case DELTA:
                        return gradients[0];
                    case GAMMA:
                        return hessian[0][0];
                    case VEGA:
                        return gradients[1];
                    case RHO_R:
                        return gradients[2];
                    case RHO_Q:
                        return gradients[3];
                    default:
                        throw new Exception("Request " + request.name() + " is not supported by PDE solver");
                }
            default:
                throw new Exception("Input pricer " + pricer.name() + " is not supported");
        }
    }

    @BctQuantApi2(
            name = "qlOptionCalcBlack76",
            description = "Computes option price and greeks by Black76 formulas",
            retType = "Double",
            retName = "result",
            retDescription = "User requested calculation result",
            args = {
                    @BctQuantApiArg(name = "request", type = "Enum", description = "Request"),
                    @BctQuantApiArg(name = "instrument", type = "Handle", description = "Instrument to be priced"),
                    @BctQuantApiArg(name = "val", type = "DateTime", description = "Valuation date"),
                    @BctQuantApiArg(name = "forward", type = "Double", description = "Underlyer's forward price"),
                    @BctQuantApiArg(name = "vol", type = "Double", description = "Volatility"),
                    @BctQuantApiArg(name = "r", type = "Double", description = "Risk free rate"),
                    @BctQuantApiArg(name = "pricer", type = "Enum", description = "Pricer: BLACK_ANALYTIC, BLACK_PDE"),
                    @BctQuantApiArg(name = "params", type = "Json", description = "Pricer parameters", required = false)
            }
    )
    public static double calcBlack76(CalcType request, Object instrument,
                                     LocalDateTime val, double forward, double vol, double r,
                                     PricerType pricer,
                                     Map<String, Object> params) throws Exception {
        double result = calcBlackScholes(request, instrument, val, forward, vol, r, r, pricer, params);
        if (request == CalcType.RHO_R) {
            double rhoQ = calcBlackScholes(CalcType.RHO_Q, instrument, val, forward, vol, r, r, pricer, params);
            result += rhoQ;
        }
        return result;
    }
}