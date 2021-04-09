package tech.tongyu.bct.service.quantlib.common.numerics.pde;

import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.service.quantlib.api.OptionPricerBlack;
import tech.tongyu.bct.service.quantlib.api.OptionPricerPDE;
import tech.tongyu.bct.service.quantlib.common.enums.*;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.KnockOutContinuous;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.VanillaEuropean;

import java.time.LocalDateTime;
import java.time.Month;


/**
 * Created by Liao Song on 16-8-23.
 * a simple test for testing the option pricer using PDE
 */
public class TestPDEOptionPricer {
    @Test
    public void testPDEOptionPricer() throws Exception {

        //constructs the option
        OptionType type = OptionType.CALL;
        double strike = 1.0;
        LocalDateTime expiry = LocalDateTime.of(2017, Month.AUGUST, 8, 9, 0);
        LocalDateTime delivery = LocalDateTime.of(2017, Month.AUGUST, 8, 9, 0);
        VanillaEuropean option = new VanillaEuropean(type, strike, expiry, delivery);

        //underlying asset price
        double spot = 1;

        //market info
        double r = 0.2;
        double q = 0.1;
        double vol = 0.0;

        //valuation date
        LocalDateTime val = LocalDateTime.of(2016, Month.AUGUST, 8, 9, 0);

        long start = System.currentTimeMillis();
        double PDEPrice = OptionPricerPDE.calc(option, val, spot, r, q, vol, 1e-6, 0.5);
        long elapsedTime = System.currentTimeMillis() - start;

        double BlackPrice = OptionPricerBlack.calc(CalcType.PRICE, option, val, spot, vol, r, q);
        Assert.assertEquals(PDEPrice, BlackPrice, spot * 1e-4);
        //System.out.println(PDEPrice);
        //System.out.println(BlackPrice);
        //System.out.println(elapsedTime);
    }
    @Test
    public void testPDEOptionPricerKnockOut() throws Exception {

        //constructs the option
        OptionType type = OptionType.CALL;
        double strike = 1.0;
        LocalDateTime expiry = LocalDateTime.of(2017, Month.AUGUST, 8, 9, 0);
        LocalDateTime delivery = LocalDateTime.of(2017, Month.AUGUST, 8, 9, 0);
        double barrier = 0.9;
        LocalDateTime barrier_start = LocalDateTime.of(2016, Month.AUGUST, 8, 9, 0);
        LocalDateTime barrier_end = LocalDateTime.of(2017, Month.AUGUST, 8, 9, 0);
        double rebate = 0.0;
        RebateType rebateType = RebateType.PAY_AT_EXPIRY;
        BarrierDirection direction = BarrierDirection.DOWN_AND_OUT;
        ExerciseType exerciseType = ExerciseType.EUROPEAN;

        KnockOutContinuous option = new KnockOutContinuous(type,strike,expiry,delivery,barrier,direction,barrier_start,
                barrier_end,rebate,rebateType,exerciseType);
        //underlying asset price
        double spot = 1.0;

        //market info
        double r = 0.1;
        double q = 0.1;
        double vol = 0.4;

        //valuation date
        LocalDateTime val = LocalDateTime.of(2016, Month.AUGUST, 8, 9, 0);

        long start = System.currentTimeMillis();
        double PDEPrice = OptionPricerPDE.calc(option, val, spot, r, q, vol, 1e-6, 0.5);
        long elapsedTime = System.currentTimeMillis() - start;

        double BlackPrice = OptionPricerBlack.calc(CalcType.PRICE, option, val, spot, vol, r, q);
        Assert.assertEquals(PDEPrice, BlackPrice, spot * 1e-4);
        //System.out.println(PDEPrice);
        //System.out.println(BlackPrice);
        //System.out.println(elapsedTime);
    }
}
