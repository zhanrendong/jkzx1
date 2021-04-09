package tech.tongyu.bct.service.quantlib.pricer.PricerPDE;

import tech.tongyu.bct.service.quantlib.common.utils.Constants;
import tech.tongyu.bct.service.quantlib.common.utils.OptionUtility;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.BiFunction;

/**
 * @author Liao Song
 * @since 2016-8-23
 * constructs boundary conditions:
 * A df/ds + Bf + C= 0
 * according to the given instrument
 */
class BoundaryConstructor {
    private Object instrument;
    private LocalDateTime val;
    private double r;
    private double q;
    private double daysInYear;

    BoundaryConstructor(Object instrument, LocalDateTime val, double r, double q, double daysInYear) {
        this.instrument = instrument;
        this.r = r;
        this.q = q;
        this.val = val;
        this.daysInYear = daysInYear;
    }

    /**
     * gets the first order derivative coefficient function A(s,t) in the boundary conditions:A df/ds + Bf + C= 0
     * according to the given instrument
     *
     * @return returns the first order derivative coefficient function A(s,t)
     * @throws Exception if the instrument is not supported
     */
    BiFunction<Double, Double, Double> getFirstOrderDerivFunction() throws Exception {

        // vanilla European option

        if (instrument instanceof VanillaEuropean) {
            return (s, t) -> 0.0;
        }
        //digital cash
        if (instrument instanceof DigitalCash) {
            return (s, t) -> 0.0;
        }

        // vanilla American option

        else if (instrument instanceof VanillaAmerican) {
            return (s, t) -> 0.0;
        }

        // Knock-out option (with continuous monitoring )

        else if (instrument instanceof KnockOutContinuous) {
            return (s, t) -> 0.0;
        }

        // double knock-out option(with continuous monitoring)

        else if (instrument instanceof DoubleKnockOutContinuous) {
            return (s, t) -> 0.0;
        } else throw new Exception("Unsupported instrument: BoundaryConstructor::getFirstOrderDerivFunction");
    }

    /**
     * gets the first order coefficient function B(s,t)  in the boundary conditions: A df/ds + Bf + C= 0
     * according to the given instrument
     *
     * @return returns the first order coefficient function B(s,t)
     * @throws Exception if the instrument is not supported
     */

    BiFunction<Double, Double, Double> getFirstOrderFunction() throws Exception {

        // vanilla European option

        if (instrument instanceof VanillaEuropean) {
            return (s, t) -> 1.0;
        }
        //digital cash
        if (instrument instanceof DigitalCash) {
            return (s, t) -> 1.0;
        }

        // vanilla American option

        else if (instrument instanceof VanillaAmerican) {
            return (s, t) -> 1.0;
        }

        // Knock-out option(with continuous monitoring)

        else if (instrument instanceof KnockOutContinuous) {
            return (s, t) -> 1.0;
        }

        // double knock-out(with continuous monitoring)

        else if (instrument instanceof DoubleKnockOutContinuous) {
            return (s, t) -> 1.0;
        } else throw new Exception("Unsupported instrument: BoundaryConstructor::getFirstOrderFunction");
    }

    /**
     * gets the constant term in the boundary conditions: Adf/ds + Bf + C = 0
     * according to the given instrument
     *
     * @return returns the constant term C(s,t)
     * @throws Exception if the instrument is not supported
     */
    BiFunction<Double, Double, Double> getConstFunction() throws Exception {


        // vanilla European option

        if (instrument instanceof VanillaEuropean) {
            double tau = val.until(((VanillaEuropean) instrument).getExpiry(), ChronoUnit.NANOS)
                    / Constants.NANOSINDAY / daysInYear;
            return (s, t) -> -OptionUtility.payoffFunction(instrument, tau - t, r, q).apply(s);
        }

        // digital cash

        if (instrument instanceof DigitalCash) {
            double tau = val.until(((DigitalCash) instrument).getExpiry(), ChronoUnit.NANOS)
                    / Constants.NANOSINDAY / daysInYear;
            return (s, t) -> -OptionUtility.payoffFunction(instrument, tau - t, r, q).apply(s);
        }


        // vanilla American option

        else if (instrument instanceof VanillaAmerican) {
            double tau = val.until(((VanillaAmerican) instrument).getExpiry(), ChronoUnit.NANOS)
                    / Constants.NANOSINDAY / daysInYear;
            return (s, t) -> -OptionUtility.payoffFunction(instrument, tau - t, r, q).apply(s);
        }

        //  Knock-out option  (with continuous monitoring)

        else if (instrument instanceof KnockOutContinuous) {
            double tau = val.until(((KnockOutContinuous) instrument).getExpiry(), ChronoUnit.NANOS)
                    / Constants.NANOSINDAY / daysInYear;
            return (s, t) -> -OptionUtility.payoffFunction(instrument, tau - t, r, q).apply(s);
        }

        // Double knock-out (with continuous monitoring)

        else if (instrument instanceof DoubleKnockOutContinuous) {
            double tau = val.until(((DoubleKnockOutContinuous) instrument).getExpiry(), ChronoUnit.NANOS)
                    / Constants.NANOSINDAY / daysInYear;
            return (s, t) -> -OptionUtility.payoffFunction(instrument, tau - t, r, q).apply(s);
        } else throw new Exception("Unsupported instrument: BoundaryConstructor::getConstFunction");
    }

}
