package tech.tongyu.bct.service.quantlib.common.numerics.mc.instrument;

import tech.tongyu.bct.service.quantlib.common.numerics.mc.McInstrument;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McPathSingleAsset;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.AutoCallPhoenix;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Phoenix option on single asset.
 */
public class McInstrumentAutoCallPhoenix implements McInstrument {
    private final AutoCallPhoenix autoCallPhoenix;
    private LocalDateTime val;
    private TreeMap<LocalDateTime, Double> fixings;

    public McInstrumentAutoCallPhoenix(AutoCallPhoenix autoCallPhoenix) {
        this.autoCallPhoenix = autoCallPhoenix;
    }

    @Override
    public LocalDateTime[] getSimDates(LocalDateTime val, double stepSize,
                                       boolean includeGridDates) {
        TreeSet<LocalDateTime> dates = new TreeSet<>();
        this.val = val;
        this.fixings = autoCallPhoenix.getFixings();

        dates.add(val);
        TreeSet<LocalDateTime> outObsDates = new TreeSet<>(
                Arrays.asList(autoCallPhoenix.getOutObsDates()));
        dates.addAll(outObsDates.tailSet(val));
        TreeSet<LocalDateTime> outDelDates = new TreeSet<>(
                Arrays.asList(autoCallPhoenix.getOutDeliveries()));
        dates.addAll(outDelDates.tailSet(val));
        TreeSet<LocalDateTime> couponObsDates = new TreeSet<>(
                Arrays.asList(autoCallPhoenix.getCouponObsDates()));
        dates.addAll(couponObsDates.tailSet(val));
        TreeSet<LocalDateTime> couponDelDates = new TreeSet<>(
                Arrays.asList(autoCallPhoenix.getCouponDeliveries()));
        dates.addAll(couponDelDates.tailSet(val));
        TreeSet<LocalDateTime> inObsDates = new TreeSet<>(
                Arrays.asList(autoCallPhoenix.getInObsDates()));
        dates.addAll(inObsDates.tailSet(val));
        dates.add(autoCallPhoenix.getExpiry());
        dates.add(autoCallPhoenix.getDelivery());

        if (includeGridDates) {
            LocalDateTime lastObs = dates.last();
            dates.addAll(McInstrument.genUniformSimDates(val, lastObs, stepSize));
        }
        return dates.toArray(new LocalDateTime[dates.size()]);
    }

    /**
     * If date is before valuation, return the history spot, otherwise return
     * the simulated spot.
     */
    public double getSpot(McPathSingleAsset path, LocalDateTime date) {
        if (date.isBefore(val))
            return fixings.get(date);
        else
            return path.getSpot(date);
    }

    public ArrayList<CashPayment> trimCashflows(ArrayList<CashPayment> cashflows,
                                               LocalDateTime val) {
        Iterator<CashPayment> iter = cashflows.iterator();
        while (iter.hasNext()) {
            CashPayment c = iter.next();
            if (c.paymentDate.isBefore(val))
                iter.remove();
        }
        return cashflows;
    }

    @Override
    public List<CashPayment> exercise(McPathSingleAsset path,
                                      PricerParams params) {
        ArrayList<CashPayment> cashflows = new ArrayList<>();

        // check if knock out
        boolean knockedOut = false;
        LocalDateTime[] outObsDates = autoCallPhoenix.getOutObsDates();
        double[] outBarriers = autoCallPhoenix.getOutBarriers();
        LocalDateTime outDate = null;
        LocalDateTime outDelivery = null;
        for (int i = 0; i < outObsDates.length; i++) {
            double spot = getSpot(path, outObsDates[i]);
            if (spot >= outBarriers[i]) {
                knockedOut = true;
                outDate = outObsDates[i];
                outDelivery = autoCallPhoenix.getOutDeliveries()[i];
                cashflows.add(new CashPayment(
                        outDelivery, autoCallPhoenix.getPrincipal()));
                break;
            }
        }

        // collect coupon
        LocalDateTime[] couponObsDates = autoCallPhoenix.getCouponObsDates();
        double[] couponBarriers = autoCallPhoenix.getCouponBarriers();
        double[] coupons = autoCallPhoenix.getCoupons();
        LocalDateTime[] couponDeliveries = autoCallPhoenix.getCouponDeliveries();
        double unpaidCoupon = 0;
        for (int i = 0; i < couponObsDates.length; i++) {
            LocalDateTime couponObsDate = couponObsDates[i];
            if (knockedOut && couponObsDate.isAfter(outDate))
                break;
            double spot = getSpot(path, couponObsDate);
            if (spot >= couponBarriers[i]) {
                LocalDateTime couponDelivery = couponDeliveries[i];
                if (knockedOut && couponDelivery.isAfter(outDelivery))
                    couponDelivery = outDelivery;
                cashflows.add(new CashPayment(couponDelivery,
                        coupons[i] + unpaidCoupon));
                unpaidCoupon = 0;
            }
            else if (autoCallPhoenix.isSnowball()) {
                unpaidCoupon += coupons[i];
            }
        }

        // if knock out, unnecessary to check knock in
        if (knockedOut)
            return trimCashflows(cashflows, val);

        // check if knock in
        boolean knockedIn = false;
        LocalDateTime[] inObsDates = autoCallPhoenix.getInObsDates();
        double[] inBarriers = autoCallPhoenix.getInBarriers();
        for (int i = 0; i < inObsDates.length; i++) {
            double spot = getSpot(path, inObsDates[i]);
            if (spot <= inBarriers[i]) {
                knockedIn = true;
                break;
            }
        }

        // not knocked out
        if (knockedIn) {
            double spot = getSpot(path, autoCallPhoenix.getExpiry());
            double initialSpot = autoCallPhoenix.getInitialSpot();
            double inStrike = autoCallPhoenix.getInStrike();
            double returnRatio = (initialSpot
                    - (inStrike > spot ? (inStrike - spot) : 0)) / initialSpot;
//            double returnRatio = inStrike > spot ? spot / inStrike : 1;
            cashflows.add(new CashPayment(autoCallPhoenix.getDelivery(),
                    autoCallPhoenix.getPrincipal() * returnRatio));
        }
        else // not knocked out or knocked in
            cashflows.add(new CashPayment(autoCallPhoenix.getDelivery(),
                    autoCallPhoenix.getPrincipal()));
        return trimCashflows(cashflows, val);
    }

    @Override
    public LocalDateTime[] getImportantDates() {
        TreeSet<LocalDateTime> dates = new TreeSet<>();
        dates.addAll(Arrays.asList(autoCallPhoenix.getOutObsDates()));
        dates.addAll(Arrays.asList(autoCallPhoenix.getCouponObsDates()));
        dates.addAll(Arrays.asList(autoCallPhoenix.getInObsDates()));
        dates.add(autoCallPhoenix.getExpiry());
        return dates.toArray(new LocalDateTime[dates.size()]);
    }
}
