package tech.tongyu.bct.service.quantlib.pricer;

import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.enums.ExerciseType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.enums.RebateType;
import tech.tongyu.bct.service.quantlib.common.numerics.tree.BinomialTree;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.function.Function;

import static java.lang.Math.exp;
import static tech.tongyu.bct.service.quantlib.common.utils.OptionUtility.getPayoff;

/**
 * @author Liao Song
 * @since 2016-8-4
 */
public class PricerBinomialTree {
    public static double price(VanillaEuropean instrument,
                               LocalDateTime val,
                               double spot, double vol, double r, double q, double daysInYear, int n) throws Exception {
        double tau = val.until(instrument.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        BinomialTree tree = new BinomialTree(spot, tau, r, q, vol, n);

        DoubleFunction<Double> payoffFunction = getPayoff(instrument);
        Function<double[], Double> boundaryConditions = BinomialTree.getBoundaryConditions(instrument);
        BiFunction<Double, Double, Double> exerciseFunction = BinomialTree.getExercise(instrument);

        return tree.getOptionPrice(payoffFunction, boundaryConditions, exerciseFunction);
    }

    public static double price(VanillaAmerican instrument,
                               LocalDateTime val,
                               double spot, double vol, double r, double q, double daysInYear, int n) throws Exception {
        double tau = val.until(instrument.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        BinomialTree tree = new BinomialTree(spot, tau, r, q, vol, n);

        DoubleFunction<Double> payoffFunction = getPayoff(instrument);
        Function<double[], Double> boundaryConditions = BinomialTree.getBoundaryConditions(instrument);
        BiFunction<Double, Double, Double> exerciseFunction = BinomialTree.getExercise(instrument);

        return tree.getOptionPrice(payoffFunction, boundaryConditions, exerciseFunction);

    }

    public static double price(DigitalCash instrument, LocalDateTime val,
                               double spot, double vol, double r, double q, double daysInYear, int n) throws Exception{
        double tau = val.until(instrument.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        BinomialTree tree = new BinomialTree(spot, tau, r, q, vol, n);

        DoubleFunction<Double> payoffFunction = getPayoff(instrument);
        Function<double[], Double> boundaryConditions = BinomialTree.getBoundaryConditions(instrument);
        BiFunction<Double, Double, Double> exerciseFunction = BinomialTree.getExercise(instrument);

        return tree.getOptionPrice(payoffFunction, boundaryConditions, exerciseFunction);
    }

    public static double price(KnockOutContinuous instrument,
                               LocalDateTime val,
                               double spot, double vol, double r, double q, double daysInYear, int n) throws Exception {

        if (val.until(instrument.getBarrierStart(), ChronoUnit.NANOS) / Constants.NANOSINDAY > 0)
            throw new Exception("Barrier must start on or before the valuation date.");
        if (instrument.getExpiry().until(instrument.getBarrierEnd(), ChronoUnit.NANOS) / Constants.NANOSINDAY < 0)
            throw new Exception("Barrier must end on or after the option expiry.");

        double tau = val.until(instrument.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        BinomialTree tree = new BinomialTree(spot, tau, r, q, vol, n);

        DoubleFunction<Double> payoffFunction = getPayoff(instrument);
        Function<double[], Double> boundaryConditions = BinomialTree.getBoundaryConditions(instrument);
        BiFunction<Double, Double, Double> exerciseFunction = BinomialTree.getExercise(instrument);

        return tree.getOptionPrice(payoffFunction, boundaryConditions, exerciseFunction);


    }

    public static double price(KnockInContinuous instrument,
                               LocalDateTime val,
                               double spot, double vol, double r, double q, double daysInYear, int n) throws Exception {

        if (val.until(instrument.getBarrierStart(), ChronoUnit.NANOS) / Constants.NANOSINDAY > 0)
            throw new Exception("Barrier must start on or before the valuation date.");
        if (instrument.getExpiry().until(instrument.getBarrierEnd(), ChronoUnit.NANOS) / Constants.NANOSINDAY < 0)
            throw new Exception("Barrier must end on or after the option expiry.");
        if (instrument.getExercise() == ExerciseType.AMERICAN)
            throw new Exception("The exercise type for Knock-in option with continuous monitoring must be European to " +
                    "use Binomial Tree pricing method.");

        double tau = val.until(instrument.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        double strike = instrument.getStrike();
        double rebate = instrument.getRebateAmount();
        double barrier = instrument.getBarrier();
        OptionType type = instrument.getType();
        ExerciseType exercisetype = instrument.getExercise();

        LocalDateTime barrierstart = instrument.getBarrierStart();
        LocalDateTime barrierend = instrument.getBarrierEnd();
        LocalDateTime expiry = instrument.getExpiry();
        LocalDateTime delivery = instrument.getDelivery();
        BarrierDirection direction = instrument.getBarrierDirection();
        BarrierDirection direction2;
        if (direction == BarrierDirection.DOWN_AND_IN) direction2 = BarrierDirection.DOWN_AND_OUT;
        else direction2 = BarrierDirection.UP_AND_OUT;


        VanillaEuropean europeanoption = new VanillaEuropean(type, strike, expiry, delivery);
        KnockOutContinuous knockoutoption = new KnockOutContinuous(type, strike, expiry, delivery, barrier, direction2,
                barrierstart, barrierend, rebate, RebateType.PAY_AT_EXPIRY, exercisetype);


        BinomialTree tree = new BinomialTree(spot, tau, r, q, vol, n);
        return tree.getOptionPrice(getPayoff(europeanoption),
                BinomialTree.getBoundaryConditions(europeanoption), BinomialTree.getExercise(europeanoption))
                + rebate * exp(-r * tau)
                - tree.getOptionPrice(getPayoff(knockoutoption),
                BinomialTree.getBoundaryConditions(knockoutoption), BinomialTree.getExercise(knockoutoption));
    }

    public static double price(DoubleKnockOutContinuous instrument,
                               LocalDateTime val,
                               double spot, double vol, double r, double q, double daysInYear, int n) throws Exception {
        double tau = val.until(instrument.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        BinomialTree tree = new BinomialTree(spot, tau, r, q, vol, n);

        DoubleFunction<Double> payoffFunction = getPayoff(instrument);
        Function<double[], Double> boundaryConditions = BinomialTree.getBoundaryConditions(instrument);
        BiFunction<Double, Double, Double> exerciseFunction = BinomialTree.getExercise(instrument);

        return tree.getOptionPrice(payoffFunction, boundaryConditions, exerciseFunction);

    }


}
