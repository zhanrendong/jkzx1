package tech.tongyu.bct.service.quantlib.pricer.PricerPDE;

import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static java.lang.Math.*;

/**
 * @author Liao Song
 * @since 2016-8-23
 * creates grids in spot price space and time space according to the given instrument and the market information
 * notes:
 * the far field boundary condition defines the sMax to be
 * double sMax = max(1.0 * exp(-0.5 * min(0, tau * (vol * vol - 2.0 * r))
 * + 0.5 * sqrt(min(0, tau * (vol * vol - 2.0 * r)) * min(0, tau * (vol * vol - 2.0 * r))
 * + 8 * vol * vol * tau * log(A))), 2.0) / projection;
 * Test results show that this condition has limited application when dealing with some exotic cases(r = 100, vol =1, for
 * example)
 * so we use sMax = max(10.0*strike, 10.0*spot) to determine the boundary, test results showed that this condition works
 * well in most cases and it is much faster.
 */
class GridConstructor {
    /**
     * constructs the spot grid for the given instrument and the market information to numerically solve the
     * Black-Scholes partial differential equations at the given accuracy.
     *
     * @param instrument financial instrument
     * @param spot       current spot price
     * @param eps        targeted accuracy
     * @param params     contains minimum and maximum spot, and number of spot steps
     * @return returns the spot array
     * @throws Exception if the instrument is not supported
     */
    static double[] getSpotArray(Object instrument, double spot, double eps,
                                 Map<String, Object> params) throws Exception {


        // vanilla European option

        if (instrument instanceof VanillaEuropean) {

            double strike = ((VanillaEuropean) instrument).getStrike();
            double projection = min(1.0 / spot, 1.0 / strike);
            double sMax = max(2.0 * strike, 2.0 * spot);
            double sMin = 0;
            // if eps <= 0, set to some unrealistic nonzero value, never used
            double ds = eps > 0 ? sqrt(eps * spot * projection) / projection
                    : strike;
            if (eps <= 0) {
                if (params == null)
                    throw new RuntimeException("pricerParams missing.");
                if (params.containsKey("maxS"))
                    sMax = ((Number)params.get("maxS")).doubleValue();
                if (params.containsKey("minS"))
                    sMin = ((Number)params.get("minS")).doubleValue();
                if (params.containsKey("spotNumSteps")) {
                    double num = ((Number)params.get("spotNumSteps")).doubleValue();
                    ds = (sMax - sMin) / num;
                } else
                    throw new RuntimeException("spotNumSteps missing.");
            }

            //make sure that spot is on the grid
            int A = (int) ((spot - sMin) / ds);
            int B = (int) ((sMax - spot) / ds);

            double[] s = new double[A + B + 1];
            for (int i = 0; i < A; i++)
                s[i] = sMin + i * ds;
            s[A] = spot;
            for (int i = A; i < A + B; i++)
                s[i] = s[A] + (i - A) * ds;
            s[A + B] = sMax;
            return s;
        }

        //digital cash
        if (instrument instanceof DigitalCash) {
            double strike = ((DigitalCash) instrument).getStrike();
            double projection = min(1.0 / spot, 1.0 / strike);
            double sMax = max(2.0 * strike, 2.0 * spot);
            double sMin = 0;
            // if eps <= 0, set to some unrealistic nonzero value, never used
            double ds = eps > 0 ? sqrt(eps * spot * projection) / projection
                    : strike;
            if (eps <= 0) {
                if (params == null)
                    throw new RuntimeException("pricerParams missing.");
                if (params.containsKey("maxS"))
                    sMax = ((Number)params.get("maxS")).doubleValue();
                if (params.containsKey("minS"))
                    sMin = ((Number)params.get("minS")).doubleValue();
                if (params.containsKey("spotNumSteps")) {
                    double num = ((Number)params.get("spotNumSteps")).doubleValue();
                    ds = (sMax - sMin) / num;
                } else
                    throw new RuntimeException("spotNumSteps missing.");
            }

            //make sure that spot is on the grid
            int A = (int) ((spot - sMin) / ds);
            int B = (int) ((sMax - spot) / ds);

            double[] s = new double[A + B + 1];
            for (int i = 0; i < A; i++)
                s[i] = i * ds;
            s[A] = spot;
            for (int i = A; i < A + B; i++)
                s[i] = s[A] + (i - A) * ds;
            s[A + B] = sMax;
            return s;
        }

        // vanilla American option

        else if (instrument instanceof VanillaAmerican) {

            double strike = ((VanillaAmerican) instrument).getStrike();
            double projection = min(1.0 / spot, 1.0 / strike);
            double sMax = max(2.0 * strike, 2.0 * spot);
            double sMin = 0;
            // if eps <= 0, set to some unrealistic nonzero value, never used
            double ds = eps > 0 ? sqrt(eps * spot * projection) / projection
                    : strike;
            if (eps <= 0) {
                if (params == null)
                    throw new RuntimeException("pricerParams missing.");
                if (params.containsKey("maxS"))
                    sMax = ((Number)params.get("maxS")).doubleValue();
                if (params.containsKey("minS"))
                    sMin = ((Number)params.get("minS")).doubleValue();
                if (params.containsKey("spotNumSteps")) {
                    double num = ((Number)params.get("spotNumSteps")).doubleValue();
                    ds = (sMax - sMin) / num;
                } else
                    throw new RuntimeException("spotNumSteps missing.");
            }

            //make sure that spot is on the grid
            int A = (int) ((spot - sMin) / ds);
            int B = (int) ((sMax - spot) / ds);

            double[] s = new double[A + B + 1];
            for (int i = 0; i < A; i++)
                s[i] = i * ds;
            s[A] = spot;
            for (int i = A; i < A + B; i++)
                s[i] = s[A] + (i - A) * ds;
            s[A + B] = sMax;

            return s;
        }

        // Knock Out option (with continuous monitoring)

        else if (instrument instanceof KnockOutContinuous) {
            double strike = ((KnockOutContinuous) instrument).getStrike();
            double barrier = ((KnockOutContinuous) instrument).getBarrier();
            BarrierDirection barrierDirection = ((KnockOutContinuous) instrument).getBarrierDirection();
            double sMax;
            double sMin;
            double projection = min(1.0 / spot, 1.0 / strike);
            double ds = eps > 0 ? sqrt(eps * spot * projection) / projection
                    : strike;

            //determine the upper and lower limit
            if (barrierDirection == BarrierDirection.UP_AND_OUT) {
                sMax = barrier;
                sMin = 0;
            } else {
                sMax = max(2.0 * strike, 2.0 * spot);
                sMin = barrier;
                if (sMax / ds > (Integer.MAX_VALUE - 10))
                    sMax = 2.0 * spot;
            }

            if (eps <= 0) {
                if (params == null)
                    throw new RuntimeException("pricerParams missing.");
                if (params.containsKey("maxS")
                        && barrierDirection != BarrierDirection.UP_AND_OUT)
                    sMax = ((Number)params.get("maxS")).doubleValue();
                if (params.containsKey("minS")
                        && barrierDirection != BarrierDirection.DOWN_AND_OUT)
                    sMin = ((Number)params.get("minS")).doubleValue();
                if (params.containsKey("spotNumSteps")) {
                    double num = ((Number)params.get("spotNumSteps")).doubleValue();
                    ds = (sMax - sMin) / num;
                } else
                    throw new RuntimeException("spotNumSteps missing.");
            }

            int A = (int) ((spot - sMin) / ds);
            int B = (int) ((sMax - spot) / ds);
            double[] s = new double[A + B + 1];
            for (int i = 0; i < A; i++)
                s[i] = sMin + i * ds;
            s[A] = spot;
            for (int i = A; i < A + B; i++)
                s[i] = s[A] + (i - A) * ds;
            s[A + B] = sMax;

            return s;

        }

        // double knock-out option (with continuous monitoring)

        else if (instrument instanceof DoubleKnockOutContinuous) {
            double strike = ((DoubleKnockOutContinuous) instrument).getStrike();
            double upperBarrier = ((DoubleKnockOutContinuous) instrument).getUpperBarrier();
            double lowerBarrier = ((DoubleKnockOutContinuous) instrument).getLowerBarrier();
            double projection = min(1.0 / spot, 1.0 / strike);
            double ds = eps > 0 ? sqrt(eps * spot * projection) / projection
                    : strike;

            if (eps <= 0) {
                if (params == null)
                    throw new RuntimeException("pricerParams missing.");
                if (params.containsKey("spotNumSteps")) {
                    double num = ((Number)params.get("spotNumSteps")).doubleValue();
                    ds = (upperBarrier - lowerBarrier) / num;
                } else
                    throw new RuntimeException("spotNumSteps missing.");
            }

            int A = (int) ((spot - lowerBarrier) / ds);
            int B = (int) ((upperBarrier - spot) / ds);
            double[] s = new double[A + B + 1];
            for (int i = 0; i < A; i++)
                s[i] = lowerBarrier + i * ds;
            s[A] = spot;
            for (int i = A; i < A + B; i++)
                s[i] = s[A] + (i - A) * ds;
            s[A + B] = upperBarrier;

            return s;
        } else throw new Exception("Unsupported instrument: GridConstructor::getSpotArray");

    }

    /**
     * constructs the time grid for the given instrument
     * for now the time grid is uniformly created.
     *
     * @param instrument financial instrument
     * @param val        valuation date
     * @param params     contains number of time steps
     * @return returns the time array
     * @throws Exception if the instrument is not supported
     */
    static double[] getTimeArray(Object instrument,
                                 LocalDateTime val, double daysInYear, double eps,
                                 Map<String, Object> params) throws Exception {

        // vanilla European option
        int n = 1;
        if (eps > 0)
            n = (int) (1.0 / (sqrt(eps * 10)));
        else if (params != null) {
            if (params.containsKey("timeNumSteps"))
                n = ((Number)params.get("timeNumSteps")).intValue();
            else throw new RuntimeException("timeNumSteps missing.");
        }

        if (instrument instanceof VanillaEuropean) {

            double tau = val.until(((VanillaEuropean) instrument).getExpiry(), ChronoUnit.NANOS)
                    / Constants.NANOSINDAY / daysInYear;
            double dt = tau / n;
            double[] t = new double[n + 1];
            for (int i = 0; i <= n; i++) {
                t[i] = i * dt;
            }
            return t;
        }
        // digital cash
        if (instrument instanceof DigitalCash) {

            double tau = val.until(((DigitalCash) instrument).getExpiry(), ChronoUnit.NANOS)
                    / Constants.NANOSINDAY / daysInYear;
            double dt = tau / n;
            double[] t = new double[n + 1];
            for (int i = 0; i <= n; i++) {
                t[i] = i * dt;
            }
            return t;
        }

        // vanilla American option

        else if (instrument instanceof VanillaAmerican) {
            double tau = val.until(((VanillaAmerican) instrument).getExpiry(), ChronoUnit.NANOS)
                    / Constants.NANOSINDAY / daysInYear;
            double dt = tau / n;
            double[] t = new double[n + 1];
            for (int i = 0; i <= n; i++) {
                t[i] = i * dt;
            }
            return t;

        }

        // knock-out option(with continuous monitoring)

        else if (instrument instanceof KnockOutContinuous) {
            double tau = val.until(((KnockOutContinuous) instrument).getExpiry(), ChronoUnit.NANOS)
                    / Constants.NANOSINDAY / daysInYear;
            double dt = tau / n;
            double[] t = new double[n + 1];
            for (int i = 0; i <= n; i++) {
                t[i] = i * dt;
            }
            return t;
        }

        // double knock-out option(with continuous monitoring)

        else if (instrument instanceof DoubleKnockOutContinuous) {
            double tau = val.until(((DoubleKnockOutContinuous) instrument).getExpiry(), ChronoUnit.NANOS)
                    / Constants.NANOSINDAY / daysInYear;
            double dt = tau / n;
            double[] t = new double[n + 1];
            for (int i = 0; i <= n; i++) {
                t[i] = i * dt;
            }
            return t;
        } else throw new Exception("Unsupported instrument: GridConstructor::getTimeArray");
    }
}
