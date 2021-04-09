package tech.tongyu.bct.quant.library.numerics.black;

import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.numerics.ad.Backwarder;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;

public class Digital {
    private static double calcTau0(CalcTypeEnum request,
                                   double S, double K,
                                   OptionTypeEnum type, boolean payCash) {
        if (payCash) {
            switch (request) {
                case PRICE:
                    if (type == OptionTypeEnum.CALL)
                        return S>=K ? 1.0 : 0.0;
                    else
                        return S<K ? 1.0 : 0.0;
                case DELTA:
                    return 0.0;
                case GAMMA:
                    return 0.0;
                case VEGA:
                    return 0.0;
                case THETA:
                    return 0.0;
                default:
                    return Double.NaN;
            }
        } else {
            switch (request) {
                case PRICE:
                    if (type == OptionTypeEnum.CALL)
                        return S>=K ? S : 0.0;
                    else
                        return S<K ? S : 0.0;
                case DELTA:
                    return 1.0;
                case GAMMA:
                    return 0.0;
                case VEGA:
                    return 0.0;
                case THETA:
                    return 0.0;
                default:
                    return Double.NaN;
            }
        }
    }

    /**
     * Digital pricer based on analytic lognormal model
     * @param request Calculation to be carried out
     * @param S Underlyer spot
     * @param K Strike
     * @param vol Volatility
     * @param tau Time to expiry, annualized
     * @param r Risk free rate, ACT365
     * @param q Dividend rate, ACT365
     * @param T Time to delivery, annualized. Usually the same as tau.
     * @param type Option type, CALL or PUT
     * @param payCash Cash or physically settled
     * @return Requested calcuation result
     */
    public static double calc(CalcTypeEnum request,
                              double S, double K, double vol, double tau, double r, double q,
                              double T,
                              OptionTypeEnum type, boolean payCash) {
        if (DoubleUtils.smallEnough(tau, DoubleUtils.SMALL_NUMBER) || tau < 0.) {
            return calcTau0(request, S, K, type, payCash);
        }
        Backwarder calculator = new Backwarder(new String[]{"S", "K", "vol", "tau", "r", "q", "T"},
                new double[]{S, K, vol, tau, r, q, T}, 2);
        String dfr = calculator.exp(calculator.mul("tau", calculator.scale("r", -1.0)));
        String dfq = calculator.exp(calculator.mul("tau", calculator.scale("q", -1.0)));
        String fwd = calculator.div(calculator.mul("S", dfq), dfr);
        String lnMoneyness = calculator.ln(calculator.div(fwd, "K"));
        String sqrtVar = calculator.sqrt(calculator.mul(calculator.power("vol", 2), "tau"));
        String dp = calculator.sum(calculator.div(lnMoneyness, sqrtVar), calculator.scale(sqrtVar, 0.5));
        String dm = calculator.sub(dp, sqrtVar);
        String price;
        if (payCash) {
            String dfToDelivery = calculator.exp(calculator.mul("T", calculator.scale("r", -1.0)));
            if (type == OptionTypeEnum.CALL) {
                price = calculator.mul(dfToDelivery, calculator.ncdf(dm));
            } else {
                price = calculator.mul(dfToDelivery,
                        calculator.sum(calculator.scale(calculator.ncdf(dm), -1.0), 1.0));
            }
        } else {
            String dfQToDelivery = calculator.exp(calculator.mul("T", calculator.scale("q", -1.0)));
            if (type == OptionTypeEnum.CALL) {
                price = calculator.mul(dfQToDelivery, calculator.mul("S", calculator.ncdf(dp)));
            } else {
                price = calculator.mul(dfQToDelivery,
                        calculator.mul("S", calculator.ncdf(calculator.scale(dp, -1.0))));
            }
        }
        calculator.rollBack();
        switch (request) {
            case PRICE:
                return calculator.getValue(price);
            case DELTA:
                return calculator.getDeriv("S");
            case DUAL_DELTA:
                return calculator.getDeriv("K");
            case GAMMA:
                return calculator.getDeriv("S", "S");
            case DUAL_GAMMA:
                return calculator.getDeriv("K", "K");
            case VEGA:
                return calculator.getDeriv("vol");
            case THETA:
                return calculator.getDeriv("tau");
            case RHO_R:
                return calculator.getDeriv("r");
            case VERA_R:
                return calculator.getDeriv("r", "vol");
            case RHO_Q:
                return calculator.getDeriv("q");
            case VERA_Q:
                return calculator.getDeriv("q", "vol");
            case VANNA:
                return calculator.getDeriv("S", "vol");
            case VOLGA:
                return calculator.getDeriv("vol", "vol");
            case VETA:
                return calculator.getDeriv("tau", "vol");
            case CHARM:
            case COLOR:
            case SPEED:
            case ULTIMA:
            case ZOMMA:
            default:
                return Double.NaN;
        }
    }
}
