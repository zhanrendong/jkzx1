package tech.tongyu.bct.quant.library.priceable.common.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.Tuple2;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.HasFixings;
import tech.tongyu.bct.quant.library.priceable.feature.ImmediateExercise;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Range accrual option. Suppose total number of observation dates is N, and total weight is W. If on m observation
 * dates, the specified price locates between the barriers (excluded), for which the accumulated weight is w, the
 * actual payment is w / W * maxPayment, paid on expiration date.
 */
public class RangeAccrual implements HasExpiry, ImmediateExercise, HasFixings {
    protected final LocalDateTime expiry;
    protected final double lowBarrier;
    protected final double highBarrier;
    protected final double maxPayment;
    protected final List<Tuple2<LocalDateTime, Double>> observationDates;  // and weights
    protected final Map<LocalDateTime, Double> fixings;

    public RangeAccrual(LocalDateTime expiry, double lowBarrier, double highBarrier, double maxPayment,
                        List<Tuple2<LocalDateTime, Double>> observationDates,
                        Map<LocalDateTime, Double> fixings) {
        this.expiry = expiry;
        this.lowBarrier = lowBarrier;
        this.highBarrier = highBarrier;
        this.maxPayment = maxPayment;
        this.observationDates = observationDates;
        this.fixings = fixings;
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        double totalWeight = observationDates.stream().mapToDouble(t -> t._2).sum();
        double inRangeWeight = 0;
        Set<LocalDateTime> fixedDates = fixings.keySet();
        for (Tuple2<LocalDateTime, Double> t : observationDates) {
            LocalDateTime date = t._1;
            if (!fixedDates.contains(date)) {
                break;
            }
            double fixing = fixings.get(date);
            inRangeWeight += (fixing > lowBarrier && fixing < highBarrier) ? t._2 : 0;
        }
        return maxPayment * inRangeWeight / totalWeight;
    }

    protected List<Tuple2<Double, DigitalCash>> decompose() {
        List<Tuple2<Double, DigitalCash>> decomposed =
                new ArrayList<>((observationDates.size() - fixings.size()) * 2 + 1);
        decomposed.add(new Tuple2<>(1.0,
                new DigitalCash(lowBarrier > 0 ? 1e-9 * lowBarrier : 1e-9 * highBarrier
                        , expiry, OptionTypeEnum.CALL, immediateExercisePayoff(0))));
        double totalWeight = observationDates.stream().mapToDouble(t -> t._2).sum();
        Set<LocalDateTime> fixedDates = fixings.keySet();
        for (Tuple2<LocalDateTime, Double> t : observationDates) {
            if (fixedDates.contains(t._1)) {
                continue;
            }
            if (lowBarrier > 0.0) {
                decomposed.add(new Tuple2<>(1.0, new DigitalCash(lowBarrier, t._1, OptionTypeEnum.CALL,
                        maxPayment * t._2 / totalWeight, expiry.toLocalDate())));
                decomposed.add(new Tuple2<>(-1.0, new DigitalCash(highBarrier, t._1, OptionTypeEnum.CALL,
                        maxPayment * t._2 / totalWeight, expiry.toLocalDate())));
            } else {
                decomposed.add(new Tuple2<>(1.0, new DigitalCash(highBarrier, t._1, OptionTypeEnum.PUT,
                        maxPayment * t._2 / totalWeight, expiry.toLocalDate())));
            }
        }
        return decomposed;
    }

    @JsonIgnore
    @Override
    public LocalDate getExpirationDate() {
        return expiry.toLocalDate();
    }

    @Override
    public LocalDateTime getExpiry() {
        return expiry;
    }

    public double getLowBarrier() {
        return lowBarrier;
    }

    public double getHighBarrier() {
        return highBarrier;
    }

    public double getMaxPayment() {
        return maxPayment;
    }

    public List<Tuple2<LocalDateTime, Double>> getObservationDates() {
        return observationDates;
    }

    public Map<LocalDateTime, Double> getFixings() {
        return fixings;
    }

    @Override
    public boolean hasSufficientFixings(LocalDateTime val) {
        for (Tuple2<LocalDateTime, Double> obs : observationDates) {
            LocalDateTime t = obs._1;
            if (t.isBefore(val) && (!fixings.containsKey(t) || Objects.isNull(fixings.get(t)))) {
                return false;
            }
        }
        return true;
    }
}
