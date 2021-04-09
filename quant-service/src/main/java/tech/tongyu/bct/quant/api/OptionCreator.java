package tech.tongyu.bct.quant.api;

import io.vavr.Tuple2;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.common.EnumUtils;
import tech.tongyu.bct.quant.library.common.QuantlibSerializableObject;
import tech.tongyu.bct.quant.library.financial.date.BusinessDayAdjustment;
import tech.tongyu.bct.quant.library.financial.date.DateCalcUtils;
import tech.tongyu.bct.quant.library.financial.date.DateRollEnum;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.cash.CashFlows;
import tech.tongyu.bct.quant.library.priceable.cash.CashPayment;
import tech.tongyu.bct.quant.library.priceable.commodity.*;
import tech.tongyu.bct.quant.library.priceable.common.flag.*;
import tech.tongyu.bct.quant.library.priceable.common.product.CustomProductModelXY;
import tech.tongyu.bct.quant.library.priceable.common.product.basket.RatioVanillaEuropean;
import tech.tongyu.bct.quant.library.priceable.common.product.basket.SpreadDigitalCash;
import tech.tongyu.bct.quant.library.priceable.common.product.basket.SpreadKnockOutTerminal;
import tech.tongyu.bct.quant.library.priceable.common.product.basket.SpreadVanillaEuropean;
import tech.tongyu.bct.quant.library.priceable.equity.*;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.service.cache.QuantlibObjectCache;

import javax.persistence.EnumType;
import javax.persistence.Tuple;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class OptionCreator {
    private Object createUnderlyer(String instrumentId, ExchangeListed.InstrumentTypeEnum instrumentTypeEnum) {
        switch (instrumentTypeEnum) {
            case EQUITY_STOCK:
                return new EquityStock(instrumentId);
            case EQUITY_INDEX:
                return new EquityIndex(instrumentId);
            case EQUITY_INDEX_FUTURES:
                return new EquityIndexFutures(instrumentId);
            case COMMODITY_FUTURES:
                return new CommodityFutures(instrumentId);
            case COMMODITY_SPOT:
                return new CommoditySpot(instrumentId);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 不支持的标的类型");
        }
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlUnderlyerCreate(
            @BctMethodArg String underlyer,
            @BctMethodArg(required = false) String underlyerType,
            @BctMethodArg(required = false)String id
    ) {
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        Object instrument = createUnderlyer(underlyer, instrumentTypeEnum);
        return QuantlibObjectCache.Instance.put((QuantlibSerializableObject) instrument, id);
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlCashPaymentCreate(
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String paymentDate,
            @BctMethodArg double amount,
            @BctMethodArg(required = false) String currency,
            @BctMethodArg(required = false) String id
    ) {
        CashPayment payment = new CashPayment(DateTimeUtils.parseToLocalDate(paymentDate), amount);
        return QuantlibObjectCache.Instance.put(payment, id);
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlCashFlowsCreate(
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDateTime) List<String> paymentDates,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble) List<Number> payments,
            @BctMethodArg(required = false) String currency,
            @BctMethodArg(required = false) String id
    ) {
        List<LocalDate> ts = paymentDates.stream().map(DateTimeUtils::parseToLocalDate).collect(Collectors.toList());
        List<Double> ps = DoubleUtils.forceDouble(payments);
        List<CashPayment> cashPayments = IntStream.range(0, ts.size())
                .mapToObj(i -> new CashPayment( ts.get(i), ps.get(i)))
                .collect(Collectors.toList());
        CashFlows cashFlows = new CashFlows(cashPayments);
        return QuantlibObjectCache.Instance.put(cashFlows, id);
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlForwardCreate(
            @BctMethodArg(required = false) String underlyer,
            @BctMethodArg(required = false) String underlyerType,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number strike,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime, required = false) String delivery,
            @BctMethodArg(required = false) String id
    ) {
        LocalDateTime expirationDateTime = DateTimeUtils.parseToLocalDateTime(expiry);
        LocalDate deliveryDate = Objects.isNull(delivery) ? expirationDateTime.toLocalDate()
                : DateTimeUtils.parseToLocalDate(delivery);
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT" : underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        Object instrument = createUnderlyer(instrumentId, instrumentTypeEnum);
        switch (instrumentTypeEnum) {
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityForward<>((EquityStock) instrument, strike.doubleValue(), expirationDateTime,
                                deliveryDate), id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityForward<>((EquityIndex) instrument, strike.doubleValue(), expirationDateTime,
                                deliveryDate), id);
            case COMMODITY_SPOT:
                return QuantlibObjectCache.Instance.put(
                        new CommodityForward<>((CommoditySpot) instrument, strike.doubleValue(), expirationDateTime,
                                deliveryDate), id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 不支持的标的类型 " + underlyerType);
        }
    }

    @BctMethodInfo(excelType = BctExcelTypeEnum.Handle, tags = {BctApiTagEnum.Excel})
    public String qlOptionCustomModelXYCreate(
            @BctMethodArg String underlyerInstrument,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg(required = false) String id
    ) {
        return QuantlibObjectCache.Instance.put(new CustomProductModelXY(underlyerInstrument,
                DateTimeUtils.parseToLocalDateTime(expiry)), id);
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlOptionVanillaEuropeanCreate(
            // @BctMethodArg(required = false) String assetClass,
            @BctMethodArg(required = false) String underlyer,
            @BctMethodArg(required = false) String underlyerType,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number strike,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg String optionType,
            @BctMethodArg(required = false) String id
    ) {
        // AssetClass.AssetClassEnum assetClassEnum = AssetClass.AssetClassEnum.valueOf(assetClass.toUpperCase());
        OptionTypeEnum optionTypeEnum = OptionTypeEnum.valueOf(optionType.toUpperCase());
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT" : underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        Object instrument = createUnderlyer(instrumentId, instrumentTypeEnum);
        switch (instrumentTypeEnum) {
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityVanillaEuropean<>((EquityStock) instrument, strike.doubleValue(), exp,
                                optionTypeEnum), id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityVanillaEuropean<>((EquityIndexFutures) instrument, strike.doubleValue(), exp,
                                optionTypeEnum), id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityVanillaEuropean<>((EquityIndex) instrument, strike.doubleValue(), exp,
                                optionTypeEnum), id);
            case COMMODITY_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new CommodityVanillaEuropean<>((CommodityFutures) instrument, strike.doubleValue(), exp,
                                optionTypeEnum), id);
            case COMMODITY_SPOT:
                return QuantlibObjectCache.Instance.put(
                        new CommodityVanillaEuropean<>((CommoditySpot) instrument, strike.doubleValue(), exp,
                                optionTypeEnum), id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 不支持的标的类型 " + underlyerType);
        }
    }

    @BctMethodInfo(tags = BctApiTagEnum.Excel)
    public String
    qlOptionVanillaAmericanCreate(
            @BctMethodArg (required = false) String underlyer,
            @BctMethodArg (required = false) String underlyerType,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number strike,
            @BctMethodArg (excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg String optionType,
            @BctMethodArg (required = false) String id
    ){
        String instrumentId= Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT" : underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        OptionTypeEnum optionTypeEnum = OptionTypeEnum.valueOf(optionType.toUpperCase());
        Object instrument = createUnderlyer(instrumentId,instrumentTypeEnum);
        switch (instrumentTypeEnum) {
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityVanillaAmerican<>((EquityStock) instrument,strike.doubleValue(),exp,optionTypeEnum),id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityVanillaAmerican<>((EquityIndexFutures) instrument,strike.doubleValue(),exp,optionTypeEnum)
                        ,id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityVanillaAmerican<>((EquityIndex) instrument,strike.doubleValue(),exp,optionTypeEnum),id);
            case COMMODITY_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new CommodityVanillaAmerican<>((CommodityFutures) instrument,strike.doubleValue(),exp,optionTypeEnum)
                        ,id);
            case COMMODITY_SPOT:
                return QuantlibObjectCache.Instance.put(
                        new CommodityVanillaAmerican<>((CommoditySpot) instrument,strike.doubleValue(),exp,optionTypeEnum)
                        ,id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 不支持的标的类型 " + underlyerType);
        }
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlOptionAsianCreate(
            @BctMethodArg (required = false) String underlyer,
            @BctMethodArg (required = false) String underlyerType,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number strike,
            @BctMethodArg (excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg String optionType,
            @BctMethodArg (excelType = BctExcelTypeEnum.ArrayDateTime) List<String> observationDates,
            @BctMethodArg (excelType = BctExcelTypeEnum.ArrayDouble) List<Number> weights,
            @BctMethodArg (excelType = BctExcelTypeEnum.ArrayDouble) List<Number> fixings,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number daysInYear,
            @BctMethodArg (required = false) String id
    ){
        OptionTypeEnum optionTypeEnum = OptionTypeEnum.valueOf(optionType.toUpperCase());
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT": underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects. isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        Object instrument = createUnderlyer(instrumentId, instrumentTypeEnum);
        switch (instrumentTypeEnum) {
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityAsianFixedStrikeArithmetic<>((EquityStock) instrument, exp,
                                optionTypeEnum, strike.doubleValue(),
                                observationDates.stream()
                                        .map(DateTimeUtils::parseToLocalDateTime)
                                        .collect(Collectors.toList()),
                                DoubleUtils.forceDouble(weights),
                                DoubleUtils.forceDouble(fixings),daysInYear.doubleValue()), id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityAsianFixedStrikeArithmetic<>((EquityIndexFutures) instrument, exp,
                                optionTypeEnum, strike.doubleValue(),
                                observationDates.stream()
                                        .map(DateTimeUtils::parseToLocalDateTime)
                                        .collect(Collectors.toList()),
                                DoubleUtils.forceDouble(weights),
                                DoubleUtils.forceDouble(fixings),daysInYear.doubleValue()),id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityAsianFixedStrikeArithmetic<>((EquityIndex) instrument,exp,
                                optionTypeEnum, strike.doubleValue(),
                                observationDates.stream()
                                        .map(DateTimeUtils::parseToLocalDateTime)
                                        .collect(Collectors.toList()),
                                DoubleUtils.forceDouble(weights),
                                DoubleUtils.forceDouble(fixings),daysInYear.doubleValue()),id);
            case COMMODITY_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new CommodityAsianFixedStrikeArithmetic<>((CommodityFutures) instrument,exp,
                                optionTypeEnum, strike.doubleValue(),
                                observationDates.stream()
                                        .map(DateTimeUtils::parseToLocalDateTime).collect(Collectors.toList()),
                                DoubleUtils.forceDouble(weights),
                                DoubleUtils.forceDouble(fixings),daysInYear.doubleValue()),id);
            case COMMODITY_SPOT:
                return QuantlibObjectCache.Instance.put(
                        new CommodityAsianFixedStrikeArithmetic<>((CommoditySpot) instrument, exp,
                                optionTypeEnum, strike.doubleValue(),
                                observationDates.stream().map(DateTimeUtils::parseToLocalDateTime)
                                        .collect(Collectors.toList()),
                                DoubleUtils.forceDouble(weights),
                                DoubleUtils.forceDouble(fixings), daysInYear.doubleValue()), id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 不支持的标的类型" + underlyerType);
        }
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlOptionDigitalCashCreate(
            // @BctMethodArg(required = false) String assetClass,
            @BctMethodArg(required = false) String underlyer,
            @BctMethodArg(required = false) String underlyerType,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number strike,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg String optionType,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number payment,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime, required = false) String deliveryDate,
            @BctMethodArg(required = false) String id
    ) {
        // AssetClass.AssetClassEnum assetClassEnum = AssetClass.AssetClassEnum.valueOf(assetClass.toUpperCase());
        OptionTypeEnum optionTypeEnum = OptionTypeEnum.valueOf(optionType.toUpperCase());
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT" : underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        LocalDate delivery = Objects.isNull(deliveryDate) ?
                exp.toLocalDate() : DateTimeUtils.parseToLocalDate(deliveryDate);
        Object instrument = createUnderlyer(instrumentId, instrumentTypeEnum);
        switch (instrumentTypeEnum) {
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityDigitalCash<>((EquityStock) instrument, strike.doubleValue(), exp, optionTypeEnum,
                                payment.doubleValue(), delivery), id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityDigitalCash<>((EquityIndexFutures) instrument, strike.doubleValue(), exp,
                                optionTypeEnum, payment.doubleValue(), delivery), id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityDigitalCash<>((EquityIndex) instrument, strike.doubleValue(), exp, optionTypeEnum,
                                payment.doubleValue(), delivery), id);
            case COMMODITY_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new CommodityDigitalCash<>((CommodityFutures) instrument, strike.doubleValue(), exp, optionTypeEnum,
                                payment.doubleValue(), delivery), id);
            case COMMODITY_SPOT:
                return QuantlibObjectCache.Instance.put(
                        new CommodityDigitalCash<>((CommoditySpot) instrument, strike.doubleValue(), exp, optionTypeEnum,
                                payment.doubleValue(), delivery), id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 不支持的标的类型 " + underlyerType);
        }
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlOptionDigitalConcaveCreate(
            @BctMethodArg (required = false) String underlyer,
            @BctMethodArg (required = false) String underlyerType,
            @BctMethodArg (excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number lowStrike,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number highStrike,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number payment,
            @BctMethodArg (required = false) String id
    ){
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT" : underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        Object instrument = createUnderlyer(instrumentId, instrumentTypeEnum);
        switch (instrumentTypeEnum){
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityDigitalConcave<>((EquityStock) instrument, exp, lowStrike.doubleValue(),
                                highStrike.doubleValue(), payment.doubleValue()),id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityDigitalConcave<>((EquityIndexFutures) instrument,exp,lowStrike.doubleValue(),
                                highStrike.doubleValue(), payment.doubleValue()),id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityDigitalConcave<>((EquityIndex) instrument,exp,lowStrike.doubleValue(),
                                highStrike.doubleValue(),payment.doubleValue()),id);
            case COMMODITY_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new CommodityDigitalConcave<>((CommodityFutures) instrument,exp,lowStrike.doubleValue(),
                                highStrike.doubleValue(),payment.doubleValue()),id);
            case COMMODITY_SPOT:
                return QuantlibObjectCache.Instance.put(
                        new CommodityDigitalConcave<>((CommoditySpot) instrument, exp,lowStrike.doubleValue(),
                                highStrike.doubleValue(), payment.doubleValue()),id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,"quantlib: 不支持的标的类型" + underlyerType);
        }
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlOptionDigitalConvexCreate(
            @BctMethodArg (required = false) String underlyer,
            @BctMethodArg (required = false) String underlyerType,
            @BctMethodArg (excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number lowStrike,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number highStrike,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number payment,
            @BctMethodArg (required = false) String id
            ){
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT" : underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        Object instrument = createUnderlyer(instrumentId, instrumentTypeEnum);
        switch (instrumentTypeEnum){
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityDigitalConvex<>((EquityStock) instrument,exp,lowStrike.doubleValue(),
                                highStrike.doubleValue(),payment.doubleValue()),id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityDigitalConvex<>((EquityIndexFutures) instrument,exp,lowStrike.doubleValue(),
                                highStrike.doubleValue(), payment.doubleValue()),id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityDigitalConvex<>((EquityIndex) instrument, exp,lowStrike.doubleValue(),
                                highStrike.doubleValue(),payment.doubleValue()),id);
            case COMMODITY_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new CommodityDigitalConvex<>((CommodityFutures) instrument,exp,lowStrike.doubleValue(),
                                highStrike.doubleValue(), payment.doubleValue()),id);
            case COMMODITY_SPOT:
                return QuantlibObjectCache.Instance.put(
                        new CommodityDigitalConvex<>((CommoditySpot) instrument,exp,lowStrike.doubleValue(),
                                highStrike.doubleValue(), payment.doubleValue()),id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,"quantlib: 不支持的标的类型" + underlyerType);
        }

    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlOptionDoubleDigitalCreate(
            @BctMethodArg (required = false) String underlyer,
            @BctMethodArg (required = false) String underlyerType,
            @BctMethodArg String optionType,
            @BctMethodArg (excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number firstStrike,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number firstPayment,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number secondStrike,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number secondPayment,
            @BctMethodArg (required = false) String id
    ){
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT" : underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK:
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        OptionTypeEnum optionTypeEnum = Objects.isNull(optionType) ? OptionTypeEnum.PUT :
                EnumUtils.fromString(optionType,OptionTypeEnum.class);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        Object instrument = createUnderlyer(instrumentId, instrumentTypeEnum);
        switch (instrumentTypeEnum){
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityDoubleDigital<>((EquityStock) instrument,optionTypeEnum,exp,firstStrike.doubleValue(),
                                firstPayment.doubleValue(),secondStrike.doubleValue(),
                                secondPayment.doubleValue()),id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityDoubleDigital<>((EquityIndexFutures) instrument, optionTypeEnum,exp,
                                firstStrike.doubleValue(),firstPayment.doubleValue(),secondStrike.doubleValue(),
                                secondPayment.doubleValue()),id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityDoubleDigital<>((EquityIndex) instrument,optionTypeEnum, exp,
                                firstStrike.doubleValue(), firstPayment.doubleValue(),secondStrike.doubleValue(),
                                secondPayment.doubleValue()),id);
            case COMMODITY_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new CommodityDoubleDigital<>((CommodityFutures) instrument,optionTypeEnum,exp,
                                firstStrike.doubleValue(), firstPayment.doubleValue(),secondStrike.doubleValue(),
                                secondPayment.doubleValue()),id);
            case COMMODITY_SPOT:
                return QuantlibObjectCache.Instance.put(
                        new CommodityDoubleDigital<>((CommoditySpot) instrument,optionTypeEnum,exp,
                                firstStrike.doubleValue(), firstPayment.doubleValue(),secondStrike.doubleValue(),
                                secondPayment.doubleValue()),id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,"quantlib: 不支持的标的类型" + underlyerType);
        }
    }

    @BctMethodInfo(tags = BctApiTagEnum.Excel)
    public String qlOptionDoubleSharkFinContinuousCreate(
            @BctMethodArg (required = false) String underlyer,
            @BctMethodArg (required = false) String underlyerType,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number lowStrike,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number highStrike,
            @BctMethodArg (excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number lowBarrier,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number highBarrier,
            @BctMethodArg String barrierType,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number lowRebate,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number highRebate,
            @BctMethodArg String rebateType,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double,required = false) Number lowParticipationRate,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double,required = false) Number highParticipationRate,
            @BctMethodArg (required = false) String id
    ){
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT" : underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        BarrierTypeEnum barrierTypeEnum = EnumUtils.fromString(barrierType, BarrierTypeEnum.class);
        RebateTypeEnum rebateTypeEnum = EnumUtils.fromString(rebateType, RebateTypeEnum.class);
        Object instrument = createUnderlyer(instrumentId,instrumentTypeEnum);
        switch (instrumentTypeEnum){
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityDoubleSharkFinConinuous<>((EquityStock) instrument, lowStrike.doubleValue()
                                ,highStrike.doubleValue(), exp, lowBarrier.doubleValue(),highBarrier.doubleValue()
                                ,barrierTypeEnum,lowRebate.doubleValue(),highRebate.doubleValue(),rebateTypeEnum,
                                Objects.isNull(lowParticipationRate) ? 1.0 : lowParticipationRate.doubleValue()
                                ,Objects.isNull(highParticipationRate) ? 1.0 : highParticipationRate.doubleValue()
                                ),id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityDoubleSharkFinConinuous<>((EquityIndexFutures) instrument,lowStrike.doubleValue()
                                ,highStrike.doubleValue(), exp,lowBarrier.doubleValue(),highBarrier.doubleValue(),
                                barrierTypeEnum,lowRebate.doubleValue(),highRebate.doubleValue(),rebateTypeEnum,
                                Objects.isNull(lowParticipationRate) ? 1.0 : lowParticipationRate.doubleValue(),
                                Objects.isNull(highParticipationRate) ? 1.0 : highParticipationRate.doubleValue()),
                                id);
            case EQUITY_INDEX:
                 return QuantlibObjectCache.Instance.put(
                         new EquityDoubleSharkFinConinuous<>((EquityIndex) instrument,lowStrike.doubleValue(),
                                 highStrike.doubleValue(),exp, lowBarrier.doubleValue(),highBarrier.doubleValue(),
                                 barrierTypeEnum,lowRebate.doubleValue(),highRebate.doubleValue(),rebateTypeEnum,
                                 Objects.isNull(lowParticipationRate) ? 1.0 : lowParticipationRate.doubleValue(),
                                 Objects.isNull(highParticipationRate) ? 1.0: highParticipationRate.doubleValue()),id);
            case COMMODITY_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new CommodityDoubleSharkFinContinuous<>((CommodityFutures) instrument,lowStrike.doubleValue(),
                                highStrike.doubleValue(), exp,lowBarrier.doubleValue(),highBarrier.doubleValue(),
                                barrierTypeEnum,lowRebate.doubleValue(),highRebate.doubleValue(),rebateTypeEnum,
                                Objects.isNull(lowParticipationRate) ? 1.0: lowParticipationRate.doubleValue(),
                                Objects.isNull(highParticipationRate) ? 1.0: highParticipationRate.doubleValue()),id);
            case COMMODITY_SPOT:
                return QuantlibObjectCache.Instance.put(
                        new CommodityDoubleSharkFinContinuous<>((CommoditySpot) instrument,lowStrike.doubleValue(),
                                highStrike.doubleValue(),exp, lowBarrier.doubleValue(),highBarrier.doubleValue(),
                                barrierTypeEnum,lowRebate.doubleValue(),highRebate.doubleValue(),rebateTypeEnum,
                                Objects.isNull(lowParticipationRate) ? 1.0: lowParticipationRate.doubleValue(),
                                Objects.isNull(highParticipationRate) ? 1.0: highParticipationRate.doubleValue()),id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,"quantlib: 不支持的标的类型" + underlyerType);
        }
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlOptionDoubleSharkFinTerminalCreate(
            @BctMethodArg (required = false) String underlyer,
            @BctMethodArg (required = false) String underlyerType,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number lowStrike,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number highStrike,
            @BctMethodArg (excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number lowBarrier,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number highBarrier,
            @BctMethodArg String barrierType,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number lowRebate,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number highRebate,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double,required = false) Number lowParticipationRate,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double,required = false) Number highParticipationRate,
            @BctMethodArg (required = false) String id
    ){
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT" : underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        BarrierTypeEnum barrierTypeEnum = EnumUtils.fromString(barrierType,BarrierTypeEnum.class);
        Object instrument = createUnderlyer(instrumentId,instrumentTypeEnum);
        switch (instrumentTypeEnum){
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityDoubleSharkFinTerminal<>((EquityStock) instrument, lowStrike.doubleValue(),
                                highStrike.doubleValue(), exp,lowBarrier.doubleValue(),highBarrier.doubleValue(),
                                barrierTypeEnum,lowRebate.doubleValue(),highRebate.doubleValue(),
                                Objects.isNull(lowParticipationRate) ? 1.0: lowParticipationRate.doubleValue(),
                                Objects.isNull(highParticipationRate) ? 1.0: highParticipationRate.doubleValue()),id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityDoubleSharkFinTerminal<>((EquityIndexFutures) instrument,lowStrike.doubleValue(),
                                highStrike.doubleValue(), exp,lowBarrier.doubleValue(),highBarrier.doubleValue(),
                                barrierTypeEnum,lowRebate.doubleValue(),highRebate.doubleValue(),
                                Objects.isNull(lowParticipationRate) ? 1.0 : lowParticipationRate.doubleValue(),
                                Objects.isNull(highParticipationRate) ? 1.0: highParticipationRate.doubleValue()),id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityDoubleSharkFinTerminal<>((EquityIndex) instrument,lowStrike.doubleValue(),
                                highStrike.doubleValue(), exp,lowBarrier.doubleValue(),highBarrier.doubleValue(),
                                barrierTypeEnum,lowRebate.doubleValue(),highRebate.doubleValue(),
                                Objects.isNull(lowParticipationRate) ? 1.0:lowParticipationRate.doubleValue(),
                                Objects.isNull(highParticipationRate) ? 1.0: highParticipationRate.doubleValue()),id);
            case COMMODITY_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new CommodityDoubleSharkFinTerminal<>((CommodityFutures) instrument,lowStrike.doubleValue(),
                                highStrike.doubleValue(), exp,lowBarrier.doubleValue(),highBarrier.doubleValue(),
                                barrierTypeEnum,lowRebate.doubleValue(),highRebate.doubleValue(),
                                Objects.isNull(lowParticipationRate) ? 1.0: lowParticipationRate.doubleValue(),
                                Objects.isNull(highParticipationRate) ? 1.0: highParticipationRate.doubleValue()),id);
            case COMMODITY_SPOT:
                return QuantlibObjectCache.Instance.put(
                        new CommodityDoubleSharkFinTerminal<>((CommoditySpot) instrument,lowStrike.doubleValue(),
                                highStrike.doubleValue(), exp,lowBarrier.doubleValue(),highBarrier.doubleValue(),
                                barrierTypeEnum,lowRebate.doubleValue(),highRebate.doubleValue(),
                                Objects.isNull(lowParticipationRate) ? 1.0: lowParticipationRate.doubleValue(),
                                Objects.isNull(highParticipationRate) ? 1.0: highParticipationRate.doubleValue()),id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,"quantlib: 不支持的标的类型" + underlyerType);
        }
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlOptionAutocallCreate(
            @BctMethodArg(required = false) String underlyer,
            @BctMethodArg(required = false) String underlyerType,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDateTime) List<String> observationDates,
            @BctMethodArg String barrierDirection,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble) List<Number> barriers,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDateTime, required = false) List<String> paymentDates,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble) List<Number> payments,
            @BctMethodArg(excelType = BctExcelTypeEnum.Boolean) boolean finalPayFixed,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime, required = false) String finalPaymentDate,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double, required = false) Number finalPayment,
            @BctMethodArg(required = false) String finalOptionType,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double, required = false) Number finalOptionStrike,
            @BctMethodArg(excelType = BctExcelTypeEnum.Boolean, required = false) Boolean knockedOut,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double, required = false) Number knockedOutPayment,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime, required = false) String knockedOutPaymentDate,
            @BctMethodArg(required = false) String id
    ) {
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT" : underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        Object instrument = createUnderlyer(instrumentId, instrumentTypeEnum);
        BarrierDirectionEnum barrierDirectionEnum = EnumUtils.fromString(barrierDirection, BarrierDirectionEnum.class);
        List<LocalDate> couponPaymentDates = (Objects.isNull(paymentDates) ? observationDates : paymentDates).stream()
                .map(DateTimeUtils::parseToLocalDate).collect(Collectors.toList());
        LocalDate settleDate = DateTimeUtils.parseToLocalDate(Objects.isNull(finalPaymentDate) ? expiry : finalPaymentDate);
        OptionTypeEnum finalOptionTypeEnum = Objects.isNull(finalOptionType) ? OptionTypeEnum.PUT :
                EnumUtils.fromString(finalOptionType, OptionTypeEnum.class);
        Double finalStrike = finalPayFixed ?
                ((finalOptionTypeEnum == OptionTypeEnum.CALL) ? Double.MAX_VALUE : 0.)
                : finalOptionStrike.doubleValue();
        Boolean knocked = Objects.isNull(knockedOut) ? Boolean.FALSE : knockedOut;
        LocalDate knockedOutPayDate = Objects.isNull(knockedOutPaymentDate) ? null
                : DateTimeUtils.parseToLocalDate(knockedOutPaymentDate);
        switch (instrumentTypeEnum) {
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityAutoCall<>((EquityStock) instrument,
                                exp,
                                observationDates.stream()
                                        .map(DateTimeUtils::parseToLocalDate)
                                        .collect(Collectors.toList()),
                                barrierDirectionEnum, DoubleUtils.forceDouble(barriers),
                                couponPaymentDates, DoubleUtils.forceDouble(payments),
                                finalPayFixed, settleDate,
                                Objects.isNull(finalPayment) ? 0.0 : finalPayment.doubleValue(),
                                finalOptionTypeEnum, finalStrike, knocked,
                                Objects.isNull(knockedOutPayment) ? 0.0: knockedOutPayment.doubleValue()
                                , knockedOutPayDate),
                        id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityAutoCall<>((EquityIndexFutures) instrument,
                                exp,
                                observationDates.stream()
                                        .map(DateTimeUtils::parseToLocalDate)
                                        .collect(Collectors.toList()),
                                barrierDirectionEnum, DoubleUtils.forceDouble(barriers),
                                couponPaymentDates, DoubleUtils.forceDouble(payments),
                                finalPayFixed, settleDate,
                                Objects.isNull(finalPayment) ? 0.0 : finalPayment.doubleValue(),
                                finalOptionTypeEnum, finalStrike, knocked,
                                Objects.isNull(knockedOutPayment) ? 0.0 : knockedOutPayment.doubleValue()
                                , knockedOutPayDate),
                        id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityAutoCall<>((EquityIndex) instrument,
                                exp,
                                observationDates.stream()
                                        .map(DateTimeUtils::parseToLocalDate)
                                        .collect(Collectors.toList()),
                                barrierDirectionEnum, DoubleUtils.forceDouble(barriers),
                                couponPaymentDates, DoubleUtils.forceDouble(payments),
                                finalPayFixed, settleDate,
                                Objects.isNull(finalPayment) ? 0.0 : finalPayment.doubleValue(),
                                finalOptionTypeEnum, finalStrike, knocked,
                                Objects.isNull(knockedOutPayment) ? 0.0 : knockedOutPayment.doubleValue()
                                , knockedOutPayDate),
                        id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        "quantlib: Autocall 不支持的标的类型 " + underlyerType);
        }
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlOptionAutocallPhoenixCreate(
            @BctMethodArg(required = false) String underlyer,
            @BctMethodArg(required = false) String underlyerType,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDateTime) List<String> observationDates,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble, required = false) List<Number> observedSpots,
            @BctMethodArg String barrierDirection,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble) List<Number> barriers,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble) List<Number> couponBarriers,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDateTime, required = false) List<String> paymentDates,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble) List<Number> coupons,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String knockInObservationStart,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String knockInObservationEnd,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString) List<String> holidays,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number knockInBarrier,
            @BctMethodArg(excelType = BctExcelTypeEnum.Boolean) boolean knockedIn,
            @BctMethodArg String knockedInOptionType,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number knockedInOptionStrike,
            @BctMethodArg(required = false) String finalPaymentDate,
            @BctMethodArg(required = false) String id
    ) {
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT" : underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        Object instrument = createUnderlyer(instrumentId, instrumentTypeEnum);
        BarrierDirectionEnum barrierDirectionEnum = EnumUtils.fromString(barrierDirection, BarrierDirectionEnum.class);
        List<LocalDate> couponPaymentDates = (Objects.isNull(paymentDates) ? observationDates : paymentDates).stream()
                .map(DateTimeUtils::parseToLocalDate).collect(Collectors.toList());
        List<Double> observed = Objects.isNull(observedSpots) ?
                new ArrayList<>() :
                DoubleUtils.forceDouble(observedSpots);
        List<LocalDate> knockInDates = new ArrayList<>();
        List<Double> knockInBarriers = new ArrayList<>();
        LocalDate start = DateTimeUtils.parseToLocalDate(knockInObservationStart);
        LocalDate end = DateTimeUtils.parseToLocalDate(knockInObservationEnd);
        while (!start.isAfter(end)) {
            knockInDates.add(start);
            knockInBarriers.add(knockInBarrier.doubleValue());
            start = DateCalcUtils.add(start, Period.ofDays(1),
                    DateRollEnum.FORWARD, BusinessDayAdjustment.FOLLOWING, holidays);
        }
        LocalDate settleDate = DateTimeUtils.parseToLocalDate(Objects.isNull(finalPaymentDate) ?
                expiry : finalPaymentDate);
        OptionTypeEnum knockedInOptionTypeEnum = EnumUtils.fromString(knockedInOptionType, OptionTypeEnum.class);
        switch (instrumentTypeEnum) {
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityAutoCallPhoenix<>((EquityStock) instrument,
                                exp,
                                observationDates.stream()
                                        .map(DateTimeUtils::parseToLocalDate)
                                        .collect(Collectors.toList()),
                                observed,
                                barrierDirectionEnum, DoubleUtils.forceDouble(barriers),
                                DoubleUtils.forceDouble(couponBarriers),
                                couponPaymentDates, DoubleUtils.forceDouble(coupons),
                                knockInDates, knockInBarriers,
                                knockedIn,
                                knockedInOptionTypeEnum,
                                knockedInOptionStrike.doubleValue(), settleDate),
                        id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityAutoCallPhoenix<>((EquityIndexFutures) instrument,
                                exp,
                                observationDates.stream()
                                        .map(DateTimeUtils::parseToLocalDate)
                                        .collect(Collectors.toList()),
                                observed,
                                barrierDirectionEnum, DoubleUtils.forceDouble(barriers),
                                DoubleUtils.forceDouble(couponBarriers),
                                couponPaymentDates, DoubleUtils.forceDouble(coupons),
                                knockInDates, knockInBarriers,
                                knockedIn,
                                knockedInOptionTypeEnum,
                                knockedInOptionStrike.doubleValue(), settleDate),
                        id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityAutoCallPhoenix<>((EquityIndex) instrument,
                                exp,
                                observationDates.stream()
                                        .map(DateTimeUtils::parseToLocalDate)
                                        .collect(Collectors.toList()),
                                observed,
                                barrierDirectionEnum, DoubleUtils.forceDouble(barriers),
                                DoubleUtils.forceDouble(couponBarriers),
                                couponPaymentDates, DoubleUtils.forceDouble(coupons),
                                knockInDates, knockInBarriers,
                                knockedIn,
                                knockedInOptionTypeEnum,
                                knockedInOptionStrike.doubleValue(), settleDate),
                        id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        "quantlib: Phoenix Autocall 不支持的标的类型 " + underlyerType);
        }
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlOptionEagleCreate(
            @BctMethodArg (required = false) String underlyer,
            @BctMethodArg (required = false) String underlyetType,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number strike1,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number strike2,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number strike3,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number strike4,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number payoff,
            @BctMethodArg (excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg (required = false) String id
    ){
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT" : underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyetType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyetType);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        Object instrument = createUnderlyer(instrumentId, instrumentTypeEnum);
        switch (instrumentTypeEnum){
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityEagle<>((EquityStock) instrument,strike1.doubleValue(),strike2.doubleValue(),
                                strike3.doubleValue(),strike4.doubleValue(),payoff.doubleValue(),exp)
                        ,id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityEagle<>((EquityIndexFutures) instrument, strike1.doubleValue(),strike2.doubleValue(),
                                strike3.doubleValue(),strike4.doubleValue(), payoff.doubleValue(),exp),id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityEagle<>((EquityIndex) instrument, strike1.doubleValue(),strike2.doubleValue(),
                                strike3.doubleValue(),strike4.doubleValue(),payoff.doubleValue(),exp)
                        ,id);
            case COMMODITY_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new CommodityEagle<>((CommodityFutures) instrument,strike1.doubleValue(),strike2.doubleValue(),
                                strike3.doubleValue(),strike4.doubleValue(),
                                payoff.doubleValue(),exp),id);
            case COMMODITY_SPOT:
                return QuantlibObjectCache.Instance.put(
                        new CommodityEagle<>((CommoditySpot) instrument,strike1.doubleValue(),strike2.doubleValue(),
                                strike3.doubleValue(),strike4.doubleValue(),
                                payoff.doubleValue(),exp),id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,"quantlib: 不支持的标的类型" + underlyetType);
        }
    }

    @BctMethodInfo (tags = BctApiTagEnum.Excel)
    public String qlOptionNoTouchCreated(
            @BctMethodArg (required = false) String underlyer,
            @BctMethodArg (required = false) String underlyerType,
            @BctMethodArg (excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number barrier,
            @BctMethodArg String barrierDirection,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number payment,
            @BctMethodArg (required = false) String id
    ){
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT" : underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        BarrierDirectionEnum barrierDirectionEnum = EnumUtils.fromString(barrierDirection,BarrierDirectionEnum.class);
        Object instrument = createUnderlyer(instrumentId,instrumentTypeEnum);
        switch (instrumentTypeEnum){
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityNoTouch<>((EquityStock) instrument,exp,barrier.doubleValue(), barrierDirectionEnum,
                               payment.doubleValue()),id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityNoTouch<>((EquityIndexFutures) instrument,exp,barrier.doubleValue(),
                                barrierDirectionEnum, payment.doubleValue()),id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityNoTouch<>((EquityIndex) instrument,exp,barrier.doubleValue(), barrierDirectionEnum,
                                payment.doubleValue()),id);
            case COMMODITY_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new CommodityNoTouch<>((CommodityFutures)instrument,exp,barrier.doubleValue(),
                                barrierDirectionEnum, payment.doubleValue()),id);
            case COMMODITY_SPOT:
                return QuantlibObjectCache.Instance.put(
                        new CommodityNoTouch<>((CommoditySpot) instrument,exp,barrier.doubleValue(),
                                barrierDirectionEnum, payment.doubleValue()),id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,"quantlib: 不支持的标的类型" + underlyerType);
    }}

    @BctMethodInfo (tags = {BctApiTagEnum.Excel})
    public String qlOptionOneTouchCreate(
            @BctMethodArg (required = false) String underlyer,
            @BctMethodArg (required = false) String underlyerType,
            @BctMethodArg (excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number barrier,
            @BctMethodArg String barrierDirection,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number rebate,
            @BctMethodArg String rebateType,
            @BctMethodArg String observationType,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double,required = false) Number daysInYear,
            @BctMethodArg (required = false) String id
    ){
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT" : underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        BarrierDirectionEnum barrierDirectionEnum = EnumUtils.fromString(barrierDirection,BarrierDirectionEnum.class);
        RebateTypeEnum rebateTypeEnum = EnumUtils.fromString(rebateType,RebateTypeEnum.class);
        ObservationTypeEnum observationTypeEnum = EnumUtils.fromString(observationType,ObservationTypeEnum.class);
        Object instrument = createUnderlyer(instrumentId,instrumentTypeEnum);
        switch (instrumentTypeEnum){
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityOneTouch<>((EquityStock) instrument,exp,barrier.doubleValue(), barrierDirectionEnum,
                                rebate.doubleValue(), rebateTypeEnum,observationTypeEnum,
                                Objects.isNull(daysInYear) ? 365.0 : daysInYear.doubleValue()),id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityOneTouch<>((EquityIndexFutures) instrument,exp,barrier.doubleValue(),
                                barrierDirectionEnum, rebate.doubleValue(),rebateTypeEnum,observationTypeEnum,
                                Objects.isNull(daysInYear) ? 365.0 :daysInYear.doubleValue()),id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityOneTouch<>((EquityIndex) instrument,exp,barrier.doubleValue(),
                                barrierDirectionEnum,rebate.doubleValue(), rebateTypeEnum,observationTypeEnum,
                                Objects.isNull(daysInYear) ? 365.0 :daysInYear.doubleValue()),id);
            case COMMODITY_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new CommodityOneTouch<>((CommodityFutures) instrument,exp,barrier.doubleValue(),
                                barrierDirectionEnum, rebate.doubleValue(),rebateTypeEnum,observationTypeEnum,
                                Objects.isNull(daysInYear) ? 365.0 :daysInYear.doubleValue()),id);
            case COMMODITY_SPOT:
                return QuantlibObjectCache.Instance.put(
                        new CommodityOneTouch<>((CommoditySpot) instrument,exp,barrier.doubleValue(),
                                barrierDirectionEnum,rebate.doubleValue(), rebateTypeEnum,observationTypeEnum,
                                Objects.isNull(daysInYear) ? 365.0 :daysInYear.doubleValue()),id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,"quantlib: 不支持的标的类型" + underlyerType);
        }
    }

    @BctMethodInfo (excelType = BctExcelTypeEnum.DataDict, tags = {BctApiTagEnum.Excel})
    public String qlOptionRangeAccuralCreate(
            @BctMethodArg (required = false) String underlyer,
            @BctMethodArg (required = false) String underlyerType,
            @BctMethodArg (excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number lowBarrier,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number highBarrier,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number maxPayment,
            @BctMethodArg (excelType = BctExcelTypeEnum.ArrayDateTime) List<Tuple2<LocalDateTime,Double>> observationDates,
            @BctMethodArg (excelType = BctExcelTypeEnum.Json) Map<LocalDateTime,Double> fixings,
            @BctMethodArg (required = false) String id
            ){
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT": underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects. isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        Object instrument = createUnderlyer(instrumentId,instrumentTypeEnum);
        switch (instrumentTypeEnum){
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityRangeAccrual<>((EquityStock) instrument,exp,lowBarrier.doubleValue(),
                                highBarrier.doubleValue(),maxPayment.doubleValue(),observationDates,
                                fixings),id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityRangeAccrual<>((EquityIndexFutures) instrument,exp,lowBarrier.doubleValue(),
                                highBarrier.doubleValue(),maxPayment.doubleValue(),observationDates,fixings
                                ),id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityRangeAccrual<>((EquityIndex) instrument,exp,lowBarrier.doubleValue(),
                                highBarrier.doubleValue(),maxPayment.doubleValue(),observationDates,
                                fixings),id);
            case COMMODITY_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new CommodityRangeAccrual<>((CommodityFutures) instrument,exp,lowBarrier.doubleValue(),
                                highBarrier.doubleValue(),maxPayment.doubleValue(),observationDates,
                                fixings),id);
            case COMMODITY_SPOT:
                return QuantlibObjectCache.Instance.put(
                        new CommodityRangeAccrual<>((CommoditySpot) instrument,exp,lowBarrier.doubleValue(),
                                highBarrier.doubleValue(),maxPayment.doubleValue(),observationDates,
                                fixings),id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,"quantlib: 不支持的标的类型" + underlyerType);
        }
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlOptionStraddleCreate(
            @BctMethodArg (required = false) String underlyer,
            @BctMethodArg (required = false) String underlyerType,
            @BctMethodArg (excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number lowStrike,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number highStrike,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double,required = false) Number lowParticipation,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double,required = false) Number highParticipation,
            @BctMethodArg (required = false) String id
    ){
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT" : underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        Object instrument = createUnderlyer(instrumentId,instrumentTypeEnum);
        switch (instrumentTypeEnum){
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityStraddle<>((EquityStock) instrument,exp,lowStrike.doubleValue(),
                                highStrike.doubleValue(),
                                Objects.isNull(lowParticipation) ? 1.0: lowParticipation.doubleValue(),
                                Objects.isNull(highParticipation) ? 1.0: highParticipation.doubleValue()),id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityStraddle<>((EquityIndexFutures) instrument,exp,lowStrike.doubleValue(),
                                highStrike.doubleValue(),
                                Objects.isNull(lowParticipation) ? 1.0: lowParticipation.doubleValue(),
                                Objects.isNull(highParticipation) ? 1.0: highParticipation.doubleValue()),id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityStraddle<>((EquityIndex) instrument,exp,lowStrike.doubleValue(),
                                highStrike.doubleValue(),
                                Objects.isNull(lowParticipation) ? 1.0: lowParticipation.doubleValue(),
                                Objects.isNull(highParticipation) ? 1.0: highParticipation.doubleValue()),id);
            case COMMODITY_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new CommodityStraddle<>((CommodityFutures) instrument,exp,lowStrike.doubleValue(),
                                highStrike.doubleValue(),
                                Objects.isNull(lowParticipation) ? 1.0: lowParticipation.doubleValue(),
                                Objects.isNull(highParticipation) ? 1.0: highParticipation.doubleValue()),id);
            case COMMODITY_SPOT:
                return QuantlibObjectCache.Instance.put(
                        new CommodityStraddle<>((CommoditySpot) instrument,exp,lowStrike.doubleValue(),
                                highStrike.doubleValue(),
                                Objects.isNull(lowParticipation) ? 1.0: lowParticipation.doubleValue(),
                                Objects.isNull(highParticipation) ? 1.0: highParticipation.doubleValue()),id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,"quantlib: 不支持的标的类型" + underlyerType);
    }}

    @BctMethodInfo (tags = BctApiTagEnum.Excel)
    public String qlOptionTripleDigitalCreate(
            @BctMethodArg (required = false) String underlyer,
            @BctMethodArg (required = false) String underlyerType,
            @BctMethodArg (excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg String optionType,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number firstStrike,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number firstPayment,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number secondStrike,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number secondPayment,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number thirdStrike,
            @BctMethodArg (excelType = BctExcelTypeEnum.Double) Number thirdPayment,
            @BctMethodArg (required = false) String id
    ){
        String instrumentId = Objects.isNull(underlyer) ? "DEFAULT_INSTRUMENT" : underlyer;
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        OptionTypeEnum optionTypeEnum = OptionTypeEnum.valueOf(optionType.toUpperCase());
        Object instrument = createUnderlyer(instrumentId,instrumentTypeEnum);
        switch (instrumentTypeEnum) {
            case EQUITY_STOCK:
                return QuantlibObjectCache.Instance.put(
                        new EquityTripleDigital<>((EquityStock) instrument,exp,optionTypeEnum,
                                firstStrike.doubleValue(),firstPayment.doubleValue(),secondStrike.doubleValue(),
                                secondPayment.doubleValue(),thirdStrike.doubleValue(),thirdPayment.doubleValue())
                                ,id);
            case EQUITY_INDEX_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new EquityTripleDigital<>((EquityIndexFutures) instrument,exp,optionTypeEnum,
                                firstStrike.doubleValue(),firstPayment.doubleValue(),secondStrike.doubleValue(),
                                secondPayment.doubleValue(),thirdStrike.doubleValue(),thirdPayment.doubleValue())
                                ,id);
            case EQUITY_INDEX:
                return QuantlibObjectCache.Instance.put(
                        new EquityTripleDigital<>((EquityIndex) instrument,exp,optionTypeEnum,
                                firstStrike.doubleValue(),firstPayment.doubleValue(),secondStrike.doubleValue(),
                                secondPayment.doubleValue(),thirdStrike.doubleValue(),thirdPayment.doubleValue())
                                ,id);
            case COMMODITY_FUTURES:
                return QuantlibObjectCache.Instance.put(
                        new CommodityTripleDigital<>((CommodityFutures) instrument,exp,optionTypeEnum,
                                firstStrike.doubleValue(),firstPayment.doubleValue(),secondStrike.doubleValue(),
                                secondPayment.doubleValue(),thirdStrike.doubleValue(),thirdPayment.doubleValue())
                                ,id);
            case COMMODITY_SPOT:
                return QuantlibObjectCache.Instance.put(
                        new CommodityTripleDigital<>((CommoditySpot) instrument,exp,optionTypeEnum,
                                firstStrike.doubleValue(),firstPayment.doubleValue(),secondStrike.doubleValue(),
                                secondPayment.doubleValue(),thirdStrike.doubleValue(),thirdPayment.doubleValue())
                                ,id);
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 不支持的标的类型 " + underlyerType);
        }
    }

    // basket/multi-asset options below
    // quotient/ratio options: A/B as underlyer
    @BctMethodInfo(excelType = BctExcelTypeEnum.Handle, tags = BctApiTagEnum.Excel)
    public String qlOptionRatioVanillaEuropeanCreate(
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString) List<String> underlyers,
            @BctMethodArg(required = false) String underlyerType,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number strike,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg String optionType,
            @BctMethodArg(required = false) String id
    ) {
        if (underlyers.size() != 2) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 比例价差标的物数量必须为２");
        }
        OptionTypeEnum optionTypeEnum = OptionTypeEnum.valueOf(optionType.toUpperCase());
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        List<Object> us = underlyers.stream()
                .map(u -> createUnderlyer(u, instrumentTypeEnum))
                .collect(Collectors.toList());
        RatioVanillaEuropean option = new RatioVanillaEuropean(
                (Priceable) us.get(0), (Priceable) us.get(1),
                DateTimeUtils.parseToLocalDateTime(expiry), strike.doubleValue(), optionTypeEnum);
        return QuantlibObjectCache.Instance.put(option, id);
    }

    // spread option: w1 * A - w2 * B as underlyer
    @BctMethodInfo(excelType = BctExcelTypeEnum.Handle, tags = BctApiTagEnum.Excel)
    public String qlOptionSpreadVanillaEuropeanCreate(
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString) List<String> underlyers,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble) List<Number> weights,
            @BctMethodArg(required = false) String underlyerType,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number strike,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg String optionType,
            @BctMethodArg(required = false) String id
    ) {
        if (underlyers.size() != 2) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 比例价差标的物数量必须为２");
        }
        double weight1 = 1.0, weight2 = 1.0;
        if (!Objects.isNull(weights) && weights.size() == 2) {
            weight1 = weights.get(0).doubleValue();
            weight2 = weights.get(1).doubleValue();
        }
        if (weight1 <= 0. || weight2 <= 0.) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 价差期权权重必须为正");
        }
        if (strike.doubleValue() < 0.) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 价差期权行权价必须为正");
        }
        OptionTypeEnum optionTypeEnum = OptionTypeEnum.valueOf(optionType.toUpperCase());
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        List<Object> us = underlyers.stream()
                .map(u -> createUnderlyer(u, instrumentTypeEnum))
                .collect(Collectors.toList());
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        SpreadVanillaEuropean option = new SpreadVanillaEuropean(
                (Priceable) us.get(0), weight1,
                (Priceable) us.get(1), weight2,
                exp,
                strike.doubleValue(), optionTypeEnum, exp.toLocalDate());
        return QuantlibObjectCache.Instance.put(option, id);
    }

    @BctMethodInfo(excelType = BctExcelTypeEnum.Handle, tags = BctApiTagEnum.Excel)
    public String qlOptionSpreadDigitalCashCreate(
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString) List<String> underlyers,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble) List<Number> weights,
            @BctMethodArg(required = false) String underlyerType,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number strike,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg String optionType,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number payment,
            @BctMethodArg(required = false) String id
    ) {
        if (underlyers.size() != 2) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 比例价差标的物数量必须为２");
        }
        double weight1 = 1.0, weight2 = 1.0;
        if (!Objects.isNull(weights) && weights.size() == 2) {
            weight1 = weights.get(0).doubleValue();
            weight2 = weights.get(1).doubleValue();
        }
        if (weight1 <= 0. || weight2 <= 0.) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 价差期权权重必须为正");
        }
        if (strike.doubleValue() < 0.) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 价差期权行权价必须为正");
        }
        OptionTypeEnum optionTypeEnum = OptionTypeEnum.valueOf(optionType.toUpperCase());
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        List<Object> us = underlyers.stream()
                .map(u -> createUnderlyer(u, instrumentTypeEnum))
                .collect(Collectors.toList());
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        SpreadDigitalCash option = new SpreadDigitalCash(
                (Priceable) us.get(0), weight1,
                (Priceable) us.get(1), weight2,
                exp,
                strike.doubleValue(), optionTypeEnum, payment.doubleValue(),
                exp.toLocalDate());
        return QuantlibObjectCache.Instance.put(option, id);
    }

    @BctMethodInfo(excelType = BctExcelTypeEnum.Handle, tags = BctApiTagEnum.Excel)
    public String qlOptionSpreadKnockOutTerminalCreate(
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString) List<String> underlyers,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble) List<Number> weights,
            @BctMethodArg(required = false) String underlyerType,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number strike,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String expiry,
            @BctMethodArg String optionType,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number barrier,
            @BctMethodArg String barrierDirection,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number rebate,
            @BctMethodArg(required = false) String id
    ) {
        if (underlyers.size() != 2) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 比例价差标的物数量必须为２");
        }
        double weight1 = 1.0, weight2 = 1.0;
        if (!Objects.isNull(weights) && weights.size() == 2) {
            weight1 = weights.get(0).doubleValue();
            weight2 = weights.get(1).doubleValue();
        }
        if (weight1 <= 0. || weight2 <= 0.) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 价差期权权重必须为正");
        }
        if (strike.doubleValue() < 0.) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 价差期权行权价必须为正");
        }
        OptionTypeEnum optionTypeEnum = OptionTypeEnum.valueOf(optionType.toUpperCase());
        ExchangeListed.InstrumentTypeEnum instrumentTypeEnum = Objects.isNull(underlyerType) ?
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK :
                ExchangeListed.InstrumentTypeEnum.valueOf(underlyerType);
        List<Object> us = underlyers.stream()
                .map(u -> createUnderlyer(u, instrumentTypeEnum))
                .collect(Collectors.toList());
        LocalDateTime exp = DateTimeUtils.parseToLocalDateTime(expiry);
        BarrierDirectionEnum direction = EnumUtils.fromString(barrierDirection, BarrierDirectionEnum.class);
        if ((optionTypeEnum == OptionTypeEnum.CALL && barrier.doubleValue() <= strike.doubleValue()) ||
                (optionTypeEnum == OptionTypeEnum.PUT && barrier.doubleValue() >= strike.doubleValue())) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "价差欧式敲出仅支持单鲨结构");
        }
        SpreadKnockOutTerminal option = new SpreadKnockOutTerminal(
                (Priceable) us.get(0), weight1,
                (Priceable) us.get(1), weight2,
                exp,
                strike.doubleValue(), optionTypeEnum,
                barrier.doubleValue(),
                direction,
                rebate.doubleValue(),
                exp.toLocalDate());
        return QuantlibObjectCache.Instance.put(option, id);
    }
}
