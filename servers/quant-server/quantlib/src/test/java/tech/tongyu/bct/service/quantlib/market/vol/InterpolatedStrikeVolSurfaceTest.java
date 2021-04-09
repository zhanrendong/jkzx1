package tech.tongyu.bct.service.quantlib.market.vol;

import org.apache.commons.math3.util.FastMath;
import org.junit.Before;
import org.junit.Test;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.ExtrapType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.numerics.black.Black;
import tech.tongyu.bct.service.quantlib.common.numerics.interp.Interpolator1DCubicSpline;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;
import tech.tongyu.bct.service.quantlib.market.vol.utils.VolCalendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class InterpolatedStrikeVolSurfaceTest {
    double err = 1e-6;
    double err1 = 1e-3;

    LocalDateTime val = LocalDateTime.parse("2018-04-02T00:00:00");
    VolCalendar volCalendar = new VolCalendar(1,
            new HashMap<LocalDate, Double>());
    double spot = 100;
    double constVol = 0.3;
    InterpolatedStrikeVolSurface constVolSurface;
    InterpolatedStrikeVolSurface linearVolSurface;

    double forward = 100;
    double[] spots = {50, 77, 100, 108, 150};
    LocalDateTime[] expiries = {
            LocalDateTime.parse("2018-04-09T00:00:00"),
            LocalDateTime.parse("2018-05-30T00:00:00"),
            LocalDateTime.parse("2018-08-15T00:00:00"),
            LocalDateTime.parse("2018-10-02T00:00:00")};

    @Before
    public void setUp() throws Exception {
        LocalDateTime[] expiries = {
                LocalDateTime.parse("2018-04-09T00:00:00"),
                LocalDateTime.parse("2018-05-02T00:00:00"),
                LocalDateTime.parse("2018-07-02T00:00:00"),
                LocalDateTime.parse("2018-10-02T00:00:00")};
        double[] strikes = new double[21];
        Arrays.setAll(strikes, i -> (50 + i * 5));
        // constant vol
        TreeMap<LocalDateTime, Interpolator1DCubicSpline> constVar = new TreeMap<>();
        for (LocalDateTime e: expiries) {
            double t = val.until(e, ChronoUnit.NANOS)
                    / Constants.NANOSINDAY / Constants.DAYS_IN_YEAR;
            double[] vars = new double[strikes.length];
            Arrays.setAll(vars, i -> constVol * constVol * t);
            constVar.put(e, new Interpolator1DCubicSpline(strikes, vars,
                    ExtrapType.EXTRAP_1D_FLAT, ExtrapType.EXTRAP_1D_FLAT));
        }
        constVolSurface = new InterpolatedStrikeVolSurface(val, constVar,
                volCalendar, spot, Constants.DAYS_IN_YEAR);
        // vol is parabolic in strike
        TreeMap<LocalDateTime, Interpolator1DCubicSpline> linearVar = new TreeMap<>();
        for (LocalDateTime e: expiries) {
            double t = val.until(e, ChronoUnit.NANOS)
                    / Constants.NANOSINDAY / Constants.DAYS_IN_YEAR;
            double[] vars = new double[strikes.length];
            Arrays.setAll(vars, i -> {
                double vol = FastMath.pow((strikes[i]-108) / 200, 2) + 0.2;
                return vol * vol * t;
            });
            linearVar.put(e, new Interpolator1DCubicSpline(strikes, vars,
                    ExtrapType.EXTRAP_1D_FLAT, ExtrapType.EXTRAP_1D_FLAT));
        }
        linearVolSurface = new InterpolatedStrikeVolSurface(val, linearVar,
                volCalendar, spot, Constants.DAYS_IN_YEAR);
    }

    @Test
    public void variance() {
        // constant vol
        for (LocalDateTime e: expiries) {
            double t = val.until(e, ChronoUnit.NANOS)
                    / Constants.NANOSINDAY / Constants.DAYS_IN_YEAR;
            for (double s: spots)
                assertEquals(constVol * constVol * t,
                        constVolSurface.variance(forward, s, e), err);
            // extrapolate
            assertEquals(constVol * constVol * t,
                    constVolSurface.variance(forward, 155, e), err);
        }
        // parabolic vol
        double[][] vars = {
                {0.00154792, 0.000962494, 0.000779446, 0.000767123, 0.00114272},
                {0.0128256, 0.00797495, 0.00645827, 0.00635616, 0.00946827},
                {0.0298527, 0.0185624, 0.0150322, 0.0147945, 0.0220382},
                {0.040467, 0.0251623, 0.020377, 0.0200548, 0.029874}};
        for (int i = 0; i < expiries.length; i++) {
            LocalDateTime e = expiries[i];
            for (int j = 0; j < spots.length; j++)
                assertEquals(vars[i][j], linearVolSurface.variance(
                        forward, spots[j], e), err1 * vars[i][j]);
            // extrapolate
            assertEquals(vars[i][0],linearVolSurface.variance(
                    forward, 40, e), err1 * vars[i][0]);
        }
    }

    @Test
    public void iv() {
        // constant vol
        for (LocalDateTime e: expiries) {
            for (double s: spots)
                assertEquals(constVol, constVolSurface.iv(forward, s, e),
                        err * constVol);
            // extrapolate
            assertEquals(constVol, constVolSurface.iv(forward, 155, e),
                    err * constVol);
        }
        // parabolic vol
        double[] vols = {0.2841, 0.224025, 0.2016, 0.2, 0.2441};
        for (LocalDateTime e: expiries) {
            for (int j = 0; j < spots.length; j++)
                assertEquals(vols[j], linearVolSurface.iv(forward, spots[j], e),
                        err1 * vols[j]);
            // extrapolate
            assertEquals(vols[0], linearVolSurface.iv(forward, 40, e),
                    err1 * vols[0]);
        }
    }

    @Test
    public void forwardPrice() {
        LocalDateTime expiry = LocalDateTime.parse("2018-06-30T00:00:00");
        double t = val.until(expiry, ChronoUnit.NANOS)
                / Constants.NANOSINDAY / Constants.DAYS_IN_YEAR;
        double strike = 98.7;
        OptionType type = OptionType.CALL;
        assertEquals(Black.calc(CalcType.PRICE, spot, strike, constVol, t, 0, 0, type),
                constVolSurface.forwardPrice(spot, strike, expiry, type), err1);
        double vol = linearVolSurface.iv(spot, strike, expiry);
        assertEquals(Black.calc(CalcType.PRICE, spot, strike, vol, t, 0, 0, type),
                linearVolSurface.forwardPrice(spot, strike, expiry, type), err1);

    }

    @Test
    public void roll() {
        LocalDateTime newVal = val.plusDays(15);
        ImpliedVolSurface rolled = linearVolSurface.roll(newVal);
        LocalDateTime expiry = val.plusDays(99);
        double strike = 121;
        assertEquals(linearVolSurface.variance(spot, strike, expiry),
                linearVolSurface.variance(spot, strike, newVal)
                        + rolled.variance(spot, strike, expiry), err);
    }

    @Test
    public void localVol() {
        double[] spots = Arrays.copyOf(this.spots, this.spots.length);
        spots[0] = 66;
        spots[spots.length-1] = 133;
        // constant vol
        for (LocalDateTime e: expiries) {
            for (double s: spots)
                assertEquals(constVol, constVolSurface.localVol(s, forward, e), err);
        }
        // parabolic vol
        double[][] vols = {
                {0.319372, 0.260132, 0.201421, 0.199777, 0.275417},
                {0.318638, 0.259166, 0.200131, 0.198172, 0.268571},
                {0.317545, 0.257729, 0.198229, 0.195821, 0.259142},
                {0.316873, 0.256848, 0.197071, 0.194397, 0.253747}};
        for (int i = 0; i < expiries.length; i++) {
            for (int j = 0; j < spots.length; j++)
                assertEquals(vols[i][j], linearVolSurface.localVol(
                        spots[j], forward, expiries[i]), 5 * err1 * vols[i][j]);
        }
    }

    @Test
    public void localVariance() {
        double[] spots = Arrays.copyOf(this.spots, this.spots.length);
        spots[0] = 66;
        spots[spots.length-1] = 133;
        // parabolic vol
        double[][] variance = {
                {0.000279448, 0.000185393, 0.000111152, 0.000109344, 0.000207821},
                {0.000278165, 0.000184019, 0.000109732, 0.000107595, 0.000197618},
                {0.00027626, 0.000181985, 0.000107657, 0.000105057, 0.000183986},
                {0.000275092, 0.000180742, 0.000106403, 0.000103534, 0.000176404}};
        for (int i = 0; i < expiries.length; i++) {
            LocalDateTime e = expiries[i];
            for (int j = 0; j < spots.length; j++)
                assertEquals(variance[i][j], linearVolSurface.localVariance(
                        spots[j], forward, e, e.plusDays(1)), 5 * err1 * variance[i][j]);
        }
    }

    @Test
    public void bump() {
        double amount = 0.05;
        ImpliedVolSurface bumped = linearVolSurface.bump(amount);
        for (LocalDateTime e: expiries)
            for (double s: spots) {
                double impliedVol = bumped.iv(forward, s, e);
                assertEquals(linearVolSurface.iv(forward, s, e) + amount,
                        impliedVol, err1 * impliedVol);
            }
    }
}