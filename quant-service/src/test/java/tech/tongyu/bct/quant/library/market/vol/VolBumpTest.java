package tech.tongyu.bct.quant.library.market.vol;

import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.quant.builder.VolSurfaceBuilder;
import tech.tongyu.bct.quant.library.common.DoubleUtils;

import java.time.LocalDate;
import java.util.Arrays;

public class VolBumpTest {
    private LocalDate val = LocalDate.of(2019, 9, 6);
    private LocalDate exp1 = LocalDate.of(2019, 10, 6);
    private LocalDate exp2 = LocalDate.of(2019, 11, 6);

    @Test
    public void testAtmPwcVolBumpPercent() {
        ImpliedVolSurface orig = VolSurfaceBuilder.createAtmPwcVolSurface(val.atStartOfDay(),
                100., Arrays.asList(exp1, exp2), Arrays.asList(0.2, 0.25));
        double percent = 0.1;
        ImpliedVolSurface bumped = orig.bumpPercent(percent);

        // between val and first
        LocalDate exp = LocalDate.of(2019, 9, 15);
        double vol = orig.iv(100., 100., exp);
        double bumpedVol = bumped.iv(100., 100., exp);
        Assert.assertEquals(vol * (1. + percent), bumpedVol, DoubleUtils.SMALL_NUMBER);

        // between first and second
        exp = LocalDate.of(2019, 11, 1);
        vol = orig.iv(100., 100., exp);
        bumpedVol = bumped.iv(100., 100., exp);
        Assert.assertEquals(vol * (1. + percent), bumpedVol, DoubleUtils.SMALL_NUMBER);

        // after the last expiry
        exp = LocalDate.of(2019, 12, 6);
        vol = orig.iv(100., 100., exp);
        bumpedVol = bumped.iv(100., 100., exp);
        Assert.assertEquals(vol * (1. + percent), bumpedVol, DoubleUtils.SMALL_NUMBER);
    }

    @Test
    public void testInterpStrikeVolBumpPercent() {
        ImpliedVolSurface orig = VolSurfaceBuilder.createInterpolatedStrikeSurface(
                val.atStartOfDay(), 100.0, Arrays.asList(exp1, exp2), Arrays.asList(95., 100., 105.),
                Arrays.asList(
                        Arrays.asList(0.28, 0.2, 0.25),
                        Arrays.asList(0.25, 0.21, 0.22)
                ),
                new VolSurfaceBuilder.InterpolatedVolSurfaceConfig()
        );

        double percent = 0.1;
        ImpliedVolSurface bumped = orig.bumpPercent(percent);

        // between val and first
        LocalDate exp = LocalDate.of(2019, 9, 15);
        double vol = orig.iv(100., 100., exp);
        double bumpedVol = bumped.iv(100., 100., exp);
        Assert.assertEquals(vol * (1. + percent), bumpedVol, DoubleUtils.SMALL_NUMBER);

        // between first and second
        exp = LocalDate.of(2019, 11, 1);
        vol = orig.iv(100., 100., exp);
        bumpedVol = bumped.iv(100., 100., exp);
        Assert.assertEquals(vol * (1. + percent), bumpedVol, DoubleUtils.SMALL_NUMBER);

        // after the last expiry
        exp = LocalDate.of(2019, 12, 6);
        vol = orig.iv(100., 100., exp);
        bumpedVol = bumped.iv(100., 100., exp);
        Assert.assertEquals(vol * (1. + percent), bumpedVol, DoubleUtils.SMALL_NUMBER);
    }
}
