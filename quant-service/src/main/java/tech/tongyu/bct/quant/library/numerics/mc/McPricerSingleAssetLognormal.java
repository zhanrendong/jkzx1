package tech.tongyu.bct.quant.library.numerics.mc;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.common.impl.BlackMcPricerParams;
import tech.tongyu.bct.quant.library.common.impl.BlackResults;
import tech.tongyu.bct.quant.library.market.curve.DiscountingCurve;
import tech.tongyu.bct.quant.library.market.curve.impl.PwlfDiscountingCurve;
import tech.tongyu.bct.quant.library.market.vol.ImpliedVolSurface;
import tech.tongyu.bct.quant.library.market.vol.impl.AtmPwcVolSurface;
import tech.tongyu.bct.quant.library.market.vol.impl.InterpolatedStrikeVolSurface;
import tech.tongyu.bct.quant.library.numerics.fd.FiniteDifference;
import tech.tongyu.bct.quant.library.numerics.mc.impl.single.McEngineSingleAssetLognormal;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class McPricerSingleAssetLognormal {
    private static McEngineSingleAsset from(double spot, ImpliedVolSurface vs,
                                            DiscountingCurve domYc, DiscountingCurve forYc) {
        return new McEngineSingleAssetLognormal(spot, domYc, forYc, vs);
    }

    public static class McPvResult {
        private double pv;
        private double stddev;

        public McPvResult(double pv, double stddev) {
            this.pv = pv;
            this.stddev = stddev;
        }

        public double getPv() {
            return pv;
        }

        public double getStddev() {
            return stddev;
        }
    }

    public static McPvResult pv(
            McInstrument optn,
            LocalDateTime val,
            double spot,
            ImpliedVolSurface vs,
            DiscountingCurve discountYc,
            DiscountingCurve dividendYc,
            BlackMcPricerParams params) {
        McEngineSingleAsset engine = new McEngineSingleAssetLognormal(spot, discountYc, dividendYc, vs);
        LocalDateTime[] simDates = optn.getSimDates(val, params.getStepSize(), params.isIncludeGridDates());
        if (simDates.length < 2) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: MC 仅有一个模拟日期。检查期权是否已到期。");
        }
        LocalDateTime last = simDates[simDates.length - 1];
        if (params.isAddVolSurfaceDates()) {
            if (vs instanceof InterpolatedStrikeVolSurface) {
                Set<LocalDateTime> all = new TreeSet<>(Arrays.asList(simDates));
                all.addAll(((InterpolatedStrikeVolSurface)vs).getVarInterp().keySet());
                all.removeIf(t -> t.isAfter(last));
                simDates = all.toArray(new LocalDateTime[0]);
            }
        }
        LocalDateTime[] dfDates = optn.getDfDates(val);
        engine.genPaths(simDates, dfDates, params.getNumPaths(), params.getSeed());
        // generate pricer params
        McInstrument.PricerParams pricerParams = new McInstrument.PricerParams(params.isBrownianBridgeAdj());
        double npv = 0.0, npv2 = 0.0; // mean and mean of square
        for(int i = 0; i < params.getNumPaths(); ++i) {
            McPathSingleAsset path = engine.getPath(i);
            List<McInstrument.CashPayment> cashflows = optn.exercise(path, pricerParams);
            double pathPv = 0.0, pathPv2 = 0.0;
            for (McInstrument.CashPayment p : cashflows) {
                pathPv += path.df(p.paymentDate.isBefore(val) ? val : p.paymentDate) * p.amount;
            }
            pathPv2 = pathPv * pathPv;
            npv = (npv * i + pathPv) / (i + 1);
            npv2 = (npv2 * i + pathPv2) / (i + 1);
        }
        return new McPvResult(npv, FastMath.sqrt((npv2 - npv * npv) / (params.getNumPaths() - 1)));
    }

    public static McPvResult pv(McInstrument instrument, LocalDateTime val,
                     double spot, double vol, double r, double q,
                     BlackMcPricerParams params) {
        ImpliedVolSurface vs = AtmPwcVolSurface.flat(val, spot, vol, DateTimeUtils.DAYS_IN_YEAR);
        DiscountingCurve domYc = PwlfDiscountingCurve.flat(val, r, DateTimeUtils.DAYS_IN_YEAR);
        DiscountingCurve forYc = PwlfDiscountingCurve.flat(val, q, DateTimeUtils.DAYS_IN_YEAR);
        return pv(instrument, val, spot, vs, domYc, forYc, params);
    }

    // this is the one called for greeks
    // note: all greeks are based on finite difference
    // so it will be slow and inaccurate
    // only for api use
    public static BlackResults mcCalcBlackScholes(List<CalcTypeEnum> requests,
                                             Priceable instrument,
                                             double spot,
                                             ImpliedVolSurface volSurface,
                                             DiscountingCurve discountCurve,
                                             DiscountingCurve dividendCurve,
                                             BlackMcPricerParams params
    ) {
        LocalDateTime val = volSurface.getValuationDateTime();
        // expiry to get iv estimate
        //   for vega we need to bump the vs up and down
        //   so we have to get an estimate to control the bump size
        LocalDateTime expiry;
        if (instrument instanceof HasExpiry) {
            expiry = ((HasExpiry) instrument).getExpiry();
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：MC只支持有到期日的期权。输入期权类型为 %s 无到期日",
                            instrument.getPriceableTypeEnum()));
        }
        McInstrument mcInstrument = McInstrumentFactory.from(instrument);
        BlackResults results = new BlackResults();
        results.setUnderlyerPrice(spot);
        // vol, r, q are not really needed
        // since there are term structures
        double iv = volSurface.iv(spot, spot, expiry);
        results.setVol(iv);
        double dfr = discountCurve.df(expiry);
        double dfq = dividendCurve.df(expiry);
        results.setUnderlyerForward(spot * dfq / dfr);
        double tau = DateTimeUtils.days(val, expiry) / 365.0;
        double r = -FastMath.log(dfr) / tau;
        results.setR(r);
        double q = -FastMath.log(dfq) / tau;
        results.setQ(q);

        /*
        0. spot
        1. spot +
        2. spot -
        3. vol +
        4. vol -
        5. r +
        6. r -
        7. q +
        8. q -
        9. all rolled
         */
        List<CompletableFuture<Double>> tasks = new ArrayList<>(Collections.nCopies(10,
                CompletableFuture.completedFuture(null)));
        // price without any bump
        if (requests.contains(CalcTypeEnum.PRICE)
                || requests.contains(CalcTypeEnum.GAMMA)
                || requests.contains(CalcTypeEnum.THETA)) {
            tasks.set(0, CompletableFuture.supplyAsync(() ->
                    pv(mcInstrument, val, spot, volSurface, discountCurve, dividendCurve, params).getPv()));
        }
        // spot + and -
        double spotBump = 0.005 * spot;
        if (DoubleUtils.smallEnough(spotBump, DoubleUtils.SMALL_NUMBER)) {
            spotBump = 0.0001;
        }
        if (requests.contains(CalcTypeEnum.DELTA) || requests.contains(CalcTypeEnum.GAMMA)) {
            double spotUp = spot + spotBump;
            tasks.set(1, CompletableFuture.supplyAsync(() ->
                    pv(mcInstrument, val, spotUp, volSurface, discountCurve, dividendCurve, params).getPv()));
            double spotDown = spot - spotBump;
            tasks.set(2, CompletableFuture.supplyAsync(() ->
                    pv(mcInstrument, val, spotDown, volSurface, discountCurve, dividendCurve, params).getPv()));
        }
        // vol + and -
        double volBumpSize = 0.1*iv;  // really just 1% bump size
        if (volBumpSize > 0.01) {
            volBumpSize = 0.01;
        }
        if (requests.contains(CalcTypeEnum.VEGA)) {
            ImpliedVolSurface vsUp = volSurface.bump(volBumpSize);
            tasks.set(3, CompletableFuture.supplyAsync(() ->
                    pv(mcInstrument, val, spot, vsUp, discountCurve, dividendCurve, params).getPv()));
            ImpliedVolSurface vsDown = volSurface.bump(-volBumpSize);
            tasks.set(4, CompletableFuture.supplyAsync(() ->
                    pv(mcInstrument, val, spot, vsDown, discountCurve, dividendCurve, params).getPv()));
        }
        // r + and -
        double rBumpSize = r * 0.01;
        if (rBumpSize > 0.0001 || rBumpSize <= 1e-8) {
            rBumpSize = 0.0001;
        } // 1 bps bump
        if (requests.contains(CalcTypeEnum.RHO_R)) {
            DiscountingCurve ycUp = discountCurve.bump(rBumpSize);
            tasks.set(5, CompletableFuture.supplyAsync(() ->
                    pv(mcInstrument, val, spot, volSurface, ycUp, dividendCurve, params).getPv()));
            DiscountingCurve ycDown = discountCurve.bump(-rBumpSize);
            tasks.set(6, CompletableFuture.supplyAsync(() ->
                    pv(mcInstrument, val, spot, volSurface, ycDown, dividendCurve, params).getPv()));
        }
        // q + and -
        double qBumpSize = q * 0.01;
        if (qBumpSize > 0.0001 || qBumpSize <= 1e-8) {
            qBumpSize = 0.0001;
        } // 1 bps bump
        if (requests.contains(CalcTypeEnum.RHO_Q)) {
            DiscountingCurve divUp = dividendCurve.bump(qBumpSize);
            tasks.set(7, CompletableFuture.supplyAsync(() ->
                    pv(mcInstrument, val, spot, volSurface, discountCurve, divUp, params).getPv()));
            DiscountingCurve divDown = dividendCurve.bump(-qBumpSize);
            tasks.set(8, CompletableFuture.supplyAsync(() ->
                    pv(mcInstrument, val, spot, volSurface, discountCurve, divDown, params).getPv()));
        }
        // roll
        if (requests.contains(CalcTypeEnum.THETA)) {
            LocalDateTime valPlusOne = val.plusDays(1);
            if (valPlusOne.isBefore(expiry)) {
                ImpliedVolSurface vsTomorow = volSurface.roll(valPlusOne);
                DiscountingCurve discountTomorrow = discountCurve.roll(valPlusOne);
                DiscountingCurve dividendTomorrow = dividendCurve.roll(valPlusOne);
                tasks.set(9, CompletableFuture.supplyAsync(() ->
                        pv(mcInstrument, valPlusOne, spot, vsTomorow, discountTomorrow, dividendTomorrow, params).getPv()));
            }
        }
        List<Double> taskResults = tasks.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        if (requests.contains(CalcTypeEnum.PRICE)) {
            results.setResult(CalcTypeEnum.PRICE, taskResults.get(0));
        }
        if (requests.contains(CalcTypeEnum.DELTA)) {
            double delta = (taskResults.get(1) - taskResults.get(2)) / spotBump * 0.5;
            results.setResult(CalcTypeEnum.DELTA, delta);
        }
        if (requests.contains(CalcTypeEnum.GAMMA)) {
            double gamma = (taskResults.get(1) - 2.*taskResults.get(0) + taskResults.get(2)) / spotBump / spotBump;
            results.setResult(CalcTypeEnum.GAMMA, gamma);
        }
        if (requests.contains(CalcTypeEnum.VEGA)) {
            double vega = (taskResults.get(3) - taskResults.get(4)) / volBumpSize * 0.5;
            results.setResult(CalcTypeEnum.VEGA, vega);
        }
        if (requests.contains(CalcTypeEnum.RHO_R)) {
            double rhoR = (taskResults.get(5) - taskResults.get(6)) / rBumpSize * 0.5;
            results.setResult(CalcTypeEnum.RHO_R, rhoR);
        }
        if (requests.contains(CalcTypeEnum.RHO_Q)) {
            double rhoQ = (taskResults.get(7) - taskResults.get(8)) / qBumpSize * 0.5;
            results.setResult(CalcTypeEnum.RHO_Q, rhoQ);
        }
        if (requests.contains(CalcTypeEnum.THETA)) {
            double theta = 0.0;
            if (val.plusDays(1).isBefore(expiry)) {
                theta = (taskResults.get(9) - taskResults.get(0)) * 365.;
            }
            results.setResult(CalcTypeEnum.THETA, theta);
        }
        return results;
    }

    // black76 caclulators are really just a wrapper around black scholes
    // only need to set q to r and then adjust some greeks
    public static BlackResults mcCalcBlack76(List<CalcTypeEnum> requests,
                                          Priceable instrument,
                                          double spot,
                                          ImpliedVolSurface volSurface,
                                          DiscountingCurve discountCurve,
                                          BlackMcPricerParams params){
        if (requests.contains(CalcTypeEnum.RHO_R) && !requests.contains(CalcTypeEnum.RHO_Q)) {
            requests.add(CalcTypeEnum.RHO_Q);
        }
        BlackResults results = mcCalcBlackScholes(requests, instrument,
                spot, volSurface, discountCurve, discountCurve, params);
        if (requests.contains(CalcTypeEnum.RHO_R)) {
            Double rhoR = results.getRhoR();
            if (!Objects.isNull(rhoR) && !rhoR.isNaN() && !rhoR.isInfinite()) {
                Double rhoQ = results.getRhoQ();
                if (!Objects.isNull(rhoQ) && !rhoQ.isNaN() && !rhoQ.isInfinite()) {
                    rhoR = rhoR + rhoQ;
                }
                results.setResult(CalcTypeEnum.RHO_R, rhoR);
            }
        }
        results.setQ(0.0);
        results.setResult(CalcTypeEnum.RHO_Q, 0.0);
        return results;
    }
}
