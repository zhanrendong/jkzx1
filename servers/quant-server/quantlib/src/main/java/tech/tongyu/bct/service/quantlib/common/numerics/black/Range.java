package tech.tongyu.bct.service.quantlib.common.numerics.black;


import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.numerics.ad.Backwarder;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.lang.Math.sqrt;

/**
 * @author Liao Song
 * @since 2016-10-17
 * Calculator for price and Greeks of the stock price range accrual
 */
public class Range {
    //the payoff amount discounted back to the valuation date
    private static double discountPayoff(CalcType request, LocalDateTime expiry, double payoff,
                                         double spot, LocalDateTime val, double r, double daysInYear) throws Exception {
        double tau = val.until(expiry, ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        Backwarder calculator = new Backwarder(new String[]{"spot", "tau", "r"},
                new double[]{spot, tau, r}, 2);
        //discount rate
        String df = calculator.exp(calculator.scale(calculator.mul("r", "tau"), -1));

        //discounted payoff
        String dp = calculator.scale(df, payoff);
        calculator.rollBack();
        switch (request) {
            case PRICE:
                return calculator.getValue(dp);
            case DELTA:
                return calculator.getDeriv("spot");
            case GAMMA:
                return calculator.getDeriv("spot", "spot");
            case VEGA:
                return calculator.getDeriv("vol");
            case THETA:
                return calculator.getDeriv("tau");
            case RHO_R:
                return calculator.getDeriv("r");
            case VERA_R:
                return calculator.getDeriv("r", "vol");
            case RHO_Q:
                return calculator.getDeriv("q");
            case VERA_Q:
                return calculator.getDeriv("q", "vol");
            case VANNA:
                return calculator.getDeriv("spot", "vol");
            case VOLGA:
                return calculator.getDeriv("vol", "vol");
            case VETA:
                return calculator.getDeriv("tau", "vol");
            case CHARM:
            case COLOR:
            case SPEED:
            case ULTIMA:
            case ZOMMA:
            default:
                return Double.NaN;
        }
    }

    //the probability factor of each scheduled terms
    static double termCalc(CalcType request, LocalDateTime expiry, double payoff,
                           double spot, LocalDateTime val,
                           double sMin, double sMax, LocalDateTime ti,
                           double vol, double r, double q, double daysInYear) throws Exception {
        double tau = val.until(expiry, ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        double tau_i = val.until(ti, ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;

        if (tau_i < 1e-14)
            return 0.0;

        Backwarder calculator = new Backwarder(new String[]{"spot", "vol", "tau", "r", "q"},
                new double[]{spot, vol, tau, r, q}, 2);

        //discount rate
        String dr = calculator.exp(calculator.scale(calculator.mul("r", "tau"), -1));
        //the probability that the spot price falls in the pre-specified range
        String dr_i = calculator.exp(calculator.scale("r", -tau_i));
        String dq_i = calculator.exp(calculator.scale("q", -tau_i));
        String forward_i = calculator.div(calculator.mul("spot", dq_i), dr_i);
        String p_i;
        //[s_min,s_max]
        if (sMin != 0 && sMax != Double.POSITIVE_INFINITY) {
            String lnMoneyness_min = calculator.ln(calculator.scale(forward_i, 1.0 / sMin));
            String lnMoneyness_max = calculator.ln(calculator.scale(forward_i, 1.0 / sMax));
            String volSqrtT_i = calculator.scale("vol", sqrt(tau_i));
            String dmax = calculator.sub(calculator.div(lnMoneyness_max, volSqrtT_i), calculator.scale(volSqrtT_i, 0.5));
            String dmin = calculator.sub(calculator.div(lnMoneyness_min, volSqrtT_i), calculator.scale(volSqrtT_i, 0.5));
            p_i = calculator.sub(calculator.ncdf(dmin), calculator.ncdf(dmax));
        }
        //[0,s_max]
        else if (sMin == 0 && sMax != Double.POSITIVE_INFINITY) {
            String lnMoneyness_max = calculator.ln(calculator.scale(forward_i, 1.0 / sMax));
            String volSqrtT_i = calculator.scale("vol", sqrt(tau_i));
            String dmax = calculator.sub(calculator.div(lnMoneyness_max, volSqrtT_i), calculator.scale(volSqrtT_i, 0.5));
            p_i = calculator.ncdf(calculator.scale(dmax, -1));
        }
        //[s_min,infinity]
        else if (sMin != 0) {
            String lnMoneyness_min = calculator.ln(calculator.scale(forward_i, 1.0 / sMin));
            String volSqrtT_i = calculator.scale("vol", sqrt(tau_i));
            String dmin = calculator.sub(calculator.div(lnMoneyness_min, volSqrtT_i), calculator.scale(volSqrtT_i, 0.5));
            p_i = calculator.ncdf(dmin);
        }
        //[0, infinity]
        else {
            p_i = calculator.div("spot", "spot");
        }


        String price = calculator.mul(calculator.scale(p_i, payoff), dr);

        calculator.rollBack();
        switch (request) {
            case PRICE:
                return calculator.getValue(price);
            case DELTA:
                return calculator.getDeriv("spot");
            case GAMMA:
                return calculator.getDeriv("spot", "spot");
            case VEGA:
                return calculator.getDeriv("vol");
            case THETA:
                return calculator.getDeriv("tau");
            case RHO_R:
                return calculator.getDeriv("r");
            case VERA_R:
                return calculator.getDeriv("r", "vol");
            case RHO_Q:
                return calculator.getDeriv("q");
            case VERA_Q:
                return calculator.getDeriv("q", "vol");
            case VANNA:
                return calculator.getDeriv("spot", "vol");
            case VOLGA:
                return calculator.getDeriv("vol", "vol");
            case VETA:
                return calculator.getDeriv("tau", "vol");
            case CHARM:
            case COLOR:
            case SPEED:
            case ULTIMA:
            case ZOMMA:
            default:
                return Double.NaN;
        }

    }


    @BctQuantApi(name = "qlRangeAccrualCalc",
            description = "stock price range accrual calculator",
            argNames = {"request", "val", "expiry", "payoff", "sMin", "sMax", "fixings", "schedule", "spot", "vol", "r",
                    "q", "daysInYear"},
            argTypes = {"Enum", "DateTime", "DateTime", "Double", "Double", "Double", "ArrayDouble", "ArrayDateTime",
                    "Double", "Double", "Double", "Double", "Double"},
            argDescriptions = {"request", "valuation date", "expiry", "payoff amount", "range minimum price",
                    "range maximum price", "past fixings", "scheduled observation dates", "spot price", "volatility",
                    "risk free interest rate", "annually dividend yield", "number of days in a year"},
            retDescription = "result",
            retType = "Double",
            retName = "result")

    public static double calc(CalcType request, LocalDateTime val,
                              LocalDateTime expiry, double payoff,
                              double sMin, double sMax, double[] fixings, LocalDateTime[] schedule,
                              double spot, double vol, double r, double q, double daysInYear) throws Exception {
        //find the number of fixings that the spot prices fall in the range

        int N = schedule.length;
        int M = fixings.length;

        int L = 0;


        for (double spotFixing : fixings) {
            if (spotFixing >= sMin && spotFixing <= sMax) {
                L++;
            }
        }

        //no past fixings
        if (M == 1 && fixings[0] == 0.0) {
            M = 0;
            L = 0;
        }


        double sum = 0;
        for (int i = M; i < N; i++) {
            double term_i = termCalc(request, expiry, payoff, spot, val, sMin, sMax, schedule[i], vol, r, q, daysInYear);
            sum += term_i;
        }

        if (L == 0)
            return sum / N;
        else {
            return discountPayoff(request, expiry, payoff, spot, val, r, daysInYear) * L / N + sum / N;
        }

    }
}
