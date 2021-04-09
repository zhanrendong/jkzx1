package tech.tongyu.bct.quant.library.market.curve;

import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.quant.builder.CurveBuilder;
import tech.tongyu.bct.quant.library.common.DoubleUtils;

import java.time.LocalDate;
import java.util.Arrays;

public class CurveBumpTest {
    private final LocalDate val = LocalDate.of(2019, 9, 6);
    private final LocalDate exp1 = LocalDate.of(2019, 12, 6);
    private final LocalDate exp2 = LocalDate.of(2020, 3, 6);

    @Test
    public void testBumpPercentage() {
        DiscountingCurve curve = CurveBuilder.createCurveFromRates(
                val.atStartOfDay(),
                Arrays.asList(exp1, exp2),
                Arrays.asList(0.05, 0.08));
        double percent = 0.01;
        DiscountingCurve bumped = curve.bumpPercent(percent);
        // between val and first
        LocalDate t = LocalDate.of(2019, 11, 6);
        double r = curve.shortRate(t.atStartOfDay());
        double bumpedR = bumped.shortRate(t.atStartOfDay());
        Assert.assertEquals((1. + percent) * r, bumpedR, DoubleUtils.SMALL_NUMBER);

        // between first and second
        t = LocalDate.of(2020, 1, 15);
        r = curve.shortRate(t.atStartOfDay());
        bumpedR = bumped.shortRate(t.atStartOfDay());
        Assert.assertEquals((1. + percent) * r, bumpedR, DoubleUtils.SMALL_NUMBER);

        // beyond last exp
        t = LocalDate.of(2020, 4, 6);
        r = curve.shortRate(t.atStartOfDay());
        bumpedR = bumped.shortRate(t.atStartOfDay());
        Assert.assertEquals((1. + percent) * r, bumpedR, DoubleUtils.SMALL_NUMBER);
    }
}
