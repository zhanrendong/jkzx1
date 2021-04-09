package tech.tongyu.bct.quant.library.numerics.mc.impl.instrument;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.numerics.mc.McInstrument;
import tech.tongyu.bct.quant.library.numerics.mc.McPathSingleAsset;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class McInstrumentAutocall implements McInstrument {
    private final LocalDateTime expiry;

    private final List<LocalDateTime> observationDates;
    private final BarrierDirectionEnum barrierDirectionEnum;
    private final List<Double> barriers;
    private final List<Double> payments;
    private final List<LocalDateTime> paymentDates;

    private final boolean finalPayFixed;
    private final double finalPayment;
    private final LocalDateTime finalPaymentDate;
    private final OptionTypeEnum finalOptionType;
    private final double finalOptionStrike;

    private final boolean knockedOut;
    private final double knockedOutPyment;
    private final LocalDateTime knockedOutPaymentDate;

    public McInstrumentAutocall(LocalDateTime expiry,
                                List<LocalDate> observationDates,
                                BarrierDirectionEnum barrierDirectionEnum, List<Double> barriers,
                                List<Double> payments, List<LocalDate> paymentDates,
                                boolean finalPayFixed, LocalDate finalPaymentDate, double finalPayment,
                                OptionTypeEnum finalOptionType, double finalOptionStrike,
                                boolean knockedOut, double knockedOutPayment, LocalDate knockedOutPaymentDate) {
        this.expiry = expiry;
        this.observationDates = observationDates.stream()
                .map(t -> LocalDateTime.of(t, LocalTime.MIDNIGHT))
                .collect(Collectors.toList());
        this.barrierDirectionEnum = barrierDirectionEnum;
        this.barriers = barriers;
        this.payments = payments;
        this.paymentDates = paymentDates.stream()
                .map(t -> LocalDateTime.of(t, LocalTime.MIDNIGHT))
                .collect(Collectors.toList());
        this.finalPayFixed = finalPayFixed;
        this.finalPayment = finalPayment;
        this.finalPaymentDate = LocalDateTime.of(finalPaymentDate, LocalTime.MIDNIGHT);
        this.finalOptionType = finalOptionType;
        this.finalOptionStrike = finalOptionStrike;
        this.knockedOut = knockedOut;
        this.knockedOutPyment = knockedOutPayment;
        this.knockedOutPaymentDate = Objects.isNull(knockedOutPaymentDate) ? null
                : LocalDateTime.of(knockedOutPaymentDate, LocalTime.MIDNIGHT);
    }

    @Override
    public LocalDateTime[] getSimDates(LocalDateTime val, double stepSize, boolean includeGridDates) {
        if (knockedOut) {
            return new LocalDateTime[]{val};
        }
        Set<LocalDateTime> all = new HashSet<>(Arrays.asList(getImportantDates()));
        all.add(val);
        if (includeGridDates) {
            all.addAll(McInstrument.genUniformSimDates(val, expiry, stepSize));
        }
        return all.stream().filter(t -> !t.isBefore(val)).sorted().toArray(LocalDateTime[]::new);
    }

    @Override
    public List<CashPayment> exercise(McPathSingleAsset path, PricerParams params) {
        LocalDateTime val = path.getSimDates()[0];
        List<CashPayment> cashflows = new ArrayList<>();
        // already knocked out
        if (knockedOut) {
            if (!val.isAfter(knockedOutPaymentDate)) {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        "quantlib: Autoclal: 已经敲出。请通过生命周期事件管理对该交易进行结算。");
            }
            cashflows.add(new CashPayment(knockedOutPaymentDate, knockedOutPyment));
            return cashflows;
        }
        // barrier monitoring
        for (int i = 0; i < observationDates.size(); i++) {
            LocalDateTime t = observationDates.get(i);
            if (t.isBefore(val)) {
                continue;
            }
            double spot = path.getSpot(observationDates.get(i));
            if (barrierDirectionEnum == BarrierDirectionEnum.DOWN ?
                    spot <= barriers.get(i) :
                    spot >= barriers.get(i)) { // knock out
                cashflows.add(new CashPayment(paymentDates.get(i), payments.get(i)));
                return cashflows;
            }
        }
        // not knocked out till end
        if (finalPayFixed) {
            cashflows.add(new CashPayment(finalPaymentDate, finalPayment));
        } else {
            double spot = path.getSpot(expiry);
            cashflows.add(new CashPayment(finalPaymentDate,
                    finalOptionType == OptionTypeEnum.CALL ?
                            FastMath.max(spot - finalOptionStrike, 0.0) :
                            FastMath.max(finalOptionStrike - spot, 0.)));
        }
        return cashflows;
    }

    @Override
    public LocalDateTime[] getDfDates(LocalDateTime val) {
        Set<LocalDateTime> all = new HashSet<>(paymentDates);
        all.add(val);
        all.add(finalPaymentDate);
        return all.stream()
                .filter(t -> !t.isBefore(val))
                .sorted().toArray(LocalDateTime[]::new);
    }

    @Override
    public LocalDateTime[] getImportantDates() {
        Set<LocalDateTime> all = new HashSet<>(observationDates);
        all.add(expiry);
        return all.stream().sorted().toArray(LocalDateTime[]::new);
    }
}
