package tech.tongyu.bct.service.quantlib.common.numerics.black;

import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.enums.RebateType;

/**
 * Created by Liao Song on 16-7-22.
 * Test for barrier options
 */
public class TestBarrier {
    private double eps = 1e-15;

    @Test
    public void testKnockOutCalculation() throws Exception {

        double r = 0.08;
        double tau = 0.5;
        double q = 0.03;
        double vol = 0.25;
        double spot = 100;
        double barrier = 60;
        double strike = 100;
        double rebate = 10;
        CalcType request = CalcType.PRICE;
        OptionType type = OptionType.CALL;
        BarrierDirection direction = BarrierDirection.DOWN_AND_OUT;

        double price = KnockOut.calc(request, type, direction, RebateType.PAY_WHEN_HIT, barrier, rebate, spot, strike,
                vol, tau, r, q);

        Assert.assertEquals(8.168993213672419, price, eps);

    }

    @Test
    public void testKnockInCalculation() throws Exception {

        double r = 0.08;
        double tau = 0.5;
        double q = 0.03;
        double vol = 0.25;
        double spot = 100;
        double barrier = 60;
        double strike = 100;
        double rebate = 100;
        CalcType request = CalcType.PRICE;
        OptionType type = OptionType.CALL;
        BarrierDirection direction = BarrierDirection.DOWN_AND_IN;

        double price = KnockIn.calc(request, type, direction, barrier, rebate, spot, strike, vol, tau, r, q);

        Assert.assertEquals(95.76143597616229, price, eps);

        //System.out.println(price);
    }


}
