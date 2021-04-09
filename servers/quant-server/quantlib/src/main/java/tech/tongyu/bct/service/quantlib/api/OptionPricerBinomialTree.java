package tech.tongyu.bct.service.quantlib.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;
import tech.tongyu.bct.service.quantlib.common.numerics.tree.BinomialTree;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;
import tech.tongyu.bct.service.quantlib.financial.instruments.Portfolio;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.*;
import tech.tongyu.bct.service.quantlib.pricer.PricerBinomialTree;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.function.Function;

/**
 * @author Liao Song
 * @since 2016-8-4
 */
public class OptionPricerBinomialTree {
    private static Logger logger = LoggerFactory.getLogger(OptionPricerBinomialTree.class);

    @BctQuantApi(
            name = "qlOptionCalcBinomialTree",
            description = "Computes option price by Binomial Tree method",
            argNames = {"instrument", "valDate", "spot", "vol", "r", "q", "N"},
            argTypes = {"Handle", "DateTime", "Double", "Double", "Double", "Double", "Integer"},
            argDescriptions = {"The instrument to price", "Valuation date", "Spot",
                    "vol", "r", "q", "Number of steps"},
            retName = "", retDescription = "", retType = "Double"
    )
    public static double calc(Object instrument,
                              LocalDateTime val, double spot, double vol, double r, double q, int N) throws Exception {
        double daysInYear = 365.25;
        String optnType = instrument.getClass().getSimpleName();
        //vanilla European
        if (instrument instanceof VanillaEuropean) {
            return PricerBinomialTree.price((VanillaEuropean) instrument, val, spot, vol, r, q, daysInYear, N);
        }
        //vanilla American
        else if (instrument instanceof VanillaAmerican) {
            return PricerBinomialTree.price((VanillaAmerican) instrument, val, spot, vol, r, q, daysInYear, N);
        }
        //Knock In Continuous
        else if (instrument instanceof KnockInContinuous) {
            return PricerBinomialTree.price((KnockInContinuous) instrument, val, spot, vol, r, q, daysInYear, N);
        }
        //Knock Out Continuous
        else if (instrument instanceof KnockOutContinuous) {
            return PricerBinomialTree.price((KnockOutContinuous) instrument, val, spot, vol, r, q, daysInYear, N);
        }
        //Double Knock Out Continuous
        else if (instrument instanceof DoubleKnockOutContinuous) {
            return PricerBinomialTree.price((DoubleKnockOutContinuous) instrument, val, spot, vol, r, q, daysInYear, N);
        }
        //Knock In Terminal
        else if (instrument instanceof KnockInTerminal) {
            Portfolio combination = ((KnockInTerminal) instrument).split();
            return PortfolioTools.portfolioCalcBinomialTree(combination, val, spot, vol, r, q, N);
        }
        //Knock Out Terminal
        else if (instrument instanceof KnockOutTerminal) {
            Portfolio combination = ((KnockOutTerminal) instrument).split();
            return PortfolioTools.portfolioCalcBinomialTree(combination, val, spot, vol, r, q, N);
        }
        //Double Shark Fin Continuous
        else if (instrument instanceof DoubleSharkFinContinuous) {
            Portfolio combination = ((DoubleSharkFinContinuous) instrument).split();
            return PortfolioTools.portfolioCalcBinomialTree(combination, val, spot, vol, r, q, N);
        }
        //Double Shark Fin Terminal
        else if (instrument instanceof DoubleSharkFinTerminal) {
            Portfolio combination = ((DoubleSharkFinTerminal) instrument).split();
            return PortfolioTools.portfolioCalcBinomialTree(combination, val, spot, vol, r, q, N);
        }
        //digital cash
        else if (instrument instanceof DigitalCash) {
            return PricerBinomialTree.price((DigitalCash) instrument, val, spot, vol, r, q, daysInYear, N);
        }
        //vertical spread
        else if (instrument instanceof VerticalSpread) {
            Portfolio combination = ((VerticalSpread) instrument).split();
            return PortfolioTools.portfolioCalcBinomialTree(combination, val, spot, vol, r, q, N);
        }
        //strangle
        else if (instrument instanceof VanillaStrangle) {
            Portfolio combination = ((VanillaStrangle) instrument).split();
            return PortfolioTools.portfolioCalcBinomialTree(combination, val, spot, vol, r, q, N);
        } else
            throw new Exception("Option type " + optnType + " is not supported by Binomial Tree");
    }

    /**
     * This API interface provides a flexible tool for pricing any user defined financial instrument using binomial
     * tree method.
     * Users can define the instrument that needs to be priced using three functions :Payoff, BoundaryConditions
     * and Exercise.
     * One can refer to BinomialTree.java for details of how to define those functions.
     *
     * @param val                valuation date
     * @param expiry             expiry date
     * @param spot               current price of the underlying asset
     * @param r                  risk free interest rate
     * @param q                  annually dividend yield
     * @param vol                volatility
     * @param N                  number of periods in constructing the binomial tree
     * @param payoff             The string of the javascript that defines the Payoff function of the instrument.
     *                           The function takes the spot as the input parameter, and the function name must be
     *                           Payoff, i.e., it must follows the form:"function Payoff(spot){return  ...}"
     * @param boundaryConditions The string of the javascript that defines the BoundaryConditions function of the
     *                           instrument. The function takes {spot,r,t,option} as the  inout parameter, and the
     *                           function name must be BoundaryConditions,i.e., it must follows the form:
     *                           "function BoundaryConditions(spot, r, t, option}{return ...}"
     * @param exercise           The string of the javascript that defines the Exercise function of the instrument.
     *                           The function takes {spot, option} as the input parameter, and the function name must be
     *                           Exercise, i.e., it must follows the form:
     *                           "function Exercise(spot, option){return ..}"
     * @return returns the price of the instrument
     * @throws Exception if failed to price.
     */
    @BctQuantApi(
            name = "qlScriptCalcBinomialTree",
            description = "Computes the instrument price using the given describing functions(payoff, boundary " +
                    "conditions and exercise function) by Binomial Tree ",
            argNames = {"valDate", "expiry", "spot", "r", "q", "vol", "N", "Payoff", "BoundaryConditions", "Exercise"},
            argTypes = {"DateTime", "DateTime", "Double", "Double", "Double", "Double", "Double", "String", "String",
                    "String"},
            argDescriptions = {"Valuation date", "Expiry date of the instrument",
                    "Current spot price", "r", "q", "volatility", "Number of steps", "Instrument payoff javascript",
                    "Instrument boundary condition javascript", "Instrument exercise javascript"},
            retName = "", retDescription = "", retType = "Double"
    )

    public static double calcScript(LocalDateTime val, LocalDateTime expiry, double spot, double r, double q,
                                    double vol, double N, String payoff, String boundaryConditions, String exercise)
            throws Exception {
        double daysInYear = 365.25;
        double tau = val.until(expiry, ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        BinomialTree tree = new BinomialTree(spot, tau, r, q, vol, (int) N);

        return tree.getOptionPrice(payoff(payoff), boundaryConditions(boundaryConditions), exercise(exercise));

    }

    private static DoubleFunction<Double> payoff(String script) throws Exception {

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        engine.eval(script);
        Invocable inv = (Invocable) engine;
        return (double s) -> {
            try {
                return (Double) inv.invokeFunction("Payoff", s);
            } catch (ScriptException e) {
                logger.error("method with given name or matching argument types cannot be found parameter ={}",s);
            } catch (NoSuchMethodException e) {
                logger.error(" method cannot be found  parameter ={}",s);
            }
            return 0.0;
        };
    }


    private static Function<double[], Double> boundaryConditions(String script) throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        engine.eval(script);
        Invocable inv = (Invocable) engine;
        Function<double[], Double> boundaryConditions = (double[] u) -> {
            try {
                return (Double) inv.invokeFunction("BoundaryConditions", u[0], u[1], u[2], u[3]);
            } catch (ScriptException e) {
                logger.error("method with given name or matching argument types cannot be found parameter ={}",u[0]+ u[1]+u[2]+ u[3]);
            } catch (NoSuchMethodException e) {
                logger.error(" method cannot be found  parameter ={}",u[0]+ u[1]+u[2]+ u[3]);
            }
            return 0.0;
        };
        return boundaryConditions;
    }


    private static BiFunction<Double, Double, Double> exercise(String script) throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        engine.eval(script);
        Invocable inv = (Invocable) engine;
        BiFunction<Double, Double, Double> exercise = (Double s, Double p) -> {
            try {
                return (Double) inv.invokeFunction("Exercise", s, p);
            } catch (ScriptException e) {
                logger.error("method with given name or matching argument types cannot be found parameter ={}",p+s);
            } catch (NoSuchMethodException e) {
                logger.error(" method cannot be found  parameter ={}",p+s);
            }
            return 0.0;
        };
        return exercise;
    }
}


