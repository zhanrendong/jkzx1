package tech.tongyu.bct.service.quantlib.common.numerics.pde;

import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.ExerciseType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.numerics.black.Black;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;


/**
 * Created by Liao Song on 16-8-15.
 * temporary test for PDE
 */
public class TestBSPDE {
    @Test
    public void testPDE() throws Exception {
        OptionType optionType = OptionType.CALL;
        ExerciseType exerciseType = ExerciseType.EUROPEAN;
        LocalDateTime expiry = LocalDateTime.of(2016, Month.JANUARY, 5, 9, 0);
        LocalDateTime val = LocalDateTime.of(2015, Month.JANUARY, 5, 9, 0);
        double tau = val.until(expiry, ChronoUnit.NANOS) / Constants.NANOSINDAY / 365.25;


        double spot = 0.90;

        double strike = 1.00;
        double r = 0.1;
        double q = 0.00;
        double vol = 0.2;


        double s3;
        double s4;

        long start = System.currentTimeMillis();
        s3 = BSPDESolver.getOptionPrice(optionType, exerciseType, spot, strike, tau, r, q, vol, 1e-4);
        long elapsedTime = System.currentTimeMillis() - start;
        s4 = Black.calc(CalcType.PRICE, spot, strike, vol, tau, r, q, optionType);

        Assert.assertEquals(s3, s4, spot * 1e-5);
        //System.out.println(s3);
        //System.out.println(s4);
        //System.out.println(elapsedTime);


    }
}
