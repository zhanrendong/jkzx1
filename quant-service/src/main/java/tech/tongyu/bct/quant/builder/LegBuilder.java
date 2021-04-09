package tech.tongyu.bct.quant.builder;

import tech.tongyu.bct.quant.library.financial.date.DayCountBasis;
import tech.tongyu.bct.quant.library.priceable.ir.leg.FixedLeg;
import tech.tongyu.bct.quant.library.priceable.ir.leg.FloatingLeg;
import tech.tongyu.bct.quant.library.priceable.ir.period.AccrualPeriod;
import tech.tongyu.bct.quant.library.priceable.ir.period.FixedPaymentPeriod;
import tech.tongyu.bct.quant.library.priceable.ir.period.FloatingRateDefinitionPeriod;
import tech.tongyu.bct.quant.library.priceable.ir.period.PeriodTypeEnum;
import tech.tongyu.bct.quant.library.priceable.ir.period.impl.CompoundingPaymentPeriod;
import tech.tongyu.bct.quant.library.priceable.ir.period.impl.NonCompoundingPaymentPeriod;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LegBuilder {
    public static FixedLeg createFixedLeg(List<LocalDate> accrualStartDates,
                                          List<LocalDate> accrualEndDates,
                                          List<LocalDate> paymentDates,
                                          List<Double> rates,
                                          List<Double> notionals,
                                          DayCountBasis dayCountBasis,
                                          List<PeriodTypeEnum> periodTypes) {
        return new FixedLeg(IntStream.range(0, accrualEndDates.size())
                .mapToObj(i -> new FixedPaymentPeriod(
                        new AccrualPeriod(accrualStartDates.get(i),
                                accrualEndDates.get(i),
                                dayCountBasis.daycountFraction(accrualStartDates.get(i), accrualEndDates.get(i)),
                                periodTypes.get(i)),
                        paymentDates.get(i),
                        rates.get(i),
                        notionals.get(i)
                )).collect(Collectors.toList()));
    }

    public static FloatingLeg createNonCompoundingFloatingLeg(
            List<LocalDate> paymentStartDates,
            List<LocalDate> paymentEndDates,
            List<LocalDate> paymentDates,
            List<Double> notionals,
            DayCountBasis paymentBasis,
            List<PeriodTypeEnum> periodTypes,
            List<LocalDate> resetDates,
            List<LocalDate> accrualStartDates,
            List<LocalDate> accrualEndDates,
            List<Double> spreads,
            DayCountBasis rateBasis,
            List<Double> fixings
    ) {
        return new FloatingLeg(
                IntStream.range(0, paymentEndDates.size())
                        .mapToObj(i -> new NonCompoundingPaymentPeriod(
                                new AccrualPeriod(
                                        paymentStartDates.get(i),
                                        paymentEndDates.get(i),
                                        paymentBasis.daycountFraction(
                                                paymentStartDates.get(i),
                                                paymentEndDates.get(i)),
                                        periodTypes.get(i)),
                                new FloatingRateDefinitionPeriod(
                                        resetDates.get(i),
                                        accrualStartDates.get(i),
                                        accrualEndDates.get(i),
                                        rateBasis.daycountFraction(
                                                accrualStartDates.get(i),
                                                accrualEndDates.get(i)
                                        ),
                                        spreads.get(i),
                                        fixings.get(i)
                                ),
                                paymentDates.get(i),
                                notionals.get(i)
                        ))
                        .collect(Collectors.toList())
        );
    }

    public static FloatingLeg createCompoundingFloatingLeg(
            List<List<LocalDate>> paymentStartDates,
            List<List<LocalDate>> paymentEndDates,
            List<LocalDate> paymentDates,
            List<Double> notionals,
            DayCountBasis paymentBasis,
            List<PeriodTypeEnum> periodTypes,
            List<List<LocalDate>> resetDates,
            List<List<LocalDate>> accrualStartDates,
            List<List<LocalDate>> accrualEndDates,
            List<List<Double>> spreads,
            List<List<Double>> fixings,
            DayCountBasis rateBasis,
            List<List<PeriodTypeEnum>> subPeriodTypes
    ) {
        return new FloatingLeg(
                IntStream.range(0, paymentDates.size())
                        .mapToObj(i -> new CompoundingPaymentPeriod(
                                IntStream.range(0, paymentStartDates.get(i).size())
                                        .mapToObj(j -> new AccrualPeriod(
                                                paymentStartDates.get(i).get(j),
                                                paymentEndDates.get(i).get(j),
                                                paymentBasis.daycountFraction(
                                                        paymentStartDates.get(i).get(j),
                                                        paymentEndDates.get(i).get(j)
                                                ),
                                                subPeriodTypes.get(i).get(j)
                                        )).collect(Collectors.toList()),
                                IntStream.range(0, resetDates.get(i).size())
                                        .mapToObj(k -> new FloatingRateDefinitionPeriod(
                                                resetDates.get(i).get(k),
                                                accrualStartDates.get(i).get(k),
                                                accrualEndDates.get(i).get(k),
                                                rateBasis.daycountFraction(
                                                        accrualStartDates.get(i).get(k),
                                                        accrualEndDates.get(i).get(k)
                                                ),
                                                spreads.get(i).get(k),
                                                fixings.get(i).get(k)
                                        )).collect(Collectors.toList()),
                                paymentDates.get(i),
                                notionals.get(i),
                                periodTypes.get(i)
                        ))
                        .collect(Collectors.toList())
        );
    }
}
