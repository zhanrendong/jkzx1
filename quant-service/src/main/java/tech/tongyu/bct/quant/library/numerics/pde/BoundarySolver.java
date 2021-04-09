package tech.tongyu.bct.quant.library.numerics.pde;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.function.BiFunction;

/**
 * Created by Liao Song on 16-8-18.
 * solves the boundary condition: A df/dx + B f + C = 0, where A, B, and C are all known functions of x and t
 */
public class BoundarySolver {
    /**
     * @param firstOrderDerivFunc coefficient function of the first order derivative df/dx term
     * @param firstOrderFunc      coefficient function of the first order term
     * @param constFunc           coefficient function of the constant term
     * @param x                   arrays of x, from x_0 to x_max
     * @param t                   array of time, from t_0 to t_max
     * @return returns the initial values at each node for the given boundary conditions
     */
    static RealMatrix getBoundary(BiFunction<Double, Double, Double> firstOrderDerivFunc,
                                  BiFunction<Double, Double, Double> firstOrderFunc,
                                  BiFunction<Double, Double, Double> constFunc,
                                  double[] x,
                                  double[] t) throws Exception {

        int n = x.length - 1;// the sub_index  goes from 0 to n
        int m = t.length - 1;
        RealMatrix f = new Array2DRowRealMatrix(new double[n + 1][m + 1]);


        RealVector a = new ArrayRealVector(new double[n]);
        RealVector b = new ArrayRealVector(new double[n + 1]);
        RealVector c = new ArrayRealVector(new double[n]);
        RealVector v = new ArrayRealVector(new double[n + 1]);

        for (int j = 0; j < m + 1; j++) {

            b.setEntry(0, firstOrderFunc.apply(x[0], t[j]) - firstOrderDerivFunc.apply(x[0], t[j]) / (x[1] - x[0]));
            c.setEntry(0, firstOrderDerivFunc.apply(x[0], t[j]) / (x[1] - x[0]));
            v.setEntry(0, -constFunc.apply(x[0], t[j]));

             /* f'(i,j) = A(i) f(i-1,j) +B(i) f(i,j) + G(i)f(i+1,j)
              where
              A(i) = -(x[i+1]-x[i]) / (x[i] - x[i-1]) / (x[i+1]-x[i-1])
              B(i) = ((x[i+1] -  x[i])/ (x[i] - x[i-1]) / (x[i+1]-x[i-1]) - (x[i] - x[i-1])/ (x[i+1]-x[i])
                     / (x[i+1]-x[i-1]))
              G(i) = (x[i] - x[i-1]) / (x[i+1]-x[i]) / (x[i+1]-x[i-1])
             */

            for (int i = 1; i < n; i++) {
                a.setEntry(i - 1, firstOrderDerivFunc.apply(x[i], t[j]) * (-(x[i + 1] - x[i])
                        / (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1])));
                b.setEntry(i, firstOrderDerivFunc.apply(x[i], t[j]) * (((x[i + 1] - x[i])
                        / (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1])
                        - (x[i] - x[i - 1]) / (x[i + 1] - x[i])
                        / (x[i + 1] - x[i - 1])))
                        + firstOrderFunc.apply(x[i], t[j]));
                c.setEntry(i, firstOrderDerivFunc.apply(x[i], t[j]) * ((x[i] - x[i - 1])
                        / (x[i + 1] - x[i]) / (x[i + 1] - x[i - 1])));
                v.setEntry(i, -constFunc.apply(x[i], t[j]));
            }
            a.setEntry(n - 1, -firstOrderDerivFunc.apply(x[n], t[j]) / (x[n] - x[n - 1]));
            b.setEntry(n, firstOrderFunc.apply(x[n], t[j]) + firstOrderDerivFunc.apply(x[n], t[j]) / (x[n] - x[n - 1]));
            v.setEntry(n, -constFunc.apply(x[n], t[j]));
            f.setColumnVector(j, MatrixUtils.TDMASolver(a, b, c, v));

        }
        return f;
    }
}
