package tech.tongyu.bct.service.quantlib.common.utils;

import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.enums.ExerciseType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.enums.RebateType;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.*;

import java.time.LocalDateTime;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;

import static java.lang.Math.exp;
import static java.lang.Math.max;

/**
 * @author Liao Song
 * @since 2016-8-30
 */
public class OptionUtility {

    /**
     * get the payoff function of the given instrument
     *
     * @param instrument the financial instrument
     * @return the payoff function
     */
    public static DoubleFunction<Double> getPayoff(Object instrument) throws Exception {
        //vanilla European
        if (instrument instanceof VanillaEuropean) {
            if (((VanillaEuropean) instrument).getType() == OptionType.CALL)
                return s -> max(s - ((VanillaEuropean) instrument).getStrike(), 0);
            else return s -> max(((VanillaEuropean) instrument).getStrike() - s, 0);
        }
        //vanilla American
        else if (instrument instanceof VanillaAmerican) {
            if (((VanillaAmerican) instrument).getType() == OptionType.CALL)
                return s -> max(s - ((VanillaAmerican) instrument).getStrike(), 0);
            else return s -> max(((VanillaAmerican) instrument).getStrike() - s, 0);
        }
        //knock in continuous
        else if (instrument instanceof KnockInContinuous) {
            if (((KnockInContinuous) instrument).getType() == OptionType.CALL)
                return s -> max(s - ((KnockInContinuous) instrument).getStrike(), 0);
            else return s -> max(((KnockInContinuous) instrument).getStrike() - s, 0);
        }
        //knock out continuous
        else if (instrument instanceof KnockOutContinuous) {
            if (((KnockOutContinuous) instrument).getType() == OptionType.CALL)
                return s -> max(s - ((KnockOutContinuous) instrument).getStrike(), 0);
            else return s -> max(((KnockOutContinuous) instrument).getStrike() - s, 0);
        }
        //double knock out
        else if (instrument instanceof DoubleKnockOutContinuous) {
            if (((DoubleKnockOutContinuous) instrument).getType() == OptionType.CALL)
                return s -> max(s - ((DoubleKnockOutContinuous) instrument).getStrike(), 0);
            else return s -> max(((DoubleKnockOutContinuous) instrument).getStrike() - s, 0);
        }

        //digital cash
        else if (instrument instanceof DigitalCash) {
            double strike = ((DigitalCash) instrument).getStrike();
            double payment = ((DigitalCash) instrument).getPayment();
            if (((DigitalCash) instrument).getType() == OptionType.CALL)
                return s -> s > strike ? payment : 0;
            else
                return s -> s < strike ? payment : 0;
        }
        throw new Exception("This option is not supported by Binomial Tree (payoff)");

    }


    /**
     * get the function that returns the intrinsic value of the given instrument.
     *
     * @param instrument the financial instrument
     * @param tau        time to payment
     * @param r          risk free interest rate
     * @param q          annually dividend yield
     * @return return the payoff function f(s)
     */
    public static DoubleFunction<Double> payoffFunction(Object instrument, double tau, double r, double q) {
        //digital cash
        if (instrument instanceof DigitalCash) {
            double payment = ((DigitalCash) instrument).getPayment();
            OptionType type = ((DigitalCash) instrument).getType();
            double strike = ((DigitalCash) instrument).getStrike();
            if (type == OptionType.CALL)
                return spot -> spot * exp((r - q) * tau) <= strike ? 0 : payment * exp(-r * tau);
            else
                return spot -> spot * exp((r - q) * tau) >= strike ? 0 : payment * exp(-r * tau);
        }
        //vanilla European
        else if (instrument instanceof VanillaEuropean) {
            double strike = ((VanillaEuropean) instrument).getStrike();
            OptionType type = ((VanillaEuropean) instrument).getType();
            if (type == OptionType.CALL) {
                return spot -> max(spot * exp(-q * tau) - strike * exp(-r * tau), 0);
            } else {
                return spot -> max(strike * exp(-r * tau) - spot * exp(-q * tau), 0);
            }
        }
        //vanilla American
        else if (instrument instanceof VanillaAmerican) {
            double strike = ((VanillaAmerican) instrument).getStrike();
            OptionType type = ((VanillaAmerican) instrument).getType();
            if (type == OptionType.CALL) {
                return spot -> max(spot * exp(-q * tau) - strike * exp(-r * tau), 0);
            } else {
                return spot -> max(strike * exp(-r * tau) - spot * exp(-q * tau), 0);
            }
        }
        //knock in continuous
        else if (instrument instanceof KnockInContinuous) {
            double strike = ((KnockInContinuous) instrument).getStrike();
            OptionType type = ((KnockInContinuous) instrument).getType();
            if (type == OptionType.CALL) {
                return spot -> isKnockedIn((KnockInContinuous) instrument, spot)
                        ? max(spot * exp(-q * tau) - strike * exp(-r * tau), 0) : rebateValue(instrument, spot, tau, r);
            } else {
                return spot -> isKnockedIn((KnockInContinuous) instrument, spot)
                        ? max(strike * exp(-r * tau) - spot * exp(-q * tau), 0) : rebateValue(instrument, spot, tau, r);
            }
        }
        //knock out continuous
        else if (instrument instanceof KnockOutContinuous) {
            double strike = ((KnockOutContinuous) instrument).getStrike();
            OptionType type = ((KnockOutContinuous) instrument).getType();
            if (type == OptionType.CALL) {
                return spot -> isKnockedOut(instrument, spot) ? rebateValue(instrument, spot, tau, r)
                        : max(spot * exp(-q * tau) - strike * exp(-r * tau), 0);
            } else {
                return spot -> isKnockedOut(instrument, spot) ? rebateValue(instrument, spot, tau, r)
                        : max(strike * exp(-r * tau) - spot * exp(-q * tau), 0);
            }
        }
        // if (instrument instanceof DoubleKnockOutContinuous)
        else {
            double strike = ((DoubleKnockOutContinuous) instrument).getStrike();
            OptionType type = ((DoubleKnockOutContinuous) instrument).getType();
            if (type == OptionType.CALL) {
                return spot -> isKnockedOut(instrument, spot) ? rebateValue(instrument, spot, tau, r)
                        : max(spot * exp(-q * tau) - strike * exp(-r * tau), 0);
            } else {
                return spot -> isKnockedOut(instrument, spot) ? rebateValue(instrument, spot, tau, r)
                        : max(strike * exp(-r * tau) - spot * exp(-q * tau), 0);
            }
        }


    }


    /**
     * gets the immediate exercise function according to the exercise type:
     *
     * @return an array consisting of the max elements of the two input arrays if early exercise is permitted,
     * else return the original array
     * @throws Exception if the instrument is not supported
     */
    public static BiFunction<Double, Double, Double> exerciseFunction(Object instrument, double r, double q)
            throws Exception {
        DoubleFunction<Double> f = payoffFunction(instrument, 0, r, q);

        // vanilla European option
        if (instrument instanceof VanillaEuropean) {
            return (p, s) -> p;
        }
        // digital cash
        if (instrument instanceof DigitalCash) {
            return (p, s) -> p;
        }

        // vanilla American option
        else if (instrument instanceof VanillaAmerican) {
            return (p, s) -> max(p, f.apply(s));
        }

        //  Knock-out option (with continuous monitoring)
        else if (instrument instanceof KnockOutContinuous) {
            ExerciseType exerciseType = ((KnockOutContinuous) instrument).getExercise();
            if (exerciseType == ExerciseType.EUROPEAN) {
                return (p, s) -> p;
            } else {
                return (p, s) -> isKnockedOut(instrument, s) ?
                        p : max(p, f.apply(s));
            }
        }

        // double knock-out (with continuous monitoring)
        else if (instrument instanceof DoubleKnockOutContinuous) {
            ExerciseType exerciseType = ((DoubleKnockOutContinuous) instrument).getExercise();
            if (exerciseType == ExerciseType.EUROPEAN) {
                return (p, s) -> p;
            } else {
                return (p, s) -> isKnockedOut(instrument, s) ?
                        p : max(p, f.apply(s));
            }
        } else throw new Exception("Unsupported instrument: BoundaryConstructor::getExerciseFunction");
    }

    /**
     * intrinsic value of the rebate
     *
     * @param instrument financial instrument
     * @param spot       current spot
     * @param tau        time to maturity
     * @param r          risk free interest rate
     * @return returns the rebate intrinsic value at the valuation time.
     */
    public static double rebateValue(Object instrument, double spot, double tau, double r) {
        if (instrument instanceof KnockOutContinuous) {
            if (isKnockedOut(instrument, spot)) {
                RebateType rebateType = ((KnockOutContinuous) instrument).getRebateType();
                double rebateAmount = ((KnockOutContinuous) instrument).getRebateAmount();
                if (rebateType == RebateType.PAY_WHEN_HIT)
                    tau = 0.0;
                return rebateAmount * exp(-r * tau);
            }
        } else if (instrument instanceof KnockInContinuous) {
            if (!isKnockedIn((KnockInContinuous) instrument, spot)) {
                double rebateAmount = ((KnockInContinuous) instrument).getRebateAmount();
                return rebateAmount * exp(-r * tau);
            }
        } else if (instrument instanceof DoubleKnockOutContinuous) {
            if (isKnockedOut(instrument, spot)) {
                double upperBarrier = ((DoubleKnockOutContinuous) instrument).getUpperBarrier();
                double lowerBarrier = ((DoubleKnockOutContinuous) instrument).getLowerBarrier();
                double upperRebate = ((DoubleKnockOutContinuous) instrument).getUpperRebate();
                double lowerRebate = ((DoubleKnockOutContinuous) instrument).getLowerRebate();
                RebateType rebateType = ((DoubleKnockOutContinuous) instrument).getRebateType();
                if (rebateType == RebateType.PAY_WHEN_HIT)
                    tau = 0.0;
                if (spot >= upperBarrier)
                    return upperRebate * exp(-r * tau);
                else if (spot <= lowerBarrier)
                    return lowerRebate * exp(-r * tau);
            }
        }
        return 0;

    }

    /**
     * check if the option is knocked out or not for knock-out options
     *
     * @param option the knock-out option (with continuous monitoring)
     * @param spot   the current spot price
     * @return true if knocked out , false if not
     */
    public static boolean isKnockedOut(Object option, double spot) {
        if (option instanceof KnockOutContinuous) {
            BarrierDirection direction = ((KnockOutContinuous) option).getBarrierDirection();
            double barrier = ((KnockOutContinuous) option).getBarrier();
            return ((direction == BarrierDirection.UP_AND_OUT && spot >= barrier) ||
                    (direction == BarrierDirection.DOWN_AND_OUT && spot <= barrier));
        }
        //double knock out
        else {
            double upperBarrier = ((DoubleKnockOutContinuous) option).getUpperBarrier();
            double lowerBarrier = ((DoubleKnockOutContinuous) option).getLowerBarrier();
            return (spot >= upperBarrier || spot <= lowerBarrier);
        }


    }

    /**
     * check if the option is knocked in or not for knock-in options
     *
     * @param option the knock in option (with continuous monitoring)
     * @param spot   the current spot price
     * @return true if knocked in, false if not
     */
    public static boolean isKnockedIn(KnockInContinuous option, double spot) {
        BarrierDirection direction = option.getBarrierDirection();
        double barrier = option.getBarrier();
        return ((direction == BarrierDirection.DOWN_AND_IN && spot <= barrier) ||
                (direction == BarrierDirection.UP_AND_IN && spot >= barrier));
    }

    /**
     * Constructs the knockout option paired with the given knock-in option
     *
     * @param option the knock in option
     * @return the knock out option
     */
    public static KnockOutContinuous getPairedKnockOutOption(KnockInContinuous option) {
        double barrier = option.getBarrier();
        double strike = option.getStrike();
        double rebate = option.getRebateAmount();
        LocalDateTime expiry = option.getExpiry();
        LocalDateTime delivery = option.getDelivery();
        LocalDateTime barrierStart = option.getBarrierStart();
        LocalDateTime barrierEnd = option.getBarrierEnd();
        ExerciseType exerciseType = option.getExercise();
        OptionType type = option.getType();

        BarrierDirection barrierDirection = option.getBarrierDirection();
        if (barrierDirection == BarrierDirection.DOWN_AND_IN)
            barrierDirection = BarrierDirection.DOWN_AND_OUT;
        else if (barrierDirection == BarrierDirection.UP_AND_IN)
            barrierDirection = BarrierDirection.UP_AND_OUT;

        return new KnockOutContinuous(type, strike, expiry, delivery, barrier, barrierDirection,
                barrierStart, barrierEnd, rebate, RebateType.PAY_AT_EXPIRY, exerciseType);
    }

    public static VanillaEuropean getPairedVanillaEuropeanOption(KnockInContinuous option) {
        double strike = option.getStrike();
        LocalDateTime expiry = option.getExpiry();
        LocalDateTime delivery = option.getDelivery();
        OptionType type = option.getType();
        return new VanillaEuropean(type, strike, expiry, delivery);
    }

    public static VanillaAmerican getPairedVanillaAmericanOption(KnockInContinuous option) {
        double strike = option.getStrike();
        LocalDateTime expiry = option.getExpiry();
        OptionType type = option.getType();
        return new VanillaAmerican(strike, type, expiry);
    }
}
