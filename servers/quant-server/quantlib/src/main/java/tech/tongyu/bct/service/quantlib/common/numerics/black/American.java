package tech.tongyu.bct.service.quantlib.common.numerics.black;

import org.apache.commons.math3.distribution.NormalDistribution;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.numerics.fd.FiniteDifference;

import static java.lang.Math.*;

/**
 * Created by Liao Song on 16-6-30.
 * American option pricing formulas by B-A and Whaley approximation method
 * reference : Efficient Analytic Approximation of American Option Values (1987)
 */
public class American {

    private static NormalDistribution Norm = new NormalDistribution();

    private static double d1(double spot, double strike, double tau, double r, double q, double vol) {
        double b = r - q;

        return (log(spot / strike) + (b + 0.5 * pow(vol, 2)) * tau) / (vol * sqrt(tau));
    }

    private static double GS(OptionType type, double spot, double strike, double tau, double r, double q, double vol) {
        double b = r - q;
        double M = 2 * r / pow(vol, 2);
        double N = 2 * b / pow(vol, 2);

        double K = 1 - exp(-r * tau);
        double q1 = 0.5 * (-N + 1 - sqrt(pow(N - 1, 2) + 4 * M / K));
        double q2 = 0.5 * (-N + 1 + sqrt(pow(N - 1, 2) + 4 * M / K));
        if (type == OptionType.CALL)
            return spot - strike - Black.calc(CalcType.PRICE, spot, strike, vol, tau, r, q, type) -
                    (1 - exp((b - r) * tau) * Norm.cumulativeProbability(d1(spot, strike, tau, r, q, vol))) * spot / q2;
        else
            return strike - spot - Black.calc(CalcType.PRICE, spot, strike, vol, tau, r, q, type) +
                    (1 - exp((b - r) * tau) * Norm.cumulativeProbability(-d1(spot, strike, tau, r, q, vol))) * spot / q1;
    }

    private static double GSP(OptionType type, double spot, double strike, double tau, double r, double q, double vol) {
        double b = r - q;
        double M = 2 * r / pow(vol, 2);
        double N = 2 * b / pow(vol, 2);

        double K = 1 - exp(-r * tau);
        double q1 = 0.5 * (-N + 1 - sqrt(pow(N - 1, 2) + 4 * M / K));
        double q2 = 0.5 * (-N + 1 + sqrt(pow(N - 1, 2) + 4 * M / K));
        if (type == OptionType.CALL)
            return (1 - exp((b - r) * tau) * Norm.cumulativeProbability(d1(spot, strike, tau, r, q, vol)))
                    * (1 - 1 / q2) + exp((b - r) * tau) * Norm.density(d1(spot, strike, tau, r, q, vol))
                    / (vol * sqrt(tau)) / q2;
        else
            return (1 - exp((b - r) * tau) * Norm.cumulativeProbability(-d1(spot, strike, tau, r, q, vol)))
                    * (1 / q1 - 1) + exp((b - r) * tau) * Norm.density(-d1(spot, strike, tau, r, q, vol))
                    / (vol * sqrt(tau)) / q1;
    }

    private static double CriticalSpot(OptionType type, double strike, double tau, double r, double q, double vol) {
        double eps = 1e-5;
        double spot;
        double b = r - q;
        double M = 2 * r / pow(vol, 2);
        double N = 2 * b / pow(vol, 2);
        if (type == OptionType.CALL) {
            double q2_inf = 0.5 * (-N + 1 + sqrt(pow(N - 1, 2) + 4 * M));
            double s_inf = strike / (1 - 1 / q2_inf);
            double h2 = -(b * tau + 2 * vol * sqrt(tau)) * (strike / (s_inf - strike));
            spot = strike + (s_inf - strike) * (1 - exp(h2));
            while (abs(GS(type, spot, strike, tau, r, q, vol)) / strike > eps && spot > 0) {
                spot = spot - GS(type, spot, strike, tau, r, q, vol) / GSP(type, spot, strike, tau, r, q, vol);
            }
        } else {
            double q1_inf = 0.5 * (-N + 1 - sqrt(pow(N - 1, 2) + 4 * M));
            double s_inf = strike / (1 - 1 / q1_inf);
            double h1 = (b * tau - 2 * vol * sqrt(tau)) * (strike / (strike - s_inf));
            spot = s_inf + (strike - s_inf) * exp(h1);
            while (abs(GS(type, spot, strike, tau, r, q, vol)) / strike > eps && spot > 0) {
                spot = spot - GS(type, spot, strike, tau, r, q, vol) / GSP(type, spot, strike, tau, r, q, vol);
            }
        }
        return spot;
    }

    private static double price(OptionType optionType,
                                double spot, double strike, double vol, double tau, double r, double q) {


        double b = r - q;
        double M = 2 * r / pow(vol, 2);
        double N = 2 * b / pow(vol, 2);

        double K = 1 - exp(-r * tau);

        // temporary fix:
        // to handle the case r = 0 => K = 0 => M / K becomes undefined
        // use small r expansion
        double mOverK = 0.0;
        if (abs(K)<1e-14)
            mOverK = 2.0/(tau * vol * vol);
        else
            mOverK = M / K;

        double q1 = 0.5 * (-N + 1 - sqrt(pow(N - 1, 2) + 4 * mOverK));
        double q2 = 0.5 * (-N + 1 + sqrt(pow(N - 1, 2) + 4 * mOverK));


        double s_critical;
        if (optionType == OptionType.CALL) {
            if (b < r) {
                s_critical = CriticalSpot(optionType, strike, tau, r, q, vol);
                double A2 = (s_critical / q2) * (1 - exp((b - r) * tau)
                        * Norm.cumulativeProbability(d1(s_critical, strike, tau, r, q, vol)));
                if (spot < s_critical)
                    return Black.calc(CalcType.PRICE, spot, strike, vol, tau, r, q, optionType)
                            + A2 * pow(spot / s_critical, q2);
                else
                    return spot - strike;
            } else
                return Black.calc(CalcType.PRICE, spot, strike, vol, tau, r, q, optionType);
        } else {
            s_critical = CriticalSpot(optionType, strike, tau, r, q, vol);
            double A1 = -(s_critical / q1) * (1 - exp((b - r) * tau)
                    * Norm.cumulativeProbability(-d1(s_critical, strike, tau, r, q, vol)));
            if (spot > s_critical)
                return Black.calc(CalcType.PRICE, spot, strike, vol, tau, r, q, optionType)
                        + A1 * pow(spot / s_critical, q1);
            else
                return strike - spot;
        }


    }

    /**
     * calculates price and Greeks of American options by B-A Whaley approximation methods
     *
     * @param request PRICE or Greeks
     * @param type    option type CALL or PUT
     * @param spot    current price of the underlying asset
     * @param strike  strike
     * @param vol     volatility
     * @param tau     time to maturity in years
     * @param r       risk free interest rate
     * @param q       volatility
     * @return requested calculation result
     * @throws Exception unsupported calculation type
     */
    public static double calc(CalcType request, OptionType type,
                              double spot, double strike, double vol, double tau, double r, double q) throws Exception {
        double[] variables = new double[]{spot, strike, vol, tau, r, q};
        FiniteDifference calcFD = new FiniteDifference(u -> price(type, u[0], u[1], u[2], u[3], u[4], u[5]));
        double[] Gradients = calcFD.getGradient(variables);
        double[][] Hessian = calcFD.getHessian(variables);
        switch (request) {
            case PRICE:
                return calcFD.getValue(variables);
            case DELTA:
                return Gradients[0];
            case DUAL_DELTA:
                return Gradients[1];
            case VEGA:
                return Gradients[2];
            case THETA:
                return Gradients[3];
            case RHO_R:
                return Gradients[4];
            case RHO_Q:
                return Gradients[5];
            case GAMMA:
                return Hessian[0][0];
            case DUAL_GAMMA:
                return Hessian[1][1];
            case VANNA:
                return Hessian[0][2];
            case CHARM:
                return Hessian[0][3];
            case VOLGA:
                return Hessian[2][2];
            case VETA:
                return Hessian[2][3];
            case VERA_R:
                return Hessian[2][4];
            case VERA_Q:
                return Hessian[2][5];
            default:
                throw new Exception("Calculation type is not supported for vanilla American option.");
        }
    }

}