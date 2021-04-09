package tech.tongyu.bct.service.quantlib.market.vol;

import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.numerics.black.Black;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TestPwcVol {
    private static double eps = 1e-14;
    @Test
    public void TestFlatVol() {
        LocalDateTime val = LocalDateTime.of(2016, 7, 1, 0, 0);
        double vol = 0.25;
        double daysInYear = 360.;
        ImpliedVolSurface vs = AtmPwc.flat(val, 1.0, vol, daysInYear);
        double spot = 1.0;
        double strike = 1.1;
        LocalDateTime expiry = LocalDateTime.of(2017, 7, 1, 0, 0);
        double tau = val.until(expiry, ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        double put = Black.put(spot, strike, vol, tau, 0.0, 0.0);
        Assert.assertEquals(put, vs.forwardPrice(spot, strike, expiry, OptionType.PUT), eps);
    }
}