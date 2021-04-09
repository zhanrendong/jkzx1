package tech.tongyu.bct.service.quantlib.pricer;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.enums.*;
import tech.tongyu.bct.service.quantlib.common.numerics.black.*;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;
import tech.tongyu.bct.service.quantlib.financial.instruments.linear.Cash;
import tech.tongyu.bct.service.quantlib.financial.instruments.linear.Spot;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.*;
import tech.tongyu.bct.service.quantlib.market.curve.Discount;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


public class PricerBlack {
    // a single cash payment
    public static double calc(CalcType request,
                              Cash instrument,
                              LocalDateTime val,
                              double spot, double vol, double r, double q, double daysInYear) {
        double tau = val.until(instrument.getPayDate(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        if (tau < 0.0)
            return 0.0;
        double amount = instrument.getAmount();
        switch (request) {
            case PRICE:
                return amount * FastMath.exp(-r * tau);
            case RHO_R:
                return -tau * amount * FastMath.exp(-r * tau);
            case THETA:
                return -r * amount * FastMath.exp(-r * tau);
            default:
                return 0.0;
        }
    }
    public static double calc(CalcType request,
                              Cash instrument,
                              Discount yc,
                              double daysInYear) {
        LocalDateTime val = yc.getVal();
        LocalDateTime payDate = instrument.getPayDate();
        double tau = val.until(instrument.getPayDate(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        if (tau < 0.0)
            return 0.0;
        double amount = instrument.getAmount();
        double df = yc.df(payDate);
        switch (request) {
            case PRICE:
                return amount * df;
            case RHO_R:
                return -tau * amount * df;
            case THETA:
                return -amount * df * FastMath.log(df) / tau;
            default:
                return 0.0;
        }
    }
    // spot
    public static double calc(CalcType request,
                              Spot instrument,
                              LocalDateTime val,
                              double spot, double vol, double r, double q, double daysInYear) {
        return calc(request, spot);
    }
    public static double calc(CalcType request,
                              double spot) {
        switch (request) {
            case PRICE:
                return spot;
            case DELTA:
                return 1.0;
            default:
                return 0.0;
        }
    }
    // vanilla european
    public static double calc(CalcType request,
                              VanillaEuropean instrument,
                              LocalDateTime val,
                              double spot, double vol, double r, double q, double daysInYear) {
        double tau = val.until(instrument.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        return Black.calc(request, spot, instrument.getStrike(), vol, tau,
                r, q, instrument.getType());
    }
    // digital (cash settled)
    public static double calc(CalcType request,
                              DigitalCash instrument,
                              LocalDateTime val,
                              double spot, double vol, double r, double q, double daysInYear) throws Exception {
        double tauInDays = val.until(instrument.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY;
        return Digital.calc(request, spot, instrument.getStrike(), vol, tauInDays / daysInYear,
                r, q, instrument.getType(), PayoffType.CASH) * instrument.getPayment();
    }

    //Discrete Asian option
    public static double calc(CalcType request,
                              AverageRateArithmetic instrument,
                              LocalDateTime val,
                              double spot, double vol, double r, double q, double daysInYear) throws Exception {
        double tau = val.until(instrument.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        return Asian.calc(request, instrument.getType(), val, instrument.getFixings(), instrument.getSchedule(),
                instrument.getWeights(), daysInYear,
                spot, instrument.getStrike(), vol, tau, r, q);
    }

    //Knock In continuous
    public static double calc(CalcType request,
                              KnockInContinuous instrument,
                              LocalDateTime val,
                              double spot, double vol, double r, double q, double daysInYears) throws Exception {
        //currently we can not price window barrier.
        if (val.until(instrument.getBarrierStart(), ChronoUnit.NANOS) / Constants.NANOSINDAY > 0)
            throw new Exception("Barrier must start on or before the valuation date.");
        if (instrument.getExpiry().until(instrument.getBarrierEnd(), ChronoUnit.NANOS) / Constants.NANOSINDAY < 0)
            throw new Exception("Barrier must end on or after the option expiry.");

        double tau = val.until(instrument.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYears;
        double barrier = instrument.getBarrier();
        BarrierDirection direction = instrument.getBarrierDirection();
        OptionType type = instrument.getType();
        double rebate = instrument.getRebateAmount();
        double strike = instrument.getStrike();

        return KnockIn.calc(request, type, direction, barrier, rebate, spot, strike, vol, tau, r, q);
    }

    //Knock Out continuous
    public static double calc(CalcType request,
                              KnockOutContinuous instrument,
                              LocalDateTime val,
                              double spot, double vol, double r, double q, double daysInYears) throws Exception {

        if (val.until(instrument.getBarrierStart(), ChronoUnit.NANOS) / Constants.NANOSINDAY > 0)
            throw new Exception("Barrier must start on or before the valuation date.");
        if (instrument.getExpiry().until(instrument.getBarrierEnd(), ChronoUnit.NANOS) / Constants.NANOSINDAY < 0)
            throw new Exception("Barrier must end on or after the option expiry.");

        double tau = val.until(instrument.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYears;
        double barrier = instrument.getBarrier();
        BarrierDirection direction = instrument.getBarrierDirection();
        OptionType type = instrument.getType();
        RebateType rebateType = instrument.getRebateType();
        double rebate = instrument.getRebateAmount();
        double strike = instrument.getStrike();

        return KnockOut.calc(request, type, direction, rebateType, barrier, rebate, spot, strike, vol, tau, r, q);
    }

    //Double KnockOut Continuous
    public static double calc(CalcType request,
                              DoubleKnockOutContinuous instrument,
                              LocalDateTime val,
                              double spot, double vol, double r, double q, double daysInYears) throws Exception {
        //currently we can not price window barrier.
        if (val.until(instrument.getBarrierStart(), ChronoUnit.NANOS) / Constants.NANOSINDAY > 0)
            throw new Exception("Barrier must start on or before the valuation date.");
        if (instrument.getExpiry().until(instrument.getBarrierEnd(), ChronoUnit.NANOS) / Constants.NANOSINDAY < 0)
            throw new Exception("Barrier must end on or after the option expiry.");
        double tau = val.until(instrument.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYears;
        OptionType type = instrument.getType();
        double upperBarrier = instrument.getUpperBarrier();
        double lowerBarrier = instrument.getLowerBarrier();
        double upperRebate = instrument.getUpperRebate();
        double lowerRebate = instrument.getLowerRebate();
        double strike = instrument.getStrike();
        RebateType rebateType = instrument.getRebateType();
        return DoubleKnockOut.calc(request, type, rebateType, upperBarrier, lowerBarrier, upperRebate,
                lowerRebate, spot, strike, vol, tau, r, q);

    }

    //OneTouch
    public static double calc(CalcType request,
                              OneTouch instrument,
                              LocalDateTime val,
                              double spot, double vol, double r, double q, double daysInYears) throws Exception {
        if (request != CalcType.PRICE) throw new Exception("Calculation type is not supported for one touch.");
        double barrier = instrument.getBarrier();
        double rebate = instrument.getRebateAmount();
        BarrierDirection direction = instrument.getBarrierDirection();
        RebateType rebateType = instrument.getRebateType();
        if (val.until(instrument.getBarrierStart(), ChronoUnit.NANOS) / Constants.NANOSINDAY > 0)
            throw new Exception("Barrier must start on or before the valuation date.");
        if (instrument.getExpiry().until(instrument.getBarrierEnd(), ChronoUnit.NANOS) / Constants.NANOSINDAY < 0)
            throw new Exception("Barrier must end on or after the option expiry.");
        double tau = val.until(instrument.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYears;

        return Touch.oneTouch(rebate, barrier, spot, vol, tau, r, q, direction, rebateType);
    }

    //NoTouch
    public static double calc(CalcType request,
                              NoTouch instrument,
                              LocalDateTime val,
                              double spot, double vol, double r, double q, double daysInYears) throws Exception {
        if (request != CalcType.PRICE) throw new Exception("Calculation type is not supported for one touch.");
        double barrier = instrument.getBarrier();
        double rebate = instrument.getPayment();
        BarrierDirection direction = instrument.getBarrierDirection();
        if (val.until(instrument.getBarrierStart(), ChronoUnit.NANOS) / Constants.NANOSINDAY > 0)
            throw new Exception("Barrier must start on or before the valuation date.");
        if (instrument.getExpiry().until(instrument.getBarrierEnd(), ChronoUnit.NANOS) / Constants.NANOSINDAY < 0)
            throw new Exception("Barrier must end on or after the option expiry.");
        double tau = val.until(instrument.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYears;

        return Touch.noTouch(rebate, barrier, spot, vol, tau, r, q, direction);
    }

    //vanilla American option
    public static double calc(CalcType request,
                              VanillaAmerican instrument,
                              LocalDateTime val,
                              double spot, double vol, double r, double q, double daysInYear) throws Exception {
        double tau = val.until(instrument.getExpiry(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        return American.calc(request, instrument.getType(), spot, instrument.getStrike(), vol, tau, r, q);
    }

    //range accrual option
    public static double calc(CalcType request,
                              RangeAccrual instrument,
                              LocalDateTime val,
                              double spot, double vol, double r, double q, double daysInYear) throws Exception {
        String cumulative = instrument.getCumulative();
        double[] fixings = instrument.getFixings();
        LocalDateTime[] schedule = instrument.getSchedule();
        LocalDateTime expiry = instrument.getExpiry();
        double payoff = instrument.getPayoff();
        if (cumulative.equalsIgnoreCase("in_range")) {
            double sMin = instrument.getRangeMin();
            double sMax = instrument.getRangeMax();
            return Range.calc(request, val, expiry, payoff, sMin, sMax, fixings, schedule, spot, vol, r, q, daysInYear);
        } else {
            double smin1 = 0;
            double smax1 = instrument.getRangeMin();
            double smin2 = instrument.getRangeMax();
            double smax2 = Double.POSITIVE_INFINITY;
            return Range.calc(request, val, expiry, payoff, smin1, smax1, fixings, schedule, spot, vol, r, q, daysInYear) +
                    Range.calc(request, val, expiry, payoff, smin2, smax2, fixings, schedule, spot, vol, r, q, daysInYear);
        }
    }
}