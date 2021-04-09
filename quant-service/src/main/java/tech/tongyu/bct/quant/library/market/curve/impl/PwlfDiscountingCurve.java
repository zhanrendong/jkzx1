package tech.tongyu.bct.quant.library.market.curve.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.library.common.QuantlibSerializableObject;
import tech.tongyu.bct.quant.library.market.curve.DiscountingCurve;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * A piecewise linear forward curve
 * <p>
 * On each time interval the forward rate is a linear function of time. This is described by the structure Segment.
 * Each segment is determined by its forward rate on its starting time, the slope and the log of the discounting
 * factor accumulates so far up to the segment's starting time. Any future forward rate is thus determined by
 * the starting rate and the slope. The accumulated discounting factor only serves to speed up the calculation
 * to find a future discounting factor.
 * </p>
 * <p>
 * Note: This implementation chooses number of days as the time unit to interpolate discounting factors. Internally
 * interest rates are converted to daily rates from discounting factors
 * </p>
 *
 * @author Lu Lu
 * @since 2016-06-21
 */
public class PwlfDiscountingCurve implements DiscountingCurve {
    private LocalDateTime val;
    private TreeMap<Double, Segment> segments;

    @JsonCreator
    private PwlfDiscountingCurve(
            @JsonProperty("val") LocalDateTime val,
            @JsonProperty("segments") TreeMap<Double, Segment> segments) {
        this.val = val;
        this.segments = segments;
    }

    /**
     * Creates a flat continuously compounded  curve.
     * <p>
     * The input annualized rate will be converted into daily rate by the formula <br>
     * r = daily rate * number of days in a year
     * </p>
     *
     * @param val        The curve start date
     * @param r          The continuous compounding rate (annualized)
     * @param daysInYear Number of days in a year.
     * @return A piece-wise constant forward rate curve
     */
    public static PwlfDiscountingCurve flat(LocalDateTime val, double r, double daysInYear) {
        Segment seg = new Segment();
        // convert the annual rate into daily rate
        seg.r = r / daysInYear;
        seg.slope = 0.0;
        seg.log_df = 0.0;
        TreeMap<Double, Segment> segments = new TreeMap<>();
        segments.put(0.0, seg);
        return new PwlfDiscountingCurve(val, segments);
    }

    /**
     * Construct a piecewise constant forward rate curve from input dates and discount factors
     *
     * @param ts  Dates on which discount factors are known,these time stamps should include the valuation date
     * @param dfs Discount factors, including the discount factor on the valuation date
     * @return A piecewise constant forward curve
     */
    public static PwlfDiscountingCurve pwcfFromDfs(LocalDateTime val, LocalDateTime[] ts, double[] dfs) {
        TreeMap<Double, Segment> segments = new TreeMap<>();
        LocalDateTime prevT = val;
        double prevDf = 1.0;
        double t = 0.0;
        for (int i = 0; i < ts.length; ++i) {
            if (ts[i].isAfter(val)) {
                Segment seg = new Segment();
                seg.slope = 0.0;
                double tau = DateTimeUtils.days(prevT, ts[i]);
                seg.r = -FastMath.log(dfs[i] / prevDf) / tau;
                seg.log_df = FastMath.log(prevDf);
                segments.put(t, seg);
                t += tau;
                prevT = ts[i];
                prevDf = dfs[i];
            }
        }
        return new PwlfDiscountingCurve(val, segments);
    }

    // return log df given a time (number of days)
    private double lnDf(double t) {
        // use floor so that t=0.9999... will return t=0 segment
        Map.Entry<Double, Segment> lower = segments.floorEntry(t);
        if (lower == null) {
            lower = segments.firstEntry();
        }
        double dt = t - lower.getKey();
        Segment seg = lower.getValue();
        double logdf = seg.log_df;
        logdf -= dt * (seg.r + 0.5 * dt * seg.slope);
        return logdf;
    }

    private double df(double t) {
        double logdf = lnDf(t);
        return FastMath.exp(logdf);
    }

    @Override
    public double df(LocalDateTime t) {
        double days = DateTimeUtils.days(val, t);
        return df(days);
    }

    @Override
    public double df(LocalDate t) {
        return df(t.atStartOfDay());
    }

    @Override
    public Double apply(LocalDate t) {
        return df(t);
    }

    @Override
    public PwlfDiscountingCurve roll(LocalDateTime newVal) {
        double days = DateTimeUtils.days(val, newVal);
        // use floor so that t=0.9999... will return t=0 segment
        TreeMap<Double, Segment> newSegments = new TreeMap<>();
        // handle the first segment
        Map.Entry<Double, Segment> lower = segments.floorEntry(days);
        if (lower == null) {
            lower = segments.firstEntry();
        }
        double dt = days - lower.getKey();
        Segment seg = lower.getValue();
        double logdf = lnDf(days);
        Segment newSeg = new Segment();
        newSeg.r = seg.r + dt * seg.slope;
        newSeg.slope = seg.slope;
        newSeg.log_df = 0.0;

        newSegments.put(0.0, newSeg);

        // add the rest
        Iterator<Map.Entry<Double, Segment>> iter = segments.tailMap(lower.getKey()).entrySet().iterator();
        iter.next();
        while(iter.hasNext()) {
            Map.Entry<Double, Segment> t = iter.next();
            newSeg = new Segment();
            newSeg.r = t.getValue().r;
            newSeg.slope = t.getValue().slope;
            newSeg.log_df = t.getValue().log_df - logdf;
            newSegments.put(t.getKey() - days, newSeg);
        }
        return new PwlfDiscountingCurve(newVal, newSegments);
    }

    @Override
    public PwlfDiscountingCurve bump(double amount) {
        TreeMap<Double, Segment> newSegments = new TreeMap<>();
        segments.forEach((t, segment) -> {
            Segment s = new Segment();
            s.slope = segment.slope;
            s.r = segment.r + amount / DateTimeUtils.DAYS_IN_YEAR;
            s.log_df = segment.log_df - amount * t / DateTimeUtils.DAYS_IN_YEAR;
            newSegments.put(t, s);
        });
        return new PwlfDiscountingCurve(this.val, newSegments);
    }

    @Override
    public DiscountingCurve bumpPercent(double percent) {
        TreeMap<Double, Segment> newSegments = new TreeMap<>();
        double prevLnDf = 0.;
        double prevT = 0.;
        double prevBump = 0.;
        for(Map.Entry<Double, Segment> kv : segments.entrySet()) {
            double t = kv.getKey();
            Segment seg = kv.getValue();
            Segment s = new Segment();
            s.slope = seg.slope;
            prevLnDf -= prevBump * (t - prevT);
            s.log_df = seg.log_df + prevLnDf;
            prevBump = percent * seg.r;
            s.r = seg.r + prevBump;
            prevT = t;
            newSegments.put(t, s);
        }
        return new PwlfDiscountingCurve(this.val, newSegments);
    }

    @Override
    public double shortRate(LocalDateTime t) {
        double days = DateTimeUtils.days(val, t);
        // use floor so that t=0.9999... will return t=0 piecewise
        Map.Entry<Double, Segment> lower = segments.floorEntry(days);
        if (lower == null) {
            lower = segments.firstEntry();
        }
        double dt = days - lower.getKey();
        Segment seg = lower.getValue();
        return seg.r + dt * seg.slope;
    }

    public LocalDateTime getVal() {
        return val;
    }

    @JsonIgnore
    @Override
    public LocalDate getValuationDate() {
        return val.toLocalDate();
    }

    @JsonIgnore
    @Override
    public LocalDateTime getValuationDateTime() {
        return val;
    }

    public TreeMap<Double, Segment> getSegments() {
        return segments;
    }

    public static class Segment {
        public double r;
        public double slope;
        public double log_df;
    }
}
