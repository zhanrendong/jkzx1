package tech.tongyu.bct.service.quantlib.common.numerics.interp;

import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.service.quantlib.common.enums.ExtrapType;
import tech.tongyu.bct.service.quantlib.common.utils.JsonMapper;

public class TestInterp1DCubicSpline {
    @Test
    public void testSerialization() throws Exception {
        double[] xs = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] ys = {1.0, 4.0, 9.0, 16.0, 25.0};
        Interpolator1DCubicSpline interp = new Interpolator1DCubicSpline(xs, ys,
                ExtrapType.EXTRAP_1D_LINEAR, ExtrapType.EXTRAP_1D_LINEAR);
        String json = JsonMapper.toJson(interp);
        System.out.println(json);
        Interpolator1DCubicSpline interpCopy = (Interpolator1DCubicSpline)JsonMapper.fromJson(
                "tech.tongyu.bct.service.quantlib.common.numerics.interp.Interpolator1DCubicSpline",
                json
        );
        double y = interp.value(5.5);
        double yCopy = interpCopy.value(5.5);
        Assert.assertEquals(y, yCopy, 1e-15);
    }
}
