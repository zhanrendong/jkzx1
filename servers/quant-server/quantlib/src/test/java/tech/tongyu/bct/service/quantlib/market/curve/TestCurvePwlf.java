package tech.tongyu.bct.service.quantlib.market.curve;

import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TestCurvePwlf {
    private static double eps = 1e-14;

    @Test
    public void TestCurveConst360() {
        double r = 0.01;
        LocalDateTime t = LocalDateTime.of(2016, 7, 1, 0, 0, 0);
        LocalDateTime T = LocalDateTime.of(2017, 7, 1, 0, 0, 0);
        double tau = t.until(T, ChronoUnit.DAYS);
        Pwlf yc = Pwlf.flat(t, r, 360.0);
        Assert.assertEquals(FastMath.exp(-r * tau / 360.0), yc.df(T), eps);
    }

    @Test
    public void TestCurveConst365() {
        double r = 0.01;
        LocalDateTime t = LocalDateTime.of(2016, 7, 1, 0, 0, 0);
        LocalDateTime T = LocalDateTime.of(2017, 7, 1, 0, 0, 0);
        double tau = t.until(T, ChronoUnit.DAYS);
        Pwlf yc = Pwlf.flat(t, r, 365.0);
        Assert.assertEquals(FastMath.exp(-r * tau / 365.0), yc.df(T), eps);
    }

    @Test
    public void TestCurveFromDfs() {
        double daysInYear = 365.25;
        LocalDateTime val = LocalDateTime.of(2016, 7, 1, 0, 0, 0);
        LocalDateTime[] ts = new LocalDateTime[]{
                val,
                LocalDateTime.of(2016, 8, 1, 0, 0),
                LocalDateTime.of(2016, 9, 1, 0, 0),
                LocalDateTime.of(2016, 10, 1, 0, 0)
        };
        double[] rs = new double[]{0.01, 0.02, 0.03};
        double[] dfs = new double[4];
        dfs[0] = 1.0;
        for (int i = 1; i < dfs.length; ++i) {
            double tau = val.until(ts[i], ChronoUnit.NANOS) / Constants.NANOSINDAY;
            dfs[i] = FastMath.exp(-rs[i - 1] * tau / daysInYear);
        }
        Pwlf yc = Pwlf.pwcfFromDfs(val, ts, dfs);
        // recover all known dfs
        for (int i = 0; i < dfs.length; ++i) {
            Assert.assertEquals(dfs[i], yc.df(ts[i]), eps);
        }
        // middle
        LocalDateTime t = LocalDateTime.of(2016, 8, 15, 0, 0);
        double T1 = val.until(ts[1], ChronoUnit.NANOS) / Constants.NANOSINDAY;
        double T2 = val.until(ts[2], ChronoUnit.NANOS) / Constants.NANOSINDAY;
        double tau = ts[1].until(t, ChronoUnit.NANOS) / Constants.NANOSINDAY;
        double r = -(FastMath.log(dfs[2]) - FastMath.log(dfs[1])) / (T2 - T1);
        Assert.assertEquals(dfs[1] * FastMath.exp(-r * tau), yc.df(t), eps);
    }
}