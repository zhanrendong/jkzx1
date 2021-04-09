package tech.tongyu.bct.quant.library.numerics.pde;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * Created by Liao Song on 16-8-18.
 *
 * provides some common tools tp solve linear algebra using matrices.
 */
public class MatrixUtils {
    /**
     * solves Ax = v  using Thomas' algorithm, where A is a tridiagonal matrix consisting of vectors a(subdiagonal) ,
     * b(diagonal) and c(supdiagonal).
     * <p>
     * Note: Thomas' algorithm is not stable in general, but is so in special cases, such as when the matrix is diagonally
     * dominant or symmetric positive definite.
     *
     * @param a subdiagonal elements of the matrix
     * @param b main diagonal elements of the matrix
     * @param c supdiagonal elements of the matrix
     * @param v the input vector
     * @return returns the solution vector
     * @throws Exception if the component arrays are not consistent in size.
     */
    public static RealVector TDMASolver(RealVector a, RealVector b, RealVector c, RealVector v) throws Exception {
        int n = v.getDimension();
        if (b.getDimension() != n || a.getDimension() != n - 1 || c.getDimension() != n - 1)
            throw new Exception("Invalid argument: incompatible sizes between the input vectors");
        RealVector ac = a.copy();
        RealVector bc = b.copy();
        RealVector cc = c.copy();
        RealVector vc = v.copy();
        RealVector x = new ArrayRealVector(new double[n]);
        for (int i = 2; i < n + 1; i++) {
            double m = ac.getEntry(i - 2) / bc.getEntry(i - 2);
            bc.setEntry(i - 1, bc.getEntry(i - 1) - m * cc.getEntry(i - 2));
            vc.setEntry(i - 1, vc.getEntry(i - 1) - m * vc.getEntry(i - 2));
        }
        x.setEntry(n - 1, vc.getEntry(n - 1) / bc.getEntry(n - 1));
        for (int i = n - 1; i > 0; i--) {
            x.setEntry(i - 1, (vc.getEntry(i - 1) - cc.getEntry(i - 1) * x.getEntry(i)) / bc.getEntry(i - 1));
        }
        return x;
    }

    /**
     * performs a simple matrix operation v = Ax and returns v, where A is a tridiagonal matrix consisting of vectors
     * a(subdiagonal) ,b(diagonal) and c(supdiagonal).
     *
     * @param a subdiagonal elements of the matrix
     * @param b main diagonal elements of the matrix
     * @param c supdiagonal elements of the matrix
     * @param x the x input vector
     * @return returns v = Ax
     * @throws Exception if the component arrays are not consistent in size.
     */

    public static RealVector TMSolver(RealVector a, RealVector b, RealVector c, RealVector x) throws Exception {
        int n = x.getDimension();
        if (b.getDimension() != n || a.getDimension() != n - 1 || c.getDimension() != n - 1)
            throw new Exception("Invalid argument: incompatible sizes between the input vectors");
        RealVector v = new ArrayRealVector(new double[n]);

        v.setEntry(0, b.getEntry(0) * x.getEntry(0) + c.getEntry(0) * x.getEntry(1));
        if (n > 2) {
            for (int i = 1; i < n - 1; i++) {
                v.setEntry(i, a.getEntry(i - 1) * x.getEntry(i - 1) + b.getEntry(i) * x.getEntry(i) + c.getEntry(i) *
                        x.getEntry(i + 1));
            }
        }
        v.setEntry(n - 1, a.getEntry(n - 2) * x.getEntry(n - 2) + b.getEntry(n - 1) * x.getEntry(n - 1));
        return v;
    }
}
