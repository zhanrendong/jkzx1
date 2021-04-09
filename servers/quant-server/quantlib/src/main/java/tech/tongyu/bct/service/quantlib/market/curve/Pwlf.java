package tech.tongyu.bct.service.quantlib.market.curve;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;

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
@BctQuantSerializable
public class Pwlf implements Discount {
    @JsonProperty("class")
    private final String type = Pwlf.class.getSimpleName();

    private LocalDateTime val;
    private TreeMap<Double, Segment> segments;

    @JsonCreator
    private Pwlf(
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
    @BctQuantApi(
            name = "qlCurveConstCreate",
            description = "Create a constant (flat) discount curve",
            argNames = {"val", "rate", "daysInYear"},
            argTypes = {"DateTime", "Double", "Double"},
            argDescriptions = {"Curve start date", "Continuous compounding rate", "Number of days in a year"},
            retName = "yc", retType = "Handle", retDescription = "A newly constructed curve"
    )
    public static Pwlf flat(LocalDateTime val, double r, double daysInYear) {
        Segment seg = new Segment();
        // convert the annual rate into daily rate
        seg.r = r / daysInYear;
        seg.slope = 0.0;
        seg.log_df = 0.0;
        TreeMap<Double, Segment> segments = new TreeMap<>();
        segments.put(0.0, seg);
        return new Pwlf(val, segments);
    }

    /**
     * Construct a piecewise constant forward rate curve from input dates and discount factors
     *
     * @param ts  Dates on which discount factors are known,these time stamps should include the valuation date
     * @param dfs Discount factors, including the discount factor on the valuation date
     * @return A piecewise constant forward curve
     */
    public static Pwlf pwcfFromDfs(LocalDateTime val, LocalDateTime[] ts, double[] dfs) {
        TreeMap<Double, Segment> segments = new TreeMap<>();
        LocalDateTime prevT = val;
        double prevDf = 1.0;
        double t = 0.0;
        for (int i = 0; i < ts.length; ++i) {
            if (ts[i].isAfter(val)) {
                Segment seg = new Segment();
                seg.slope = 0.0;
                double tau = prevT.until(ts[i], ChronoUnit.NANOS) / Constants.NANOSINDAY;
                seg.r = -FastMath.log(dfs[i] / prevDf) / tau;
                seg.log_df = FastMath.log(prevDf);
                segments.put(t, seg);
                t += tau;
                prevT = ts[i];
                prevDf = dfs[i];
            }
        }
        return new Pwlf(val, segments);
    }

    /**
     * @param val        valuation date
     * @param rts        known repo rates time stamps (do not include the valuation date)
     * @param r          annually repo rates known on each time stamps
     * @param daysInYear number of days in a year
     * @return the piece wise linear forward discount factor curve
     *//*
    @BctQuantApi(
            name = "qldfrCurveCreate",
            description = "generate the r discount factor curve",
            argNames = {"val", "rts", "r", "daysInYear"},
            argTypes = {"DateTime", "ArrayDateTime", "ArrayDouble", "Double"},
            argDescriptions = {"valuation date", "repo rates time stamps", "repo rate", "number of days in a year"},
            retName = "qfrCurve", retType = "Handle", retDescription = "")
    public static Pwlf rCurveCreate(LocalDateTime val, LocalDateTime[] rts, double[] r, double daysInYear) {
        int rn = rts.length;
        LocalDateTime[] erts = new LocalDateTime[rn + 1];
        double[] dfr = new double[rn + 1];
        erts[0] = val;
        dfr[0] = 1.0;
        for (int i = 0; i < rn; i++) {
            erts[i + 1] = rts[i];

            double tau_i = val.until(rts[i], ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
            dfr[i + 1] = exp(-r[i] * tau_i);
        }
        return Pwlf.pwcfFromDfs(val, erts, dfr);
    }*/


    // this is necessary because a map needs a type hint to deserialize
    /* not needed since a default constructor is provided. uncomment if later want to disable the default constructor.
    @JsonCreator
    public static Pwlf create(
            @JsonProperty("val") LocalDateTime val,
            @JsonProperty("segments") TreeMap<Double, Segment> segments
    ) {
        return new Pwlf(val, segments);
    }
    */

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
        double days = val.until(t, ChronoUnit.NANOS) / Constants.NANOSINDAY;
        return df(days);
    }

    @Override
    public Pwlf roll(LocalDateTime newVal) {
        double days = val.until(newVal, ChronoUnit.NANOS) / Constants.NANOSINDAY;
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
        return new Pwlf(newVal, newSegments);
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getVal() {
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