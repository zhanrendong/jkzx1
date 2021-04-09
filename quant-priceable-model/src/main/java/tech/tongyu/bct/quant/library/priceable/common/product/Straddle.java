package tech.tongyu.bct.quant.library.priceable.common.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.ImmediateExercise;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Straddle implements HasExpiry, ImmediateExercise {
    protected final LocalDateTime expiry;
    protected final double lowStrike;
    protected final double highStrike;
    protected final double lowParticipation;
    protected final double highParticipation;

    public Straddle(LocalDateTime expiry, double lowStrike, double highStrike,
                    double lowParticipation, double highParticipation) {
        this.expiry = expiry;
        this.lowStrike = lowStrike;
        this.highStrike = highStrike;
        this.lowParticipation = lowParticipation;
        this.highParticipation = highParticipation;
    }

    @Override
    public double immediateExercisePayoff(double underlyerPrice) {
        return (underlyerPrice < lowStrike ? lowStrike - underlyerPrice : 0)
                + (underlyerPrice > highStrike ? underlyerPrice - highStrike : 0);
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
}
