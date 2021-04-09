package tech.tongyu.bct.quant.library.numerics.pde;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.function.BiFunction;

import static tech.tongyu.bct.quant.library.numerics.pde.MatrixUtils.TDMASolver;

/**
 * Created by Liao Song on 16-8-19.
 * <p>
 * solves the initial and boundary-value partial differential equations :
 * df/dt + A df/dx + B d^2f/dx^2 +C f + D = 0
 * where A, B , C and D are functions of x and t
 * the initial and boundary-values are given by the boundary conditions:
 * Ab df/dx + Bb df + Cb = 0
 * where Ab, Bb and Cb are functions of x and t
 * <p>
 * The first and second order derivatives using non-uniform grids are(accuracy to the second order):
 * f'(i,j) = A(i) f(i-1,j) +B(i) f(i,j) + G(i)f(i+1,j)
 * where
 * A(i) = -(x[i+1]-x[i]) / (x[i] - x[i-1]) / (x[i+1]-x[i-1])
 * B(i) = ((x[i+1] -  x[i])/ (x[i] - x[i-1]) / (x[i+1]-x[i-1]) - (x[i] - x[i-1])/ (x[i+1]-x[i]) / (x[i+1]-x[i-1]))
 * G(i) = (x[i] - x[i-1]) / (x[i+1]-x[i]) / (x[i+1]-x[i-1])
 * <p>
 * f"(i,j) = D(i) f(i-1,j) +E(i) f(i,j) + F(i) f(i+1,j)
 * where
 * D(i) = 2.0 / (x[i] - x[i-1]) / (x[i+1]-x[i-1])
 * E(i) = -2.0  * ( 1.0 /  (x[i+1]-x[i]) / (x[i+1]-x[i-1]) + 1.0 /  (x[i] - x[i-1]) / (x[i+1]-x[i-1]) )
 * F(i) =  2.0/ (x[i+1]-x[i]) / (x[i+1]-x[i-1])
 */
public class PDESolver {
    /**
     * This method solves the partial differential equations df/dt + A df/dx + B d^2f/dx^2 + C f + D = 0
     * with the boundary conditions:
     * Ab df/dx + Bb f + Cb = 0 ( for x= x_0,x_N, and t =  t_M (backward evolution))
     * where A,B,C,D, Ab,Bb,Cb  are all functions of x and t.
     * It returns a RealMatrix f which gives the values of f at all the grid nodes(x_i, t_j)
     *
     * @param firstOrderDerivFunc         the coefficient function of the first order derivative term df/dx in the PDE
     * @param secondOrderDerivFunc        the coefficient function of the second order derivative term d^2f/dx^2 in the PDE
     * @param firstOrderFunc              the coefficient function of the first order term f(x,t) in the PDE
     * @param constFunc                   the coefficient function of the zero order term in the PDE
     * @param boundaryFirstOrderDerivFunc the coefficient function of the first order derivative term df/dx in the boundary condition
     * @param boundaryFirstOrderFunc      the coefficient function of the first order term f(x,t) in the boundary condition
     * @param boundaryConstFunc           the coefficient function of the zero order term in the boundary condition
     * @param exerciseFunc                the function that defines the exercise style at each node
     * @param x                           space variable arrays
     * @param t                           time variable arrays
     * @param alpha                       implicit index (1 for fully implicit, 0 for fully explicit, 0.5 for Crank-Nicolson)
     * @return the RealMatrix f is now replaced by the values at each node calculated from the Partial Differential Equation.
     * @throws Exception if error
     */
    public static RealMatrix getGridValues(BiFunction<Double, Double, Double> firstOrderDerivFunc,
                                           BiFunction<Double, Double, Double> secondOrderDerivFunc,
                                           BiFunction<Double, Double, Double> firstOrderFunc,
                                           BiFunction<Double, Double, Double> constFunc,
                                           BiFunction<Double, Double, Double> boundaryFirstOrderDerivFunc,
                                           BiFunction<Double, Double, Double> boundaryFirstOrderFunc,
                                           BiFunction<Double, Double, Double> boundaryConstFunc,
                                           BiFunction<Double, Double, Double> exerciseFunc,
                                           double[] x,
                                           double[] t,
                                           double alpha) throws Exception {
        int N = x.length - 1;//sub-index for x goes from 0 to N
        int M = t.length - 1;//sub-index for t goes from 0 to M
        double[][] a = new double[N + 1][M + 1];
        double[][] b = new double[N + 1][M + 1];
        double[][] c = new double[N + 1][M + 1];
        double[][] d = new double[N + 1][M + 1];
        RealMatrix f = new Array2DRowRealMatrix(new double[N + 1][M + 1]);

        double beta = 1.0 - alpha;

        for (int i = 0; i < N + 1; i++) {
            for (int j = 0; j < M + 1; j++) {
                a[i][j] = firstOrderDerivFunc.apply(x[i], t[j]);
                b[i][j] = secondOrderDerivFunc.apply(x[i], t[j]);
                c[i][j] = firstOrderFunc.apply(x[i], t[j]);
                d[i][j] = constFunc.apply(x[i], t[j]);
            }
        }


        // solves the terminal

        RealVector ab = new ArrayRealVector(new double[N]);
        RealVector bb = new ArrayRealVector(new double[N + 1]);
        RealVector cb = new ArrayRealVector(new double[N]);
        RealVector vb = new ArrayRealVector(new double[N + 1]);
        bb.setEntry(0, boundaryFirstOrderFunc.apply(x[0], t[M]) - boundaryFirstOrderDerivFunc.apply(x[0], t[M])
                / (x[1] - x[0]));
        cb.setEntry(0, boundaryFirstOrderDerivFunc.apply(x[0], t[M]) / (x[1] - x[0]));
        vb.setEntry(0, -boundaryConstFunc.apply(x[0], t[M]));

        for (int i = 1; i < N; i++) {
            ab.setEntry(i - 1, boundaryFirstOrderDerivFunc.apply(x[i], t[M]) * (-(x[i + 1] - x[i]) / (x[i] - x[i - 1])
                    / (x[i + 1] - x[i - 1])));
            bb.setEntry(i, boundaryFirstOrderDerivFunc.apply(x[i], t[M]) * (((x[i + 1] - x[i]) / (x[i] - x[i - 1])
                    / (x[i + 1] - x[i - 1]) - (x[i] - x[i - 1]) / (x[i + 1] - x[i]) / (x[i + 1] - x[i - 1])))
                    + boundaryFirstOrderFunc.apply(x[i], t[M]));
            cb.setEntry(i, boundaryFirstOrderDerivFunc.apply(x[i], t[M]) * ((x[i] - x[i - 1]) / (x[i + 1] - x[i])
                    / (x[i + 1] - x[i - 1])));
            vb.setEntry(i, -boundaryConstFunc.apply(x[i], t[M]));
        }
        ab.setEntry(N - 1, -boundaryFirstOrderDerivFunc.apply(x[N], t[M]) / (x[N] - x[N - 1]));
        bb.setEntry(N, boundaryFirstOrderFunc.apply(x[N], t[M]) + boundaryFirstOrderDerivFunc.apply(x[N], t[M])
                / (x[N] - x[N - 1]));
        vb.setEntry(N, -boundaryConstFunc.apply(x[N], t[M]));


        f.setColumnVector(M, TDMASolver(ab, bb, cb, vb));
        /*
         * for  i = 0:
         * (Bb(0,j) - Ab(0,j)/(x[1]-x[0]) ) *f(0,j) + Ab(0,j)/(x[1]-x[0]) * f(1,j) + Cb(0,j) = 0
         * for i = N:
         *  - Ab(N,j)/(x[N]-x[N-1]) *f(N-1,j) +  (Bb(N,j) + Ab(N,j)/(x[N]-x[N-1]) ) * f(N,j) + Cb(N,j) = 0
         * for i = 1 to N-1
         * la(i) * f(i-1,j) + lb(i) * f(i,j) + lc(i) * f(i+1,j) + ra(i)* f(i-1,j+1) + rb(i) * f(i,j+1) +rc(i)* f(i+1,j+1) + cc(i) = 0
         * where
         * la(i) =  alpha * (a(i,j) * A(i) +b(i,j)*D(i))
         * lb(i) =  alpha * (a(i,j) * B(i) + b(i,j) * E(i) + c(i,j)) - 1.0 / (t[j+1] - t[j])
         * lc(i) =  alpha * (a(i,j) * G(i) + b(i,j) * F(i))
         * and
         * ra(i) =  beta * (a(i,j+1) * A(i) +b(i,j+1)*D(i))
         * rb(i) =  beta * (a(i,j+1) * B(i) + b(i,j+1) * E(i) + c(i,j+1)) + 1.0 / (t[j+1] - t[j])
         * rc(i) =  beta * (a(i,j+1) * G(i) + b(i,j+1) * F(i))
         * and
         * cc(i) =  alpha * d(i,j) + beta * d(i,j+1)
         */

        RealVector la = new ArrayRealVector(new double[N]);
        RealVector lb = new ArrayRealVector(new double[N + 1]);
        RealVector lc = new ArrayRealVector(new double[N]);

        RealVector ra = new ArrayRealVector(new double[N]);
        RealVector rb = new ArrayRealVector(new double[N + 1]);
        RealVector rc = new ArrayRealVector(new double[N]);

        RealVector cc = new ArrayRealVector(new double[N + 1]);


        for (int j = M - 1; j >= 0; j--) {

            lb.setEntry(0, boundaryFirstOrderFunc.apply(x[0], t[j]) - boundaryFirstOrderDerivFunc.apply(x[0], t[j])
                    / (x[1] - x[0]));
            lc.setEntry(0, boundaryFirstOrderDerivFunc.apply(x[0], t[j]) / (x[1] - x[0]));

            rb.setEntry(0, boundaryFirstOrderFunc.apply(x[0], t[j + 1])
                    - boundaryFirstOrderDerivFunc.apply(x[0], t[j + 1]) / (x[1] - x[0]));
            rc.setEntry(0, boundaryFirstOrderDerivFunc.apply(x[0], t[j + 1]) / (x[1] - x[0]));

            cc.setEntry(0, boundaryConstFunc.apply(x[0], t[j]) + boundaryConstFunc.apply(x[0], t[j + 1]));

            for (int i = 1; i <= N - 1; i++) {

                la.setEntry(i - 1, alpha * (a[i][j] * (-(x[i + 1] - x[i]) / (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1])) +
                        b[i][j] * (2.0 / (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]))));
                lb.setEntry(i, alpha * (a[i][j] * ((x[i + 1] - x[i]) / (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1])
                        - (x[i] - x[i - 1]) / (x[i + 1] - x[i]) / (x[i + 1] - x[i - 1])) +
                        b[i][j] * (-2.0 * (1.0 / (x[i + 1] - x[i]) / (x[i + 1] - x[i - 1]) +
                                1.0 / (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]))) +
                        c[i][j])
                        - 1.0 / (t[j + 1] - t[j]));
                lc.setEntry(i, alpha * (a[i][j] * ((x[i] - x[i - 1]) / (x[i + 1] - x[i]) / (x[i + 1] - x[i - 1])) +
                        b[i][j] * (2.0 / (x[i + 1] - x[i]) / (x[i + 1] - x[i - 1]))));

                ra.setEntry(i - 1, beta * (a[i][j + 1] * (-(x[i + 1] - x[i]) / (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1])) +
                        b[i][j + 1] * (2.0 / (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]))));
                rb.setEntry(i, beta * (a[i][j + 1] * ((x[i + 1] - x[i]) / (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1])
                        - (x[i] - x[i - 1]) / (x[i + 1] - x[i]) / (x[i + 1] - x[i - 1])) +
                        b[i][j + 1] * (-2.0 * (1.0 / (x[i + 1] - x[i]) / (x[i + 1] - x[i - 1]) +
                                1.0 / (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]))) +
                        c[i][j + 1])
                        + 1.0 / (t[j + 1] - t[j]));
                rc.setEntry(i, beta * (a[i][j + 1] * ((x[i] - x[i - 1]) / (x[i + 1] - x[i]) / (x[i + 1] - x[i - 1])) +
                        b[i][j + 1] * (2.0 / (x[i + 1] - x[i]) / (x[i + 1] - x[i - 1]))));

                cc.setEntry(i, alpha * d[i][j] + beta * d[i][j + 1]);
            }

            rb.setEntry(N, (boundaryFirstOrderFunc.apply(x[N], t[j]) + boundaryFirstOrderDerivFunc.apply(x[N], t[j])
                    / (x[N] - x[N - 1])));
            ra.setEntry(N - 1, -boundaryFirstOrderDerivFunc.apply(x[N], t[j]) / (x[N] - x[N - 1]));

            lb.setEntry(N, (boundaryFirstOrderFunc.apply(x[N], t[j + 1])
                    + boundaryFirstOrderDerivFunc.apply(x[N], t[j + 1]) / (x[N] - x[N - 1])));
            la.setEntry(N - 1, -boundaryFirstOrderDerivFunc.apply(x[N], t[j + 1]) / (x[N] - x[N - 1]));


            cc.setEntry(N, boundaryConstFunc.apply(x[N], t[j]) + boundaryConstFunc.apply(x[N], t[j + 1]));

            RealVector v = MatrixUtils.TMSolver(ra, rb, rc, f.getColumnVector(j + 1));

            v = v.add(cc).mapMultiply(-1.0);


            RealVector g = TDMASolver(la, lb, lc, v);


            for (int i = 0; i <= N; i++) {
                g.setEntry(i, exerciseFunc.apply(g.getEntry(i), x[i]));
            }

            f.setColumnVector(j, g);
        }
        return f;
    }
}
