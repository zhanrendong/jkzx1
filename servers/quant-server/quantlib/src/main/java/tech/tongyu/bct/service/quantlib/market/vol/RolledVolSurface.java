package tech.tongyu.bct.service.quantlib.market.vol;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.numerics.black.Black;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class RolledVolSurface implements ImpliedVolSurface {
    private final ImpliedVolSurface original;
    private final LocalDateTime val;

    public RolledVolSurface(LocalDateTime newVal, ImpliedVolSurface original) {
        this.val = newVal;
        this.original = original;
    }

    @Override
    public double variance(double forward, double strike, LocalDateTime expiry) {
        double varToNewVal = original.variance(forward, strike, val);
        double varToExpiry = original.variance(forward, strike, expiry);
        double var = varToExpiry - varToNewVal;
        return var > 0.0 ? var : 0.0;
    }

    @Override
    public double iv(double forward, double strike, LocalDateTime expiry) {
        double var = variance(forward, strike, expiry);
        double tau = val.until(expiry, ChronoUnit.NANOS) / Constants.NANOSINDAY / Constants.DAYS_IN_YEAR;
        return FastMath.sqrt(var / tau);
    }

    @Override
    public double forwardPrice(double forward, double strike, LocalDateTime expiry, OptionType type) {
        double var = variance(forward, strike, expiry);
        if (type == OptionType.CALL) {
            return Black.call(forward, strike, FastMath.sqrt(var), 1.0, 0.0, 0.0);
        } else {
            return Black.put(forward, strike, FastMath.sqrt(var), 1.0, 0.0, 0.0);
        }
    }

    @Override
    public ImpliedVolSurface bump(double amount) {
        throw new UnsupportedOperationException("RolledVolSurface does not support bump().");
    }

    @Override
    public LocalDateTime getVal() {
        return val;
    }

    @Override
    public double getSpot() {
        return original.getSpot();
    }

    @Override
    public ImpliedVolSurface roll(LocalDateTime newVal) {
        return new RolledVolSurface(newVal, original);
    }
}
