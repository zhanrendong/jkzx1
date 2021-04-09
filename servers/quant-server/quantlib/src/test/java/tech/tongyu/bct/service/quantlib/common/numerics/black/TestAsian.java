package tech.tongyu.bct.service.quantlib.common.numerics.black;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;
import tech.tongyu.bct.service.quantlib.financial.dateservice.Roll;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.AverageRateArithmetic;
import tech.tongyu.bct.service.quantlib.pricer.PricerBlack;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;


/**
 * Created by Liao Song on 16-7-20.
 * Test for discrete arithmetic average options
 * The test is a replication of the table 4-26 on page 194 from the book: The Complete Guide to Option Pricing Formulas
 */
public class TestAsian {
    final static NormalDistribution Norm = new NormalDistribution();

    @Ignore("http://10.1.2.16:8080/browse/OTMS-208")
    @Test
    public void testAsianCalculation() throws Exception {

        double eps = 1e-4;

        //construct the Asian option
        double strike = 100;
        OptionType type = OptionType.CALL;
        //current time
        LocalDateTime val = LocalDateTime.of(2015, Month.JANUARY, 5, 9, 0);
        LocalDateTime expiry = LocalDateTime.of(2015, Month.JULY, 6, 9, 0);
        LocalDateTime delivery = LocalDateTime.of(2015, Month.JULY, 6, 9, 0);

        LocalDateTime[] schedule = new LocalDateTime[27];
        schedule[0] = LocalDateTime.of(2015, Month.JANUARY, 5, 9, 0);
        schedule[1] = LocalDateTime.of(2015, Month.JANUARY, 12, 9, 0);
        schedule[2] = LocalDateTime.of(2015, Month.JANUARY, 19, 9, 0);
        schedule[3] = LocalDateTime.of(2015, Month.JANUARY, 26, 9, 0);
        schedule[4] = LocalDateTime.of(2015, Month.FEBRUARY, 2, 9, 0);
        schedule[5] = LocalDateTime.of(2015, Month.FEBRUARY, 9, 9, 0);
        schedule[6] = LocalDateTime.of(2015, Month.FEBRUARY, 16, 9, 0);
        schedule[7] = LocalDateTime.of(2015, Month.FEBRUARY, 23, 9, 0);
        schedule[8] = LocalDateTime.of(2015, Month.MARCH, 2, 9, 0);
        schedule[9] = LocalDateTime.of(2015, Month.MARCH, 9, 9, 0);
        schedule[10] = LocalDateTime.of(2015, Month.MARCH, 16, 9, 0);
        schedule[11] = LocalDateTime.of(2015, Month.MARCH, 23, 9, 0);
        schedule[12] = LocalDateTime.of(2015, Month.MARCH, 30, 9, 0);
        schedule[13] = LocalDateTime.of(2015, Month.APRIL, 6, 9, 0);
        schedule[14] = LocalDateTime.of(2015, Month.APRIL, 13, 9, 0);
        schedule[15] = LocalDateTime.of(2015, Month.APRIL, 20, 9, 0);
        schedule[16] = LocalDateTime.of(2015, Month.APRIL, 27, 9, 0);
        schedule[17] = LocalDateTime.of(2015, Month.MAY, 4, 9, 0);
        schedule[18] = LocalDateTime.of(2015, Month.MAY, 11, 9, 0);
        schedule[19] = LocalDateTime.of(2015, Month.MAY, 18, 9, 0);
        schedule[20] = LocalDateTime.of(2015, Month.MAY, 25, 9, 0);
        schedule[21] = LocalDateTime.of(2015, Month.JUNE, 1, 9, 0);
        schedule[22] = LocalDateTime.of(2015, Month.JUNE, 8, 9, 0);
        schedule[23] = LocalDateTime.of(2015, Month.JUNE, 15, 9, 0);
        schedule[24] = LocalDateTime.of(2015, Month.JUNE, 22, 9, 0);
        schedule[25] = LocalDateTime.of(2015, Month.JUNE, 29, 9, 0);
        schedule[26] = LocalDateTime.of(2015, Month.JULY, 6, 9, 0);


        // no fixing
        double[] fixings = new double[]{};

        double[] weights = new double[27];
        for (int i = 0; i < 27; i++) {
            weights[i] = 1.0;
        }

        //MARKET INFO


        double spot = 100;
        double r = 0.08;
        double b = 0.03;
        double q = r - b;
        double vol = 0.5;


        //CALCULATION TYPE
        CalcType request = CalcType.PRICE;

        double daysInYear = 364;

        double tau = val.until(expiry, ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;


        //TEST
        //set 1 year = 52 weeks
        double asianOptionPrice = Asian.calc(request, type, val, fixings, schedule, weights, daysInYear,
                spot, strike, vol, tau, r, q);

        Assert.assertEquals(8.1951, asianOptionPrice, eps);


    }

    @Ignore("http://10.1.2.16:8080/browse/OTMS-208")
    @Test
    public void testAsianNew() throws Exception {
        LocalDateTime schedule_start = LocalDateTime.of(2015, Month.JANUARY, 5, 9, 0);
        String tenor = "3M";
        String freq = "1M";
        Roll roll = Roll.FORWARD;
        double strike = 100;
        OptionType type = OptionType.CALL;
        //current time
        LocalDateTime val = LocalDateTime.of(2015, Month.JANUARY, 5, 9, 0);
        LocalDateTime expiry = LocalDateTime.of(2015, Month.JULY, 6, 9, 0);
        LocalDateTime delivery = LocalDateTime.of(2015, Month.JULY, 6, 9, 0);

        double spot = 100;
        double r = 0.08;
        double b = 0.03;
        double q = r - b;
        double vol = 0.5;

        AverageRateArithmetic asian_option = new AverageRateArithmetic(type, strike, expiry, delivery, schedule_start, tenor, freq, roll);
        LocalDateTime[] schedule = asian_option.getSchedule();
        double price = PricerBlack.calc(CalcType.PRICE, asian_option, val, spot, vol, r, q, 365.25);
        Assert.assertEquals(5.332827803360004, price, 0.001);

    }


}

