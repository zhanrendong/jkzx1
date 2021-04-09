package tech.tongyu.bct.service.quantlib.common.numerics.black;

import org.junit.Test;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.numerics.ad.Backwarder;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.RangeAccrual;
import tech.tongyu.bct.service.quantlib.pricer.PricerBlack;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;

/**
 *@since 2016-11-4
 */
public class testRange {
    @Test
    public void testRangeCalculation() throws Exception {
        LocalDateTime expiry = LocalDateTime.of(2017, Month.FEBRUARY, 2, 9, 0);
        double pr = 0;
        double spot = 1;
        LocalDateTime val = LocalDateTime.of(2016, Month.NOVEMBER, 4, 9, 0);
        double sMin1 = 0;
        double sMax1 = 1;
        double sMin2 = 1.05;
        double sMax2 = Double.POSITIVE_INFINITY;
        LocalDateTime ti = LocalDateTime.of(2017, Month.FEBRUARY, 2, 9, 0);
        LocalDateTime[] schedule = new LocalDateTime[]{ti};
        double[] fixings = new double[]{0};
        double vol = 0.3;
        double r = 0;
        double q = 0.5;
        double daysInYear = 365.25;

        RangeAccrual option = new RangeAccrual(pr, expiry, expiry, schedule, fixings, 1.0, 1.05, "out_range");
        double delta_i = PricerBlack.calc(CalcType.DELTA, option, val, spot, vol, r, q, daysInYear);

        double term_delta = Range.termCalc(CalcType.DELTA, expiry, pr, spot, val, sMin1, sMax1, ti, vol, r, q, daysInYear);
        double price = Range.calc(CalcType.PRICE, val, expiry, pr, sMin1, sMax1, fixings, schedule, spot, vol, r, q, daysInYear);
        double delta = Range.calc(CalcType.DELTA, val, expiry, pr, sMin1, sMax1, fixings, schedule, spot, vol, r, q, daysInYear) +
                Range.calc(CalcType.DELTA, val, expiry, pr, sMin2, sMax2, fixings, schedule, spot, vol, r, q, daysInYear);
        ;

        double tau = val.until(expiry, ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        double tau_i = val.until(ti, ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        Backwarder calculator = new Backwarder(new String[]{"spot", "vol", "tau", "r", "q"},
                new double[]{spot, vol, tau, r, q}, 2);
        //discount rate

        System.out.print(delta);

    }
}
