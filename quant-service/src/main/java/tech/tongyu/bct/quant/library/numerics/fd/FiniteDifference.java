package tech.tongyu.bct.quant.library.numerics.fd;

import tech.tongyu.bct.quant.library.common.DoubleUtils;

import java.util.function.Function;

import static java.lang.Math.abs;
import static org.apache.commons.math3.util.MathArrays.ebeAdd;
import static org.apache.commons.math3.util.MathArrays.ebeSubtract;

public class FiniteDifference {
    private Function<double[], Double> function;

    public FiniteDifference(Function<double[], Double> f) {
        this.function = f;
    }

    public double getValue(double[] variables) {

        return function.apply(variables);
    }

    /**
     * This method gives the gradient of the function at the given point using the user predefined intervals.
     *
     * @param variables input variables
     * @param delta     assigned step vector
     * @return return the first derivatives with respect to the variables using the assigned steps
     */
    private double[] getGradient(double[] variables, double[] delta) {
        int varLength = variables.length;
        double[][] step = stepMatrix(delta, variables);
        double[] pfpx = new double[varLength];

        for (int i = 0; i < varLength; i++) {
            double ff = function.apply(ebeAdd(variables, step[i]));
            double fb = function.apply(ebeSubtract(variables, step[i]));
            pfpx[i] = (ff - fb) / delta[i];
        }
        return pfpx;
    }

    /**
     * This method gives the Hessian matrix of the function at the given point using the user predefined intervals
     *
     * @param variables input variables
     * @param delta     assigned step vector
     * @return return the Hessian matrix  using the assigned steps
     */

    private double[][] getHessian(double[] variables, double[] delta) {
        int varLength = variables.length;

        double[][] step = stepMatrix(delta, variables);
        double[][] psquarefpxpy = new double[varLength][varLength];

        for (int i = 0; i < varLength; i++) {
            for (int j = i; j < varLength; j++) {
                if (j == i) {
                    double f1 = function.apply(ebeAdd(variables, step[i]));
                    double f2 = function.apply(variables);
                    double f3 = function.apply(ebeSubtract(variables, step[i]));
                    psquarefpxpy[i][j] = (f1 - 2 * f2 + f3) / (0.5 * delta[i] * 0.5 * delta[i]);
                } else {
                    double f1 = function.apply(ebeAdd(ebeAdd(variables, step[i]), step[j]));
                    double f2 = function.apply(ebeSubtract(ebeAdd(variables, step[i]), step[j]));
                    double f3 = function.apply(ebeAdd(ebeSubtract(variables, step[i]), step[j]));
                    double f4 = function.apply(ebeSubtract(ebeSubtract(variables, step[i]), step[j]));

                    psquarefpxpy[i][j] = (f1 - f2 - f3 + f4) / (4 * 0.5 * delta[i] * 0.5 * delta[j]);
                    psquarefpxpy[j][i] = psquarefpxpy[i][j];
                }
            }
        }
        return psquarefpxpy;
    }

    /**
     * This method gives the gradient of the function at the given point by the default intervals calculated using
     * 1/100 * x as the step
     *
     * @param variables input variables
     * @return return the gradient calculated by default accuracy
     */
    public double[] getGradient(double[] variables) {
        return getGradient(variables, defaultStepSize(variables));

    }

    /**
     * This method gives the Hessian matrix of the function at the given point by the default intervals calculated using
     * 1/100 * x as the step
     *
     * @param variables input variables
     * @return return the Hessian matrix calculated by default accuracy
     */
    public double[][] getHessian(double[] variables) {
        return getHessian(variables, defaultStepSize(variables));
    }


    private double[][] stepMatrix(double[] step, double[] variables) {
        int varLength = variables.length;
        double[][] stepmatrix = new double[varLength][varLength];
        for (int i = 0; i < varLength; i++) {
            for (int j = 0; j < varLength; j++)
                if (i == j)
                    stepmatrix[i][j] = 0.5 * step[i];
                else stepmatrix[i][j] = 0;
        }
        return stepmatrix;
    }

    private double[] defaultStepSize(double[] x) {
        int len = x.length;
        double[] dx = new double[len];
        for (int i = 0; i < len; ++i) {
            dx[i] = abs(x[i]) * 0.01;
            if (DoubleUtils.smallEnough(dx[i], DoubleUtils.SMALL_NUMBER))
                dx[i] = 0.0001;
        }
        return dx;
    }
}
