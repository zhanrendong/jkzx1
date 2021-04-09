package tech.tongyu.bct.quant.library.market.custom;

import com.fasterxml.jackson.annotation.JsonCreator;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.QuantlibSerializableObject;
import tech.tongyu.bct.quant.library.numerics.interp.Interpolator1DCubicSpline;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A simple interpolated pricer. The model contains the following cubic spline interpolators:
 * price, delta, gamma, vega, theta
 * The input is underlyer price and the out put is interpolated price and greeks
 */
public class ModelXY implements QuantlibSerializableObject {
    private final List<LocalDateTime> timestamps;
    private final List<Interpolator1DCubicSpline> priceInterp;
    private final List<Interpolator1DCubicSpline> deltaInterp;
    private final List<Interpolator1DCubicSpline> gammaInterp;
    private final List<Interpolator1DCubicSpline> vegaInterp;
    private final List<Interpolator1DCubicSpline> thetaInterp;

    @JsonCreator
    public ModelXY(List<LocalDateTime> timestamps,
                   List<Interpolator1DCubicSpline> priceInterp,
                   List<Interpolator1DCubicSpline> deltaInterp,
                   List<Interpolator1DCubicSpline> gammaInterp,
                   List<Interpolator1DCubicSpline> vegaInterp,
                   List<Interpolator1DCubicSpline> thetaInterp) {
        this.timestamps = timestamps;
        this.priceInterp = priceInterp;
        this.deltaInterp = deltaInterp;
        this.gammaInterp = gammaInterp;
        this.vegaInterp = vegaInterp;
        this.thetaInterp = thetaInterp;
    }

    public double calc(CalcTypeEnum calcTypeEnum,
                       double underlyerPrice,
                       LocalDateTime valuationDateTime) {
        int index = 0;
        while(index < timestamps.size()) {
            if (timestamps.get(index).isAfter(valuationDateTime)) {
                break;
            }
            ++index;
        }
        if (index > 0) {
            --index;
        }
        switch (calcTypeEnum) {
            case PRICE:
                return priceInterp.get(index).value(underlyerPrice);
            case DELTA:
                return deltaInterp.get(index).value(underlyerPrice);
            case GAMMA:
                return gammaInterp.get(index).value(underlyerPrice);
            case VEGA:
                return vegaInterp.get(index).value(underlyerPrice);
            case THETA:
                return thetaInterp.get(index).value(underlyerPrice);
            case RHO_R:
                return 0.0;
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        "quantlib: ModelXY: 不支持计算类型 " + calcTypeEnum.name());
        }
    }

    public List<LocalDateTime> getTimestamps() {
        return timestamps;
    }

    public List<Interpolator1DCubicSpline> getPriceInterp() {
        return priceInterp;
    }

    public List<Interpolator1DCubicSpline> getDeltaInterp() {
        return deltaInterp;
    }

    public List<Interpolator1DCubicSpline> getGammaInterp() {
        return gammaInterp;
    }

    public List<Interpolator1DCubicSpline> getVegaInterp() {
        return vegaInterp;
    }

    public List<Interpolator1DCubicSpline> getThetaInterp() {
        return thetaInterp;
    }
}
