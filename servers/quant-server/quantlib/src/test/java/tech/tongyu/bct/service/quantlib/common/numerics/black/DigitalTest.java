package tech.tongyu.bct.service.quantlib.common.numerics.black;

import io.vavr.Tuple2;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;
import org.junit.Test;
import org.junit.Assert;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.enums.PayoffType;
import tech.tongyu.bct.service.quantlib.common.numerics.fd.FiniteDifference;
import static java.lang.Math.*;


public class DigitalTest {
    public final static NormalDistribution N = new NormalDistribution();

    @Test
    public void calcDigitalPrice() throws Exception {

     double digitalPrice = Digital.calc(CalcType.PRICE, 10, 9, 0.2,
             30, 0.05, 0.01, OptionType.PUT, PayoffType.CASH);
     double price = Digital.calcPrice(OptionType.PUT,10, 9, 0.2, 0.05, 0.01,
             30);

     Assert.assertEquals(price, digitalPrice, 1e-14);
    }

    @Test
    public void calcDelta() throws Exception{
        double delta = Digital.calcDelta(OptionType.CALL, 10, 15, 0.2,
                0.05, 0.01, 30);
        double digitalDelta = Digital.calc(CalcType.DELTA, 10, 15, 0.2,
                30,  0.05, 0.01, OptionType.CALL, PayoffType.CASH);
        double x = 10;
        double[] param = new double[]{x};
        FiniteDifference fd = new FiniteDifference(u -> Digital.calcPrice(OptionType.CALL, u[0], 15, 0.2,
                0.05, 0.01, 30));

        Assert.assertEquals(delta, digitalDelta, 1e-14);
        Assert.assertEquals(digitalDelta, fd.getGradient(param)[0], 1e-5);
    }

    @Test
    public void calcGamma() throws Exception{
        double gamma = Digital.calcGamma(OptionType.CALL, 10, 12, 0.2,
                0.05, 0.01, 30);
        double digitalGamma = Digital.calc(CalcType.GAMMA, 10, 12, 0.2,
                30,  0.05, 0.01, OptionType.CALL, PayoffType.CASH);
        double x = 10;
        double[] param = new double[]{x};
        Tuple2<FiniteDifference, FiniteDifference> fd = new Tuple2 (
                new FiniteDifference(u -> Digital.calcDelta(OptionType.CALL, u[0], 12, 0.2,
                0.05, 0.01, 30)),
                new FiniteDifference(u -> Digital.calcPrice(OptionType.CALL, u[0], 12, 0.2,
                0.05, 0.01, 30)));

        Assert.assertEquals(gamma, digitalGamma, 1e-14);
        Assert.assertEquals(digitalGamma, fd._1.getGradient(param)[0], 1e-5);
        Assert.assertEquals(digitalGamma, fd._2.getHessian(param)[0][0], 1e-5);
    }

    @Test
    public void calcVega() throws Exception{
        double vega = Digital.calcVega(OptionType.CALL, 10, 12, 0.2,
                0.05, 0.01, 30);
        double digitalVega = Digital.calc(CalcType.VEGA, 10, 12, 0.2,
                30,  0.05, 0.01, OptionType.CALL, PayoffType.CASH);
        double x = 0.2;
        double[] param = new double[]{x};
        FiniteDifference fd = new FiniteDifference(u -> Digital.calcPrice(OptionType.CALL, 10, 12, u[0],
                0.05, 0.01, 30));

        Assert.assertEquals(vega, digitalVega, 1e-14);
        Assert.assertEquals(vega, fd.getGradient(param)[0], 1e-5);
    }

    @Test
    public void calcTheta() throws Exception{
        double theta = Digital.calcTheta(OptionType.PUT, 10, 8, 0.2,
                0.05, 0.01, 30);
        double digitalTheta = Digital.calc(CalcType.THETA, 10, 8, 0.2,
                30,  0.05, 0.01, OptionType.PUT, PayoffType.CASH);
        double x = 30;
        double[] param = new double[]{x};
        FiniteDifference fd = new FiniteDifference(u -> Digital.calcPrice(OptionType.PUT, 10, 8, 0.2,
                0.05, 0.01, u[0]));

        Assert.assertEquals(theta, digitalTheta, 1e-14);
        Assert.assertEquals(theta, fd.getGradient(param)[0], 1e-5);
    }

    @Test
    public void calcRhoR() throws Exception{
        double rhoR = Digital.calcRhoR(OptionType.CALL, 10, 15, 0.2,
                0.05, 0.01, 30);
        double digitalRhoR = Digital.calc(CalcType.RHO_R, 10, 15, 0.2,
                30,  0.05, 0.01, OptionType.CALL, PayoffType.CASH);
        double x = 0.05;
        double[] param = new double[]{x};
        FiniteDifference fd = new FiniteDifference(u -> Digital.calcPrice(OptionType.CALL, 10, 15, 0.2,
                u[0], 0.01, 30));

        Assert.assertEquals(rhoR, digitalRhoR, 1e-14);
        Assert.assertEquals(rhoR, fd.getGradient(param)[0], 1e-4);
    }

    @Test
    public void calcRhoQ() throws Exception{
        double rhoQ = Digital.calcRhoQ(OptionType.CALL, 10, 12, 0.2,
                0.05, 0.01, 30);
        double digitalRhoQ = Digital.calc(CalcType.RHO_Q, 10, 12, 0.2,
                30,  0.05, 0.01, OptionType.CALL, PayoffType.CASH);
        double x = 0.01;
        double[] param = new double[]{x};
        FiniteDifference fd = new FiniteDifference(u -> Digital.calcPrice(OptionType.CALL, 10, 12, 0.2,
               0.05, u[0], 30));

        Assert.assertEquals(rhoQ, digitalRhoQ, 1e-14);
        Assert.assertEquals(rhoQ, fd.getGradient(param)[0], 1e-4);
    }
}