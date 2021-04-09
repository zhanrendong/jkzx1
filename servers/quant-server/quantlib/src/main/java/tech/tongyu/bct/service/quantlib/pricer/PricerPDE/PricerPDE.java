package tech.tongyu.bct.service.quantlib.pricer.PricerPDE;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.linear.RealMatrix;
import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.ExerciseType;
import tech.tongyu.bct.service.quantlib.common.numerics.black.KnockOut;
import tech.tongyu.bct.service.quantlib.common.numerics.pde.PDESolver;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;
import tech.tongyu.bct.service.quantlib.common.utils.OptionUtility;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.BiFunction;

import static java.lang.Math.*;

/**
 * Created by Liao Song on 16-8-26.
 * option pricer by solving PDE using finite different method
 **/
public class PricerPDE {

    public static double price(VanillaEuropean option, LocalDateTime val,
                               double spot, double r, double q, double vol,
                               double daysInYear,
                               double eps, double alpha, Map<String, Object> params) throws Exception {
        //check if tau = 0 or vol = 0
        double tau = val.until(option.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        if (tau == 0 || vol == 0)
            return OptionUtility.payoffFunction(option, tau, r, q).apply(spot);

        //check if strike >> spot, in this case, the grid can not be constructed
        if (eps > 0 && (20.0 * option.getStrike() / sqrt(eps) / spot) > (Integer.MAX_VALUE - 10))
            return OptionUtility.payoffFunction(option, tau, r, q).apply(spot);

        double[] s = GridConstructor.getSpotArray(option, spot, eps, params);
        double[] t = GridConstructor.getTimeArray(option, val, daysInYear, eps, params);
        BoundaryConstructor boundary = new BoundaryConstructor(option, val, r, q, daysInYear);

        BiFunction<Double, Double, Double> Ab = boundary.getFirstOrderDerivFunction();
        BiFunction<Double, Double, Double> Bb = boundary.getFirstOrderFunction();
        BiFunction<Double, Double, Double> Cb = boundary.getConstFunction();

        BiFunction<Double, Double, Double> A = BSPDEConstructor.getFirstOrderDerivFunction(r, q, vol);
        BiFunction<Double, Double, Double> B = BSPDEConstructor.getSecondOrderDerivFunction(r, q, vol);
        BiFunction<Double, Double, Double> C = BSPDEConstructor.getFirstOrderFunction(r, q, vol);
        BiFunction<Double, Double, Double> D = BSPDEConstructor.getConstFunction(r, q, vol);

        RealMatrix f = PDESolver.getGridValues(A, B, C, D, Ab, Bb, Cb, OptionUtility.exerciseFunction(option, r, q), s, t, alpha);

        LinearInterpolator LI = new LinearInterpolator();
        return LI.interpolate(s, f.getColumnVector(0).toArray()).value(spot);

    }

    public static double price(DigitalCash option, LocalDateTime val,
                               double spot, double r, double q, double vol,
                               double daysInYear,
                               double eps, double alpha, Map<String, Object> params) throws Exception {
        //check if tau =0
        double tau = val.until(option.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        if (tau == 0 || vol == 0)
            return OptionUtility.payoffFunction(option, tau, r, q).apply(spot);

        //check if strike >> spot, in this case, the grid can not be constructed
        if (eps > 0 && (20.0 * option.getStrike() / sqrt(eps) / spot) > (Integer.MAX_VALUE - 10))
            return OptionUtility.payoffFunction(option, tau, r, q).apply(spot);

        double[] s = GridConstructor.getSpotArray(option, spot, eps, params);
        double[] t = GridConstructor.getTimeArray(option, val, daysInYear, eps, params);
        BoundaryConstructor boundary = new BoundaryConstructor(option, val, r, q, daysInYear);

        BiFunction<Double, Double, Double> Ab = boundary.getFirstOrderDerivFunction();
        BiFunction<Double, Double, Double> Bb = boundary.getFirstOrderFunction();
        BiFunction<Double, Double, Double> Cb = boundary.getConstFunction();

        BiFunction<Double, Double, Double> A = BSPDEConstructor.getFirstOrderDerivFunction(r, q, vol);
        BiFunction<Double, Double, Double> B = BSPDEConstructor.getSecondOrderDerivFunction(r, q, vol);
        BiFunction<Double, Double, Double> C = BSPDEConstructor.getFirstOrderFunction(r, q, vol);
        BiFunction<Double, Double, Double> D = BSPDEConstructor.getConstFunction(r, q, vol);

        RealMatrix f = PDESolver.getGridValues(A, B, C, D, Ab, Bb, Cb, OptionUtility.exerciseFunction(option, r, q), s, t, alpha);

        LinearInterpolator LI = new LinearInterpolator();
        return LI.interpolate(s, f.getColumnVector(0).toArray()).value(spot);

    }

    public static double price(VanillaAmerican option, LocalDateTime val,
                               double spot, double r, double q, double vol,
                               double daysInYear,
                               double eps, double alpha, Map<String, Object> params) throws Exception {
        //check if tau = 0
        double tau = val.until(option.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        if (tau == 0)
            return OptionUtility.payoffFunction(option, tau, r, q).apply(spot);

        //check if strike >> spot, in this case, the spot grid can not be constructed.
        if (eps > 0 && (2.0 * option.getStrike() / sqrt(eps) / spot) > (Integer.MAX_VALUE - 10))
            return OptionUtility.payoffFunction(option, tau, r, q).apply(spot);

        double[] s = GridConstructor.getSpotArray(option, spot, eps, params);
        double[] t = GridConstructor.getTimeArray(option, val, daysInYear, eps, params);

        BoundaryConstructor boundary = new BoundaryConstructor(option, val, r, q, daysInYear);

        BiFunction<Double, Double, Double> Ab = boundary.getFirstOrderDerivFunction();
        BiFunction<Double, Double, Double> Bb = boundary.getFirstOrderFunction();
        BiFunction<Double, Double, Double> Cb = boundary.getConstFunction();


        BiFunction<Double, Double, Double> A = BSPDEConstructor.getFirstOrderDerivFunction(r, q, vol);
        BiFunction<Double, Double, Double> B = BSPDEConstructor.getSecondOrderDerivFunction(r, q, vol);
        BiFunction<Double, Double, Double> C = BSPDEConstructor.getFirstOrderFunction(r, q, vol);
        BiFunction<Double, Double, Double> D = BSPDEConstructor.getConstFunction(r, q, vol);

        RealMatrix f = PDESolver.getGridValues(A, B, C, D, Ab, Bb, Cb, OptionUtility.exerciseFunction(option, r, q), s, t, alpha);

        LinearInterpolator LI = new LinearInterpolator();
        return LI.interpolate(s, f.getColumnVector(0).toArray()).value(spot);

    }

    public static double price(KnockOutContinuous option, LocalDateTime val,
                               double spot, double r, double q, double vol,
                               double daysInYear,
                               double eps, double alpha, Map<String, Object> params) throws Exception {


        double tau = val.until(option.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        //check if already knocked out or tau =0
        if (OptionUtility.isKnockedOut(option, spot) || tau == 0)
            return OptionUtility.payoffFunction(option, tau, r, q).apply(spot);

        if (vol * vol < 1e-4*abs(r-q))
            return KnockOut.calc(CalcType.PRICE, option.getType(), option.getBarrierDirection(), option.getRebateType(),
                    option.getBarrier(), option.getRebateAmount(), spot, option.getStrike(), vol, tau, r, q);

        //for UP_AND_OUT,check if barrier is too high, when barrier is too high , the option can be regarded as vanilla
        if (option.getBarrierDirection() == BarrierDirection.UP_AND_OUT && eps > 0
                && (option.getBarrier() / sqrt(eps) / spot > (Integer.MAX_VALUE - 10))) {
            if (option.getExercise() == ExerciseType.AMERICAN) {
                VanillaAmerican newOption = new VanillaAmerican(option.getStrike(), option.getType(), option.getExpiry());
                return price(newOption, val, spot, r, q, vol, daysInYear, eps, alpha, params);
            } else {
                VanillaEuropean newOption = new VanillaEuropean(option.getType(), option.getStrike(), option.getExpiry(),
                        option.getDelivery());
                return price(newOption, val, spot, r, q, vol, daysInYear, eps, alpha, params);
            }

        }

        double[] s = GridConstructor.getSpotArray(option, spot, eps, params);
        double[] t = GridConstructor.getTimeArray(option, val, daysInYear, eps, params);

        BoundaryConstructor boundary = new BoundaryConstructor(option, val, r, q, daysInYear);

        BiFunction<Double, Double, Double> Ab = boundary.getFirstOrderDerivFunction();
        BiFunction<Double, Double, Double> Bb = boundary.getFirstOrderFunction();
        BiFunction<Double, Double, Double> Cb = boundary.getConstFunction();

        BiFunction<Double, Double, Double> A = BSPDEConstructor.getFirstOrderDerivFunction(r, q, vol);
        BiFunction<Double, Double, Double> B = BSPDEConstructor.getSecondOrderDerivFunction(r, q, vol);
        BiFunction<Double, Double, Double> C = BSPDEConstructor.getFirstOrderFunction(r, q, vol);
        BiFunction<Double, Double, Double> D = BSPDEConstructor.getConstFunction(r, q, vol);

        RealMatrix f = PDESolver.getGridValues(A, B, C, D, Ab, Bb, Cb, OptionUtility.exerciseFunction(option, r, q), s, t, alpha);

        LinearInterpolator LI = new LinearInterpolator();
        return LI.interpolate(s, f.getColumnVector(0).toArray()).value(spot);


    }

    public static double price(KnockInContinuous option, LocalDateTime val,
                               double spot, double r, double q, double vol,
                               double daysInYear, double eps, double alpha,
                               Map<String, Object> params) throws Exception {

        double tau = val.until(option.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        //check if tau =0
        if (tau == 0)
            return OptionUtility.payoffFunction(option, tau, r, q).apply(spot);

        double rebateAmount = option.getRebateAmount();

        if (option.getExercise() == ExerciseType.AMERICAN) {
            VanillaAmerican vanillaAmericanOption = OptionUtility.getPairedVanillaAmericanOption(option);
            if (OptionUtility.isKnockedIn(option, spot)) {
                return price(vanillaAmericanOption, val, spot, r, q, vol, daysInYear, eps, alpha, params);
            } else {

                KnockOutContinuous knockOutOption = OptionUtility.getPairedKnockOutOption(option);
                return price(vanillaAmericanOption, val, spot, r, q, vol, daysInYear, eps, alpha, params)
                        + rebateAmount * exp(-r * tau)
                        - price(knockOutOption, val, spot, r, q, vol, daysInYear, eps, alpha, params);
            }
        } else {
            VanillaEuropean vanillaEuropeanOption = OptionUtility.getPairedVanillaEuropeanOption(option);
            if (OptionUtility.isKnockedIn(option, spot)) {
                return price(vanillaEuropeanOption, val, spot, r, q, vol, daysInYear, eps, alpha, params);
            } else {

                KnockOutContinuous knockOutOption = OptionUtility.getPairedKnockOutOption(option);
                return price(vanillaEuropeanOption, val, spot, r, q, vol, daysInYear, eps, alpha, params)
                        + rebateAmount * exp(-r * tau)
                        - price(knockOutOption, val, spot, r, q, vol, daysInYear, eps, alpha, params);
            }
        }
    }

    public static double price(DoubleKnockOutContinuous option, LocalDateTime val,
                               double spot, double r, double q, double vol,
                               double daysInYear, double eps, double alpha,
                               Map<String, Object> params) throws Exception {

        double tau = val.until(option.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        //check if tau = 0 or if the option is already knocked out
        if (OptionUtility.isKnockedOut(option, spot) || tau == 0)
            return OptionUtility.payoffFunction(option, tau, r, q).apply(spot);

        //check if upper barrier is too high,in this case, the option can be approximated as down and out option
        if (eps > 0 && option.getUpperBarrier() / sqrt(eps) / spot > (Integer.MAX_VALUE - 10)) {
            KnockOutContinuous newOption = new KnockOutContinuous(option.getType(), option.getStrike(), option.getExpiry(),
                    option.getDelivery(), option.getLowerBarrier(), BarrierDirection.DOWN_AND_OUT, option.getBarrierStart(),
                    option.getBarrierEnd(), option.getLowerRebate(), option.getRebateType(), option.getExercise());
            return price(newOption, val, spot, r, q, vol, daysInYear, eps, alpha, params);
        }

        double[] s = GridConstructor.getSpotArray(option, spot, eps, params);
        double[] t = GridConstructor.getTimeArray(option, val, daysInYear, eps, params);

        BoundaryConstructor boundary = new BoundaryConstructor(option, val, r, q, daysInYear);

        BiFunction<Double, Double, Double> Ab = boundary.getFirstOrderDerivFunction();
        BiFunction<Double, Double, Double> Bb = boundary.getFirstOrderFunction();
        BiFunction<Double, Double, Double> Cb = boundary.getConstFunction();

        BiFunction<Double, Double, Double> A = BSPDEConstructor.getFirstOrderDerivFunction(r, q, vol);
        BiFunction<Double, Double, Double> B = BSPDEConstructor.getSecondOrderDerivFunction(r, q, vol);
        BiFunction<Double, Double, Double> C = BSPDEConstructor.getFirstOrderFunction(r, q, vol);
        BiFunction<Double, Double, Double> D = BSPDEConstructor.getConstFunction(r, q, vol);

        RealMatrix f = PDESolver.getGridValues(A, B, C, D, Ab, Bb, Cb, OptionUtility.exerciseFunction(option, r, q), s, t, alpha);

        LinearInterpolator LI = new LinearInterpolator();
        return LI.interpolate(s, f.getColumnVector(0).toArray()).value(spot);


    }

}

