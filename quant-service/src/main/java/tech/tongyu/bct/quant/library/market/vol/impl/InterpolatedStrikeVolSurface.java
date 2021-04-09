package tech.tongyu.bct.quant.library.market.vol.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.common.QuantlibSerializableObject;
import tech.tongyu.bct.quant.library.market.vol.ImpliedVolSurface;
import tech.tongyu.bct.quant.library.market.vol.LocalVolSurface;
import tech.tongyu.bct.quant.library.market.vol.VolCalendar;
import tech.tongyu.bct.quant.library.numerics.interp.ExtrapTypeEnum;
import tech.tongyu.bct.quant.library.numerics.interp.Interpolator1DCubicSpline;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implied Volatility surface interpolated in (t, K). Data is stored as implied
 * variance(not volatility) in (t, K) grid, linearly interpolated along t-axis
 * and cubic spline interpolated along K-axis.
 *
 * Variance on valuation is supposed to be zero, if not stated otherwise.
 *
 * Implied variance is calculated according to a vol calendar.
 *
 * Local volatility can be calculated using Dupire equation.
 */
public class InterpolatedStrikeVolSurface implements ImpliedVolSurface, LocalVolSurface {
    private static final Logger logger = LoggerFactory.getLogger(InterpolatedStrikeVolSurface.class);

    private final LocalDateTime val;
    private final TreeMap<LocalDateTime, Interpolator1DCubicSpline> varInterp;
    private final VolCalendar volCalendar;
    private final double spot;
    private final double daysInYear;

    @JsonCreator
    public InterpolatedStrikeVolSurface(
            @JsonProperty("val") LocalDateTime val,
            @JsonProperty("varInterp") TreeMap<LocalDateTime, Interpolator1DCubicSpline> varInterp,
            @JsonProperty("volCalendar") VolCalendar volCalendar,
            @JsonProperty("spot") double spot,
            @JsonProperty("daysInYear") double daysInYear) {
        this.val = val;
        this.varInterp = varInterp;
        // set variance on valuation date to zero if not specified.
        if (!varInterp.containsKey(val)) {
            double[] strikes = varInterp.firstEntry().getValue().getXs();
            double[] zeros = new double[strikes.length];
            Arrays.setAll(zeros, i -> 0);
            this.varInterp.put(val, new Interpolator1DCubicSpline(strikes, zeros,
                    ExtrapTypeEnum.EXTRAP_1D_FLAT, ExtrapTypeEnum.EXTRAP_1D_FLAT));
        }
        this.volCalendar = volCalendar;
        this.spot = spot;
        this.daysInYear = daysInYear;
    }

    @Override
    public double variance(double forward, double strike, LocalDateTime expiry) {
        Map.Entry<LocalDateTime, Interpolator1DCubicSpline> lower = varInterp.floorEntry(expiry);
        if (lower == null) {
            lower = varInterp.firstEntry();
        }
        Map.Entry<LocalDateTime, Interpolator1DCubicSpline> higher = varInterp.higherEntry(lower.getKey());
        if (higher == null) {
            higher = lower;
            lower = varInterp.lowerEntry(higher.getKey());
        }
        double lowerVar = lower.getValue().value(strike);
        double higherVar = higher.getValue().value(strike);
        return volCalendar.interpVar(expiry, lower.getKey(), lowerVar, higher.getKey(), higherVar);
    }

    @Override
    public double iv(double forward, double strike, LocalDateTime expiry) {
        double var = variance(forward, strike, expiry);
        double tau = volCalendar.getEffectiveNumDays(val, expiry) / daysInYear;
        return FastMath.sqrt(var / tau);
    }

    /*@Override
    public double forwardPrice(double forward, double strike, LocalDateTime expiry, OptionType type) {
        double var = variance(forward, strike, expiry);
        if (type == OptionTypeEnum.CALL) {
            return Black.call(forward, strike, FastMath.sqrt(var), 1.0, 0.0, 0.0);
        } else {
            return Black.put(forward, strike, FastMath.sqrt(var), 1.0, 0.0, 0.0);
        }
    }*/

    public LocalDateTime getVal() {
        return val;
    }

    @JsonIgnore
    @Override
    public LocalDateTime getValuationDateTime() {
        return val;
    }

    @JsonIgnore
    @Override
    public LocalDate getValuationDate() {
        return val.toLocalDate();
    }

    @Override
    public double getSpot() {
        return spot;
    }

    @Override
    public ImpliedVolSurface roll(LocalDateTime newVal) {
        return new RolledVolSurface(newVal, this);
    }

    @Override
    public double localVol(double S, double forward, LocalDateTime t) {
        Map.Entry<LocalDateTime, Interpolator1DCubicSpline> lower
                = varInterp.floorEntry(t);
        if (lower == null) {
            lower = varInterp.firstEntry();
        }
        Map.Entry<LocalDateTime, Interpolator1DCubicSpline> higher
                = varInterp.higherEntry(lower.getKey());
        if (higher == null) {
            higher = lower;
            lower = varInterp.lowerEntry(higher.getKey());
        }
        double lowerVar = lower.getValue().value(S);
        double lowerVarK = lower.getValue().derivative(S, 1);
        double lowerVarKK = lower.getValue().derivative(S, 2);
        double higherVar = higher.getValue().value(S);
        double higherVarK = higher.getValue().derivative(S, 1);
        double higherVarKK = higher.getValue().derivative(S, 2);
        double delta = volCalendar.getEffectiveNumDays(lower.getKey(), higher.getKey());
        double a = volCalendar.getEffectiveNumDays(t, higher.getKey());
        double b = delta - a;
        a /= delta;
        b /= delta;
        double w = a * lowerVar + b * higherVar;
        double wK = a * lowerVarK + b * higherVarK;
        double wKK = a * lowerVarKK + b * higherVarKK;
        double wt = volCalendar.timeDerivative(t, lower.getKey(), lowerVar,
                higher.getKey(), higherVar) * daysInYear;
        double y = FastMath.log(S / forward);
        double localVar = t.isEqual(val) ? wt : wt / (1 + S * (0.5 - y / w) * wK
                + 0.25 * S * S * (-0.25 - 1 / w + y * y / w / w) * wK * wK
                + 0.5 * S * S * wKK);
        if (localVar < 0.0)
            logger.warn("negative local variance");
        return localVar >= 0.0 ? FastMath.sqrt(localVar) : 0.0;
    }

    @Override
    public double localVariance(double S, double forward, LocalDateTime start, LocalDateTime end) {
        double localVol = localVol(S, forward, start);
        double dt = volCalendar.getEffectiveNumDays(start, end) / daysInYear;
        return localVol * localVol * dt;
    }

    @Override
    public ImpliedVolSurface bump(double amount) {
        VolCalendar newVolCalendar = new VolCalendar(volCalendar.getWeekendWeight(),
                new HashMap<>(volCalendar.getSpecialWeights()));
        TreeMap<LocalDateTime, Interpolator1DCubicSpline> newVarInterp = new TreeMap<>();
        for (Map.Entry<LocalDateTime, Interpolator1DCubicSpline> e: varInterp.entrySet()) {
            LocalDateTime expiry = e.getKey();
            Interpolator1DCubicSpline var = e.getValue();
            double t = DateTimeUtils.days(val, expiry) / daysInYear;
            double[] xs = var.getXs();
            double[] ys = var.getYs();
            double[] newXs = Arrays.copyOf(xs, xs.length);
            double[] newYs = new double[ys.length];
            for (int i = 0; i < xs.length; i++) {
                double newImpliedVol = DoubleUtils.smallEnough(t, DoubleUtils.SMALL_NUMBER) ?
                        0.0 : FastMath.sqrt(ys[i] / t) + amount;
                newYs[i] = newImpliedVol * newImpliedVol * t;
            }
            newVarInterp.put(expiry, new Interpolator1DCubicSpline(newXs, newYs,
                    var.getExtrapMin(), var.getExtrapMax()));
        }
        return new InterpolatedStrikeVolSurface(val, newVarInterp,
                newVolCalendar, spot, daysInYear);
    }

    @Override
    public ImpliedVolSurface bumpPercent(double percent) {
        VolCalendar newVolCalendar = new VolCalendar(volCalendar.getWeekendWeight(),
                new HashMap<>(volCalendar.getSpecialWeights()));
        TreeMap<LocalDateTime, Interpolator1DCubicSpline> newVarInterp = new TreeMap<>();
        for (Map.Entry<LocalDateTime, Interpolator1DCubicSpline> e: varInterp.entrySet()) {
            LocalDateTime expiry = e.getKey();
            Interpolator1DCubicSpline var = e.getValue();
            double t = DateTimeUtils.days(val, expiry) / daysInYear;
            double[] xs = var.getXs();
            double[] ys = var.getYs();
            double[] newXs = Arrays.copyOf(xs, xs.length);
            double[] newYs = new double[ys.length];
            for (int i = 0; i < xs.length; i++) {
                double newImpliedVol = DoubleUtils.smallEnough(t, DoubleUtils.SMALL_NUMBER) ?
                        0.0 : FastMath.sqrt(ys[i] / t) * (1. + percent);
                newYs[i] = newImpliedVol * newImpliedVol * t;
            }
            newVarInterp.put(expiry, new Interpolator1DCubicSpline(newXs, newYs,
                    var.getExtrapMin(), var.getExtrapMax()));
        }
        return new InterpolatedStrikeVolSurface(val, newVarInterp,
                newVolCalendar, spot, daysInYear);
    }

    public TreeMap<LocalDateTime, Interpolator1DCubicSpline> getVarInterp() {
        return varInterp;
    }

    public VolCalendar getVolCalendar() {
        return volCalendar;
    }

    public double getDaysInYear() {
        return daysInYear;
    }
}
