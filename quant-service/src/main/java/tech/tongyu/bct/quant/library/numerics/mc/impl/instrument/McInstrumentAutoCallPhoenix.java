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

public class McInstrumentAutoCallPhoenix implements McInstrument {
    // known state
    private final double accumulatedCoupon;
    private final boolean knockedIn;

    // option related
    private final LocalDateTime expiry;
    // future knock-out related
    private final List<LocalDateTime> observationDates;
    private final BarrierDirectionEnum barrierDirection;
    private final List<Double> barriers;
    private final List<Double> couponBarriers;
    private final List<Double> coupons;
    private final List<LocalDateTime> couponPaymentDates;
    // future knock-in related
    private final List<LocalDateTime> knockInObservationDates;
    private final List<Double> knockInBarriers;
    private final OptionTypeEnum knockedInOptionType;
    private final double knockedInOptionStrike;
    private final LocalDateTime knockedInOptionPaymentDate;

    public McInstrumentAutoCallPhoenix(
            LocalDateTime expiry,
            BarrierDirectionEnum knockOutDirection,
            List<LocalDate> observationDates,
            List<Double> observed,
            List<Double> barriers,
            List<Double> couponBarriers,
            List<Double> coupons,
            List<LocalDate> couponPaymentDates,
            List<LocalDate> knockInObservationDates,
            List<Double> knockInBarriers,
            boolean knockedIn,
            OptionTypeEnum knockedInOptionType,
            double knockedInOptionStrike,
            LocalDate knockedInOptionPaymentDate
    ) {
        this.expiry = expiry;
        // knock out and coupon
        this.observationDates = observationDates.stream()
                .map(t -> LocalDateTime.of(t, LocalTime.MIDNIGHT))
                .collect(Collectors.toList());
        this.barrierDirection = knockOutDirection;
        this.barriers = barriers;
        this.couponBarriers = couponBarriers;
        this.coupons = coupons;
        this.couponPaymentDates = couponPaymentDates.stream()
                .map(t -> LocalDateTime.of(t, LocalTime.MIDNIGHT))
                .collect(Collectors.toList());
        // handle fixings
        //   the caller is responsible to fill correct fixings
        double accumulated = 0.0;
        if (!Objects.isNull(observed) && !observationDates.isEmpty()) {
            for (int i = 0; i < observed.size(); ++i) {
                double spot = observed.get(i);
                if (barrierDirection == BarrierDirectionEnum.UP ?
                        spot >= barriers.get(i) : spot <= barriers.get(i)) {
                    throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                            "quantlib: Autoclal Phoenix: 已经敲出。请通过生命周期事件管理对该交易进行结算。");
                }
                if (barrierDirection == BarrierDirectionEnum.UP ?
                        spot >= couponBarriers.get(i) : spot <= couponBarriers.get(i)) {
                    accumulated += coupons.get(i);
                }
            }
        }
        this.accumulatedCoupon = accumulated;
        // knock in
        this.knockedIn = knockedIn;
        this.knockedInOptionStrike = knockedInOptionStrike;
        this.knockedInOptionType = knockedInOptionType;
        this.knockedInOptionPaymentDate = LocalDateTime.of(knockedInOptionPaymentDate, LocalTime.MIDNIGHT);
        this.knockInObservationDates = knockInObservationDates.stream()
                .map(t -> LocalDateTime.of(t, LocalTime.MIDNIGHT))
                .collect(Collectors.toList());
        this.knockInBarriers = knockInBarriers;

    }

    @Override
    public LocalDateTime[] getSimDates(LocalDateTime val, double stepSize, boolean includeGridDates) {
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
        ArrayList<CashPayment> cashflows = new ArrayList<>();
        double totalCoupons = accumulatedCoupon;
        for (int i = 0; i < observationDates.size(); ++i) {
            LocalDateTime t = observationDates.get(i);
            if (t.isBefore(val)) {
                continue;
            }
            double spot = path.getSpot(t);
            double barrier = barriers.get(i);
            double couponBarrier = couponBarriers.get(i);
            if (barrierDirection == BarrierDirectionEnum.UP ?
                    spot >= couponBarrier : spot <= couponBarrier) {
                totalCoupons += coupons.get(i);
            }
            if (barrierDirection == BarrierDirectionEnum.UP ?
                    spot >=barrier : spot <= barrier) {
                // knocked out
                // WARNING: this assumes ko dominates ki, i.e. even if expiry == last obs, and
                //   already knocked in, if ko, ki does not take effect
                cashflows.add(new CashPayment(couponPaymentDates.get(i), totalCoupons));
                return cashflows;
            }
        }
        // so far not knocked out. so have to check if the option has already knocked in or on the
        // particular path it will be knocked in
        boolean pathKnockedIn = knockedIn;
        if (!pathKnockedIn) {
            for (int i = 0; i < knockInObservationDates.size(); ++i) {
                LocalDateTime t = knockInObservationDates.get(i);
                if (t.isBefore(val)) {
                    continue;
                }
                double spot = path.getSpot(t);
                if (barrierDirection == BarrierDirectionEnum.UP ?
                        spot <= knockInBarriers.get(i) : spot >= knockInBarriers.get(i)) {
                    pathKnockedIn = true;
                    break;
                }
            }
        }
        if (pathKnockedIn) {
            double spot = path.getSpot(expiry);
            double payoff = knockedInOptionType == OptionTypeEnum.PUT ?
                    -FastMath.max(knockedInOptionStrike - spot, 0.0) :
                    -FastMath.max(spot - knockedInOptionStrike, 0.);
            cashflows.add(new CashPayment(knockedInOptionPaymentDate, payoff));
        } else {
            // Not knocked in or knocked out. Get all due coupons.
            cashflows.add(new CashPayment(knockedInOptionPaymentDate, totalCoupons));
        }
        return cashflows;
    }

    @Override
    public LocalDateTime[] getDfDates(LocalDateTime val) {
        Set<LocalDateTime> all = new HashSet<>(couponPaymentDates);
        all.add(val);
        all.add(knockedInOptionPaymentDate);
        return all.stream()
                .filter(t -> !t.isBefore(val))
                .sorted().toArray(LocalDateTime[]::new);
    }

    @Override
    public LocalDateTime[] getImportantDates() {
        Set<LocalDateTime> all = new HashSet<>(observationDates);
        all.addAll(knockInObservationDates);
        all.add(expiry);
        return all.stream().sorted().toArray(LocalDateTime[]::new);
    }
}
