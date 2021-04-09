package tech.tongyu.bct.service.quantlib.api;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi2;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApiArg;
import tech.tongyu.bct.service.quantlib.financial.instruments.Portfolio;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.*;
import tech.tongyu.bct.service.quantlib.pricer.PricerPDE.PricerPDE;

import java.time.LocalDateTime;
import java.util.Map;

import static java.lang.Math.max;

/**
 * EXCEL API for pricing an option using PDESolver
 * Created by Liao Song on 16-8-16.
 */
public class OptionPricerPDE {
    @BctQuantApi2(
            name = "qlOptionCalcPDE",
            description = "Computes option price by PDE using finite difference ",
            args = {@BctQuantApiArg(name = "instrument", type = "Handle",
                    description = "financial instrument to be priced"),
                    @BctQuantApiArg(name = "valDate", type = "DateTime",
                    description = "valuation date"),
                    @BctQuantApiArg(name = "spot", type = "Double", description = "spot"),
                    @BctQuantApiArg(name = "r", type = "Double",
                    description = "risk free interest rate"),
                    @BctQuantApiArg(name = "q", type = "Double",
                    description = "annually dividend yield"),
                    @BctQuantApiArg(name = "vol", type = "Double", description = "volatility"),
                    @BctQuantApiArg(name = "eps", type = "Double",
                    description = "if > 0, pricerParams ignored; if <=0, use pricerParams instead"),
                    @BctQuantApiArg(name = "alpha", type = "Double", description = "implicit index"),
                    @BctQuantApiArg(name = "pricerParams", type = "Json", required = false,
                    description = "contains: minS, maxS, spotNumSteps, timeNumSpots")},
            retName = "", retDescription = "", retType = "Double"
    )
    public static double calc(Object instrument, LocalDateTime val, double spot, double r, double q, double vol,
                              double eps, double alpha, Map<String, Object> params) throws Exception {
        double daysInYear = 365;

        // vanilla European option

        if (instrument instanceof VanillaEuropean) {
            return max(PricerPDE.price((VanillaEuropean) instrument, val, spot, r, q, vol,
                    daysInYear, eps, alpha, params), 0);
        }
        // Digital cash
        if (instrument instanceof DigitalCash) {
            return max(PricerPDE.price((DigitalCash) instrument, val, spot, r, q, vol,
                    daysInYear, eps, alpha, params), 0);
        }

        //vanilla American option

        else if (instrument instanceof VanillaAmerican) {
            return max(PricerPDE.price((VanillaAmerican) instrument, val, spot, r, q, vol,
                    daysInYear, eps, alpha, params), 0);
        }

        //knock-out option(with continuous monitoring)

        else if (instrument instanceof KnockOutContinuous) {
            return max(PricerPDE.price((KnockOutContinuous) instrument, val, spot, r, q, vol,
                    daysInYear, eps, alpha, params), 0);
        }

        //knock-in option(with continuous monitoring)

        else if (instrument instanceof KnockInContinuous) {
            return max(PricerPDE.price((KnockInContinuous) instrument, val, spot, r, q, vol,
                    daysInYear, eps, alpha, params), 0);
        }
        //double knock-out option(with continuous monitoring)

        else if (instrument instanceof DoubleKnockOutContinuous) {
            return max(PricerPDE.price((DoubleKnockOutContinuous) instrument, val, spot, r, q, vol,
                    daysInYear, eps, alpha, params), 0);
        }
        //Knock In Terminal
        else if (instrument instanceof KnockInTerminal) {
            Portfolio combinations = ((KnockInTerminal) instrument).split();
            return PortfolioTools.portfolioCalcPDE(combinations, val, spot, vol, r, q,
                    eps, alpha, params);

        }
        //Knock Out Terminal
        else if (instrument instanceof KnockOutTerminal) {
            Portfolio combinations = ((KnockOutTerminal) instrument).split();
            return PortfolioTools.portfolioCalcPDE(combinations, val, spot, vol, r, q,
                    eps, alpha, params);
        }
        //vertical spread
        else if (instrument instanceof VerticalSpread) {
            Portfolio combinations = ((VerticalSpread) instrument).split();
            return PortfolioTools.portfolioCalcPDE(combinations, val, spot, vol, r, q,
                    eps, alpha, params);
        }
        //strangle
        else if (instrument instanceof VanillaStrangle) {
            Portfolio combinations = ((VanillaStrangle) instrument).split();
            return PortfolioTools.portfolioCalcPDE(combinations, val, spot, vol, r, q,
                    eps, alpha, params);
        }
        //Double Shark Fin Continuous
        else if (instrument instanceof DoubleSharkFinContinuous) {
            Portfolio combinations = ((DoubleSharkFinContinuous) instrument).split();
            return PortfolioTools.portfolioCalcPDE(combinations, val, spot, vol, r, q,
                    eps, alpha, params);
        }
        //Double Shark Fin Terminal
        else if (instrument instanceof DoubleSharkFinTerminal) {
            Portfolio combinations = ((DoubleSharkFinTerminal) instrument).split();
            return PortfolioTools.portfolioCalcPDE(combinations, val, spot, vol, r, q,
                    eps, alpha, params);
        }
        // Exception
        else
            throw new Exception("Unsupported instrument");
    }

    public static double calc(Object instrument, LocalDateTime val, double spot, double r, double q, double vol,
                              double eps, double alpha) throws Exception {
        return calc(instrument, val, spot, r, q, vol, eps, alpha, null);
    }
}

