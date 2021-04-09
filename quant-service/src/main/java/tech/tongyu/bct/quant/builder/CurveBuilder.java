package tech.tongyu.bct.quant.builder;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.library.market.curve.DiscountingCurve;
import tech.tongyu.bct.quant.library.market.curve.impl.PwlfDiscountingCurve;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CurveBuilder {
    public static DiscountingCurve createCurveFromRates(LocalDateTime val,
                                                        List<LocalDate> expiries, List<Double> rates) {
        double spotDf = 1.0;
        // compute dfs from spot date
        LocalDateTime[] dfTimes = new LocalDateTime[expiries.size() + 1];
        double dfs[] = new double[expiries.size()+1];
        dfTimes[0] = val;
        dfs[0] = spotDf;
        for (int i = 1; i < dfs.length; ++i) {
            double r = rates.get(i-1);
            double dcf = DateTimeUtils.days(val, expiries.get(i-1).atStartOfDay())
                    / DateTimeUtils.DAYS_IN_YEAR ;
            dfs[i] = spotDf * FastMath.exp(-dcf*r);
            dfTimes[i] = expiries.get(i-1).atStartOfDay();
        }
        return PwlfDiscountingCurve.pwcfFromDfs(val, dfTimes, dfs);
    }
}
