package tech.tongyu.bct.quant.library.market.vol.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.common.QuantlibSerializableObject;
import tech.tongyu.bct.quant.library.market.vol.ImpliedVolSurface;
import tech.tongyu.bct.quant.library.market.vol.LocalVolSurface;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static tech.tongyu.bct.quant.library.common.DoubleUtils.SMALL_NUMBER;

/**
 * Piece-wise constant flat vol surface, i.e. local vols are constant in between expiries
 * WARNING: This vol surface stores and works internally with daily vols
 * To be consistent, please make sure that daysInYears parameter is set correctly when constructing this vol surface.
 * Vol quotes are usually annualized. Thus daysInYear parameter is required for proper vol conversion.
 * NOTE: the only important thing is that variance is invariant:
 * dailyVol * dailyVol * #daysToExpiry = annualVol * annualVol * yearsToExpiry
 * where yearsToExpiry = #daysToExpiry / daysInYears
 */
public class AtmPwcVolSurface implements ImpliedVolSurface, LocalVolSurface {
    private LocalDateTime val;
    private TreeMap<Double, Segment> segments;
    private double spot;
    private double daysInYear;

    @JsonCreator
    public AtmPwcVolSurface(
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

    public static AtmPwcVolSurface flat(LocalDateTime val, double spot, double vol, double daysInYear) {
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
        return new AtmPwcVolSurface(val, spot, segments, daysInYear);
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
        double days = DateTimeUtils.days(val, expiry);
        return var(days);
    }

    @Override
    public double iv(double forward, double strike, LocalDateTime expiry) {
        double days = DateTimeUtils.days(val, expiry);
        if (DoubleUtils.smallEnough(days, SMALL_NUMBER))
            return segments.firstEntry().getValue().localVol * FastMath.sqrt(daysInYear);
        return FastMath.sqrt(var(days) * daysInYear / days);
    }

    /*@Override
    public double forwardPrice(double forward, double strike, LocalDateTime expiry, OptionTypeEnum type) {
        double days = DateTimeUtils.days(val, expiry);
        double var = var(days);
        if (type == OptionType.CALL) {
            return Black.call(forward, strike, FastMath.sqrt(var), 1.0, 0.0, 0.0);
        } else {
            return Black.put(forward, strike, FastMath.sqrt(var), 1.0, 0.0, 0.0);
        }
    }
*/

    @Override
    public double localVol(double S, double forward, LocalDateTime t) {
        double days = DateTimeUtils.days(val, t);
        if (days < DoubleUtils.SMALL_NUMBER)
            return segments.firstEntry().getValue().localVol * FastMath.sqrt(daysInYear);
        Map.Entry<Double, Segment> lower = segments.floorEntry(days);
        if (lower == null) {
            lower = segments.firstEntry();
        }
        return lower.getValue().localVol * FastMath.sqrt(daysInYear);
    }

    public double var(LocalDateTime t) {
        double days = DateTimeUtils.days(val, t);
        if (DoubleUtils.smallEnough(days, SMALL_NUMBER))
            return 0;
        return var(days);
    }

    @Override
    public double localVariance(double S, double forward, LocalDateTime start, LocalDateTime end) {
        return var(end) - var(start);
    }

    @Override
    public AtmPwcVolSurface roll(LocalDateTime newVal) {
        double days = DateTimeUtils.days(val, newVal);
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

        return new AtmPwcVolSurface(newVal, spot, newSegments, daysInYear);
    }

    @Override
    public ImpliedVolSurface bump(double amount) {
        TreeMap<Double, AtmPwcVolSurface.Segment> segs = new TreeMap<>();
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
            Segment seg = new Segment();
            seg.varSoFar = varSoFar;
            seg.localVol = FastMath.sqrt((varExpiry - varSoFar) / (e.getKey() - t));
            segs.put(t, seg);
            t = e.getKey();
            varSoFar = varExpiry;
        }
        // the last segment represents local volatility after the last expiry.
        // its local volatility is the same as the one before the last expiry.
        Segment seg = new Segment();
        seg.varSoFar = varSoFar;
        seg.localVol = segs.lastEntry().getValue().localVol;
        segs.put(t, seg);
        return new AtmPwcVolSurface(val, spot, segs, daysInYear);
    }

    @Override
    public ImpliedVolSurface bumpPercent(double percent) {
        TreeMap<Double, AtmPwcVolSurface.Segment> segs = new TreeMap<>();
        Iterator<Map.Entry<Double, Segment>> iter = segments.entrySet().iterator();
        Map.Entry<Double, Segment> e = iter.next();
        double t = e.getKey();
        double varSoFar = 0.0;
        while(iter.hasNext()) {
            e = iter.next();
            double oldIV = FastMath.sqrt(
                    e.getValue().varSoFar / e.getKey() * this.daysInYear);
            double newIV = oldIV * (1 + percent);
            double varExpiry = newIV * newIV * e.getKey() / daysInYear;
            Segment seg = new Segment();
            seg.varSoFar = varSoFar;
            seg.localVol = FastMath.sqrt((varExpiry - varSoFar) / (e.getKey() - t));
            segs.put(t, seg);
            t = e.getKey();
            varSoFar = varExpiry;
        }
        // the last segment represents local volatility after the last expiry.
        // its local volatility is the same as the one before the last expiry.
        Segment seg = new Segment();
        seg.varSoFar = varSoFar;
        seg.localVol = segs.lastEntry().getValue().localVol;
        segs.put(t, seg);
        return new AtmPwcVolSurface(val, spot, segs, daysInYear);
    }

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
