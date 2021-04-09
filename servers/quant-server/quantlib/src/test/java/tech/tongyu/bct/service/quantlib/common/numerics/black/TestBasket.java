package tech.tongyu.bct.service.quantlib.common.numerics.black;

import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;

/**
 * Created by Liao Song on 9/7/2016.
 * test for basket option
 */
public class TestBasket {
    @Test
    public void testBasketOption(){
        OptionType type = OptionType.CALL;
        double strike = 100;

        double[] spot = {95, 105};
        double[] weight = {0.5, 0.5};
        double[] q = {0.1,0.1};
        double[] vol={0.2, 0.3};
        double[][] rho ={{1,0},{0,1}};
        double tau = 1.0;
        double r = 0.1;


        double value = Basket.price(spot,weight,rho,strike,vol,tau,r,q,type);
        double eps = 1e-5;

        Assert.assertEquals(6.7010981658685695, value, eps);

    }
}
