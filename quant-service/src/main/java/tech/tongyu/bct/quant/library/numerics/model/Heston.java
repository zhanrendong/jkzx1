package tech.tongyu.bct.quant.library.numerics.model;


import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.numerics.black.BlackScholes;
import tech.tongyu.bct.quant.library.numerics.integration.GaussKronrodAdaptive;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;

/**
 * Heston model
 * dS=(r-q) S dt + \sqrt(V) S dW1
 * dV=\kappa (\theta - V) dt + \omega \sqrt(V) dW2
 * dW1*dW2 = \rho dt
 * The numerical procedure to price European options is based on Gatheral's approach
 */
public class Heston {
    private final double v0;
    private final double omega;
    private final double kappa;
    private final double theta;
    private final double rho;

    public Heston(double v0, double omega, double kappa, double theta, double rho) {
        this.v0 = v0;
        this.omega = omega;
        this.kappa = kappa;
        this.theta = theta;
        this.rho = rho;
    }

    public double calc(double S, double K, double tau,
                       double r, double q, OptionTypeEnum type) {
        double forward = S * FastMath.exp((r - q) * tau);
        double df = FastMath.exp(-r * tau);
        switch (type) {
            case CALL:
                return df * call(forward, K, tau);
            case PUT:
                return df * put(forward, K, tau);
        }
        throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: Heston 仅支持欧式看涨(CALL)/看跌期权(PUT)");
    }

    public double call(double forward, double strike, double tau) {
        final double f = forward;
        final double k = strike;
        final double t = tau;
        final double x = FastMath.log(forward/strike);
        GaussKronrodAdaptive integrator = new GaussKronrodAdaptive(0.0001, 10000);
        double ret = integrator.integrate(0.,Double.POSITIVE_INFINITY,u->f*psi(u,t,x,1)-k*psi(u,t,x,0));
        return 0.5*(forward-strike)+1.0/Math.PI*ret;
    }

    public double put(double forward, double strike, double tau) {
        return call(tau,forward,strike)-forward+strike;
    }

    public double vol(double tau, double forward, double strike) {
        double call = call(tau,forward,strike);
        return BlackScholes.iv(call, forward, strike, tau, 0., 0., OptionTypeEnum.CALL);
    }

    private double psi(double u, double t, double x, int type) {
        Complex alpha = new Complex(-0.5*u*u, (type==0)?-0.5*u:0.5*u);
        Complex beta = new Complex(kappa-((type==0)?0.0:rho*omega), -rho*omega*u);
        Complex d = beta.multiply(beta).subtract(alpha.multiply(2.0).multiply(omega*omega)).sqrt();
        Complex rplus = beta.add(d).divide(omega*omega);
        Complex rminus = beta.subtract(d).divide(omega*omega);
        Complex g = rminus.divide(rplus);
        Complex exp_d_t = d.multiply(-t).exp();
        Complex g_exp_d_t = g.multiply(exp_d_t);
        Complex D = rminus.multiply(exp_d_t.subtract(1.0).divide(g_exp_d_t.subtract(1.0))).multiply(v0);
        Complex log_ratio = g_exp_d_t.subtract(1.0).divide(g.subtract(1.0)).log().multiply(2.0/(omega*omega));
        Complex C = rminus.multiply(t).subtract(log_ratio).multiply(kappa*theta);
        Complex eye= new Complex(0.0,1.0);
        Complex integrand = C.add(D).add(eye.multiply(u*x)).exp().divide(eye.multiply(u));
        return integrand.getReal();
    }
}
