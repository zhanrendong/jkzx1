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
import tech.tongyu.bct.quant.library.common.*;
import tech.tongyu.bct.quant.library.common.impl.*;
import tech.tongyu.bct.quant.library.market.curve.DiscountingCurve;
import tech.tongyu.bct.quant.library.market.vol.ImpliedVolSurface;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.product.basket.RatioVanillaEuropean;
import tech.tongyu.bct.quant.library.pricer.BasketBlackPricer;
import tech.tongyu.bct.quant.library.pricer.GenericPricer;
import tech.tongyu.bct.quant.library.pricer.PricerUtils;
import tech.tongyu.bct.quant.service.cache.QuantlibObjectCache;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OptionPricer {
    @BctMethodInfo(tags = {BctApiTagEnum.ExcelDelayed})
    public QuantlibCalcResults qlSingleUnderlyerOptionCalc(
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString, required = false) List<String> requests,
            @BctMethodArg(excelType = BctExcelTypeEnum.Handle) String option,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double, required = false) Number underlyerPrice,
            @BctMethodArg(required = false) String volSurface,
            @BctMethodArg(required = false) String discountingCurve,
            @BctMethodArg(required = false) String dividendCurve,
            @BctMethodArg(required = false) String pricerName,
            @BctMethodArg(excelType = BctExcelTypeEnum.Json, required = false) Map<String, Object> pricerParams,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime, required = false) String valuationDateTime,
            @BctMethodArg(required = false) String timezone
    ) {
        List<CalcTypeEnum> reqs = EnumUtils.getPricingRequests(requests);
        ZonedDateTime val = DateTimeUtils.parse(valuationDateTime, timezone);
        QuantlibSerializableObject priceable = QuantlibObjectCache.Instance.getMayThrow(option);
        if (!(priceable instanceof Priceable)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, String.format("quantlib: %s 不是可定价的结构", option));
        }
        QuantlibSerializableObject vs = null;
        if (!Objects.isNull(volSurface)) {
            vs = QuantlibObjectCache.Instance.getMayThrow(volSurface);
            if (!(vs instanceof ImpliedVolSurface)) {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("quantlib: %s 不是波动率曲面", volSurface));
            }
        }
        QuantlibSerializableObject yc = null;
        if (!Objects.isNull(discountingCurve)) {
            yc = QuantlibObjectCache.Instance.getMayThrow(discountingCurve);
            if (!(yc instanceof DiscountingCurve)) {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("quantlib: %s 不是利率曲线", discountingCurve));
            }
        }
        DiscountingCurve dividend = null;
        if (!Objects.isNull(dividendCurve)) {
            QuantlibSerializableObject dc = QuantlibObjectCache.Instance.getMayThrow(dividendCurve);
            if (!(dc instanceof DiscountingCurve)) {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("quantlib: %s 不是利率曲线", dividendCurve));
            }
            dividend = (DiscountingCurve) dc;
        }
        // parse pricing params
        QuantPricerSpec spec;
        if (Objects.isNull(pricerName)) {
            if (!Objects.isNull(dividend)) {
                spec = new BlackScholesAnalyticPricer(new BlackAnalyticPricerParams());
            } else {
                spec = new Black76AnalyticPricer(new BlackAnalyticPricerParams());
            }
        } else {
            spec = PricerUtils.parsePricer(pricerName, pricerParams);
        }
        return GenericPricer.qlSingleAssetOptionCalc(reqs,
                (Priceable) priceable, val.toLocalDateTime(),
                Objects.isNull(underlyerPrice) ? null : underlyerPrice.doubleValue(),
                (ImpliedVolSurface) vs, (DiscountingCurve) yc, dividend, spec);
    }

    @BctMethodInfo(excelType = BctExcelTypeEnum.DataDict, tags = {BctApiTagEnum.ExcelDelayed})
    public QuantlibCalcResults qlBasketUnderlyerOptionCalc(
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString, required = false) List<String> requests,
            @BctMethodArg(excelType = BctExcelTypeEnum.Handle) String option,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble) List<Number> underlyerPrices,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString) List<String> volSurfaces,
            @BctMethodArg String discountingCurve,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString, required = false) List<String> dividendCurves,
            @BctMethodArg(excelType = BctExcelTypeEnum.Matrix) List<List<Number>> correlations,
            @BctMethodArg(required = false) String pricerName,
            @BctMethodArg(excelType = BctExcelTypeEnum.Json, required = false) Map<String, Object> pricerParams,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime, required = false) String valuationDateTime,
            @BctMethodArg(required = false) String timezone
    ) {
        List<CalcTypeEnum> reqs = EnumUtils.getPricingRequests(requests);
        ZonedDateTime val = DateTimeUtils.parse(valuationDateTime, timezone);
        QuantlibSerializableObject priceable = QuantlibObjectCache.Instance.getMayThrow(option);
        if (!(priceable instanceof Priceable)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, String.format("quantlib: %s 不是可定价的结构", option));
        }
        /*if (!(priceable instanceof RatioVanillaEuropean)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, String.format("quantlib: 结构 %s 尚未支持", option));
        }*/
        List<ImpliedVolSurface> volSurfaceList = volSurfaces.stream()
                .map(v -> (ImpliedVolSurface)QuantlibObjectCache.Instance.get(v))
                .collect(Collectors.toList());
        DiscountingCurve yc = (DiscountingCurve)QuantlibObjectCache.Instance.getMayThrow(discountingCurve);
        List<DiscountingCurve> dividendCurveList = dividendCurves.stream()
                .map(c -> (DiscountingCurve)QuantlibObjectCache.Instance.get(c))
                .collect(Collectors.toList());
        QuantPricerSpec spec;
        if (Objects.isNull(pricerName)) {
            if (!Objects.isNull(dividendCurves)) {
                spec = new BlackScholesAnalyticPricer(new BlackAnalyticPricerParams());
            } else {
                spec = new Black76AnalyticPricer(new BlackAnalyticPricerParams());
            }
        } else {
            spec = PricerUtils.parsePricer(pricerName, pricerParams);
        }
        return GenericPricer.multiAssetOptionCalc(reqs, (Priceable) priceable,
                val.toLocalDateTime(), DoubleUtils.forceDouble(underlyerPrices),
                volSurfaceList, yc, dividendCurveList, DoubleUtils.numberToDoubleMatrix(correlations), spec);
    }
}
