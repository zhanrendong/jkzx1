package tech.tongyu.bct.quant.api;

import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.builder.CurveBootstrapper;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.common.EnumUtils;
import tech.tongyu.bct.quant.library.financial.date.DayCountBasis;
import tech.tongyu.bct.quant.library.market.curve.DiscountingCurve;
import tech.tongyu.bct.quant.library.market.curve.impl.PwlfDiscountingCurve;
import tech.tongyu.bct.quant.service.cache.QuantlibObjectCache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class CurveApi {
    @BctMethodInfo(
            excelType = BctExcelTypeEnum.Handle,
            tags = {BctApiTagEnum.Excel}
    )
    public String qlCurveConstCreate(
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String val,
            @BctMethodArg double r,
            @BctMethodArg double daysInYear,
            @BctMethodArg(required = false) String id
    ) {
        LocalDateTime t = DateTimeUtils.parseToLocalDateTime(val);
        PwlfDiscountingCurve c = PwlfDiscountingCurve.flat(t, r, daysInYear);
        return QuantlibObjectCache.Instance.put(c, id);
    }

    @BctMethodInfo(excelType = BctExcelTypeEnum.Handle, tags = {BctApiTagEnum.Excel})
    public String qlCurveFromSpotRates(
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String valuationDate,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDateTime) List<String> expiries,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble) List<Number> rates
    ) {
        double spotDf = 1.0;
        // compute dfs from spot date
        LocalDateTime[] dfTimes = new LocalDateTime[expiries.size() + 1];
        double dfs[] = new double[expiries.size()+1];
        LocalDateTime val = DateTimeUtils.parseToLocalDateTime(valuationDate);
        dfTimes[0] = val;
        dfs[0] = spotDf;
        for (int i = 1; i < dfs.length; ++i) {
            double r = rates.get(i-1).doubleValue();
            LocalDateTime expiry = DateTimeUtils.parseToLocalDateTime(expiries.get(i-1));
            double dcf = DateTimeUtils.days(val, expiry)
                    / DateTimeUtils.DAYS_IN_YEAR ;
            dfs[i] = spotDf * FastMath.exp(-dcf*r);
            dfTimes[i] = expiry;
        }
        PwlfDiscountingCurve curve = PwlfDiscountingCurve.pwcfFromDfs(val, dfTimes, dfs);
        return QuantlibObjectCache.Instance.put(curve);
    }

    @BctMethodInfo(excelType = BctExcelTypeEnum.Handle, tags = {BctApiTagEnum.Excel})
    public String qlCurveBump(
            @BctMethodArg(excelType = BctExcelTypeEnum.Handle) String curve,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number amount,
            @BctMethodArg(required = false) String id) {
        DiscountingCurve yc = (DiscountingCurve) QuantlibObjectCache.Instance.getMayThrow(curve);
        DiscountingCurve bumped = yc.bump(amount.doubleValue());
        return QuantlibObjectCache.Instance.put(bumped, id);
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public double qlDf(
            @BctMethodArg(excelType = BctExcelTypeEnum.Handle) String curve,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String t
    ) {
        LocalDateTime d = DateTimeUtils.parseToLocalDateTime(t);
        DiscountingCurve yc = (DiscountingCurve) QuantlibObjectCache.Instance.getMayThrow(curve);
        return yc.df(d);
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlIrCurveBootstrapShibor(
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime, required = false) String valuationDate,
            @BctMethodArg(required = false) String frequency,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString) List<String> tenors,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble) List<Number> swapRates,
            @BctMethodArg(required = false) String basis,
            @BctMethodArg(required = false) String id
    ) {
        LocalDate val = DateTimeUtils.parseToLocalDate(valuationDate);
        DayCountBasis dayCountBasis = Objects.isNull(basis) ?
                DayCountBasis.ACT365 : EnumUtils.fromString(basis, DayCountBasis.class);
        DiscountingCurve curve = CurveBootstrapper.bootstrap(val, val,
                tenors, DoubleUtils.forceDouble(swapRates),
                Objects.isNull(frequency) ? "3M" : frequency, dayCountBasis);
        return QuantlibObjectCache.Instance.put(curve, id);
    }
}
