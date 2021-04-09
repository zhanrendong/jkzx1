package tech.tongyu.bct.service.quantlib.common.numerics.black;

import org.apache.commons.math3.distribution.NormalDistribution;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;

import static java.lang.Math.*;

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
        return log(S) + (r - q - vol * vol * 0.5) * T;
    }

    private static double nu(double vol, double T) {
        return vol * sqrt(T);

    }

    //functions of J_i
    private static double Jzero(double u, double v) {
        return N.cumulativeProbability(u / sqrt(1 + v * v));
    }

    private static double Jone(double u, double v) {
        double nominator = 1 + (1 + u * u) * v * v;
        double denominator = pow(1 + v * v, 2.5);
        return N.density(u / sqrt(1 + v * v)) * nominator / denominator;
    }

    private static double Jtwo(double u, double v) {
        double nominator = (6 - 6 * u * u) * v * v + (21 - 2 * u * u - pow(u, 4)) * pow(v, 4)
                + 4 * (3 + u * u) * pow(v, 6) - 3;
        double denominator = pow(1 + v * v, 5.5);
        return N.density(u / sqrt(1 + v * v)) * u * nominator / denominator;
    }

    //functions of I_i
    private static double I(double C, double D, double eps) {
        return Jzero(C, D) + Jone(C, D) * eps + 0.5 * Jtwo(C, D) * eps * eps;
    }

    //function of phi
    private static double phi(double x, double y, double u, double v, double w) {
        double nominator = (1 + y * y) * (1 + y * y) * u - x * (y + pow(y, 3)) * v + (1 + (1 + x * x) * y * y) * w;
        double denominator = pow(1 + y * y, 2.5);
        return N.density(x / sqrt(1 + y * y)) * nominator / denominator;
    }

    @BctQuantApi(name = "qlSpreadOptionPriceCalc",
            description = "Calculate the price of the spread option",
            argDescriptions = {"spot of underlying asset 1", "spot of underlying asset 2", "Quantity of asset 1",
                    "Quantity of asset 2", "strike price", "time to maturity", "risk free interest rate",
                    "annually dividend yield rate of asset 1", "annually dividend yield rate of asset 2",
                    "volatility of asset 1", "volatility of asset 2", "correlation coefficient of asset 1 and 2"},
            argNames = {"s1", "s2", "Q1", "Q2", "k", "tau", "r", "q1", "q2", "vol1", "vol2", "rho"},
            argTypes = {"Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double",
                    "Double", "Double", "Double"},
            retName = "price", retDescription = "price", retType = "Double")

    public static double price(double s1, double s2, double Q1, double Q2, double k, double tau, double r,
                               double q1, double q2, double vol1, double vol2, double rho) {
        if (k >= 0) {
            s1 = Q1 * s1;
            s2 = Q2 * s2;
            double mu1 = mu(s1, r, q1, vol1, tau);
            double mu2 = mu(s2, r, q2, vol2, tau);
            double nu1 = nu(vol1, tau);
            double nu2 = nu(vol2, tau);

            double R = exp(mu2);
            double eps = -1 / (2 * nu1 * sqrt(1 - rho * rho)) * nu2 * nu2 * R * k / (R + k) / (R + k);

            double D3 = 1 / nu1 / sqrt(1 - rho * rho) * (rho * nu1 - nu2 * R / (R + k));

            double C3 = 1 / nu1 / sqrt(1 - rho * rho) * (mu1 - log(R + k));

            double D2 = D3 + 2 * nu2 * eps;

            double C2 = C3 + D3 * nu2 + eps * nu2 * nu2;

            double D1 = D3 + 2 * rho * nu1 * eps;

            double C1 = C3 + D3 * rho * nu1 + eps * rho * rho * nu1 * nu1 + sqrt(1 - rho * rho) * nu1;

            double I1 = I(C1, D1, eps);
            double I2 = I(C2, D2, eps);
            double I3 = I(C3, D3, eps);


            return exp(nu1 * nu1 / 2 + mu1 - r * tau) * I1
                    - exp(nu2 * nu2 / 2 + mu2 - r * tau) * I2
                    - k * exp(-r * tau) * I3;
        } else
            return price(s2, s1, Q2, Q1, -k, tau, r, q2, q1, vol2, vol1, rho) + s1 * Q1 * exp(-q1 * tau)
                    - s2 * Q2 * exp(-q2 * tau) - k * exp(-r * tau);


    }


    /**
     * calculates the delta of the spread option, the output is an array {delta_1, delta_2},
     * where delta_1 = dp/ds_1, delta_2 = dp/ds_2
     *
     * @param s1   first underlying asset spot price
     * @param s2   second underlying asset spot price
     * @param k    the strike price
     * @param tau  time to maturity in years
     * @param r    annually risk free interest rate
     * @param q1   the annually dividend yield of the first underlying asset
     * @param q2   the annually dividend yield of the second underlying asset
     * @param vol1 the volatility of the first underlying asset
     * @param vol2 the volatility of the second underlying asset
     * @param rho  the correlation factor of the two underlying assets
     * @return the delta array
     */
    @BctQuantApi(name = "qlSpreadOptionDeltaCalc",
            description = "Calculate the price of the spread option",
            argDescriptions = {"spot of underlying asset 1", "spot of underlying asset 2", "Quantity of asset 1",
                    "Quantity of asset 2", "strike price", "time to maturity", "risk free interest rate",
                    "annually dividend yield rate of asset 1", "annually dividend yield rate of asset 2",
                    "volatility of asset 1", "volatility of asset 2", "correlation coefficient of asset 1 and 2"},
            argNames = {"s1", "s2","Q1","Q2", "k", "tau", "r", "q1", "q2", "vol1", "vol2", "rho"},
            argTypes = {"Double", "Double","Double","Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double",
                    "Double"},
            retName = "delta", retDescription = "delta", retType = "ArrayDouble")
    public static double[] delta(double s1, double s2, double Q1, double Q2,double k, double tau, double r,
                                 double q1, double q2, double vol1, double vol2, double rho) {
        double[] result = new double[2];

        double h1 = s1 / 100.0;
        double h2 = s2 / 100;
        double price1 = price(s1 - 0.5 * h1, s2, Q1,Q2,k, tau, r, q1, q2, vol1, vol2, rho);
        double price2 = price(s1 + 0.5 * h1, s2,Q1,Q2, k, tau, r, q1, q2, vol1, vol2, rho);
        result[0] = (price2 - price1) / h1;
        double price3 = price(s1, s2 - 0.5 * h2, Q1,Q2,k, tau, r, q1, q2, vol1, vol2, rho);
        double price4 = price(s1, s2 + 0.5 * h2,Q1,Q2, k, tau, r, q1, q2, vol1, vol2, rho);
        result[1] = (price4 - price3) / h2;
        return result;
    }

    /**
     * calculates the dual_delta of the spread option: k = dp/dk
     *
     * @param s1   first underlying asset spot price
     * @param s2   second underlying asset spot price
     * @param k    the strike price
     * @param tau  time to maturity in years
     * @param r    annually risk free interest rate
     * @param q1   the annually dividend yield of the first underlying asset
     * @param q2   the annually dividend yield of the second underlying asset
     * @param vol1 the volatility of the first underlying asset
     * @param vol2 the volatility of the second underlying asset
     * @param rho  the correlation factor of the two underlying assets
     * @return the dual_delta
     */
    @BctQuantApi(name = "qlSpreadOptionDualDeltaCalc",
            description = "Calculate the price of the spread option",
            argDescriptions = {"spot of underlying asset 1", "spot of underlying asset 2", "Quantity of asset 1",
                    "Quantity of asset 2", "strike price", "time to maturity", "risk free interest rate",
                    "annually dividend yield rate of asset 1", "annually dividend yield rate of asset 2",
                    "volatility of asset 1", "volatility of asset 2", "correlation coefficient of asset 1 and 2"},
            argNames = {"s1", "s2", "Q1", "Q2", "k", "tau", "r", "q1", "q2", "vol1", "vol2", "rho"},
            argTypes = {"Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double",
                    "Double", "Double", "Double"},
            retName = "kappa", retDescription = "kappa", retType = "Double")
    public static double dual_delta(double s1, double s2, double Q1, double Q2,double k, double tau, double r,
                                    double q1, double q2, double vol1, double vol2, double rho) {
        double h;
        if (k != 0)
            h = k / 100.0;
        else
            h = 0.01;

        double k1 = k + 0.5 * h;
        double k2 = k - 0.5 * h;
        double price1 = price(s1, s2, Q1,Q2, k1, tau, r, q1, q2, vol1, vol2, rho);
        double price2 = price(s1, s2, Q1,Q2, k2, tau, r, q1, q2, vol1, vol2, rho);
        return (price1 - price2) / h;
    }

    /**
     * calculates the Jacobian matrix of the spread option's gamma.
     * The Jacobian matrix is defined as A = (a_ij=dp^2/(ds_ids_j)), i,j = 1,2
     *
     * @param s1   first underlying asset spot price
     * @param s2   second underlying asset spot price
     * @param k    the strike price
     * @param tau  time to maturity in years
     * @param r    annually risk free interest rate
     * @param q1   the annually dividend yield of the first underlying asset
     * @param q2   the annually dividend yield of the second underlying asset
     * @param vol1 the volatility of the first underlying asset
     * @param vol2 the volatility of the second underlying asset
     * @param rho  the correlation factor of the two underlying assets
     * @return the gamma matrix
     */
    @BctQuantApi(name = "qlSpreadOptionGammaCalc",
            description = "Calculate the price of the spread option",
            argDescriptions = {"spot of underlying asset 1", "spot of underlying asset 2", "Quantity of asset 1",
                    "Quantity of asset 2", "strike price", "time to maturity", "risk free interest rate",
                    "annually dividend yield rate of asset 1", "annually dividend yield rate of asset 2",
                    "volatility of asset 1", "volatility of asset 2", "correlation coefficient of asset 1 and 2"},
            argNames = {"s1", "s2", "Q1", "Q2", "k", "tau", "r", "q1", "q2", "vol1", "vol2", "rho"},
            argTypes = {"Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double",
                    "Double", "Double", "Double"},
            retName = "gamma", retDescription = "gamma", retType = "Matrix")
    public static double[][] gamma(double s1, double s2, double Q1, double Q2, double k, double tau, double r,
                                   double q1, double q2, double vol1, double vol2, double rho) {
        double[][] result = new double[2][2];
        double h1 = s1 / 200.0;
        double h2 = s2 / 200.0;
        double price = price(s1, s2, Q1,Q2,k, tau, r, q1, q2, vol1, vol2, rho);
        //gamma11
        double price1 = price(s1 - h1, s2, Q1,Q2, k, tau, r, q1, q2, vol1, vol2, rho);
        double price2 = price(s1 + h1, s2,Q1,Q2, k, tau, r, q1, q2, vol1, vol2, rho);
        result[0][0] = (price1 + price2 - 2 * price) / h1 / h1;
        //gamma22
        double price3 = price(s1, s2 - h2,Q1,Q2, k, tau, r, q1, q2, vol1, vol2, rho);
        double price4 = price(s1, s2 + h2,Q1,Q2, k, tau, r, q1, q2, vol1, vol2, rho);
        result[1][1] = (price3 + price4 - 2 * price) / h2 / h2;
        //gamma12 & gamma21
        double price_a = price(s1 + h1, s2 + h2,Q1,Q2, k, tau, r, q1, q2, vol1, vol2, rho);
        double price_b = price(s1 + h1, s2 - h2, Q1,Q2,k, tau, r, q1, q2, vol1, vol2, rho);
        double price_c = price(s1 - h1, s2 + h2,Q1,Q2, k, tau, r, q1, q2, vol1, vol2, rho);
        double price_d = price(s1 - h1, s2 - h2,Q1,Q2, k, tau, r, q1, q2, vol1, vol2, rho);
        result[1][0] = (price_a - price_b - price_c + price_d) / 4.0 / h1 / h2;
        result[0][1] = (price_a - price_b - price_c + price_d) / 4.0 / h1 / h2;

        return result;

    }

    /**
     * calculates the vega of the spread option. The output is an array {vega_1, vega_2}
     * where vega_i = d p/d vol_i , i = 1, 2
     *
     * @param s1   first underlying asset spot price
     * @param s2   second underlying asset spot price
     * @param k    the strike price
     * @param tau  time to maturity in years
     * @param r    annually risk free interest rate
     * @param q1   the annually dividend yield of the first underlying asset
     * @param q2   the annually dividend yield of the second underlying asset
     * @param vol1 the volatility of the first underlying asset
     * @param vol2 the volatility of the second underlying asset
     * @param rho  the correlation factor of the two underlying assets
     * @return the vega array
     */

    @BctQuantApi(name = "qlSpreadOptionVegaCalc",
            description = "Calculate the price of the spread option",
            argDescriptions = {"spot of underlying asset 1", "spot of underlying asset 2", "Quantity of asset 1",
                    "Quantity of asset 2", "strike price", "time to maturity", "risk free interest rate",
                    "annually dividend yield rate of asset 1", "annually dividend yield rate of asset 2",
                    "volatility of asset 1", "volatility of asset 2", "correlation coefficient of asset 1 and 2"},
            argNames = {"s1", "s2", "Q1", "Q2", "k", "tau", "r", "q1", "q2", "vol1", "vol2", "rho"},
            argTypes = {"Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double",
                    "Double", "Double", "Double"},
            retName = "vega", retDescription = "vega", retType = "ArrayDouble")
    public static double[] vega(double s1, double s2, double Q1, double Q2,double k, double tau, double r,
                                double q1, double q2, double vol1, double vol2, double rho) {
        double[] result = new double[2];
        double h1 = vol1 / 100.0;
        double h2 = vol2 / 100.0;
        double price1 = price(s1, s2,Q1,Q2, k, tau, r, q1, q2, vol1 - 0.5 * h1, vol2, rho);
        double price2 = price(s1, s2, Q1,Q2,k, tau, r, q1, q2, vol1 + 0.5 * h1, vol2, rho);
        result[0] = (price2 - price1) / h1;
        double price3 = price(s1, s2,Q1,Q2, k, tau, r, q1, q2, vol1, vol2 - 0.5 * h2, rho);
        double price4 = price(s1, s2,Q1,Q2, k, tau, r, q1, q2, vol1, vol2 + 0.5 * h2, rho);
        result[1] = (price4 - price3) / h2;
        return result;

    }

    @BctQuantApi(name = "qlSpreadOptionThetaCalc",
            description = "Calculate the theta of the spread option",
            argDescriptions = {"spot of underlying asset 1", "spot of underlying asset 2", "Quantity of asset 1",
                    "Quantity of asset 2", "strike price", "time to maturity", "risk free interest rate",
                    "annually dividend yield rate of asset 1", "annually dividend yield rate of asset 2",
                    "volatility of asset 1", "volatility of asset 2", "correlation coefficient of asset 1 and 2"},
            argNames = {"s1", "s2", "Q1", "Q2", "k", "tau", "r", "q1", "q2", "vol1", "vol2", "rho"},
            argTypes = {"Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double",
                    "Double", "Double", "Double"},
            retName = "theta", retDescription = "theta", retType = "Double")
    public static double theta(double s1, double s2, double Q1,double Q2,double k, double tau, double r,
                               double q1, double q2, double vol1, double vol2, double rho) {
        double step = tau / 100.0;
        double tau1 = tau + step * 0.5;
        double tau2 = tau - step * 0.5;
        double price1 = price(s1, s2, Q1,Q2,k, tau1, r, q1, q2, vol1, vol2, rho);
        double price2 = price(s1, s2,Q1,Q2, k, tau2, r, q1, q2, vol1, vol2, rho);
        return (price1 - price2) / step;
    }

    @BctQuantApi(name = "qlSpreadOptionRhoRCalc",
            description = "Calculate the rho r of the spread option",
            argDescriptions = {"spot of underlying asset 1", "spot of underlying asset 2", "Quantity of asset 1",
                    "Quantity of asset 2", "strike price", "time to maturity", "risk free interest rate",
                    "annually dividend yield rate of asset 1", "annually dividend yield rate of asset 2",
                    "volatility of asset 1", "volatility of asset 2", "correlation coefficient of asset 1 and 2"},
            argNames = {"s1", "s2", "Q1", "Q2", "k", "tau", "r", "q1", "q2", "vol1", "vol2", "rho"},
            argTypes = {"Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double",
                    "Double", "Double", "Double"},
            retName = "theta", retDescription = "theta", retType = "Double")
    public static double rhor(double s1, double s2, double Q1,double Q2,double k, double tau, double r,
                              double q1, double q2, double vol1, double vol2, double rho) {
        double step = r / 100.0;
        if (step == 0)
            step = 0.01;
        double r1 = r + step * 0.5;
        double r2 = r - step * 0.5;
        double price1 = price(s1, s2, Q1,Q2,k, tau, r1, q1, q2, vol1, vol2, rho);
        double price2 = price(s1, s2, Q1,Q2,k, tau, r2, q1, q2, vol1, vol2, rho);
        return (price1 - price2) / step;
    }

    @BctQuantApi(name = "qlSpreadOptionCorrelationDeltaCalc",
            description = "Calculate the correlation delta of the spread option",
            argDescriptions = {"spot of underlying asset 1", "spot of underlying asset 2", "Quantity of asset 1",
                    "Quantity of asset 2", "strike price", "time to maturity", "risk free interest rate",
                    "annually dividend yield rate of asset 1", "annually dividend yield rate of asset 2",
                    "volatility of asset 1", "volatility of asset 2", "correlation coefficient of asset 1 and 2"},
            argNames = {"s1", "s2", "Q1", "Q2", "k", "tau", "r", "q1", "q2", "vol1", "vol2", "rho"},
            argTypes = {"Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double",
                    "Double", "Double", "Double"},
            retName = "correlationDelta", retDescription = "correlation delta", retType = "Double")
    public static double correlationDelta(double s1, double s2, double Q1,double Q2,double k, double tau, double r,
                                          double q1, double q2, double vol1, double vol2, double rho) {
        double h = rho / 100.0;
        if (h == 0)
            h = 0.001;
        double rho1 = rho + 0.5 * h;
        double rho2 = rho - 0.5 * h;
        double price1 = price(s1, s2,Q1,Q2, k, tau, r, q1, q2, vol1, vol2, rho1);
        double price2 = price(s1, s2, Q1,Q2,k, tau, r, q1, q2, vol1, vol2, rho2);
        return (price1 - price2) / h;


    }
}


