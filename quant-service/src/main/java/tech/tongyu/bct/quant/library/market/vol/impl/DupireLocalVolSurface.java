package tech.tongyu.bct.quant.library.market.vol.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.market.vol.ImpliedVolSurface;
import tech.tongyu.bct.quant.library.market.vol.LocalVolSurface;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Implementation of Dupire's formula (in European call option price version).
 * This is really an adapter of (any) implied vol surface to local vol surface by Dupire's formula.
 * The implementation is generic but relies on finite difference.
 * Hence this implementation is very inefficient and should only be used for benchmarking.
 */
public class DupireLocalVolSurface implements LocalVolSurface {
    private final ImpliedVolSurface impliedVolSurface;

    @JsonCreator
    public DupireLocalVolSurface(ImpliedVolSurface impliedVolSurface) {
        this.impliedVolSurface = impliedVolSurface;
    }

    @Override
    public double localVol(double S, double forward, LocalDateTime t) {
        // y derivatives
        double y = FastMath.log(S / forward);
        double var = this.impliedVolSurface.variance(forward, S, t);
        double deltaY = 0.01;
        double kPlus = forward * FastMath.exp(y + deltaY);
        double varPlus = this.impliedVolSurface.variance(forward, kPlus, t);
        double kMinus = forward * FastMath.exp(y - deltaY);
        double varMinus = this.impliedVolSurface.variance(forward, kMinus, t);
        double wy = (varPlus - varMinus) / deltaY * 0.5;
        double wyy = (varPlus - 2. * var + varMinus) / (deltaY * deltaY);
        // t derivative
        LocalDateTime yesterday = t.minus(1, ChronoUnit.DAYS);
        LocalDateTime tomorrow = t.plus(1, ChronoUnit.DAYS);
        double wYesterday = this.impliedVolSurface.variance(forward, S, yesterday);
        double wTomorrow = this.impliedVolSurface.variance(forward, S, tomorrow);
        double wT = (wTomorrow - wYesterday) / 2. * DateTimeUtils.DAYS_IN_YEAR;
        // dupire's formula
        double l2 = wT;
        if (DoubleUtils.smallEnough(wy, DoubleUtils.SMALL_NUMBER)) {
            l2 /= 1. + 0.5*wyy;
        } else {
            l2 /= 1. - wy*y/var + wy * wy* 0.25 * (-0.25 - 1/var + y*y/(var*var)) + 0.5*wyy;
        }
        return l2 >= 0. ? FastMath.sqrt(l2) : 0.;
    }

    @Override
    public double localVariance(double S, double forward, LocalDateTime start, LocalDateTime end) {
        double localVol = localVol(S, forward, start);
        double dt = DateTimeUtils.days(start, end) / DateTimeUtils.DAYS_IN_YEAR;
        return localVol * localVol * dt;
    }

    @JsonIgnore
    @Override
    public double getSpot() {
        return this.impliedVolSurface.getSpot();
    }

    @JsonIgnore
    @Override
    public LocalDate getValuationDate() {
        return this.impliedVolSurface.getValuationDate();
    }

    @JsonIgnore
    @Override
    public LocalDateTime getValuationDateTime() {
        return this.impliedVolSurface.getValuationDateTime();
    }

    public ImpliedVolSurface getImpliedVolSurface() {
        return impliedVolSurface;
    }
}
