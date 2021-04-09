package tech.tongyu.bct.quant.library.priceable.common.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.HasSingleStrike;
import tech.tongyu.bct.quant.library.priceable.feature.ImmediateExercise;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class AsianFixedStrikeArithmetic implements HasExpiry, HasSingleStrike, ImmediateExercise {
    protected final LocalDateTime expiry;
    protected final OptionTypeEnum optionType;
    protected final double strike;
    protected final List<LocalDateTime> observationDates;
    protected final List<Double> weights;
    protected final List<Double> fixings;
    protected final double daysInYear;

    public AsianFixedStrikeArithmetic(LocalDateTime expiry, OptionTypeEnum optionType, double strike,
                                      List<LocalDateTime> observationDates, List<Double> weights, List<Double> fixings,
                                      double daysInYear) {
        this.expiry = expiry;
        this.optionType = optionType;
        this.strike = strike;
        this.observationDates = observationDates;
        this.weights = weights;
        this.fixings = fixings;
        this.daysInYear = daysInYear;
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        double sum = 0;
        double totalWeight = 0;
        for (int i = 0; i < fixings.size(); i++) {
            sum += fixings.get(i) * weights.get(i);
            totalWeight += weights.get(i);
        }
        return optionType == OptionTypeEnum.CALL ? Math.max(sum / totalWeight - strike, 0)
                : Math.max(strike - sum / totalWeight, 0);
    }

    public List<Double> observationDatesToExpiryTime() {
        return observationDates.stream()
                .map(d -> DateTimeUtils.days(d, expiry) / daysInYear)
                .collect(Collectors.toList());
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

    @Override
    public double getStrike() {
        return strike;
    }

    public OptionTypeEnum getOptionType() {
        return optionType;
    }

    public List<LocalDateTime> getObservationDates() {
        return observationDates;
    }

    public List<Double> getWeights() {
        return weights;
    }

    public List<Double> getFixings() {
        return fixings;
    }

    public double getDaysInYear() {
        return daysInYear;
    }
}
