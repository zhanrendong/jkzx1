package tech.tongyu.bct.service.quantlib.common.numerics.black;

import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.enums.RebateType;

import static java.lang.Math.*;

/**
 * Created by Liao Song on 16-7-29.
 * Test for double knock out options
 */
public class TestDoubleKnockOut {

    private static final double eps = 1e-6;


    @Test
    /**
     * test the double knock out option price
     */
    public void testDoubleKnockOutOptionPrice() throws Exception {
        double spot = 100;
        double strike = 100;
        double r = 0.1;
        double q = 0.1;
        double vol = 0.001;
        double tau = 0.9993;

        double UpperBarrier = 110;
        double LowerBarrier = 90;
        CalcType request = CalcType.PRICE;

        double UpperRebate = 10;
        double LowerRebate = 5;
        OptionType type = OptionType.CALL;
        double l = log(UpperBarrier / LowerBarrier);
        double n = sqrt((-2 * log(1e-10) / tau - pow((r - q - 0.5 * pow(vol, 2)) / vol, 2)) / pow(PI * vol / l, 2)) + 1;

        double value = DoubleKnockOut.calc(request, type, RebateType.PAY_AT_EXPIRY, UpperBarrier, LowerBarrier,
                UpperRebate, LowerRebate, spot, strike, vol, tau, r, q);
        double mu = r - q - 0.5 * pow(vol, 2);
       // double x = -2 * log(eps) / tau - pow(mu / vol, 2);
        double x = log(spot / LowerBarrier);
        //double Lambda = 0.5 * (pow(mu / vol, 2) + pow(1.0 * PI * vol / l, 2));
        double a = 1;
        double sum = exp(mu/vol/vol*(l-x));
        double LIMIT = log(Double.MAX_VALUE);
        double mu_prime = sqrt(mu * mu + 2.0 * vol * vol * r);
        double Lambda = 0.5 * (pow(mu_prime / vol, 2) + pow(10000.0 * PI * vol / l, 2));
        double steps_prime = sqrt((-2 * log(eps) / tau - pow(mu_prime / vol, 2)) / pow(PI * vol / l, 2));

        //System.out.println((mu_prime)/vol/vol);
        //System.out.println();
        //System.out.println(n);
        //System.out.println(x);
        //System.out.println((-mu/vol/vol*x));
        //System.out.println(LIMIT);
        //System.out.println(Lambda);
      Assert.assertEquals(0.03608767840219947, value, eps);
    }
}
