package tech.tongyu.bct.quant.library.numerics.black;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.numerics.fd.FiniteDifference;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Ratio {
    public static double price(double spot1, double spot2,
                               double strike,
                               double vol1, double vol2,
                               double r,
                               double q1, double q2,
                               double tau,
                               double rho,
                               OptionTypeEnum optionType) {
        double forward = spot1 / spot2 * FastMath.exp((q2 - q1 + vol2 * vol2 - rho * vol1 * vol2) * tau);
        double vol = FastMath.sqrt(vol1 * vol1 + vol2 * vol2 - 2. * rho * vol1 * vol2);
        double dfr = FastMath.exp(-r * tau);
        return dfr * BlackScholes.calc(CalcTypeEnum.PRICE, forward, strike, vol, tau, 0., 0., optionType);
    }

    public static List<Double> delta(double spot1, double spot2,
                                     double strike,
                                     double vol1, double vol2,
                                     double r,
                                     double q1, double q2,
                                     double tau,
                                     double rho,
                                     OptionTypeEnum optionType) {
        double[] variables = new double[] {spot1, spot2};
        FiniteDifference calculator = new FiniteDifference(u -> price(u[0], u[1],
                strike, vol1, vol2, r, q1, q2, tau, rho, optionType));
        double[] gradients = calculator.getGradient(variables);
        return Arrays.stream(gradients).boxed().collect(Collectors.toList());
    }

    public static List<List<Double>> gamma(double spot1, double spot2,
                                           double strike,
                                           double vol1, double vol2,
                                           double r,
                                           double q1, double q2,
                                           double tau,
                                           double rho,
                                           OptionTypeEnum optionType) {
        double[] variables = new double[] {spot1, spot2};
        FiniteDifference calculator = new FiniteDifference(u -> price(u[0], u[1],
                strike, vol1, vol2, r, q1, q2, tau, rho, optionType));
        double[][] hessian = calculator.getHessian(variables);
        return IntStream.of(0, 1).mapToObj(i -> Arrays.stream(hessian[i]).boxed().collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public static List<Double> vega(double spot1, double spot2,
                                    double strike,
                                    double vol1, double vol2,
                                    double r,
                                    double q1, double q2,
                                    double tau,
                                    double rho,
                                    OptionTypeEnum optionType) {
        double[] variables = new double[] {vol1, vol2};
        FiniteDifference calculator = new FiniteDifference(u -> price(spot1, spot2, strike,
                u[0], u[1], r, q1, q2, tau, rho, optionType));
        double[] gradients = calculator.getGradient(variables);
        return Arrays.stream(gradients).boxed().collect(Collectors.toList());
    }

    public static double theta(double spot1, double spot2,
                               double strike,
                               double vol1, double vol2,
                               double r,
                               double q1, double q2,
                               double tau,
                               double rho,
                               OptionTypeEnum optionType) {
        double[] varialbes = new double[] { tau };
        FiniteDifference calculator = new FiniteDifference(u -> price(spot1, spot2, strike,
                vol1, vol2, r, q1, q2, u[0], rho, optionType));
        double[] gradient = calculator.getGradient(varialbes);
        return gradient[0];
    }

    public static double rhoR(double spot1, double spot2,
                              double strike,
                              double vol1, double vol2,
                              double r,
                              double q1, double q2,
                              double tau,
                              double rho,
                              OptionTypeEnum optionType) {
        double[] variables = new double[] {r};
        FiniteDifference calculator = new FiniteDifference(u -> price(spot1, spot2, strike,
                vol1, vol2, u[0], q1, q2, tau, rho, optionType));
        double[] gradient = calculator.getGradient(variables);
        return gradient[0];
    }

    public static List<List<Double>> cega(double spot1, double spot2,
                                          double strike,
                                          double vol1, double vol2,
                                          double r,
                                          double q1, double q2,
                                          double tau,
                                          double rho,
                                          OptionTypeEnum optionType) {
        double[] varialbes = new double[] { rho };
        FiniteDifference calculator = new FiniteDifference(u -> price(spot1, spot2, strike,
                vol1, vol2, r, q1, q2, tau, u[0], optionType));
        double[] gradient = calculator.getGradient(varialbes);
        return Arrays.asList(Arrays.asList(0., gradient[0]), Arrays.asList(gradient[0], 0.));
    }
}
