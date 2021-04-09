package tech.tongyu.bct.service.quantlib.common.numerics.pde;

import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.BiFunction;

import static java.lang.Math.exp;
import static java.lang.Math.max;

/**
 * Created by Liao Song on 16-8-18.
 * This is a simple test to verify the getBoundary() method in BoundarySolver class
 */
public class TestBoundaryConditions {
    @Test
    /**
     * For a vanilla European call option, the boundary condition is defined as
     * f(s, t) = max(s*exp(-q*(T-t)) - K*exp(-r(T-t)),0),
     * so set A = 0, B = 1, C(s,t) = - max(s*exp(-q*(T-t) - k*exp(-r*(T-t), 0)
     * what we need for solving the Black-Scholes PDE is  f(0, t) = 0, and f(s_max,t) = max(s_max*exp(-q*(T-t) - k*exp(-r*(T-t), 0)
     */
    public void testEuropeanCallBoundary() throws Exception{
        double q = 0.3;
        double r = 0.2;
        double T = 1;
        int N = 10;
        double strike = 50;
        double sMax = 100;
        double ds = sMax / (double) N;
        double dt = T/ (double)N;

        double[] s = new double[N+1];
        double[] t = new double[N+1];
        for(int i = 0;i<N+1;i++){
            s[i] = i*ds;
            t[i] = i*dt;
        }
        BiFunction<Double,Double,Double> A = (u,v)->0.0;
        BiFunction<Double,Double,Double> B = (u,v)->1.0;
        BiFunction<Double,Double,Double> C = (x,y)-> - max(x*exp(-q*(T-y)) - strike * exp(-r*(T-y)), 0.0);

        //terminal boundary
        RealMatrix f = BoundarySolver.getBoundary(A,B,C,s,t);


        //upper and lower boundary
        //(s_0 , ti) and  (s_max, ti)

        int i = 7;
        int j = 3;
        System.out.println(f.getEntry(i,j));
        //System.out.print(max(sMax*exp(-q*(T-t[1])) - strike*exp(-r*(T-t[1])),0));

        Assert.assertEquals(f.getEntry(i,j),-C.apply(s[i],t[j]), 1e-5);
    }
}
