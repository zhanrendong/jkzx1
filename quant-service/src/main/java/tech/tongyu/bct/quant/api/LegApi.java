package tech.tongyu.bct.quant.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.quant.builder.LegBuilder;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.EnumUtils;
import tech.tongyu.bct.quant.library.common.QuantlibSerializableObject;
import tech.tongyu.bct.quant.library.common.impl.QuantlibCalcResultsSwap;
import tech.tongyu.bct.quant.library.financial.date.BusinessDayAdjustment;
import tech.tongyu.bct.quant.library.financial.date.DateCalcUtils;
import tech.tongyu.bct.quant.library.financial.date.DateRollEnum;
import tech.tongyu.bct.quant.library.financial.date.DayCountBasis;
import tech.tongyu.bct.quant.library.market.curve.DiscountingCurve;
import tech.tongyu.bct.quant.library.priceable.ir.leg.FixedLeg;
import tech.tongyu.bct.quant.library.priceable.ir.leg.FloatingLeg;
import tech.tongyu.bct.quant.library.priceable.ir.period.AccrualPeriod;
import tech.tongyu.bct.quant.library.priceable.ir.period.FloatingPaymentPeriod;
import tech.tongyu.bct.quant.library.priceable.ir.period.FloatingRateDefinitionPeriod;
import tech.tongyu.bct.quant.library.priceable.ir.period.PeriodTypeEnum;
import tech.tongyu.bct.quant.library.priceable.ir.period.impl.CompoundingPaymentPeriod;
import tech.tongyu.bct.quant.library.priceable.ir.period.impl.NonCompoundingPaymentPeriod;
import tech.tongyu.bct.quant.library.pricer.PricerUtils;
import tech.tongyu.bct.quant.library.pricer.SwapPricer;
import tech.tongyu.bct.quant.service.cache.QuantlibObjectCache;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LegApi {
    private static final List<String> calendars = Arrays.asList("DEFAULT_CALENDAR");
    public static class FixedLegInfo {
        private final LocalDate accrualStart;
        private final LocalDate accrualEnd;
        private final LocalDate paymentDate;
        private final double dayCountFraction;
        private final double rate;
        private final double notional;

        public FixedLegInfo(LocalDate accrualStart, LocalDate accrualEnd,
                            LocalDate paymentDate, double dayCountFraction,
                            double rate, double notional) {
            this.accrualStart = accrualStart;
            this.accrualEnd = accrualEnd;
            this.paymentDate = paymentDate;
            this.dayCountFraction = dayCountFraction;
            this.rate = rate;
            this.notional = notional;
        }

        public LocalDate getAccrualStart() {
            return accrualStart;
        }

        public LocalDate getAccrualEnd() {
            return accrualEnd;
        }

        public LocalDate getPaymentDate() {
            return paymentDate;
        }

        public double getDayCountFraction() {
            return dayCountFraction;
        }

        public double getRate() {
            return rate;
        }

        public double getNotional() {
            return notional;
        }
    }

    public static class FloatingLegInfo {
        private final LocalDate paymentStart;
        private final LocalDate paymentEnd;
        private final LocalDate paymentDate;
        private final double paymentDayCountFraction;
        private final double notional;
        private final LocalDate resetDate;
        private final LocalDate accrualStart;
        private final LocalDate accrualEnd;
        private final double rateDayCountFraction;
        private final double spread;
        private final Double fixing;

        public FloatingLegInfo(LocalDate paymentStart, LocalDate paymentEnd, LocalDate paymentDate,
                               double paymentDayCountFraction, double notional,
                               LocalDate resetDate, LocalDate accrualStart, LocalDate accrualEnd,
                               double rateDayCountFraction, double spread, Double fixing) {
            this.paymentStart = paymentStart;
            this.paymentEnd = paymentEnd;
            this.paymentDate = paymentDate;
            this.paymentDayCountFraction = paymentDayCountFraction;
            this.notional = notional;
            this.resetDate = resetDate;
            this.accrualStart = accrualStart;
            this.accrualEnd = accrualEnd;
            this.rateDayCountFraction = rateDayCountFraction;
            this.spread = spread;
            this.fixing = fixing;
        }

        public LocalDate getPaymentStart() {
            return paymentStart;
        }

        public LocalDate getPaymentEnd() {
            return paymentEnd;
        }

        public LocalDate getPaymentDate() {
            return paymentDate;
        }

        public double getPaymentDayCountFraction() {
            return paymentDayCountFraction;
        }

        public double getNotional() {
            return notional;
        }

        public LocalDate getResetDate() {
            return resetDate;
        }

        public LocalDate getAccrualStart() {
            return accrualStart;
        }

        public LocalDate getAccrualEnd() {
            return accrualEnd;
        }

        public double getRateDayCountFraction() {
            return rateDayCountFraction;
        }

        public double getSpread() {
            return spread;
        }

        public Double getFixing() {
            return fixing;
        }
    }

    public static class FloatingCompoundingLegInfo {
        private final LocalDate paymentStart;
        private final LocalDate paymentEnd;
        private final LocalDate paymentDate;
        private final double notional;
        private final LocalDate accrualStart;
        private final LocalDate accrualEnd;
        private final double dayCountFraction;
        private final LocalDate resetDate;
        private final LocalDate rateAccrualStart;
        private final LocalDate rateAccrualEnd;
        private final double rateDayCountFraction;
        private final double spread;
        private final Double fixing;

        public FloatingCompoundingLegInfo(LocalDate paymentStart, LocalDate paymentEnd, LocalDate paymentDate,
                                          double notional,
                                          LocalDate accrualStart, LocalDate accrualEnd, double dayCountFraction,
                                          LocalDate resetDate, LocalDate rateAccrualStart, LocalDate rateAccrualEnd,
                                          double rateDayCountFraction, double spread, Double fixing) {
            this.paymentStart = paymentStart;
            this.paymentEnd = paymentEnd;
            this.paymentDate = paymentDate;
            this.notional = notional;
            this.accrualStart = accrualStart;
            this.accrualEnd = accrualEnd;
            this.dayCountFraction = dayCountFraction;
            this.resetDate = resetDate;
            this.rateAccrualStart = rateAccrualStart;
            this.rateAccrualEnd = rateAccrualEnd;
            this.rateDayCountFraction = rateDayCountFraction;
            this.spread = spread;
            this.fixing = fixing;
        }

        public LocalDate getPaymentStart() {
            return paymentStart;
        }

        public LocalDate getPaymentEnd() {
            return paymentEnd;
        }

        public LocalDate getPaymentDate() {
            return paymentDate;
        }

        public double getNotional() {
            return notional;
        }

        public LocalDate getAccrualStart() {
            return accrualStart;
        }

        public LocalDate getAccrualEnd() {
            return accrualEnd;
        }

        public double getDayCountFraction() {
            return dayCountFraction;
        }

        public LocalDate getResetDate() {
            return resetDate;
        }

        public LocalDate getRateAccrualStart() {
            return rateAccrualStart;
        }

        public LocalDate getRateAccrualEnd() {
            return rateAccrualEnd;
        }

        public double getRateDayCountFraction() {
            return rateDayCountFraction;
        }

        public double getSpread() {
            return spread;
        }

        public Double getFixing() {
            return fixing;
        }
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlIrLegFixedCreate(
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String spotDate,
            @BctMethodArg String tenor,
            @BctMethodArg(required = false) String frequency,
            @BctMethodArg double rate,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double, required = false) Number notional,
            @BctMethodArg(required = false) String basis,
            @BctMethodArg(required = false) String convention,
            @BctMethodArg(required = false) String id
    ) {
        LocalDate start = DateTimeUtils.parseToLocalDate(spotDate);
        if (!Objects.isNull(convention) && !convention.equalsIgnoreCase("CNY_NAFMII")) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "仅支持CNY_NAFMII");
        }
        DayCountBasis dayCountBasis = Objects.isNull(basis) ? DayCountBasis.ACT365 :
                EnumUtils.fromString(basis, DayCountBasis.class);
        Period freq = DateTimeUtils.parsePeriod(Objects.isNull(frequency) ? "3M" : frequency);
        Period tn = DateTimeUtils.parsePeriod(tenor);
        List<LocalDate> accrualDates = DateCalcUtils.generateSchedule(start, tn, freq,
                DateRollEnum.FORWARD, BusinessDayAdjustment.MODIFIED_FOLLOWING, calendars);
        int numPeriods = accrualDates.size() - 1;
        FixedLeg leg = LegBuilder.createFixedLeg(accrualDates.subList(0, numPeriods),
                accrualDates.subList(1, numPeriods + 1),
                accrualDates.subList(1, numPeriods + 1),
                Collections.nCopies(numPeriods, rate),
                Collections.nCopies(numPeriods, Objects.isNull(notional) ? 1. : notional.doubleValue()),
                dayCountBasis,
                Collections.nCopies(numPeriods, PeriodTypeEnum.FULL));
        return QuantlibObjectCache.Instance.put(leg, id);
    }

    @BctMethodInfo(excelType = BctExcelTypeEnum.Table, tags = {BctApiTagEnum.Excel})
    public List<FixedLegInfo> qlIrLegFixedInfo(
            @BctMethodArg String fixedLeg
    ) {
        FixedLeg leg = (FixedLeg) QuantlibObjectCache.Instance.getMayThrow(fixedLeg);
        return leg.getPeriods().stream()
                .map(p -> new FixedLegInfo(p.getAccrualPeriod().getAccrualStart(),
                        p.getAccrualPeriod().getAccrualEnd(),
                        p.getPaymentDate(),
                        p.getAccrualPeriod().getDayCountFraction(),
                        p.getFixedRate(), p.getNotional()))
                .collect(Collectors.toList());
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlIrLegFloatingCreate(
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String spotDate,
            @BctMethodArg String tenor,
            @BctMethodArg(required = false) String frequency,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double, required = false) Number notional,
            @BctMethodArg(required = false) String paymentBasis,
            @BctMethodArg(required = false) Number spread,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble, required = false) List<Number> fixings,
            @BctMethodArg(required = false) String rateBasis,
            @BctMethodArg(required = false) String convention,
            @BctMethodArg(required = false) String id
    ) {
        LocalDate start = DateTimeUtils.parseToLocalDate(spotDate);
        if (!Objects.isNull(convention) && !convention.equalsIgnoreCase("CNY_NAFMII")) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "仅支持CNY_NAFMII");
        }
        DayCountBasis pBasis = Objects.isNull(paymentBasis) ? DayCountBasis.ACT365 :
                EnumUtils.fromString(paymentBasis, DayCountBasis.class);
        DayCountBasis rBasis = Objects.isNull(rateBasis) ? pBasis :
                EnumUtils.fromString(rateBasis, DayCountBasis.class);
        Period freq = DateTimeUtils.parsePeriod(Objects.isNull(frequency) ? "3M" : frequency);
        Period tn = DateTimeUtils.parsePeriod(tenor);
        List<LocalDate> accrualDates = DateCalcUtils.generateSchedule(start, tn, freq,
                DateRollEnum.FORWARD, BusinessDayAdjustment.MODIFIED_FOLLOWING, calendars);
        int numPeriods = accrualDates.size() - 1;
        List<Double> rateFixings = new ArrayList<>(Collections.nCopies(numPeriods, null));
        if (!Objects.isNull(fixings)) {
            for (int i = 0; i < fixings.size(); ++i) {
                if (!Objects.isNull(fixings.get(i))) {
                    rateFixings.set(i, fixings.get(i).doubleValue());
                }
            }
        }
        FloatingLeg floatingLeg = LegBuilder.createNonCompoundingFloatingLeg(
                accrualDates.subList(0, numPeriods),
                accrualDates.subList(1, numPeriods + 1),
                accrualDates.subList(1, numPeriods + 1),
                Collections.nCopies(numPeriods, Objects.isNull(notional) ? 1. : notional.doubleValue()),
                pBasis,
                Collections.nCopies(numPeriods, PeriodTypeEnum.FULL),
                accrualDates.subList(0, numPeriods).stream()
                        .map(t ->
                                DateCalcUtils.add(
                                        t,
                                        Period.ofDays(1),
                                        DateRollEnum.BACKWARD,
                                        BusinessDayAdjustment.PRECEDING, calendars))
                        .collect(Collectors.toList()),
                accrualDates.subList(0, numPeriods),
                accrualDates.subList(0, numPeriods).stream().map(t -> DateCalcUtils.add(
                        t,
                        freq,
                        DateRollEnum.FORWARD,
                        BusinessDayAdjustment.MODIFIED_FOLLOWING,
                        calendars
                )).collect(Collectors.toList()),
                Collections.nCopies(numPeriods, Objects.isNull(spread) ? 0. : spread.doubleValue()),
                rBasis,
                rateFixings
        );
        return QuantlibObjectCache.Instance.put(floatingLeg, id);
    }

    @BctMethodInfo(excelType = BctExcelTypeEnum.Table, tags = {BctApiTagEnum.Excel})
    public JsonNode qlIrLegFloatingInfo(
            @BctMethodArg String floatingLeg
    ) {
        FloatingLeg leg = (FloatingLeg) QuantlibObjectCache.Instance.getMayThrow(floatingLeg);
        if (leg.getPeriods().get(0) instanceof NonCompoundingPaymentPeriod) {
            List<FloatingLegInfo> info = leg.getPeriods().stream()
                    .map(p -> new FloatingLegInfo(
                            ((NonCompoundingPaymentPeriod)p).getAccrualPeriod().getAccrualStart(),
                            ((NonCompoundingPaymentPeriod)p).getAccrualPeriod().getAccrualEnd(),
                            ((NonCompoundingPaymentPeriod)p).getPaymentDate(),
                            ((NonCompoundingPaymentPeriod)p).getAccrualPeriod().getDayCountFraction(),
                            ((NonCompoundingPaymentPeriod)p).getNotional(),
                            ((NonCompoundingPaymentPeriod)p).getDefinitionPeriod().getResetDate(),
                            ((NonCompoundingPaymentPeriod)p).getDefinitionPeriod().getAccrualStart(),
                            ((NonCompoundingPaymentPeriod)p).getDefinitionPeriod().getAccrualEnd(),
                            ((NonCompoundingPaymentPeriod)p).getDefinitionPeriod().getDayCountFraction(),
                            ((NonCompoundingPaymentPeriod)p).getDefinitionPeriod().getSpread(),
                            ((NonCompoundingPaymentPeriod)p).getDefinitionPeriod().getFixing())                        )
                    .collect(Collectors.toList());
            return JsonUtils.mapper.valueToTree(info);
        } else if (leg.getPeriods().get(0) instanceof CompoundingPaymentPeriod) {
            List<FloatingCompoundingLegInfo> info = qlIrLegFloatingCompoundingInfo(leg);
            return JsonUtils.mapper.valueToTree(info);
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "仅支持单/复利浮动端收益");
        }
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlIrLegFloatingCompoundingCreate(
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String spotDate,
            @BctMethodArg String tenor,
            @BctMethodArg String frequency,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double, required = false) Number notional,
            @BctMethodArg(required = false) String paymentBasis,
            @BctMethodArg(required = false) Number spread,
            @BctMethodArg(required = false) String compoundingFrequncy,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble, required = false) List<Number> fixings,
            @BctMethodArg(required = false) String rateBasis,
            @BctMethodArg(required = false) String convention,
            @BctMethodArg(required = false) String id
    ) {
        LocalDate start = DateTimeUtils.parseToLocalDate(spotDate);
        if (!Objects.isNull(convention) && !convention.equalsIgnoreCase("CNY_NAFMII")) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "仅支持CNY_NAFMII");
        }
        DayCountBasis pBasis = Objects.isNull(paymentBasis) ? DayCountBasis.ACT365 :
                EnumUtils.fromString(paymentBasis, DayCountBasis.class);
        DayCountBasis rBasis = Objects.isNull(rateBasis) ? pBasis :
                EnumUtils.fromString(rateBasis, DayCountBasis.class);
        Period freq = DateTimeUtils.parsePeriod(frequency);
        Period tn = DateTimeUtils.parsePeriod(tenor);
        Period cFreq = DateTimeUtils.parsePeriod(Objects.isNull(compoundingFrequncy) ? "7D" : compoundingFrequncy);
        List<LocalDate> accrualDates = DateCalcUtils.generateSchedule(start, tn, freq,
                DateRollEnum.FORWARD, BusinessDayAdjustment.MODIFIED_FOLLOWING, calendars);
        int numPeriods = accrualDates.size() - 1;
        List<List<LocalDate>> paymentStartDates = new ArrayList<>();
        List<List<LocalDate>> paymentEndDates = new ArrayList<>();
        List<LocalDate> paymentDates = accrualDates.subList(1, numPeriods + 1);
        List<List<LocalDate>> resetDates = new ArrayList<>();
        List<List<LocalDate>> accrualStartDates = new ArrayList<>();
        List<List<LocalDate>> accrualEndDates = new ArrayList<>();
        List<List<Double>> spreads = new ArrayList<>();
        List<List<PeriodTypeEnum>> subPeriodTypes = new ArrayList<>();
        for (int i = 0; i < numPeriods; ++i) {
            List<LocalDate> subAccrualDates = DateCalcUtils.generateSchedule(accrualDates.get(i),
                    accrualDates.get(i + 1), cFreq, DateRollEnum.FORWARD, BusinessDayAdjustment.MODIFIED_FOLLOWING,
                    calendars);
            paymentStartDates.add(subAccrualDates.subList(0, subAccrualDates.size() - 1));
            paymentEndDates.add(subAccrualDates.subList(1, subAccrualDates.size()));
            resetDates.add(subAccrualDates.subList(0, subAccrualDates.size() - 1).stream()
                    .map(t -> DateCalcUtils.add(
                    t,
                    Period.ofDays(1),
                    DateRollEnum.BACKWARD,
                    BusinessDayAdjustment.PRECEDING, Arrays.asList("DEFAULT_CALENDAR"))).collect(Collectors.toList()));
            accrualStartDates.add(subAccrualDates.subList(0, subAccrualDates.size() - 1));
            accrualEndDates.add(subAccrualDates.subList(0, subAccrualDates.size() - 1).stream()
                    .map(t ->
                            DateCalcUtils.add(t, cFreq, DateRollEnum.FORWARD,
                                    BusinessDayAdjustment.MODIFIED_FOLLOWING, calendars))
                    .collect(Collectors.toList()));
            spreads.add(Collections.nCopies(subAccrualDates.size() - 1,
                    Objects.isNull(spread) ? 0. : spread.doubleValue()));
            subPeriodTypes.add(Collections.nCopies(subAccrualDates.size() - 1, PeriodTypeEnum.FULL));
        }
        List<Double> rateFixings = new ArrayList<>(Collections.nCopies(
                accrualStartDates.stream().mapToInt(List::size).sum(), null));
        if (!Objects.isNull(fixings)) {
            for (int i = 0; i < fixings.size(); ++i) {
                if (!Objects.isNull(fixings.get(i))) {
                    rateFixings.set(i, fixings.get(i).doubleValue());
                }
            }
        }
        List<List<Double>> fixed = new ArrayList<>();
        int n = 0;
        for (int i = 0; i < paymentDates.size(); ++i) {
            fixed.add(rateFixings.subList(n, n + resetDates.get(i).size()));
            n += resetDates.get(i).size();
        }
        FloatingLeg floatingLeg = LegBuilder.createCompoundingFloatingLeg(
                paymentStartDates,
                paymentEndDates,
                paymentDates,
                Collections.nCopies(paymentDates.size(), Objects.isNull(notional) ? 1.0 : notional.doubleValue()),
                pBasis,
                Collections.nCopies(paymentDates.size(), PeriodTypeEnum.FULL), // THIS IS WRONG!!!
                resetDates,
                accrualStartDates,
                accrualEndDates,
                spreads,
                fixed,
                rBasis,
                subPeriodTypes
        );
        return QuantlibObjectCache.Instance.put(floatingLeg, id);
    }

    private List<FloatingCompoundingLegInfo> qlIrLegFloatingCompoundingInfo(
           FloatingLeg floatingLeg
    ) {
        List<FloatingCompoundingLegInfo> ret = new ArrayList<>();
        for (FloatingPaymentPeriod p : floatingLeg.getPeriods()) {
            CompoundingPaymentPeriod cp = (CompoundingPaymentPeriod) p;
            for (int i = 0; i < cp.getDefinitionPeriods().size(); ++i) {
                AccrualPeriod ap = cp.getAccrualPeriods().get(i);
                FloatingRateDefinitionPeriod rp = cp.getDefinitionPeriods().get(i);
                ret.add(new FloatingCompoundingLegInfo(
                        cp.getAccrualPeriods().get(0).getAccrualStart(),
                        cp.getAccrualPeriods().get(cp.getAccrualPeriods().size() - 1).getAccrualEnd(),
                        cp.getPaymentDate(),
                        cp.getNotional(),
                        ap.getAccrualStart(),
                        ap.getAccrualEnd(),
                        ap.getDayCountFraction(),
                        rp.getResetDate(),
                        rp.getAccrualStart(),
                        rp.getAccrualEnd(),
                        rp.getDayCountFraction(),
                        rp.getSpread(),
                        rp.getFixing()
                ));
            }
        }
        return ret;
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public QuantlibCalcResultsSwap qlIrLegCalc(
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString, required = false) List<String> requests,
            @BctMethodArg String leg,
            @BctMethodArg String discountingCurve,
            @BctMethodArg(required = false) String forecastingCurve,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime, required = false) String valuationDate
    ) {
        List<CalcTypeEnum> reqs = EnumUtils.getIrPricingRequests(requests);
        LocalDate val = DateTimeUtils.parseToLocalDate(valuationDate);
        DiscountingCurve discYc = PricerUtils.roll(val.atStartOfDay(),
                QuantlibObjectCache.Instance.getWithTypeMayThrow(discountingCurve, DiscountingCurve.class));

        DiscountingCurve fcstYc = PricerUtils.roll(val.atStartOfDay(), Objects.isNull(forecastingCurve) ?
                discYc : QuantlibObjectCache.Instance.getWithTypeMayThrow(forecastingCurve, DiscountingCurve.class));

        QuantlibSerializableObject o = QuantlibObjectCache.Instance.getMayThrow(leg);
        Double npv = null;
        Double dv01 = null;
        if (o instanceof FixedLeg) {
            for (CalcTypeEnum c : reqs) {
                switch (c) {
                    case NPV:
                        npv = SwapPricer.pv((FixedLeg)o, discYc);
                        break;
                    case DV01:
                        dv01 = SwapPricer.dv01((FixedLeg)o, discYc);
                        break;
                }
            }
        } else if (o instanceof FloatingLeg) {
            for (CalcTypeEnum c : reqs) {
                switch (c) {
                    case NPV:
                        npv = SwapPricer.pv((FloatingLeg) o, discYc, fcstYc);
                        break;
                    case DV01:
                        dv01 = SwapPricer.dv01((FloatingLeg) o, discYc);
                        break;
                }
            }
        }
        return new QuantlibCalcResultsSwap(npv, dv01);
    }
}
