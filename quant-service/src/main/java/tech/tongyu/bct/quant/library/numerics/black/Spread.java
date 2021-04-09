package tech.tongyu.bct.quant.library.numerics.black;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.quant.library.numerics.fd.FiniteDifference;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.math3.util.FastMath.exp;
import static org.apache.commons.math3.util.FastMath.pow;

/**
 * @since 2016-10-19.
 * calculates the spread option price and Greeks(DELTA, KAPPA, VEGA and GAMMA only) using fast and accurate approximation
 * method
 * reference: Deng, S., Li, M. and Zhou, J. (2008) Closed-form approximations for spread option prices and greeks,
 * Journal of Derivatives, 16(Spring 4), pp. 58–80.
 */
public class Spread {
    private final static NormalDistribution N = new NormalDistribution();

    private static double mu(double S, double r, double q, double vol, double T) {
        return FastMath.log(S) + (r - q - vol * vol * 0.5) * T;
    }

    private static double nu(double vol, double T) {
        return vol * FastMath.sqrt(T);

    }

    //functions of J_i
    private static double Jzero(double u, double v) {
        return N.cumulativeProbability(u / FastMath.sqrt(1 + v * v));
    }

    private static double Jone(double u, double v) {
        double nominator = 1 + (1 + u * u) * v * v;
        double denominator = pow(1 + v * v, 2.5);
        return N.density(u / FastMath.sqrt(1 + v * v)) * nominator / denominator;
    }

    private static double Jtwo(double u, double v) {
        double nominator = (6 - 6 * u * u) * v * v + (21 - 2 * u * u - pow(u, 4)) * pow(v, 4)
                + 4 * (3 + u * u) * pow(v, 6) - 3;
        double denominator = pow(1 + v * v, 5.5);
        return N.density(u / FastMath.sqrt(1 + v * v)) * u * nominator / denominator;
    }

    //functions of I_i
    private static double I(double C, double D, double eps) {
        return Jzero(C, D) + Jone(C, D) * eps + 0.5 * Jtwo(C, D) * eps * eps;
    }

    //function of phi
    private static double phi(double x, double y, double u, double v, double w) {
        double nominator = (1 + y * y) * (1 + y * y) * u - x * (y + pow(y, 3)) * v + (1 + (1 + x * x) * y * y) * w;
        double denominator = pow(1 + y * y, 2.5);
        return N.density(x / FastMath.sqrt(1 + y * y)) * nominator / denominator;
    }

    private static double vanilla(double s1, double s2, double Q1, double Q2, double k, double tau, double r,
                                  double q1, double q2, double vol1, double vol2, double rho) {
        if (k >= 0) {
            s1 = Q1 * s1;
            s2 = Q2 * s2;
            double mu1 = mu(s1, r, q1, vol1, tau);
            double mu2 = mu(s2, r, q2, vol2, tau);
            double nu1 = nu(vol1, tau);
            double nu2 = nu(vol2, tau);

            double R = exp(mu2);
            double eps = -1 / (2 * nu1 * FastMath.sqrt(1 - rho * rho)) * nu2 * nu2 * R * k / (R + k) / (R + k);

            double D3 = 1 / nu1 / FastMath.sqrt(1 - rho * rho) * (rho * nu1 - nu2 * R / (R + k));

            double C3 = 1 / nu1 / FastMath.sqrt(1 - rho * rho) * (mu1 - FastMath.log(R + k));

            double D2 = D3 + 2 * nu2 * eps;

            double C2 = C3 + D3 * nu2 + eps * nu2 * nu2;

            double D1 = D3 + 2 * rho * nu1 * eps;

            double C1 = C3 + D3 * rho * nu1 + eps * rho * rho * nu1 * nu1 + FastMath.sqrt(1 - rho * rho) * nu1;

            double I1 = I(C1, D1, eps);
            double I2 = I(C2, D2, eps);
            double I3 = I(C3, D3, eps);


            return exp(nu1 * nu1 / 2 + mu1 - r * tau) * I1
                    - exp(nu2 * nu2 / 2 + mu2 - r * tau) * I2
                    - k * exp(-r * tau) * I3;
        } else
            return vanilla(s2, s1, Q2, Q1, -k, tau, r, q2, q1, vol2, vol1, rho) + s1 * Q1 * exp(-q1 * tau)
                    - s2 * Q2 * exp(-q2 * tau) - k * exp(-r * tau);
    }

    private static double digital(double s1, double s2, double Q1, double Q2, double k, double tau, double r,
                                  double q1, double q2, double vol1, double vol2, double rho) {
        if (k >= 0) {
            s1 = Q1 * s1;
            s2 = Q2 * s2;
            double mu1 = mu(s1, r, q1, vol1, tau);
            double mu2 = mu(s2, r, q2, vol2, tau);
            double nu1 = nu(vol1, tau);
            double nu2 = nu(vol2, tau);

            double R = exp(mu2);
            double eps = -1 / (2 * nu1 * FastMath.sqrt(1 - rho * rho)) * nu2 * nu2 * R * k / (R + k) / (R + k);

            double D3 = 1 / nu1 / FastMath.sqrt(1 - rho * rho) * (rho * nu1 - nu2 * R / (R + k));

            double C3 = 1 / nu1 / FastMath.sqrt(1 - rho * rho) * (mu1 - FastMath.log(R + k));

            double I3 = I(C3, D3, eps);

            return exp(-r * tau) * I3;
        } else
            return -digital(s2, s1, Q2, Q1, -k, tau, r, q2, q1, vol2, vol1, rho) + exp(-r * tau);
    }

    public static double price(double spot1, double weight1,
                        double spot2, double weight2,
                        double strike,
                        double vol1, double vol2,
                        double r,
                        double q1, double q2,
                        double tau,
                        double rho,
                        OptionTypeEnum optionType, boolean isDigital) {
        if (optionType == OptionTypeEnum.CALL) {
            return isDigital ? digital(spot1, spot2, weight1, weight2, strike, tau, r, q1, q2, vol1, vol2, rho)
                    : vanilla(spot1, spot2, weight1, weight2, strike, tau, r, q1, q2, vol1, vol2, rho);
        } else {
            return isDigital ? digital(spot2, spot1, weight2, weight1, -strike, tau, r, q2, q1, vol2, vol1, rho)
                    : vanilla(spot2, spot1, weight2, weight1, -strike, tau, r, q2, q1, vol2, vol1, rho);
        }
    }

    public static List<Double> delta(double spot1, double weight1,
                                     double spot2, double weight2,
                                     double strike,
                                     double vol1, double vol2,
                                     double r,
                                     double q1, double q2,
                                     double tau,
                                     double rho,
                                     OptionTypeEnum optionType, boolean isDigital) {
        double[] variables = new double[] {spot1, spot2};
        FiniteDifference calculator = new FiniteDifference(u -> price(u[0], weight1, u[1], weight2,
                strike, vol1, vol2, r, q1, q2, tau, rho, optionType, isDigital));
        double[] gradients = calculator.getGradient(variables);
        return Arrays.stream(gradients).boxed().collect(Collectors.toList());
    }

    public static List<List<Double>> gamma(double spot1, double weight1,
                                           double spot2, double weight2,
                                           double strike,
                                           double vol1, double vol2,
                                           double r,
                                           double q1, double q2,
                                           double tau,
                                           double rho,
                                           OptionTypeEnum optionType, boolean isDigital) {
        double[] variables = new double[] {spot1, spot2};
        FiniteDifference calculator = new FiniteDifference(u -> price(u[0], weight1, u[1], weight2,
                strike, vol1, vol2, r, q1, q2, tau, rho, optionType, isDigital));
        double[][] hessian = calculator.getHessian(variables);
        return IntStream.of(0, 1).mapToObj(i -> Arrays.stream(hessian[i]).boxed().collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public static List<Double> vega(double spot1, double weight1,
                                    double spot2, double weight2,
                                    double strike,
                                    double vol1, double vol2,
                                    double r,
                                    double q1, double q2,
                                    double tau,
                                    double rho,
                                    OptionTypeEnum optionType, boolean isDigital) {
        double[] variables = new double[] {vol1, vol2};
        FiniteDifference calculator = new FiniteDifference(u -> price(spot1, weight1, spot2, weight2, strike,
                u[0], u[1], r, q1, q2, tau, rho, optionType, isDigital));
        double[] gradients = calculator.getGradient(variables);
        return Arrays.stream(gradients).boxed().collect(Collectors.toList());
    }

    public static double theta(double spot1, double weight1,
                               double spot2, double weight2,
                               double strike,
                               double vol1, double vol2,
                               double r,
                               double q1, double q2,
                               double tau,
                               double rho,
                               OptionTypeEnum optionType, boolean isDigital) {
        double[] varialbes = new double[] { tau };
        FiniteDifference calculator = new FiniteDifference(u -> price(spot1, weight1, spot2, weight2, strike,
                vol1, vol2, r, q1, q2, u[0], rho, optionType, isDigital));
        double[] gradient = calculator.getGradient(varialbes);
        return gradient[0];
    }

    public static double rhoR(double spot1, double weight1,
                              double spot2, double weight2,
                              double strike,
                              double vol1, double vol2,
                              double r,
                              double q1, double q2,
                              double tau,
                              double rho,
                              OptionTypeEnum optionType, boolean isDigital) {
        double[] varialbes = new double[] { r };
        FiniteDifference calculator = new FiniteDifference(u -> price(spot1, weight1, spot2, weight2, strike,
                vol1, vol2, u[0], q1, q2, tau, rho, optionType, isDigital));
        double[] gradient = calculator.getGradient(varialbes);
        return gradient[0];
    }

    public static List<List<Double>> cega(double spot1, double weight1,
                                          double spot2, double weight2,
                                          double strike,
                                          double vol1, double vol2,
                                          double r,
                                          double q1, double q2,
                                          double tau,
                                          double rho,
                                          OptionTypeEnum optionType, boolean isDigital) {
        double[] varialbes = new double[] { rho };
        FiniteDifference calculator = new FiniteDifference(u -> price(spot1, weight1, spot2, weight2, strike,
                vol1, vol2, r, q1, q2, tau, u[0], optionType, isDigital));
        double[] gradient = calculator.getGradient(varialbes);
        return Arrays.asList(Arrays.asList(0., gradient[0]), Arrays.asList(gradient[0], 0.));
    }
}
