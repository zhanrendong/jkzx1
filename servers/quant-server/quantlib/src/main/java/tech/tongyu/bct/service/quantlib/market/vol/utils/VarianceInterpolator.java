package tech.tongyu.bct.service.quantlib.market.vol.utils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

/**
 * Variance Interpolator based on weighted vols on weekends and special days
 * It is common to assign different vol weights to holidays and important dates. For example, 0 weight
 * on holidays and higher than 1 weights on Fed meeting days to reflect the fact that certain events
 * have positive/negative impact to volatilities. Thus when interpolating variances in time we need
 * an interpolator to take into account vol weights.
 *
 * This class implements a simple interpolation scheme for this purpose. The basic assumption is that
 * the variance is linear in weighted time scale. That is, we assume
 *
 * variance(t_0, t_n) = local_vol ^ 2 * \sum weight_i * (t_i - t_(i-1))
 *
 * where t_0 is the interval beginning date and t_n the end. t_i are dates in between. The variances
 * on t_0 and t_n are known (from, say, market vol quotes on t_0 and t_n). For variances on intermediate
 * points t_i between t_0 and t_n:
 *
 * variance(t_0, t) = local_vol ^ 2 * \sum weight_j * (t_j - t_(j-1)) for t_j <= t
 */
public class VarianceInterpolator {
    private final VolCalendar volCalendar;
    private final TreeMap<LocalDateTime, Segment> segments;

    public static class Segment {
        double localVol;
        double varSoFar;
    }

    public VarianceInterpolator(VolCalendar volCalendar, TreeMap<LocalDateTime, Segment> segments) {
        this.volCalendar = volCalendar;
        this.segments = segments;
    }

    public double var(LocalDateTime t) {
        Map.Entry<LocalDateTime, Segment> lower = segments.floorEntry(t);
        if (lower == null) {
            lower = segments.firstEntry();
        }
        double effectiveNumDays = volCalendar.getEffectiveNumDays(lower.getKey(), t);
        return lower.getValue().varSoFar + lower.getValue().localVol * effectiveNumDays;
    }
}
