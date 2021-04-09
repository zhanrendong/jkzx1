package tech.tongyu.bct.quant.builder;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.library.financial.date.BusinessDayAdjustment;
import tech.tongyu.bct.quant.library.financial.date.DateCalcUtils;
import tech.tongyu.bct.quant.library.financial.date.DateRollEnum;
import tech.tongyu.bct.quant.library.financial.date.DayCountBasis;
import tech.tongyu.bct.quant.library.market.curve.DiscountingCurve;
import tech.tongyu.bct.quant.library.market.curve.impl.PwlfDiscountingCurve;
import tech.tongyu.bct.quant.library.numerics.interp.ExtrapTypeEnum;
import tech.tongyu.bct.quant.library.numerics.interp.Interpolator1D;
import tech.tongyu.bct.quant.library.numerics.interp.Interpolator1DCubicSpline;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CurveBootstrapper {
    private static List<String> calendars = Arrays.asList("DEFAULT_CALENDAR");
    private static double nextDf(double prevDf, double prevDv01, double prevS, double newS, double dcf) {
        return (prevDf - (newS - prevS) * prevDv01) / (1. + newS * dcf);
    }

    private static Interpolator1D swapRateInterpolator(LocalDate start, List<LocalDate> ends, List<Double> rates) {
        double[] xs = new double[ends.size()];
        double[] ys = new double[rates.size()];
        for (int i = 0; i < ends.size(); ++i) {
            xs[i] = start.until(ends.get(i), ChronoUnit.DAYS);
            ys[i] = rates.get(i);
        }
        return new Interpolator1DCubicSpline(xs, ys, ExtrapTypeEnum.EXTRAP_1D_FLAT, ExtrapTypeEnum.EXTRAP_1D_FLAT);
    }

    // df0 is the df on accrualDates[0]. the starting point.
    // do not assume accrual start is the same as val
    // interpoloatr is from (days from accrual start) to swap rates
    // dfs has 1 less element than accrual dates
    private static List<Double> bootstrapFromLeg(List<LocalDate> accrualDates,
                                                 List<Double> dcfs,
                                                 Interpolator1D interp,
                                                 double df0) {
        List<Double> dfs = new ArrayList<>();
        dfs.add(df0);
        double dv01 = 0.;
        double S = 0.;
        double df = df0;
        for (int i = 1; i < accrualDates.size(); ++i) {
            double newS = interp.value(accrualDates.get(0).until(accrualDates.get(i), ChronoUnit.DAYS));
            double newDf = nextDf(df, dv01, S, newS, dcfs.get(i-1));
            dfs.add(newDf);
            df = newDf;
            dv01 += dcfs.get(i-1) * newDf;
            S = newS;
        }
        return dfs;
    }

    public static DiscountingCurve bootstrap(LocalDate valuationDate,
                                             LocalDate start,
                                             List<String> tenors,
                                             List<Double> swapRates,
                                             String freq,
                                             DayCountBasis basis) {
        Period frequency = DateTimeUtils.parsePeriod(freq);
        long freqInMonth = frequency.toTotalMonths();
        Period lastTenor = DateTimeUtils.parsePeriod(tenors.get(tenors.size() - 1));
        List<LocalDate> accrualDates = DateCalcUtils.generateSchedule(start, lastTenor, frequency,
                DateRollEnum.FORWARD, BusinessDayAdjustment.MODIFIED_FOLLOWING, calendars);
        List<LocalDate> ends = tenors.stream()
                .map(DateTimeUtils::parsePeriod)
                .map(Period::toTotalMonths)
                .map(n -> accrualDates.get((int)(n / freqInMonth)))
                .collect(Collectors.toList());
        Interpolator1D interp = swapRateInterpolator(start, ends, swapRates);
        List<Double> dcfs = IntStream.range(1, accrualDates.size())
                .mapToObj(i -> basis.daycountFraction(accrualDates.get(i-1), accrualDates.get(i)))
                .collect(Collectors.toList());
        List<Double> dfs = bootstrapFromLeg(accrualDates, dcfs, interp, 1.);
        if (valuationDate.isBefore(start)) {
            // back extrapolate
            double dt = basis.daycountFraction(valuationDate, start);
            double dt2 = basis.daycountFraction(start, accrualDates.get(1));
            double f = FastMath.log(dfs.get(0) / dfs.get(1)) / dt2;
            double adj = FastMath.exp(-f * dt);
            for (int i = 0; i < dfs.size(); ++i) {
                dfs.set(i, dfs.get(i) * adj);
            }
        }
        double[] dfs2 = new double[dfs.size()];
        LocalDateTime[] ts = new LocalDateTime[accrualDates.size()];
        for(int i = 0; i < dfs.size(); ++i) {
            dfs2[i] = dfs.get(i);
            ts[i] = accrualDates.get(i).atStartOfDay();
        }
        return PwlfDiscountingCurve.pwcfFromDfs(valuationDate.atStartOfDay(),
                ts,
                dfs2);
    }
}
