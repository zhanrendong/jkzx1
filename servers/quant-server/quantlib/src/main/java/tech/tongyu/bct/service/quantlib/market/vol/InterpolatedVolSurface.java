package tech.tongyu.bct.service.quantlib.market.vol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.numerics.black.Black;
import tech.tongyu.bct.service.quantlib.common.numerics.interp.Interpolator1DCubicSpline;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;
import tech.tongyu.bct.service.quantlib.market.vol.utils.VolCalendar;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.TreeMap;

@BctQuantSerializable
public class InterpolatedVolSurface implements ImpliedVolSurface, LocalVolSurface {
    private static final Logger logger = LoggerFactory.getLogger(InterpolatedVolSurface.class);
    @JsonProperty("class")
    private final String type = InterpolatedVolSurface.class.getSimpleName();

    private final LocalDateTime val;
    private final TreeMap<LocalDateTime, Interpolator1DCubicSpline> varInterp;
    private final VolCalendar volCalendar;
    private final double spot;

    @JsonCreator
    public InterpolatedVolSurface(
            @JsonProperty("val") LocalDateTime val,
            @JsonProperty("varInterp") TreeMap<LocalDateTime, Interpolator1DCubicSpline> varInterp,
            @JsonProperty("volCalendar") VolCalendar volCalendar,
            @JsonProperty("spot") double spot) {
        this.val = val;
        this.varInterp = varInterp;
        this.volCalendar = volCalendar;
        this.spot = spot;
    }

    @Override
    public double variance(double forward, double strike, LocalDateTime expiry) {
        double m = FastMath.log(forward / strike);
        Map.Entry<LocalDateTime, Interpolator1DCubicSpline> lower = varInterp.floorEntry(expiry);
        if (lower == null) {
            lower = varInterp.firstEntry();
        }
        Map.Entry<LocalDateTime, Interpolator1DCubicSpline> higher = varInterp.higherEntry(lower.getKey());
        if (higher == null) {
            higher = lower;
            lower = varInterp.lowerEntry(higher.getKey());
        }
        double lowerVar = lower.getValue().value(m);
        double higherVar = higher.getValue().value(m);
        return volCalendar.interpVar(expiry, lower.getKey(), lowerVar, higher.getKey(), higherVar);
    }

    @Override
    public double iv(double forward, double strike, LocalDateTime expiry) {
        double var = variance(forward, strike, expiry);
        double tau = val.until(expiry, ChronoUnit.NANOS) / Constants.NANOSINDAY / Constants.DAYS_IN_YEAR;
        return FastMath.sqrt(var / tau);
    }

    @Override
    public double forwardPrice(double forward, double strike, LocalDateTime expiry, OptionType type) {
        double var = variance(forward, strike, expiry);
        if (type == OptionType.CALL) {
            return Black.call(forward, strike, FastMath.sqrt(var), 1.0, 0.0, 0.0);
        } else {
            return Black.put(forward, strike, FastMath.sqrt(var), 1.0, 0.0, 0.0);
        }
    }

    @Override
    public LocalDateTime getVal() {
        return val;
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
        double m = FastMath.log(forward / S);
        double y = -m;
        Map.Entry<LocalDateTime, Interpolator1DCubicSpline> lower = varInterp.floorEntry(t);
        if (lower == null) {
            lower = varInterp.firstEntry();
        }
        Map.Entry<LocalDateTime, Interpolator1DCubicSpline> higher = varInterp.higherEntry(lower.getKey());
        if (higher == null) {
            higher = lower;
            lower = varInterp.lowerEntry(higher.getKey());
        }
        double lowerVar = lower.getValue().value(m);
        double lowerVarY = -lower.getValue().derivative(m, 1);
        double lowervarYY = lower.getValue().derivative(m, 2);
        double higherVar = higher.getValue().value(m);
        double higherVarY = -higher.getValue().derivative(m, 1);
        double higherVarYY = higher.getValue().derivative(m, 2);
        double delta = volCalendar.getEffectiveNumDays(lower.getKey(), higher.getKey());
        double a = volCalendar.getEffectiveNumDays(t, higher.getKey());
        double b = delta - a;
        a /= delta;
        b /= delta;
        double w = a * lowerVar + b * higherVar;
        double wy = a * lowerVarY + b * higherVarY;
        double wyy = a * lowervarYY + b * higherVarYY;
        double wt = volCalendar.timeDerivative(t, lower.getKey(), lowerVar,
                higher.getKey(), higherVar) * Constants.DAYS_IN_YEAR;
        double localVar = t.isEqual(val) ? wt : wt / (1-y/w*wy + 0.25*(-0.25-1/w + y*y/(w*w))*wy*wy + 0.5*wyy);
        if (localVar < 0.0)
            logger.warn("negative local variance");
        return localVar >= 0.0 ? FastMath.sqrt(localVar) : 0.0;
    }

    @Override
    public double localVariance(double S, double forward, LocalDateTime start, LocalDateTime end) {
        double localVol = localVol(S, forward, start);
        double dt = volCalendar.getEffectiveNumDays(start, end) / Constants.DAYS_IN_YEAR;
        return localVol * localVol * dt;
    }

    @Override
    public ImpliedVolSurface bump(double amount) {
        throw new UnsupportedOperationException(type + " does not support bump().");
//        return null;
    }

    public String getType() {
        return type;
    }

    public TreeMap<LocalDateTime, Interpolator1DCubicSpline> getVarInterp() {
        return varInterp;
    }

    public VolCalendar getVolCalendar() {
        return volCalendar;
    }
}
