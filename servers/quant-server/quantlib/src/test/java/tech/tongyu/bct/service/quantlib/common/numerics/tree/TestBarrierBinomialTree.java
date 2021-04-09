package tech.tongyu.bct.service.quantlib.common.numerics.tree;

import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.enums.ExerciseType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.enums.RebateType;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.KnockInContinuous;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.KnockOutContinuous;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.VanillaAmerican;

import java.time.LocalDateTime;
import java.time.Month;

import static tech.tongyu.bct.service.quantlib.common.utils.OptionUtility.getPayoff;

/**
 * Created by Liao Song on 16-8-2.
 */
public class TestBarrierBinomialTree {
    private double eps = 1e-3;

    @Test
    public void testBarrierBT() throws Exception {
        double r = 0.08;
        double tau = 0.5;
        double q = 0.03;
        double vol = 0.25;
        double spot = 100;
        double barrier = 60;
        double strike = 100;
        double rebate = 10;
        OptionType type = OptionType.CALL;
        BarrierDirection direction = BarrierDirection.DOWN_AND_OUT;
        LocalDateTime val = LocalDateTime.of(2015, Month.JANUARY, 5, 9, 0);
        LocalDateTime expiry = LocalDateTime.of(2015, Month.JULY, 6, 9, 0);
        LocalDateTime delivery = LocalDateTime.of(2015, Month.JULY, 6, 9, 0);

        BarrierDirection direction2 = BarrierDirection.DOWN_AND_IN;

        BinomialTree tree = new BinomialTree(spot, tau, r, q, vol, 3001);


        KnockOutContinuous option1 = new KnockOutContinuous(type, strike, expiry, delivery, barrier, direction, val, expiry, rebate, RebateType.PAY_AT_EXPIRY, ExerciseType.EUROPEAN);
        KnockInContinuous option2 = new KnockInContinuous(type, strike, expiry, delivery, barrier, direction2, val, expiry, rebate, ExerciseType.EUROPEAN);

        VanillaAmerican option3 = new VanillaAmerican(strike, type, expiry);
        double price1 = tree.getOptionPrice(getPayoff(option1), BinomialTree.getBoundaryConditions(option1), BinomialTree.getExercise(option1));
        //double price3 = tree.getOptionPrice(BinomialTree.getPayoff(option3),BinomialTree.getBoundary(option3),BinomialTree.getExercise(option3));

        //double price = PricerBinomialTree.price(option1,val,spot,vol,r,q,365.25,3001);
        Assert.assertEquals(8.1690, price1, eps);
    }
}
