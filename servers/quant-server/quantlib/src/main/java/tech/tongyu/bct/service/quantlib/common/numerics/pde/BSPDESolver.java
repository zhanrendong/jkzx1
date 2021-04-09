package tech.tongyu.bct.service.quantlib.common.numerics.pde;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import tech.tongyu.bct.service.quantlib.common.enums.ExerciseType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;

import static java.lang.Math.*;
import static tech.tongyu.bct.service.quantlib.common.utils.MatrixUtility.TDMASolver;
import static tech.tongyu.bct.service.quantlib.common.utils.MatrixUtility.TMSolver;


/**
 * Created by Liao Song on 16-8-15.
 * Solves Black-Scholes partial differential equation using finite difference method
 */
class BSPDESolver {


    /**
     * vanilla option payoff as a function of spot, strike, risk free interest rate r, annually dividend yield, and time
     * to maturity
     *
     * @param type   option type, CALL or PUT
     * @param spot   spot price
     * @param strike strike price
     * @param r      risk free interest rate
     * @param q      annually dividend yield
     * @param tau    time to maturity (i.e., time to get the payoff amount)
     * @return returns the payoff
     * @throws Exception if option type is not supported
     */
    private static double payoff(OptionType type, double spot, double strike, double r, double q, double tau)
            throws Exception {
        if (type == OptionType.CALL) {
            return max(spot * exp(-q * tau) - strike * exp(-r * tau), 0);
        } else if (type == OptionType.PUT) {
            return max(strike * exp(-r * tau) - spot * exp(-q * tau), 0);
        } else throw new Exception("The option type is not supported.");
    }

    /**
     * calculate the option prices at each nodes using (maybe) non-uniform grids
     *
     * @param optionType   option type: CALL or PUT
     * @param exerciseType exercise type: AMERICAN or EUROPEAN
     * @param tau          time to maturity
     * @param N            number of grids in time space
     * @param s            spot grids, s[0] = 0, s[end] = s_max
     * @param strike       strike price
     * @param r            risk free interest rate
     * @param q            annually dividend yield
     * @param vol          volatility
     * @param alpha        implicit index
     * @return returns the option price by solving the PDE using finite different method
     * @throws Exception argument error
     */
    private static RealVector getOptionVector(OptionType optionType, ExerciseType exerciseType, double tau, int N,
                                              double[] s, double strike, double r, double q, double vol, double alpha)
            throws Exception {

        if (alpha < 0 || alpha > 1)
            throw new Exception("Invalid argument: alpha must within the range [0, 1]. ");
        double beta = 1.0 - alpha;

        double dt = tau / (double) N;
        int M = s.length - 1;


        RealVector fm = new ArrayRealVector(new double[M + 1]);

        // initial conditions at maturity
        for (int j = 0; j <= M; j++) {
            fm.setEntry(j, payoff(optionType, s[j], strike, r, q, 0));

        }
        RealVector la = new ArrayRealVector(new double[M]);
        RealVector lb = new ArrayRealVector(new double[M + 1]);
        RealVector lc = new ArrayRealVector(new double[M]);

        RealVector ra = new ArrayRealVector(new double[M]);
        RealVector rb = new ArrayRealVector(new double[M + 1]);
        RealVector rc = new ArrayRealVector(new double[M]);

        //the augmented matrix
        lb.setEntry(0, 1.0);
        lc.setEntry(0, 0);

        rb.setEntry(0, 1.0);
        rc.setEntry(0, 0);

        for (int j = 1; j <= M - 1; j++) {
            double A = -(s[j + 1] - s[j]) / (s[j] - s[j - 1]) / (s[j + 1] - s[j - 1]);
            double B = ((s[j + 1] - s[j]) / (s[j] - s[j - 1]) / (s[j + 1] - s[j - 1]) - (s[j] - s[j - 1])
                    / (s[j + 1] - s[j]) / (s[j + 1] - s[j - 1]));
            double G = (s[j] - s[j - 1]) / (s[j + 1] - s[j]) / (s[j + 1] - s[j - 1]);
            double D = 2.0 / (s[j] - s[j - 1]) / (s[j + 1] - s[j - 1]);
            double E = -2.0 * (1.0 / (s[j + 1] - s[j]) / (s[j + 1] - s[j - 1]) + 1.0 / (s[j] - s[j - 1])
                    / (s[j + 1] - s[j - 1]));
            double F = 2.0 / (s[j + 1] - s[j]) / (s[j + 1] - s[j - 1]);

            la.setEntry(j - 1, alpha * (0.5 * vol * vol * s[j] * s[j] * D + (r - q) * s[j] * A));
            lb.setEntry(j, -1.0 / dt + alpha * (0.5 * vol * vol * s[j] * s[j] * E + (r - q) * s[j] * B - r));
            lc.setEntry(j, alpha * (0.5 * vol * vol * s[j] * s[j] * F + (r - q) * s[j] * G));

            ra.setEntry(j - 1, -beta * (0.5 * vol * vol * s[j] * s[j] * D + (r - q) * s[j] * A));
            rb.setEntry(j, -1.0 / dt - beta * (0.5 * vol * vol * s[j] * s[j] * E + (r - q) * s[j] * B - r));
            rc.setEntry(j, -beta * (0.5 * vol * vol * s[j] * s[j] * F + (r - q) * s[j] * G));
        }


        rb.setEntry(M, 1.0);
        ra.setEntry(M - 1, 0);

        lb.setEntry(M, 1.0);
        la.setEntry(M - 1, 0);

        RealVector g;

        for (int i = N - 1; i >= 0; i--) {
            //
            g = TMSolver(ra, rb, rc, fm);
            if (alpha != 0.0) {
                fm = TDMASolver(la, lb, lc, g);
            } else fm = g;
            if (exerciseType == ExerciseType.AMERICAN) {
                for (int j = 0; j <= M; j++) {
                    double temp = fm.getEntry(j);
                    fm.setEntry(j, max(payoff(optionType, s[j], strike, r, q, 0), temp));
                }
            }
        }
        return fm;

    }

    /**
     * Calculates the vanilla European or American option price using the finite difference method and linear
     * interpolation at the given accuracy.
     * The algorithm forces the strike to lie in the middle of two grids.
     *
     * @param optionType   option type: CALL or PUT
     * @param exerciseType exercise type: AMERICAN or EUROPEAN
     * @param spot         current spot price
     * @param strike       strike price
     * @param tau          time to maturity in years
     * @param r            risk free interest rate
     * @param q            annually dividend yield
     * @param vol          volatility
     * @param eps          accuracy
     * @return returns the option price
     * @throws Exception argument error
     */
    static double getOptionPrice(OptionType optionType, ExerciseType exerciseType,
                                 double spot, double strike, double tau, double r, double q, double vol,
                                 double eps) throws Exception {

        // the far field boundary
        // reference: R. Kangro and R. Nicolaides, Far field boundary conditions for Black-Scholes equations,
        // SIAM J. Numer. Anal. 38(2000), no. 4, 1357-1367

        // first project the strike to 1
        double projection = 1.0 / strike;
        strike *= projection;
        spot *= projection;

        // spot grids

        double A = strike / eps;
        double sMax = max(strike * exp(-0.5 * min(0, tau * (vol * vol - 2.0 * r))
                + 0.5 * sqrt(min(0, tau * (vol * vol - 2.0 * r)) + 8 * vol * vol * tau * log(A))), 2.0 * strike);

        double ds = sqrt(eps * spot / 10.0);
        int Ma = (int) (strike / ds);
        int Mb = (int) ((sMax - strike) / ds);
        double[] s = new double[Ma + Mb + 2];
        s[0] = 0;
        s[Ma + Mb + 1] = sMax;

        for (int i = 0; i < Ma; i++) {
            s[Ma - i] = strike - 0.5 * ds - i * ds;
        }
        for (int i = 0; i < Mb; i++) {
            s[Ma + i + 1] = strike + 0.5 * ds + i * ds;
        }
        int N = (int) sqrt(tau / eps);

        RealVector y = getOptionVector(optionType, exerciseType, tau, N, s, strike, r, q, vol, 0.5);

        LinearInterpolator f = new LinearInterpolator();
        f.interpolate(s, y.toArray());
        return f.interpolate(s, y.toArray()).value(spot) / projection;


    }


}
