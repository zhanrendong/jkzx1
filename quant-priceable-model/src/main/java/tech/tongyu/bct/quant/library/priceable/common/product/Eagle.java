package tech.tongyu.bct.quant.library.priceable.common.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import tech.tongyu.bct.quant.library.priceable.common.flag.ExerciseTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.ImmediateExercise;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Eagle implements HasExpiry, ImmediateExercise {
    protected final double strike1, strike2, strike3, strike4;
    protected final double payoff;
    protected final LocalDateTime expiry;

    public Eagle(double strike1, double strike2,
                 double strike3, double strike4,
                 double payoff,
                 LocalDateTime expiry) {
        this.strike1 = strike1;
        this.strike2 = strike2;
        this.strike3 = strike3;
        this.strike4 = strike4;
        this.payoff = payoff;
        this.expiry = expiry;
    }

    @JsonIgnore
    @Override
    public LocalDate getExpirationDate() {
        return this.expiry.toLocalDate();
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        if (underlyerPrice < strike1 || underlyerPrice >= strike4) {
            return 0.0;
        }
        if (underlyerPrice >= strike2 && underlyerPrice < strike3) {
            return payoff;
        }
        if (underlyerPrice <= strike1 && underlyerPrice <= strike2) {
            return payoff * (underlyerPrice - strike1) / (strike2 - strike1);
        }
        return payoff * (strike4 - underlyerPrice) / (strike4 - strike3);
    }

    protected List<Tuple2<Double, Vanilla>> decompose() {
        List<Tuple2<Double, Vanilla>> ret = new ArrayList<>();
        double p1 = payoff / (strike2 - strike1);
        double p2 = payoff / (strike4 - strike3);
        ret.add(Tuple.of(p1, new Vanilla(strike1, expiry, OptionTypeEnum.CALL, ExerciseTypeEnum.EUROPEAN)));
        ret.add(Tuple.of(-p1, new Vanilla(strike2, expiry, OptionTypeEnum.CALL, ExerciseTypeEnum.EUROPEAN)));
        ret.add(Tuple.of(-p2, new Vanilla(strike3, expiry, OptionTypeEnum.CALL, ExerciseTypeEnum.EUROPEAN)));
        ret.add(Tuple.of(p2, new Vanilla(strike4, expiry, OptionTypeEnum.CALL, ExerciseTypeEnum.EUROPEAN)));
        return ret;
    }

    public double getStrike1() {
        return strike1;
    }

    public double getStrike2() {
        return strike2;
    }

    public double getStrike3() {
        return strike3;
    }

    public double getStrike4() {
        return strike4;
    }

    public double getPayoff() {
        return payoff;
    }

    @Override
    public LocalDateTime getExpiry() {
        return expiry;
    }
}
