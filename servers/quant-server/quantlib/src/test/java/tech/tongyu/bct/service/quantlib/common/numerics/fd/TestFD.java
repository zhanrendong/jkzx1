package tech.tongyu.bct.service.quantlib.common.numerics.fd;


import org.junit.Assert;
import org.junit.Test;

import static java.lang.Math.exp;

/**
 * Created by liaosong on 16-7-26.
 */
public class TestFD {
    @Test
    public void testFDGradientdefault() {

        double x = 1;
        double y = 1.1;
        double[] param = new double[]{x, y};
        FiniteDifference fd = new FiniteDifference(u -> u[0] * exp(u[0] * u[1]));
        double firstderiv1 = exp(x * y) + x * y * exp(x * y);


        Assert.assertEquals(firstderiv1, fd.getGradient(param)[0], 1e-4);


    }

    @Test
    public void testFDHessiandefault() {
        double x = 1;
        double y = 1.1;
        double[] param = new double[]{x, y};
        FiniteDifference fd = new FiniteDifference(u -> u[0] * exp(u[0] * u[1]));
        double pfpxpy = (2 * x + x * x * y) * exp(x * y);
        double hessianxy = fd.getHessian(param)[0][1];
        Assert.assertEquals(pfpxpy, hessianxy, 1e-3);
    }

    @Test
    public void testFDHessiandeSymmetryfault() {
        double x = 1;
        double y = 1.1;
        double[] param = new double[]{x, y};
        FiniteDifference fd = new FiniteDifference(u -> u[0] * exp(u[0] * u[1]));
        double pfpxpy = (2 * x + x * x * y) * exp(x * y);
        double hessianxy = fd.getHessian(param)[0][1];
        double hessianyx = fd.getHessian(param)[1][0];
        Assert.assertEquals(hessianyx, hessianxy, 1e-10);
    }


}
