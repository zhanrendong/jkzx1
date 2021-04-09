package tech.tongyu.bct.quant.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.math3.util.FastMath;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.request.JsonRpcRequest;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.quant.library.financial.date.Holidays;
import tech.tongyu.bct.quant.library.market.curve.impl.PwlfDiscountingCurve;
import tech.tongyu.bct.quant.library.market.vol.VolCalendar;
import tech.tongyu.bct.quant.library.market.vol.impl.AtmPwcVolSurface;
import tech.tongyu.bct.quant.library.market.vol.impl.InterpolatedStrikeVolSurface;
import tech.tongyu.bct.quant.library.numerics.interp.ExtrapTypeEnum;
import tech.tongyu.bct.quant.library.numerics.interp.Interpolator1DCubicSpline;
import tech.tongyu.bct.quant.service.QuantService;
import tech.tongyu.bct.quant.service.cache.QuantlibObjectCache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Service
@Primary
public class QuantServiceLocalImpl implements QuantService {

    @Override
    public Object execute(JsonRpcRequest task) {
        throw new CustomException(ErrorCode.NOT_IMPLEMENTED, "该版本quantlib实现不支持远程调用");
    }

    @Override
    public List<Object> parallel(List<JsonRpcRequest> tasks) {
        throw new CustomException(ErrorCode.NOT_IMPLEMENTED, "该版本quantlib实现不支持远程调用");
    }

    @Override
    public String qlVolSurfaceAtmPwcCreate(LocalDate val, double spot, List<LocalDate> expiries,
                                    List<Double> vols) {
        TreeMap<Double, AtmPwcVolSurface.Segment> segs = new TreeMap<>();
        ArrayList<Double> taus = new ArrayList<>();
        taus.add(0.0);
        ArrayList<Double> vars = new ArrayList<>();
        vars.add(0.0);
        for (int i = 0; i < expiries.size(); ++i) {
            if (!expiries.get(i).isAfter(val))
                continue;
            double days = DateTimeUtils.days(val.atStartOfDay(), expiries.get(i).atStartOfDay());
            taus.add(days);
            double var = vols.get(i) * vols.get(i) * days / DateTimeUtils.DAYS_IN_YEAR;
            vars.add(var);
        }
        for (int i = 1 ; i < taus.size(); ++i) {
            double tau = taus.get(i) - taus.get(i-1);
            double dailyVar = (vars.get(i) - vars.get(i-1)) / tau;
            AtmPwcVolSurface.Segment seg = new AtmPwcVolSurface.Segment();
            seg.localVol = FastMath.sqrt(dailyVar);
            seg.varSoFar = vars.get(i-1);
            segs.put(taus.get(i-1), seg);
        }
        // the last segment represents local volatility after the last expiry.
        // its local volatility is the same as the one before the last expiry.
        AtmPwcVolSurface.Segment seg = new AtmPwcVolSurface.Segment();
        seg.localVol = segs.lastEntry().getValue().localVol;
        seg.varSoFar = vars.get(vars.size() - 1);
        segs.put(taus.get(taus.size() - 1), seg);
        AtmPwcVolSurface vs = new AtmPwcVolSurface(val.atStartOfDay(), spot, segs, DateTimeUtils.DAYS_IN_YEAR);
        return QuantlibObjectCache.Instance.put(vs);
    }

    @Override
    public String qlVolSurfaceInterpolatedStrikeCreate(LocalDate val, double spot, List<LocalDate> expiries,
            List<Double> strikes, List<List<Double>> vols, double daysInYear) {
        if (vols.size() != expiries.size())
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("期限数目 %s 与波动率行数 %s 不符", expiries.size(), vols.size()));
        if (vols.get(0).size() != strikes.size())
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("行权价数目 %s 与波动率列数 %s 不符", vols.get(0).size(), strikes.size()));
        TreeMap<LocalDateTime, Interpolator1DCubicSpline> varInterp = new TreeMap<>();
        double[] interpStrikes = strikes.stream().mapToDouble(Double::doubleValue).toArray();
        for (int i = 0; i < expiries.size(); i++) {
            LocalDateTime e = expiries.get(i).atStartOfDay();
            double t = DateTimeUtils.days(val.atStartOfDay(), e) / DateTimeUtils.DAYS_IN_YEAR;
            double[] vars = new double[strikes.size()];
            for (int j = 0; j < strikes.size(); j++)
                vars[j] = vols.get(i).get(j) * vols.get(i).get(j) * t;
            varInterp.put(e, new Interpolator1DCubicSpline(interpStrikes, vars,
                    ExtrapTypeEnum.EXTRAP_1D_FLAT, ExtrapTypeEnum.EXTRAP_1D_FLAT));
        }
        InterpolatedStrikeVolSurface vs = new InterpolatedStrikeVolSurface(val.atStartOfDay(), varInterp,
                VolCalendar.none(), spot, daysInYear);
        return QuantlibObjectCache.Instance.put(vs);
    }

    @Override
    public String qlCurveFromSpotRates(LocalDate val, List<LocalDate> expiries, List<Double> rates) {
        double spotDf = 1.0;
        // compute dfs from spot date
        LocalDateTime[] dfTimes = new LocalDateTime[expiries.size() + 1];
        double dfs[] = new double[expiries.size()+1];
        dfTimes[0] = val.atStartOfDay();
        dfs[0] = spotDf;
        for (int i = 1; i < dfs.length; ++i) {
            double r = rates.get(i-1);
            double dcf = DateTimeUtils.days(val.atStartOfDay(), expiries.get(i-1).atStartOfDay())
                    / DateTimeUtils.DAYS_IN_YEAR ;
            dfs[i] = spotDf * FastMath.exp(-dcf*r);
            dfTimes[i] = expiries.get(i-1).atStartOfDay();
        }
        PwlfDiscountingCurve curve = PwlfDiscountingCurve.pwcfFromDfs(val.atStartOfDay(), dfTimes, dfs);
        return QuantlibObjectCache.Instance.put(curve);
    }

    @Override
    public JsonNode qlObjectInfo(String handle) {
        return qlObjectSerialize(handle);
    }

    @Override
    public JsonNode qlObjectSerialize(String handle) {
        return JsonUtils.mapper.valueToTree(QuantlibObjectCache.Instance.get(handle));
    }

    @Override
    public String qlObjectDeserialize(String id, JsonNode objJson) {
        QuantlibObjectCache.Instance.create(id, objJson);
        return id;
    }

    @Override
    public void createCalendar(String name, List<LocalDate> holidays) {
        Holidays.Instance.replace(name, holidays);
    }

    @Override
    public void deleteCalendar(String name) {
        Holidays.Instance.deleteAll(name);
    }

    @Override
    public void mergeHolidays(String name, List<LocalDate> holidays) {
        Holidays.Instance.mergeHolidays(name, holidays);
    }

    @Override
    public void removeHolidays(String name, List<LocalDate> holidays) {
        Holidays.Instance.removeHolidays(name, holidays);
    }
}
