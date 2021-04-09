package tech.tongyu.bct.quant.library.market.vol.impl;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.library.market.vol.ImpliedVolSurface;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        double tau = DateTimeUtils.days(val, expiry) / DateTimeUtils.DAYS_IN_YEAR;
        return FastMath.sqrt(var / tau);
    }

    /*@Override
    public double forwardPrice(double forward, double strike, LocalDateTime expiry, OptionTypeEnum type) {
        double var = variance(forward, strike, expiry);
        if (type == OptionTypeEnum.CALL) {
            return BlackScholes.call(forward, strike, FastMath.sqrt(var), 1.0, 0.0, 0.0);
        } else {
            return BlackScholes.put(forward, strike, FastMath.sqrt(var), 1.0, 0.0, 0.0);
        }
    }*/

    @Override
    public ImpliedVolSurface bump(double amount) {
        return new RolledVolSurface(this.val, this.original.bump(amount));
    }

    @Override
    public ImpliedVolSurface bumpPercent(double percent) {
        return this.original.bumpPercent(percent);
    }

    @Override
    public LocalDateTime getValuationDateTime() {
        return val;
    }

    @Override
    public LocalDate getValuationDate() {
        return val.toLocalDate();
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
