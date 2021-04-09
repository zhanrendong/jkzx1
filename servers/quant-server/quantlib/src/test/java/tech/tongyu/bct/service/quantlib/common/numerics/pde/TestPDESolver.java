package tech.tongyu.bct.service.quantlib.common.numerics.pde;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.enums.RebateType;
import tech.tongyu.bct.service.quantlib.common.numerics.black.Black;
import tech.tongyu.bct.service.quantlib.common.numerics.black.KnockOut;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.function.BiFunction;

import static java.lang.Math.exp;
import static java.lang.Math.max;

/**
 * Created by Liao Song on 16-8-19.
 * several simple tests for the PDESolver class
 */
public class TestPDESolver {
    /**
     * tries to reproduce the results in table 19.4 on Page 432 in the book : Options Futures and Other Derivatives
     */
    @Test
    public void testPDESolverAmerican() throws Exception {
        double s_max = 100;
        double strike = 50;
        double tau = 0.4167;
        int N = 20;
        int M = 10;
        double r = 0.1;
        double q = 0;
        double vol = 0.40;
        double ds = s_max / (double) N;
        double dt = tau / (double) M;
        double[] s = new double[N + 1];
        double[] t = new double[M + 1];
        for (int i = 0; i <= N; i++) {
            s[i] = i * ds;
        }
        for (int i = 0; i <= M; i++) {
            t[i] = i * dt;
        }


        /**
         * functions used to define the boundary conditions
         * Ab df/ds + Bb f + Cb = 0
         */
        BiFunction<Double, Double, Double> Ab = (u, v) -> 0.0;
        BiFunction<Double, Double, Double> Bb = (u, v) -> 1.0;
        BiFunction<Double, Double, Double> Cb = (u, v) -> -max(strike - u, 0);


        /**
         * functions used to define the PDE
         * df/dt + A df/ds + B d^2f/ds^s + C f + D = 0
         * as
         * df / dt + 0.5 * vol * vol * s * s d^2f/ds^2 + (r-q)*s*df/ds - r*f =0
         *
         */
        BiFunction<Double, Double, Double> A = (u, v) -> (r - q) * u;
        BiFunction<Double, Double, Double> B = (u, v) -> 0.5 * vol * vol * u * u;
        BiFunction<Double, Double, Double> C = (u, v) -> -r;
        BiFunction<Double, Double, Double> D = (u, v) -> 0.0;
        BiFunction<Double, Double, Double> exerciseFunc = (u, v) -> max(u, max(strike - v, 0));

        RealMatrix f = PDESolver.getGridValues(A, B, C, D, Ab, Bb, Cb, exerciseFunc, s, t, 1.0);
        //System.out.println(f.getColumnVector(0));
        Assert.assertEquals(f.getEntry(9, 5), 5.77, 5e-3);


    }

    /**
     * price up and out call option (with continuous monitoring) using PDESolver and compare with the analytic results
     */
    @Test
    public void testPDESolverKnockout() throws Exception {
        //European style up and out
        double barrier = 1.0;
        double strike = 0.9;
        double tau = 2.0;
        double r = 0.1;
        double q = 0.0;
        double vol = 0.1;
        double rebate = 0;

        int N = 100;//(100*100 grids)
        int M = 100;

        //Black-Scholes Merton PDE
        BiFunction<Double, Double, Double> A = (u, v) -> (r - q) * u;
        BiFunction<Double, Double, Double> B = (u, v) -> 0.5 * vol * vol * u * u;
        BiFunction<Double, Double, Double> C = (u, v) -> -r;
        BiFunction<Double, Double, Double> D = (u, v) -> 0.0;


        //boundary conditions
        BiFunction<Double, Double, Double> Ab = (u, v) -> 0.0;
        BiFunction<Double, Double, Double> Bb = (u, v) -> 1.0;
        //up and out call
        BiFunction<Double, Double, Double> Cb = (u, v) ->
                u < barrier ? -max(u * exp(-q * (tau - v)) - strike * exp(-r * (tau - v)), 0) : -rebate;
        //BiFunction<Double, Double, Double> Cb = (u, v) -> u < barrier ? -max(u - strike , 0) : -rebate;

        //the initial values are given by the boundary conditions

        double[] s = new double[N + 1];
        double[] t = new double[M + 1];
        double ds = 1.0 / (double) N;
        double dt = tau / (double) M;
        for (int i = 0; i <= N; i++) {
            s[i] = ds * i;

        }

        for (int i = 0; i <= M; i++) {
            t[i] = dt * i;
        }
        BiFunction<Double, Double, Double> exerciseFunc = (u, v) -> u;

        RealMatrix f = PDESolver.getGridValues(A, B, C, D, Ab, Bb, Cb, exerciseFunc, s, t, 0.5);


        int j = 90;
        double anaPrice = KnockOut.calc(CalcType.PRICE, OptionType.CALL, BarrierDirection.UP_AND_OUT,
                RebateType.PAY_WHEN_HIT, barrier, rebate, s[j], strike, vol, tau, r, q);

        Assert.assertEquals(f.getEntry(j, 0), anaPrice, 1e-5);

        //System.out.println(f.getEntry(j, 0));

        //System.out.println(anaPrice);


    }

    @Test
    public void testPDESolverEuropean() throws Exception {


        OptionType optionType = OptionType.CALL;
        LocalDateTime expiry = LocalDateTime.of(2016, Month.JANUARY, 5, 9, 0);
        LocalDateTime val = LocalDateTime.of(2015, Month.JANUARY, 5, 9, 0);

        double tau = val.until(expiry, ChronoUnit.NANOS) / Constants.NANOSINDAY / 365.25;

        double spot = 1.0;

        double strike = 1.0;
        double vol = 0.20;
        double r = 0.1;
        double q = 0.0;

        double sMax = 2.0;
        int M = 10;
        double[] s = new double[M + 1];
        double ds = sMax / (double) M;
        for (int i = 0; i <= M; i++) {
            s[i] = ds * i;
        }


        int N = 10;
        double dt = tau / (double) N;

        double[] t = new double[N + 1];

        for (int i = 0; i <= N; i++) {
            t[i] = i * dt;
        }


        /**
         * functions used to define the boundary conditions
         * Ab df/ds + Bb f + Cb = 0
         */
        BiFunction<Double, Double, Double> Ab = (u, v) -> 0.0;
        BiFunction<Double, Double, Double> Bb = (u, v) -> 1.0;
        BiFunction<Double, Double, Double> Cb = (u, v) -> -max(u * exp(-q * (tau - v)) - strike * exp(-r * (tau - v)), 0);


        /**
         * functions used to define the PDE
         * df/dt + A df/ds + B d^2f/ds^s + C f + D = 0
         * as
         * df / dt + 0.5 * vol * vol * s * s d^2f/ds^2 + (r-q)*s*df/ds - r*f =0
         *
         */
        BiFunction<Double, Double, Double> Af = (u, v) -> (r - q) * u;
        BiFunction<Double, Double, Double> Bf = (u, v) -> 0.5 * vol * vol * u * u;
        BiFunction<Double, Double, Double> Cf = (u, v) -> -r;
        BiFunction<Double, Double, Double> Df = (u, v) -> 0.0;
        BiFunction<Double, Double, Double> exerciseFunc = (u, v) -> u;

        RealMatrix f = PDESolver.getGridValues(Af, Bf, Cf, Df, Ab, Bb, Cb, exerciseFunc, s, t, 0.5);

        //System.out.println(f.getEntry(5, 0));


        LinearInterpolator LI = new LinearInterpolator();

        double pdeprice = LI.interpolate(s, f.getColumnVector(0).toArray()).value(1.0);
        double bsprice = Black.calc(CalcType.PRICE, 1.0, strike, vol, tau, r, q, optionType);
        System.out.println(pdeprice);
        System.out.println(bsprice);
    }
}
