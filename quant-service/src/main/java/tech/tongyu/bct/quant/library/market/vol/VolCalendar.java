package tech.tongyu.bct.quant.library.market.vol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.library.common.QuantlibSerializableObject;
import tech.tongyu.bct.quant.library.financial.date.Holidays;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Variance Interpolator based on weighted vols on weekends and special days
 * It is common to assign different vol weights to holidays and important dates. For example, 0 weight
 * on holidays and higher than 1 weights on Fed meeting days to reflect the fact that certain events
 * have positive/negative impact to volatilities. Thus when interpolating variances in time we need
 * an interpolator to take into account vol weights.
 *
 * This class provides a utility function that returns effective number of days between two given dates.
 * The number of days are weighted by vol weights.
 */
public class VolCalendar implements QuantlibSerializableObject {
    private final double weekendWeight;
    private final SortedSet<LocalDate> specialDates;
    private final Map<LocalDate, Double> specialWeights;

    @JsonCreator
    public VolCalendar(
            @JsonProperty("weekendWeight") double weekendWeight,
            @JsonProperty("specialWeights") Map<LocalDate, Double> specialWeights) {
        this.weekendWeight = weekendWeight;
        this.specialWeights = specialWeights;
        // handle the dates are both a weekend and a special date
        this.specialDates = new TreeSet<>(specialWeights.keySet());
        for (LocalDate t : specialDates) {
            if (DateTimeUtils.isWeekend(t)) {
                double orig = specialWeights.get(t);
                specialWeights.put(t, orig - weekendWeight);
            }
        }
    }

    public static VolCalendar none() {
        return new VolCalendar(1.0, new HashMap<>());
    }

    public static VolCalendar weekendsOnly(double weekendWeight) {
        return new VolCalendar(weekendWeight, new HashMap<>());
    }

    public static VolCalendar fromCalendars(List<String> calendars) {
        Map<LocalDate, Double> specialWeights = Holidays.Instance.listHolidays(calendars)
                .stream().collect(Collectors.toMap(t -> t, t -> 0.0));
        return new VolCalendar(0.0, specialWeights);
    }

    public double getEffectiveNumDays(LocalDateTime start, LocalDateTime end) {
        // buggy code below
        // replace with much slower version, but correct
        /*// weekends
        double effectiveNumDays = start.until(end, ChronoUnit.NANOS) / Constants.NANOSINDAY;
        long numWeekends = DateUtils.countWeekends(start.toLocalDate(), end.toLocalDate());
        effectiveNumDays += (weekendWeight - 1.0) * numWeekends;
        double remaining = end.toLocalTime().toNanoOfDay() / Constants.NANOSINDAY;
        if (end.toLocalDate().getDayOfWeek().getValue() > 5)
            remaining *= weekendWeight;
        effectiveNumDays += remaining;
        double extra = start.toLocalTime().toNanoOfDay() / Constants.NANOSINDAY;
        if (start.toLocalDate().getDayOfWeek().getValue() > 5)
            extra *= 1.0 - weekendWeight;
        effectiveNumDays += extra;*/
        // special dates
        /*if (!specialDates.isEmpty()) {
            SortedSet<LocalDate> dates = specialDates.subSet(start.toLocalDate(), end.toLocalDate());
            for (LocalDate t : dates) {
                effectiveNumDays += (specialWeights.get(t) - 1.0);
            }
            // check if end happens to be a special date
            if (specialDates.contains(end.toLocalDate())) {
                effectiveNumDays += (specialWeights.get(end.toLocalDate()))
                        * end.toLocalTime().toNanoOfDay() / Constants.NANOSINDAY;
            }
            // check if start happens to be a special date
            if (specialDates.contains(start.toLocalDate())) {
                effectiveNumDays -= (specialWeights.get(start.toLocalDate()))
                        * start.toLocalTime().toNanoOfDay() / Constants.NANOSINDAY;
            }
        }*/
        if (end.isBefore(start))
            throw new RuntimeException("End time is before start time.");
        double effectiveNumDays = 0.0;
        if (start.toLocalDate().isEqual(end.toLocalDate())) {
            effectiveNumDays = DateTimeUtils.days(start, end);
            if (start.toLocalDate().getDayOfWeek().getValue() < 6 && !specialDates.contains(start.toLocalDate()))
                return effectiveNumDays;
            double w = 0.0;
            if (start.toLocalDate().getDayOfWeek().getValue() > 5)
                w += weekendWeight;
            if (specialDates.contains(start.toLocalDate()))
                w += specialWeights.get(start.toLocalDate());
            return effectiveNumDays * w;
        }
        LocalDate d = start.toLocalDate().plusDays(1);
        while(d.isBefore(end.toLocalDate())) {
            if (d.getDayOfWeek().getValue() < 6 && !specialDates.contains(d))
                effectiveNumDays += 1.0;
            else {
                if (d.getDayOfWeek().getValue() > 5)
                    effectiveNumDays += weekendWeight;
                if (specialDates.contains(d))
                    effectiveNumDays += specialWeights.get(d);
            }
            d = d.plusDays(1);
        }
        // start
        double extra = 1.0 - (double)start.toLocalTime().toNanoOfDay() / DateTimeUtils.NANOS_IN_DAY;
        double w = 0.0;
        if (start.toLocalDate().getDayOfWeek().getValue() < 6 && !specialDates.contains(start.toLocalDate()))
            w = 1.0;
        else {
            if (start.toLocalDate().getDayOfWeek().getValue() > 5)
                w += weekendWeight;
            if (specialDates.contains(start.toLocalDate()))
                w += specialWeights.get(start.toLocalDate());
        }
        effectiveNumDays += extra * w;
        // end
        extra = (double)end.toLocalTime().toNanoOfDay() / DateTimeUtils.NANOS_IN_DAY;
        w = 0.0;
        if (end.toLocalDate().getDayOfWeek().getValue() < 6 && !specialDates.contains(end.toLocalDate()))
            w = 1.0;
        else {
            if (end.toLocalDate().getDayOfWeek().getValue() > 5)
                w += weekendWeight;
            if (specialDates.contains(end.toLocalDate()))
                w += specialWeights.get(end.toLocalDate());
        }
        effectiveNumDays += extra * w;
        return effectiveNumDays;
    }

    public double interpVar(LocalDateTime t, LocalDateTime start, double varStart, LocalDateTime end, double varEnd) {
        if (t.isBefore(start))
            throw new RuntimeException("Required time is before start time.");
//        if (t.isAfter(end))
//            throw new RuntimeException("Required time is after end time.");
        double effectiveT = getEffectiveNumDays(start, end);
        if (effectiveT <= 0)
            return varStart;
        double var = varStart + (varEnd - varStart) / effectiveT * getEffectiveNumDays(start, t);
        if (var < 0.0)
            var = 0.0;
        return var;
    }

    public double getWeight(LocalDateTime t) {
        if (!DateTimeUtils.isWeekend(t) && !specialDates.contains(t.toLocalDate()))
            return 1.0;
        double weight = 0.0;
        if (DateTimeUtils.isWeekend(t))
            weight = weekendWeight;
        if (specialDates.contains(t.toLocalDate())) {
            weight += specialWeights.get(t.toLocalDate());
        }
        return weight;
    }

    public double timeDerivative(LocalDateTime t,
                                 LocalDateTime start, double varStart,
                                 LocalDateTime end, double varEnd) {
        double effectiveNumDays = getEffectiveNumDays(start, end);
        double effectiveLocalVariance = (varEnd - varStart) / effectiveNumDays;
        return effectiveLocalVariance * getWeight(t);
    }

    public double getWeekendWeight() {
        return weekendWeight;
    }

    public SortedSet<LocalDate> getSpecialDates() {
        return specialDates;
    }

    public Map<LocalDate, Double> getSpecialWeights() {
        return specialWeights;
    }
}
