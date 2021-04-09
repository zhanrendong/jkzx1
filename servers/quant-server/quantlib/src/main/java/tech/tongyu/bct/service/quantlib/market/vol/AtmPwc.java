package tech.tongyu.bct.service.quantlib.market.vol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.numerics.black.Black;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


/**
 * Piece-wise constant flat vol surface, i.e. local vols are constant in between expiries
 * WARNING: This vol surface stores and works internally with daily vols
 * To be consistent, please make sure that daysInYears parameter is set correctly when constructing this vol surface.
 * Vol quotes are usually annualized. Thus daysInYear parameter is required for proper vol conversion.
 * NOTE: the only important thing is that variance is invariant:
 * dailyVol * dailyVol * #daysToExpiry = annualVol * annualVol * yearsToExpiry
 * where yearsToExpiry = #daysToExpiry / daysInYears
 */
@BctQuantSerializable
public class AtmPwc implements ImpliedVolSurface, LocalVolSurface {
    @JsonProperty("class")
    private final String type = AtmPwc.class.getSimpleName();
    private LocalDateTime val;
    private TreeMap<Double, Segment> segments;
    private double spot;
    private double daysInYear;

    @JsonCreator
    public AtmPwc(
            @JsonProperty("val") LocalDateTime val,
            @JsonProperty("spot") double spot,
            @JsonProperty("segments") TreeMap<Double, Segment> segments,
            @JsonProperty("daysInYear") double daysInYear
    ) {
        this.val = val;
        this.spot = spot;
        this.segments = segments;
        this.daysInYear = daysInYear;
    }

    @BctQuantApi(
            name = "qlVolSurfaceConstCreate",
            description = "Create a constant (flat) vol surface",
            argNames = {"val", "spot", "vol", "daysInYear"},
            argTypes = {"DateTime", "Double", "Double", "Double"},
            argDescriptions = {"Vol surface start date", "spot", "Volatility", "Number of days in a year"},
            retName = "vs", retType = "Handle", retDescription = "A newly constructed vol surface"
    )
    public static AtmPwc flat(LocalDateTime val, double spot, double vol, double daysInYear) {
        Segment seg = new Segment();
        // convert vol into daily vol
        double localVol = vol / FastMath.sqrt(daysInYear);
        seg.localVol = localVol;
        seg.varSoFar = 0.0;
        TreeMap<Double, Segment> segments = new TreeMap<>();
        segments.put(0.0, seg);
        // At least one expiry
        seg = new Segment();
        seg.localVol = localVol;
        seg.varSoFar = vol * vol;
        segments.put(daysInYear, seg);
        return new AtmPwc(val, spot, segments, daysInYear);
    }

    private double var(double tau) {
        Map.Entry<Double, Segment> lower = segments.floorEntry(tau);
        if (lower == null) {
            lower = segments.firstEntry();
        }
        double dt = tau - lower.getKey();
        Segment seg = lower.getValue();
        return seg.varSoFar + seg.localVol * seg.localVol * dt;
    }

    @Override
    public double variance(double forward, double strike, LocalDateTime expiry) {
        double days = val.until(expiry, ChronoUnit.NANOS) / Constants.NANOSINDAY;
        return var(days);
    }

    @Override
    public double iv(double forward, double strike, LocalDateTime expiry) {
        double days = val.until(expiry, ChronoUnit.NANOS) / Constants.NANOSINDAY;
        if (days < Constants.SMALL_NUMBER)
            return segments.firstEntry().getValue().localVol * FastMath.sqrt(daysInYear);
        return FastMath.sqrt(var(days) * daysInYear / days);
    }

    @Override
    public double forwardPrice(double forward, double strike, LocalDateTime expiry, OptionType type) {
        double days = val.until(expiry, ChronoUnit.NANOS) / Constants.NANOSINDAY;
        double var = var(days);
        if (type == OptionType.CALL) {
            return Black.call(forward, strike, FastMath.sqrt(var), 1.0, 0.0, 0.0);
        } else {
            return Black.put(forward, strike, FastMath.sqrt(var), 1.0, 0.0, 0.0);
        }
    }

    @Override
    public double localVol(double S, double forward, LocalDateTime t) {
        double days = val.until(t, ChronoUnit.NANOS) / Constants.NANOSINDAY;
        if (days < Constants.SMALL_NUMBER)
            return segments.firstEntry().getValue().localVol * FastMath.sqrt(daysInYear);
        Map.Entry<Double, Segment> lower = segments.floorEntry(days);
        if (lower == null) {
            lower = segments.firstEntry();
        }
        return lower.getValue().localVol * FastMath.sqrt(daysInYear);
    }

    public double var(LocalDateTime t) {
        double days = val.until(t, ChronoUnit.NANOS) / Constants.NANOSINDAY;
        if (days < Constants.SMALL_NUMBER)
            return 0;
        return var(days);
    }

    @Override
    public double localVariance(double S, double forward, LocalDateTime start, LocalDateTime end) {
        return var(end) - var(start);
    }

    @Override
    public AtmPwc roll(LocalDateTime newVal) {
        double days = val.until(newVal, ChronoUnit.NANOS) / Constants.NANOSINDAY;
        Map.Entry<Double, Segment> lower = segments.floorEntry(days);
        if (lower == null) {
            lower = segments.firstEntry();
        }
        double dt = days - lower.getKey();
        Segment seg = lower.getValue();
        Segment newSeg = new Segment();
        newSeg.localVol = seg.localVol;
        newSeg.varSoFar = 0.0;
        double var = seg.varSoFar + newSeg.localVol * newSeg.localVol * dt;

        TreeMap<Double, Segment> newSegments = new TreeMap<>();
        newSegments.put(0.0, newSeg);

        // add the rest
        Iterator<Map.Entry<Double, Segment>> iter = segments.tailMap(lower.getKey()).entrySet().iterator();
        iter.next();
        while(iter.hasNext()) {
            Map.Entry<Double, Segment> t = iter.next();
            newSeg = new Segment();
            newSeg.localVol = t.getValue().localVol;
            newSeg.varSoFar = t.getValue().varSoFar - var;
            newSegments.put(t.getKey() - days, newSeg);
        }

        return new AtmPwc(newVal, spot, newSegments, daysInYear);
    }

    @Override
    public ImpliedVolSurface bump(double amount) {
        TreeMap<Double, AtmPwc.Segment> segs = new TreeMap<>();
        Iterator<Map.Entry<Double, Segment>> iter = segments.entrySet().iterator();
        Map.Entry<Double, Segment> e = iter.next();
        double t = e.getKey();
        double varSoFar = 0.0;
        while(iter.hasNext()) {
            e = iter.next();
            double oldIV = FastMath.sqrt(
                    e.getValue().varSoFar / e.getKey() * this.daysInYear);
            double newIV = oldIV + amount;
            double varExpiry = newIV * newIV * e.getKey() / daysInYear;
            AtmPwc.Segment seg = new AtmPwc.Segment();
            seg.varSoFar = varSoFar;
            seg.localVol = FastMath.sqrt((varExpiry - varSoFar) / (e.getKey() - t));
            segs.put(t, seg);
            t = e.getKey();
            varSoFar = varExpiry;
        }
        // the last segment represents local volatility after the last expiry.
        // its local volatility is the same as the one before the last expiry.
        AtmPwc.Segment seg = new AtmPwc.Segment();
        seg.varSoFar = varSoFar;
        seg.localVol = segs.lastEntry().getValue().localVol;
        segs.put(t, seg);
        return new AtmPwc(val, spot, segs, daysInYear);
    }

    public LocalDateTime getVal() {
        return val;
    }

    public void setVal(LocalDateTime val) {
        this.val = val;
    }

    public TreeMap<Double, Segment> getSegments() {
        return segments;
    }

    public void setSegments(TreeMap<Double, Segment> segments) {
        this.segments = segments;
    }

    public double getDaysInYear() {
        return daysInYear;
    }

    public void setDaysInYear(double daysInYear) {
        this.daysInYear = daysInYear;
    }

    public String getType() {
        return type;
    }

    @Override
    public double getSpot() {
        return spot;
    }

    public void setSpot(double spot) {
        this.spot = spot;
    }

    public static class Segment {
        public double localVol;
        public double varSoFar;
    }
}