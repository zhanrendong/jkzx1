package tech.tongyu.bct.quant.library.numerics.black;

import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;

public class Black76 {
    /**
     * Black76 formula that uses forward, vol, r, tau as independent variables. This formula is mainly
     * used to price European options on futures. Futures price, without considering rate/underlyer correlations,
     * is the same as forward price.
     * @param req Calculation request. See {@link CalcTypeEnum}
     * @param forward Underlyer forward
     * @param K Strike
     * @param vol Volatility (annualized)
     * @param tau Time to expiry (in years)
     * @param r Risk free rate (continuous compounding)
     * @param type Option type (CALL or PUT). See {@link OptionTypeEnum}
     * @return Requested calculation result
     */
    public static double calc(CalcTypeEnum req, double forward, double K, double vol, double tau,
                                 double r, OptionTypeEnum type) {
        switch (req) {
            case INTRINSIC_VALUE:
            case PRICE:
            case DELTA:
            case DUAL_DELTA:
            case GAMMA:
            case DUAL_GAMMA:
            case VEGA:
            case THETA:
                return BlackScholes.calc(req, forward, K, vol, tau, r, r, type);
            case RHO_R:
                return BlackScholes.calc(CalcTypeEnum.RHO_R, forward, K, vol, tau, r, r, type)
                        + BlackScholes.calc(CalcTypeEnum.RHO_Q, forward, K, vol, tau, r, r, type);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID, "Black76不支持计算类型：" + req);
        }
    }
}
