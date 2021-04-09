package tech.tongyu.bct.quant.builder;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.library.market.vol.ImpliedVolSurface;
import tech.tongyu.bct.quant.library.market.vol.VolCalendar;
import tech.tongyu.bct.quant.library.market.vol.VolCalendarCache;
import tech.tongyu.bct.quant.library.market.vol.impl.AtmPwcVolSurface;
import tech.tongyu.bct.quant.library.market.vol.impl.InterpolatedStrikeVolSurface;
import tech.tongyu.bct.quant.library.numerics.interp.ExtrapTypeEnum;
import tech.tongyu.bct.quant.library.numerics.interp.Interpolator1DCubicSpline;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public class VolSurfaceBuilder {
    public static class InterpolatedVolSurfaceConfig {
        public double daysInYear;
        public boolean useVolCalendar;
        public String volCalendar;
        public List<String> calendars;

        public InterpolatedVolSurfaceConfig() {
            this.daysInYear = DateTimeUtils.DAYS_IN_YEAR;
            this.useVolCalendar = false;
            this.volCalendar = null;
            this.calendars = null;
        }
    }

    public static ImpliedVolSurface createAtmPwcVolSurface(LocalDateTime val, double spot, List<LocalDate> expiries,
                                                           List<Double> vols) {
        TreeMap<Double, AtmPwcVolSurface.Segment> segs = new TreeMap<>();
        ArrayList<Double> taus = new ArrayList<>();
        taus.add(0.0);
        ArrayList<Double> vars = new ArrayList<>();
        vars.add(0.0);
        for (int i = 0; i < expiries.size(); ++i) {
            if (!expiries.get(i).isAfter(val.toLocalDate()))
                continue;
            double days = DateTimeUtils.days(val, expiries.get(i).atStartOfDay());
            taus.add(days);
            double var = vols.get(i) * vols.get(i) * days / DateTimeUtils.DAYS_IN_YEAR;
            vars.add(var);
        }
        for (int i = 1 ; i < taus.size(); ++i) {
            double tau = taus.get(i) - taus.get(i-1);
            double dailyVar = (vars.get(i) - vars.get(i-1)) / tau;
            AtmPwcVolSurface.Segment seg = new AtmPwcVolSurface.Segment();
            seg.localVol = FastMath.sqrt(dailyVar);
            seg.varSoFar = vars.get(i-1);
            segs.put(taus.get(i-1), seg);
        }
        // the last segment represents local volatility after the last expiry.
        // its local volatility is the same as the one before the last expiry.
        AtmPwcVolSurface.Segment seg = new AtmPwcVolSurface.Segment();
        seg.localVol = segs.lastEntry().getValue().localVol;
        seg.varSoFar = vars.get(vars.size() - 1);
        segs.put(taus.get(taus.size() - 1), seg);
        return new AtmPwcVolSurface(val, spot, segs, DateTimeUtils.DAYS_IN_YEAR);
    }

    public static ImpliedVolSurface createInterpolatedStrikeSurface(
            LocalDateTime val,
            double spot,
            List<LocalDate> expiries, List<Double> strikes, List<List<Double>> vols,
            InterpolatedVolSurfaceConfig config) {
        VolCalendar volCalendar;
        if (config.useVolCalendar) {
            if (!Objects.isNull(config.volCalendar)) {
                if (config.daysInYear <= 0.) {
                    throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 一年天数必须为正: " + config.daysInYear);
                }
                volCalendar = VolCalendarCache.getMayThrow(config.volCalendar);
            } else {
                // no explict vol calendar given
                // construct one from holiday calendars
                if (Objects.isNull(config.calendars) || config.calendars.isEmpty()) {
                    throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 缺少波动率日历或者交易日历");
                }
                volCalendar = VolCalendar.fromCalendars(config.calendars);
            }
        } else {
            volCalendar = VolCalendar.none();
        }

        TreeMap<LocalDateTime, Interpolator1DCubicSpline> varInterp = new TreeMap<>();
        double[] interpStrikes = strikes.stream().mapToDouble(Number::doubleValue).toArray();
        for (int i = 0; i < expiries.size(); i++) {
            LocalDate e = expiries.get(i);
            double t = volCalendar.getEffectiveNumDays(val, e.atStartOfDay()) / config.daysInYear;
            double[] vars = new double[strikes.size()];
            for (int j = 0; j < strikes.size(); j++)
                vars[j] = vols.get(i).get(j) * vols.get(i).get(j) * t;
            varInterp.put(e.atStartOfDay(), new Interpolator1DCubicSpline(interpStrikes, vars,
                    ExtrapTypeEnum.EXTRAP_1D_FLAT, ExtrapTypeEnum.EXTRAP_1D_FLAT));
        }
        return new InterpolatedStrikeVolSurface(val, varInterp,
                volCalendar, spot, config.daysInYear);
    }
}
