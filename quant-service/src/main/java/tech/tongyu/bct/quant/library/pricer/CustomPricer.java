package tech.tongyu.bct.quant.library.pricer;

import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.impl.BlackResults;
import tech.tongyu.bct.quant.library.market.custom.ModelXY;

import java.time.LocalDateTime;
import java.util.List;

public class CustomPricer {
    public static BlackResults modelXYCalc(List<CalcTypeEnum> requests,
                                           ModelXY modelXY,
                                           double underlyerPrice,
                                           LocalDateTime valuationDateTime) {
        BlackResults results = new BlackResults();
        for (CalcTypeEnum req : requests) {
            switch (req) {
                case PRICE:
                case DELTA:
                case GAMMA:
                case VEGA:
                case THETA:
                case RHO_R:
                    results.setResult(req, modelXY.calc(req, underlyerPrice, valuationDateTime));
                    break;
            }
        }
        results.setUnderlyerPrice(underlyerPrice);
        return results;
    }
}
