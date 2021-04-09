package tech.tongyu.bct.service.quantlib.common.utils;

/**
 * @since 2016-11-9
 */
public class FittingUtility {
    /**
     * get the interception b by linearly fitting the weighted data points (x,y) using least square method y=ax+b
     *
     * @param w the weights
     * @param x x values
     * @param y y values
     * @param a the slop
     * @return the interception
     */
    public static double getIntercept(double[] w, double[] x, double[] y, double a) throws Exception {
        int N = w.length;
        double weightSum = 0;
        double denominator = 0;
        for (int i = 0; i < N; i++) {
            weightSum += w[i];
            denominator += w[i] * y[i] - a * w[i] * x[i];
        }
        return denominator / weightSum;
    }

    public static double[] getLinearFitParams(double[] w, double[] x, double[] y) {
        double xWeightedAverage = 0;
        double yWeightedAverage = 0;
        double xyWeightedAverage = 0;
        double weightedSum = 0;
        double xxWeightedAverage = 0;
        int N = w.length;
        for (double aW : w) {
            weightedSum += aW;
        }
        for (int i = 0; i < N; i++) {
            xWeightedAverage += w[i] * x[i] / weightedSum;
            yWeightedAverage += w[i] * y[i] / weightedSum;
            xyWeightedAverage += w[i] * x[i] * y[i] / weightedSum;
            xxWeightedAverage += w[i] * x[i] * x[i] / weightedSum;

        }
        double a = (xyWeightedAverage - xWeightedAverage * yWeightedAverage)
                / (xxWeightedAverage - xWeightedAverage * xWeightedAverage);
        double b = (yWeightedAverage - xWeightedAverage * a);

        return new double[]{a, b};
    }
}
