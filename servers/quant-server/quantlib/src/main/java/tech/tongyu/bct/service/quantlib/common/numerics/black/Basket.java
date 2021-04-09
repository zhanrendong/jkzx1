package tech.tongyu.bct.service.quantlib.common.numerics.black;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;

import static java.lang.Math.*;

/**
 * Created by Liao Song on 16-9-5.
 * closed-form analytic formula for basket-options price.
 * reference: Approximated moment-matching dynamics for basket-options simulation, Damiano Brigo, Fabio Mercurio, etc..
 */
public class Basket {
    private static double s_bar(double[] spot, double[] weight) {
        int n = spot.length - 1;
        double sum = 0;
        for (int i = 0; i <= n; i++) {
            sum += weight[i] * spot[i];
        }
        return sum;
    }

    private static double q_bar(double[] spot, double[] weight, double[] q, double T) {
        int n = spot.length - 1;
        double sum = 0;
        double discountedSum = 0;
        for (int i = 0; i <= n; i++) {
            sum += weight[i] * spot[i];
            discountedSum += weight[i] * spot[i] * exp(-q[i] * T);
        }
        return -1 / T * log(discountedSum / sum);

    }

    private static double vol_bar(double[] spot, double[] weight, double[] q, double[] vol, double[][] rho, double T) {
        int n = spot.length - 1;
        double discountedSum = 0;
        double correlatedSum = 0;
        for (int i = 0; i <= n; i++) {
            discountedSum += weight[i] * spot[i] * exp(-q[i] * T);
            for (int j = 0; j <= n; j++) {
                correlatedSum += weight[i] * weight[j] * spot[i] * spot[j] * exp((-q[i] - q[j] + rho[i][j] * vol[i] * vol[j]) * T);
            }
        }
        return sqrt(1.0 / T * log(correlatedSum / discountedSum / discountedSum));
    }

    @BctQuantApi(name = "qlBasketPriceCalc", description = "Basket option price calculator",
            argNames = {"spot", "weight", "rho", "strike", "vol", "tau", "r", "q", "type"},
            argTypes = {"ArrayDouble", "ArrayDouble", "Matrix", "Double", "ArrayDouble", "Double", "Double",
                    "ArrayDouble", "Enum"},
            argDescriptions = {"Spots of the underlying assets", "Averaging weights", "Correlation matrix",
                    "Strike", "Volatility of the underlying assets", "Time to expiry", "risk free interest rate",
                    "Annually dividend yields of the underlying assets", "Option type"},
            retDescription = "result", retType = "Double", retName = "result")

    public static double price(double[] spot, double[] weight, double[][] rho, double strike,
                               double[] vol, double tau, double r, double[] q, OptionType type) {
        double qBar = q_bar(spot, weight, q, tau);
        double volBar = vol_bar(spot, weight, q, vol, rho, tau);
        double sBar = s_bar(spot, weight);

        return Black.calc(CalcType.PRICE, sBar, strike, volBar, tau, r, qBar, type);
    }

    @BctQuantApi(name = "qlBasketDeltaCalc", description = "calculates basket option delta ",
            argNames = {"spot", "weight", "rho", "strike", "vol", "tau", "r", "q", "type"},
            argTypes = {"ArrayDouble", "ArrayDouble", "Matrix", "Double", "ArrayDouble", "Double", "Double",
                    "ArrayDouble", "Enum"},
            argDescriptions = {"Spots of the underlying assets", "Averaging weights", "Correlation matrix",
                    "Strike", "Volatility of the underlying assets", "Time to expiry", "risk free interest rate",
                    "Annually dividend yields of the underlying assets", "Option type"},
            retDescription = "result", retType = "ArrayDouble", retName = "result")
    public static double[] delta(double[] spot, double[] weight, double[][] rho, double strike,
                                 double[] vol, double tau, double r, double[] q, OptionType type) {
        int N = spot.length;
        double[] result = new double[N];


        for (int i = 0; i < N; i++) {
            double[] temp1 = spot.clone();
            double[] temp2 = spot.clone();
            double h = spot[i] / 100.0;
            temp1[i] = 0.5 * h + spot[i];
            temp2[i] = -0.5 * h + spot[i];
            double price1 = price(temp1, weight, rho, strike, vol, tau, r, q, type);
            double price2 = price(temp2, weight, rho, strike, vol, tau, r, q, type);
            result[i] = (price1 - price2) / h;
        }
        return result;
    }

    @BctQuantApi(name = "qlBasketGammaCalc", description = "calculates basket option gamma ",
            argNames = {"spot", "weight", "rho", "strike", "vol", "tau", "r", "q", "type"},
            argTypes = {"ArrayDouble", "ArrayDouble", "Matrix", "Double", "ArrayDouble", "Double", "Double",
                    "ArrayDouble", "Enum"},
            argDescriptions = {"Spots of the underlying assets", "Averaging weights", "Correlation matrix",
                    "Strike", "Volatility of the underlying assets", "Time to expiry", "risk free interest rate",
                    "Annually dividend yields of the underlying assets", "Option type"},
            retDescription = "result", retType = "Matrix", retName = "result")
    public static double[][] gamma(double[] spot, double[] weight, double[][] rho, double strike,
                                   double[] vol, double tau, double r, double[] q, OptionType type) {
        int N = spot.length;
        double[][] result = new double[N][N];


        //upper triangle
        for (int i = 0; i < N; i++) {
            for (int j = i + 1; j < N; j++) {
                double[] temp1 = spot.clone();
                double[] temp2 = spot.clone();
                double[] temp3 = spot.clone();
                double[] temp4 = spot.clone();
                double hi = spot[i] / 200.0;
                double hj = spot[j] / 200.0;
                temp1[i] = spot[i] + hi;
                temp1[j] = spot[j] + hj;

                temp2[i] = spot[i] + hi;
                temp2[j] = spot[j] - hj;

                temp3[i] = spot[i] - hi;
                temp3[j] = spot[j] + hj;

                temp4[i] = spot[i] - hi;
                temp4[j] = spot[j] - hj;

                double price1 = price(temp1, weight, rho, strike, vol, tau, r, q, type);
                double price2 = price(temp2, weight, rho, strike, vol, tau, r, q, type);
                double price3 = price(temp3, weight, rho, strike, vol, tau, r, q, type);
                double price4 = price(temp4, weight, rho, strike, vol, tau, r, q, type);
                result[i][j] = (price1 - price2 - price3 + price4) / 4 / hi / hj;


            }
        }
        //diagonal
        for (int i = 0; i < N; i++) {
            double h = spot[i] / 100.0;
            double[] temp1 = spot.clone();
            double[] temp2 = spot.clone();
            temp1[i] = 0.5 * h + spot[i];
            temp2[i] = -0.5 * h + spot[i];
            double price1 = price(temp1, weight, rho, strike, vol, tau, r, q, type);
            double price2 = price(temp2, weight, rho, strike, vol, tau, r, q, type);
            double price0 = price(spot, weight, rho, strike, vol, tau, r, q, type);

            result[i][i] = (price1 + price2 - 2 * price0) / (0.5 * h) / (0.5 * h);
        }
        //lower triangle
        for (int j = 0; j < N; j++) {
            for (int i = j + 1; i < N; i++) {
                result[i][j] = result[j][i];
            }
        }


        return result;
    }

    @BctQuantApi(name = "qlBasketDualDeltaCalc", description = "calculates basket option dual_delta ",
            argNames = {"spot", "weight", "rho", "strike", "vol", "tau", "r", "q", "type"},
            argTypes = {"ArrayDouble", "ArrayDouble", "Matrix", "Double", "ArrayDouble", "Double", "Double",
                    "ArrayDouble", "Enum"},
            argDescriptions = {"Spots of the underlying assets", "Averaging weights", "Correlation matrix",
                    "Strike", "Volatility of the underlying assets", "Time to expiry", "risk free interest rate",
                    "Annually dividend yields of the underlying assets", "Option type"},
            retDescription = "result", retType = "Double", retName = "result")
    public static double dual_delta(double[] spot, double[] weight, double[][] rho, double strike,
                                    double[] vol, double tau, double r, double[] q, OptionType type) {
        double step = strike / 100.0;
        double strike1 = strike + step * 0.5;
        double strike2 = strike - step * 0.5;
        double price1 = price(spot, weight, rho, strike1, vol, tau, r, q, type);
        double price2 = price(spot, weight, rho, strike2, vol, tau, r, q, type);
        return (price1 - price2) / step;
    }

    @BctQuantApi(name = "qlBasketThetaCalc", description = "calculates basket option theta ",
            argNames = {"spot", "weight", "rho", "strike", "vol", "tau", "r", "q", "type"},
            argTypes = {"ArrayDouble", "ArrayDouble", "Matrix", "Double", "ArrayDouble", "Double", "Double",
                    "ArrayDouble", "Enum"},
            argDescriptions = {"Spots of the underlying assets", "Averaging weights", "Correlation matrix",
                    "Strike", "Volatility of the underlying assets", "Time to expiry", "risk free interest rate",
                    "Annually dividend yields of the underlying assets", "Option type"},
            retDescription = "result", retType = "Double", retName = "result")
    public static double theta(double[] spot, double[] weight, double[][] rho, double strike,
                               double[] vol, double tau, double r, double[] q, OptionType type) {
        double step = tau / 100.0;
        double tau1 = tau + step * 0.5;
        double tau2 = tau - step * 0.5;
        double price1 = price(spot, weight, rho, strike, vol, tau1, r, q, type);
        double price2 = price(spot, weight, rho, strike, vol, tau2, r, q, type);
        return (price1 - price2) / step;
    }

    @BctQuantApi(name = "qlBasketVegaCalc", description = "calculates basket option gamma ",
            argNames = {"spot", "weight", "rho", "strike", "vol", "tau", "r", "q", "type"},
            argTypes = {"ArrayDouble", "ArrayDouble", "Matrix", "Double", "ArrayDouble", "Double", "Double",
                    "ArrayDouble", "Enum"},
            argDescriptions = {"Spots of the underlying assets", "Averaging weights", "Correlation matrix",
                    "Strike", "Volatility of the underlying assets", "Time to expiry", "risk free interest rate",
                    "Annually dividend yields of the underlying assets", "Option type"},
            retDescription = "result", retType = "ArrayDouble", retName = "result")
    public static double[] vega(double[] spot, double[] weight, double[][] rho, double strike,
                                double[] vol, double tau, double r, double[] q, OptionType type) {
        int N = vol.length;
        double[] result = new double[N];


        for (int i = 0; i < N; i++) {
            double[] temp1 = vol.clone();
            double[] temp2 = vol.clone();
            double h = vol[i] / 100.0;

            temp1[i] = 0.5 * h + vol[i];
            temp2[i] = -0.5 * h + vol[i];

            double price1 = price(spot, weight, rho, strike, temp1, tau, r, q, type);
            double price2 = price(spot, weight, rho, strike, temp2, tau, r, q, type);

            result[i] = (price1 - price2) / h;
        }
        return result;
    }

    @BctQuantApi(name = "qlBasketRhoRCalc", description = "calculates basket option rho_r ",
            argNames = {"spot", "weight", "rho", "strike", "vol", "tau", "r", "q", "type"},
            argTypes = {"ArrayDouble", "ArrayDouble", "Matrix", "Double", "ArrayDouble", "Double", "Double",
                    "ArrayDouble", "Enum"},
            argDescriptions = {"Spots of the underlying assets", "Averaging weights", "Correlation matrix",
                    "Strike", "Volatility of the underlying assets", "Time to expiry", "risk free interest rate",
                    "Annually dividend yields of the underlying assets", "Option type"},
            retDescription = "rho_r", retType = "Double", retName = "result")
    public static double rhor(double[] spot, double[] weight, double[][] rho, double strike,
                              double[] vol, double tau, double r, double[] q, OptionType type) {
        double step = r / 100.0;
        if (step == 0)
            step = 0.01;
        double r1 = r + step * 0.5;
        double r2 = r - step * 0.5;
        double price1 = price(spot, weight, rho, strike, vol, tau, r1, q, type);
        double price2 = price(spot, weight, rho, strike, vol, tau, r2, q, type);
        return (price1 - price2) / step;
    }

}
