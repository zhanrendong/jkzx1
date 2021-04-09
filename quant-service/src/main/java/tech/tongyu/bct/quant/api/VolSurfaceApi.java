package tech.tongyu.bct.quant.api;

import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.quant.builder.VolSurfaceBuilder;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.common.QuantlibSerializableObject;
import tech.tongyu.bct.quant.library.market.vol.ImpliedVolSurface;
import tech.tongyu.bct.quant.library.market.vol.LocalVolSurface;
import tech.tongyu.bct.quant.library.market.vol.impl.AtmPwcVolSurface;
import tech.tongyu.bct.quant.library.market.vol.impl.DupireLocalVolSurface;
import tech.tongyu.bct.quant.library.market.vol.impl.InterpolatedStrikeVolSurface;
import tech.tongyu.bct.quant.service.cache.QuantlibObjectCache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VolSurfaceApi {
    @BctMethodInfo(
            excelType = BctExcelTypeEnum.Handle,
            tags = {BctApiTagEnum.Excel}
    )
    public String qlVolConstCreate(
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String val,
            @BctMethodArg double vol,
            @BctMethodArg double daysInYear,
            @BctMethodArg(required = false) String id) {
        LocalDateTime t = DateTimeUtils.parseToLocalDateTime(val);
        AtmPwcVolSurface vs = AtmPwcVolSurface.flat(t, 100, vol, daysInYear);
        return QuantlibObjectCache.Instance.put(vs,id);
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public double qlVolSurfaceImpliedVolGet(
            @BctMethodArg(excelType = BctExcelTypeEnum.Handle) String volSurface,
            @BctMethodArg double forward,
            @BctMethodArg double strike,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String expiry) {
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        ImpliedVolSurface vs = (ImpliedVolSurface) QuantlibObjectCache.Instance.getMayThrow(volSurface);
        return vs.iv(forward, strike, exp);
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlVolSurfaceInterpolatedCreate(
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String valuationDate,
            @BctMethodArg double spot,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDateTime) List<String> expirationDates,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble) List<Number> strikes,
            @BctMethodArg(excelType = BctExcelTypeEnum.Matrix) List<List<Number>> vols,
            @BctMethodArg(excelType = BctExcelTypeEnum.Json, required = false) Map<String, Object> config,
            @BctMethodArg(required = false) String id
    ) {
        LocalDateTime val = DateTimeUtils.parseToLocalDateTime(valuationDate);
        List<LocalDate> expiries = expirationDates.stream()
                .map(DateTimeUtils::parseToLocalDate)
                .collect(Collectors.toList());
        if (vols.size() != expiries.size())
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("期限数目 %s 与波动率行数 %s 不符", expiries.size(), vols.size()));

        if (vols.get(0).size() != strikes.size())
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("行权价数目 %s 与波动率列数 %s 不符", vols.get(0).size(), strikes.size()));
        VolSurfaceBuilder.InterpolatedVolSurfaceConfig volSurfaceConfig;
        if (Objects.isNull(config)) {
            volSurfaceConfig = new VolSurfaceBuilder.InterpolatedVolSurfaceConfig();
        } else {
            volSurfaceConfig = JsonUtils.mapper.convertValue(config,
                    VolSurfaceBuilder.InterpolatedVolSurfaceConfig.class);
        }
        ImpliedVolSurface vs = VolSurfaceBuilder.createInterpolatedStrikeSurface(val, spot,
                expiries, strikes.stream().map(Number::doubleValue).collect(Collectors.toList()),
                DoubleUtils.numberToDoubleMatrix(vols), volSurfaceConfig);
        return QuantlibObjectCache.Instance.put(vs, id);
    }

    @BctMethodInfo(excelType = BctExcelTypeEnum.Handle, tags = {BctApiTagEnum.Excel})
    public String qlVolSurfaceBump(
            @BctMethodArg(excelType = BctExcelTypeEnum.Handle) String volSurface,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number amount,
            @BctMethodArg(required = false) String id
    ) {
        ImpliedVolSurface vs = (ImpliedVolSurface) QuantlibObjectCache.Instance.getMayThrow(volSurface);
        ImpliedVolSurface bumped = vs.bump(amount.doubleValue());
        return QuantlibObjectCache.Instance.put(bumped, id);
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlLocalVolSurfaceFromImpliedVolSurface(
            @BctMethodArg(excelType = BctExcelTypeEnum.Handle) String impliedVolSurface,
            @BctMethodArg(required = false) String id
    ) {
        ImpliedVolSurface vs = QuantlibObjectCache.Instance.getWithTypeMayThrow(impliedVolSurface,
                ImpliedVolSurface.class);
        LocalVolSurface lv = new DupireLocalVolSurface(vs);
        return QuantlibObjectCache.Instance.put(lv, id);
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public double qlVolSurfaceLocalVolGet(
            @BctMethodArg(excelType = BctExcelTypeEnum.Handle) String localVolSurface,
            @BctMethodArg double futureSpot,
            @BctMethodArg double forward,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String expiry
    ) {
        LocalVolSurface lv = QuantlibObjectCache.Instance.getWithTypeMayThrow(localVolSurface, LocalVolSurface.class);
        LocalDateTime T = DateTimeUtils.parseToLocalDateTime(expiry);
        return lv.localVol(futureSpot, forward, T);
    }
}
