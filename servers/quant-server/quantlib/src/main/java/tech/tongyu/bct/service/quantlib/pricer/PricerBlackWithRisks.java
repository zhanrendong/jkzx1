package tech.tongyu.bct.service.quantlib.pricer;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.api.OptionPricerBlack;
import tech.tongyu.bct.service.quantlib.api.OptionPricerPDE;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.numerics.fd.FiniteDifference;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;
import tech.tongyu.bct.service.quantlib.financial.instruments.linear.Cash;
import tech.tongyu.bct.service.quantlib.market.curve.Discount;
import tech.tongyu.bct.service.quantlib.market.curve.Pwlf;
import tech.tongyu.bct.service.quantlib.market.vol.AtmPwc;
import tech.tongyu.bct.service.quantlib.market.vol.ImpliedVolSurface;
import tech.tongyu.bct.service.quantlib.pricer.mc.PricerSingleAsset;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Black analytic pricers that outputs price together with greeks and vol/rates used in pricing
 * This is basically the vectorized version of PricerBlack
 * Created by lu on 5/10/17.
 */
public class PricerBlackWithRisks {
    // cash (single cash flow)
    public static Map<String, Double> calc(Object[] requests,
                                           Cash instrument,
                                           Discount yc,
                                           double daysInYear) {
        Map<String, Double> ret = new HashMap<>();
        LocalDateTime val = yc.getVal();
        LocalDateTime payDate = instrument.getPayDate();
        double tau = val.until(instrument.getPayDate(), ChronoUnit.NANOS) / Constants.NANOSINDAY / daysInYear;
        if (tau < 0.0)
            return ret;
        double amount = instrument.getAmount();
        double df = yc.df(payDate);
        double r = -FastMath.log(df) / tau;
        double pv = amount * df;
        for (Object request : requests) {
            CalcType calcType = (CalcType) request;
            double result = 0.0;
            switch (calcType) {
                case PRICE:
                    result = pv;
                    break;
                case RHO_R:
                    result = -tau * pv;
                    break;
                case THETA:
                    result = r * pv;
                    break;
            }
            ret.put(calcType.name().toLowerCase(), result);
        }
        ret.put("r", r);
        return ret;
    }

    // spot (including futures, i.e. any securities whose price is its quote)
    public static Map<String, Double> calc(Object[] requests,
                                           double spot) {
        Map<String, Double> ret = new HashMap<>();
        for (Object request : requests) {
            CalcType calcType = (CalcType) request;
            double result = 0.0;
            switch (calcType) {
                case PRICE:
                    result = spot;
                    break;
                case DELTA:
                    result = 1.0;
                    break;
            }
            ret.put(calcType.name().toLowerCase(), result);
        }
        ret.put("spot", spot);
        return ret;
    }

    // helper: get black parameters from curves and vol surfaces
    private static Map<String, Double> getBlackParams(
            LocalDateTime val,
            LocalDateTime expiry,
            double spot,
            ImpliedVolSurface volSurface,
            Discount discountCurve,
            Discount dividendCurve
    ) {
        Map<String, Double> ret = new HashMap<>();
        double df_r = discountCurve.df(expiry);
        double df_q = dividendCurve.df(expiry);
        double forward = spot * df_q / df_r;
//        double iv = volSurface.iv(forward, forward, expiry);
        double variance = volSurface.variance(forward, forward, expiry);
        double daysInYear = 365;
        long tauNanos = val.until(expiry, ChronoUnit.NANOS);
        double tau = tauNanos / Constants.NANOSINDAY / daysInYear;
        double iv = Math.sqrt(variance / tau);
        double r = tauNanos == 0 ? 0.0 : -FastMath.log(df_r) / tau;
        double q = tauNanos == 0 ? 0.0 : -FastMath.log(df_q) / tau;
        ret.put("spot", spot);
        ret.put("vol", iv);
        ret.put("r", r);
        ret.put("q", q);
        return ret;
    }
    // options: analytic formulas
    public static Map<String, Double> calc(Object[] requests,
                                           Object instrument,
                                           double spot,
                                           ImpliedVolSurface volSurface,
                                           Discount discountCurve,
                                           Discount dividendCurve) throws Exception {
        LocalDateTime val = volSurface.getVal();
        LocalDateTime expiry = OptionPricerBlack.getExpiry(instrument);
        Map<String, Double> ret = getBlackParams(val, expiry, spot, volSurface, discountCurve, dividendCurve);
        double iv = ret.get("vol");
        double r = ret.get("r");
        double q = ret.get("q");
        for (Object request : requests) {
            CalcType calcType = (CalcType) request;
            double result = OptionPricerBlack.calc(calcType, instrument, val, spot, iv, r, q);
            if (calcType == CalcType.THETA)
                result = -result;
            ret.put(calcType.name().toLowerCase(), result);
        }
        return ret;
    }

    // options: pde
    public static Map<String, Double> pdeCalc(Object[] requests,
                                           Object instrument,
                                           double spot,
                                           ImpliedVolSurface volSurface,
                                           Discount discountCurve,
                                           Discount dividendCurve,
                                           Map<String, Object> params
    ) throws Exception {
        LocalDateTime val = volSurface.getVal();
        LocalDateTime expiry = OptionPricerBlack.getExpiry(instrument);
        Map<String, Double> ret = getBlackParams(val, expiry, spot, volSurface, discountCurve, dividendCurve);
        double iv = ret.get("vol");
        double r = ret.get("r");
        double q = ret.get("q");
        // gather pricer params
        //   defaults
        double eps = 0.0001;
        double alpha = 0.5;
        // parse params inputs
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getKey().toLowerCase().equals("alpha"))
                    alpha = (double)entry.getValue();
                if (entry.getKey().toLowerCase().equals("eps"))
                    eps = (double)entry.getValue();
            }
        }
        // pde calculator only computes price
        // so for greeks we use finite difference approximation
        //   delta, gamma, vega
        final double epsUsed = eps;
        final double alphaUsed = alpha;
        double[] variables = new double[]{spot, iv, r, q};
        FiniteDifference calcFD = new FiniteDifference(u -> {
            try {
                return OptionPricerPDE.calc(
                        instrument, val, u[0], u[2], u[3], u[1],
                        epsUsed, alphaUsed, params);
            } catch (Exception e) {
                return 0.0;
            }
        });
        double price = calcFD.getValue(variables);
        double[] gradients = calcFD.getGradient(variables);
        double[][] hessian = calcFD.getHessian(variables);
        //  theta
        LocalDateTime valPlusOne = val.plusDays(1);
        double pricePlusOne = OptionPricerPDE.calc(instrument, valPlusOne,
                spot, r, q, iv, eps, alpha, params);
        double theta = (pricePlusOne - price) * 365;
        for (Object request : requests) {
            CalcType calcType = (CalcType) request;
            switch (calcType) {
                case PRICE:
                    ret.put(calcType.name().toLowerCase(), price);
                    break;
                case DELTA:
                    ret.put(calcType.name().toLowerCase(), gradients[0]);
                    break;
                case GAMMA:
                    ret.put(calcType.name().toLowerCase(), hessian[0][0]);
                    break;
                case VEGA:
                    ret.put(calcType.name().toLowerCase(), gradients[1]);
                    break;
                case THETA:
                    ret.put("theta", theta);
                    break;
                case RHO_R:
                    ret.put("rho_r", gradients[2]);
                    break;
                case RHO_Q:
                    ret.put("rho_q", gradients[3]);
                    break;
                default:
                    throw new Exception("Request " + calcType.name() + " is not supported by PDE solver");
            }
        }
        return ret;
    }

    // options: mc
    public static Map<String, Double> mcCalc(Object[] requests,
                                              Object instrument,
                                              double spot,
                                              ImpliedVolSurface volSurface,
                                              Discount discountCurve,
                                              Discount dividendCurve,
                                              Map<String, Object> params
    ) throws Exception {
        LocalDateTime val = volSurface.getVal();
        LocalDateTime expiry = OptionPricerBlack.getExpiry(instrument);
        Map<String, Double> ret = getBlackParams(val, expiry, spot, volSurface, discountCurve, dividendCurve);
        double iv = ret.get("vol");
        double r = ret.get("r");
        double q = ret.get("q");

        // mc calculator only computes price
        // so for greeks we use finite difference approximation
        //   delta, gamma, vega
        double[] variables = new double[]{spot, iv, r, q};
        FiniteDifference calcFD = new FiniteDifference(u -> {
            try {
                AtmPwc vs = AtmPwc.flat(val, u[0], u[1], Constants.DAYS_IN_YEAR);
                Pwlf discount = Pwlf.flat(val, u[2], Constants.DAYS_IN_YEAR);
                Pwlf dividend = Pwlf.flat(val, u[3], Constants.DAYS_IN_YEAR);
                return PricerSingleAsset.pv(instrument, val, u[0], vs, discount, dividend, params).get("price");
            } catch (Exception e) {
                return 0.0;
            }
        });
        double price = calcFD.getValue(variables);
        double[] gradients = calcFD.getGradient(variables);
        double[][] hessian = calcFD.getHessian(variables);
        //  theta
        LocalDateTime valPlusOne = val.plusDays(1);
        AtmPwc vs = AtmPwc.flat(val, spot, iv, Constants.DAYS_IN_YEAR);
        Pwlf discount = Pwlf.flat(val, r, Constants.DAYS_IN_YEAR);
        Pwlf dividend = Pwlf.flat(val, q, Constants.DAYS_IN_YEAR);
        double pricePlusOne = PricerSingleAsset.
                pv(instrument, valPlusOne, spot, vs, discount, dividend, params)
                .get("price");
        double theta = (pricePlusOne - price) * 365;
        for (Object request : requests) {
            CalcType calcType = (CalcType) request;
            switch (calcType) {
                case PRICE:
                    ret.put(calcType.name().toLowerCase(), price);
                    break;
                case DELTA:
                    ret.put(calcType.name().toLowerCase(), gradients[0]);
                    break;
                case GAMMA:
                    ret.put(calcType.name().toLowerCase(), hessian[0][0]);
                    break;
                case VEGA:
                    ret.put(calcType.name().toLowerCase(), gradients[1]);
                    break;
                case THETA:
                    ret.put("theta", theta);
                    break;
                case RHO_R:
                    ret.put("rho_r", gradients[2]);
                    break;
                case RHO_Q:
                    ret.put("rho_q", gradients[3]);
                    break;
                default:
                    throw new Exception("Request " + calcType.name() + " is not supported by MC solver");
            }
        }
        return ret;
    }
}
