package tech.tongyu.bct.quant.library.pricer;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.quant.library.common.EnumUtils;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;
import tech.tongyu.bct.quant.library.common.impl.*;
import tech.tongyu.bct.quant.library.financial.date.DateCalcUtils;
import tech.tongyu.bct.quant.library.market.curve.DiscountingCurve;
import tech.tongyu.bct.quant.library.market.vol.ImpliedVolSurface;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.HasSingleDelivery;
import tech.tongyu.bct.quant.library.priceable.feature.HasSingleStrike;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PricerUtils {
    static BlackPricingParameters extractBlackParameters(Priceable priceable,
                                                         Double underlyerPrice,
                                                         ImpliedVolSurface volSurface,
                                                         DiscountingCurve discountingCurve,
                                                         DiscountingCurve dividendCurve,
                                                         QuantPricerSpec pricer) {
        if (!(priceable instanceof HasExpiry)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：产品 %s 缺少到期日", priceable.getPriceableTypeEnum()));
        }
        if (!(pricer instanceof BlackScholesAnalyticPricer) && !(pricer instanceof Black76AnalyticPricer)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价： Pricer %s 不支持Black-Scholes定价模型", pricer.getClass().getSimpleName()));
        }
        BlackAnalyticPricerParams params = (BlackAnalyticPricerParams) pricer.getPricerParams();
        LocalDateTime val = discountingCurve.getValuationDateTime();
        LocalDateTime expiry = ((HasExpiry) priceable).getExpiry();
        double dfr = discountingCurve.df(expiry);
        double dfq = Objects.isNull(dividendCurve) ? dfr : dividendCurve.df(expiry);
        double forward = underlyerPrice * dfq / dfr;
        double strike = (priceable instanceof HasSingleStrike) ? ((HasSingleStrike) priceable).getStrike() : forward;
        double vol = Objects.isNull(volSurface) ? 0. : volSurface.iv(forward, strike, expiry);
        double tau;
        if (params.isUseCalendar()) {
            tau = DateCalcUtils.countBusinessTimeInDays(val, expiry, params.getCalendars()) / params.getDaysInYear();
        } else {
            tau = DateTimeUtils.days(val, expiry) / DateTimeUtils.DAYS_IN_YEAR;
        }
        // r and q are still calculated using act365, continuous compounding
        // however, when used in black formula, r and q are multiplied by tau, which is based on business time
        double dt = DateTimeUtils.days(val, expiry) / DateTimeUtils.DAYS_IN_YEAR;
        double r = -FastMath.log(dfr) / dt;
        double q = -FastMath.log(dfq) / dt;
        // T is time to delivery and is associated with discounting
        // thus we calculate it using the same conventions as r and q
        double T = (priceable instanceof HasSingleDelivery) ?
                DateTimeUtils.days(val,
                        ((HasSingleDelivery) priceable).getDeliveryDate()
                                .atTime(((HasExpiry) priceable).getExpiry().toLocalTime()))
                        / DateTimeUtils.DAYS_IN_YEAR
                : tau;
        return new BlackPricingParameters(tau, vol, r, q, forward, T);
    }

    static BasketBlackPricingParameters extractBasketBlackPricingParameters(
            Priceable priceable,
            List<Double> underlyerPrices,
            List<ImpliedVolSurface> volSurfaces,
            DiscountingCurve discountingCurve,
            List<DiscountingCurve> dividendCurves,
            QuantPricerSpec pricer
    ) {
        if (!(priceable instanceof HasExpiry)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：产品 %s 缺少到期日", priceable.getPriceableTypeEnum()));
        }
        if (!(pricer instanceof BlackScholesAnalyticPricer) && !(pricer instanceof Black76AnalyticPricer)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价： Pricer %s 不支持Black-Scholes定价模型", pricer.getClass().getSimpleName()));
        }
        BlackAnalyticPricerParams params = (BlackAnalyticPricerParams) pricer.getPricerParams();
        LocalDateTime val = discountingCurve.getValuationDateTime();
        LocalDateTime expiry = ((HasExpiry) priceable).getExpiry();
        double dfr = discountingCurve.df(expiry);
        List<Double> dfqs = dividendCurves.stream()
                .map(c -> Objects.isNull(c) ? dfr : c.df(expiry))
                .collect(Collectors.toList());
        List<Double> forwards = IntStream.range(0, underlyerPrices.size())
                .mapToObj(i -> underlyerPrices.get(i) * dfqs.get(i) / dfr)
                .collect(Collectors.toList());
        double tau;
        if (params.isUseCalendar()) {
            tau = DateCalcUtils.countBusinessTimeInDays(val, expiry, params.getCalendars()) / params.getDaysInYear();
        } else {
            tau = DateTimeUtils.days(val, expiry) / DateTimeUtils.DAYS_IN_YEAR;
        }
        // atmf vol for now
        List<Double> vols = IntStream.of(0, 1)
                .mapToObj(i -> volSurfaces.get(i).iv(forwards.get(i), forwards.get(i), expiry))
                .collect(Collectors.toList());
        // r and q are still calculated using act365, continuous compounding
        // however, when used in black formula, r and q are multiplied by tau, which is based on business time
        double dt = DateTimeUtils.days(val, expiry) / DateTimeUtils.DAYS_IN_YEAR;
        double r = -FastMath.log(dfr) / dt;
        List<Double> qs = dfqs.stream().map(dfq -> -FastMath.log(dfq) / dt).collect(Collectors.toList());
        // T is time to delivery and is associated with discounting
        // thus we calculate it using the same conventions as r and q
        double T = (priceable instanceof HasSingleDelivery) ?
                DateTimeUtils.days(val,
                        ((HasSingleDelivery) priceable).getDeliveryDate()
                                .atTime(((HasExpiry) priceable).getExpiry().toLocalTime()))
                        / DateTimeUtils.DAYS_IN_YEAR
                : tau;
        return new BasketBlackPricingParameters(tau, forwards, vols, r, qs, T);
    }

    public static DiscountingCurve roll(LocalDateTime val, DiscountingCurve curve) {
        if (!Objects.isNull(curve)) {
            if (curve.getValuationDateTime().isAfter(val)) {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("Quantlib: 利率曲线起始时间 %s 在估值日 %s 之后",
                                curve.getValuationDateTime().toString(), val.toString()));
            }
            if (curve.getValuationDateTime().isBefore(val)) {
                return curve.roll(val);
            }
        }
        return curve;
    }

    public static ImpliedVolSurface roll(LocalDateTime val, ImpliedVolSurface volSurface) {
        if (!Objects.isNull(volSurface)) {
            if (volSurface.getValuationDateTime().isAfter(val)) {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("Quantlib: 波动率曲面起始时间 %s 在估值日 %s 之后",
                                volSurface.getValuationDateTime().toString(), val.toString()));
            }
            if (volSurface.getValuationDateTime().isBefore(val)) {
                return volSurface.roll(val);
            }
        }
        return volSurface;
    }

    public static QuantPricerSpec parsePricer(String pricerName, Map<String, Object> pricerParams) {
        QuantPricerSpec.QuantlibPricerEnum pricerEnum = EnumUtils.fromString(pricerName,
                QuantPricerSpec.QuantlibPricerEnum.class);
        switch (pricerEnum) {
            case BLACKSHOLES_ANALYTIC:
                return new BlackScholesAnalyticPricer(
                        JsonUtils.mapper.convertValue(pricerParams, BlackAnalyticPricerParams.class));
            case BLACK76_ANALYTIC:
                return new Black76AnalyticPricer(
                        JsonUtils.mapper.convertValue(pricerParams, BlackAnalyticPricerParams.class));
            case BLACKSCHOLES_MC:
                    /*if (!pricerParams.containsKey(JsonUtils.BCT_JACKSON_TYPE_TAG)) {
                        pricerParams.put(JsonUtils.BCT_JACKSON_TYPE_TAG, BlackMcPricerParams.class.getName());
                    }*/
                return new BlackMcPricer(JsonUtils.mapper.convertValue(pricerParams, BlackMcPricerParams.class));
            case BLACK76_MC:
                return new Black76McPricer(JsonUtils.mapper.convertValue(pricerParams, BlackMcPricerParams.class));
            case MODEL_XY:
                return new ModelXYPricer(JsonUtils.mapper.convertValue(pricerParams, ModelXYPricerParams.class));
            default:
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("quantlib: 不支持定价方法 %s", pricerName));
        }
    }

    public static void validateCorrelations(List<List<Double>> correlations) {
        int n = correlations.size();
        for (int i = 0; i < n; ++n) {
            List<Double> row = correlations.get(i);
            if (row.size() != n) {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 相关性系数矩阵必须为正方形");
            }
        }
    }

    static class BlackPricingParameters {
        private double tau; // time to expiry
        private double forward;
        private double vol;
        private double r;
        private double q;
        private double T; // time to delivery

        public BlackPricingParameters(double tau, double vol, double r, double q,
                                      double forward, double T) {
            this.tau = tau;
            this.vol = vol;
            this.r = r;
            this.q = q;
            this.forward = forward;
            this.T = T;
        }

        public double getTau() {
            return tau;
        }

        public void setTau(double tau) {
            this.tau = tau;
        }

        public double getVol() {
            return vol;
        }

        public void setVol(double vol) {
            this.vol = vol;
        }

        public double getR() {
            return r;
        }

        public void setR(double r) {
            this.r = r;
        }

        public double getQ() {
            return q;
        }

        public void setQ(double q) {
            this.q = q;
        }

        public double getForward() {
            return forward;
        }

        public void setForward(double forward) {
            this.forward = forward;
        }

        public double getT() {
            return T;
        }

        public void setT(double t) {
            T = t;
        }
    }

    static class BasketBlackPricingParameters {
        private double tau;
        private List<Double> forwards;
        private List<Double> vols;
        private double r;
        private List<Double> qs;
        private double T;

        public BasketBlackPricingParameters(double tau, List<Double> forwards,
                                            List<Double> vols, double r, List<Double> qs, double t) {
            this.tau = tau;
            this.forwards = forwards;
            this.vols = vols;
            this.r = r;
            this.qs = qs;
            this.T = t;
        }

        public double getTau() {
            return tau;
        }

        public void setTau(double tau) {
            this.tau = tau;
        }

        public List<Double> getForwards() {
            return forwards;
        }

        public void setForwards(List<Double> forwards) {
            this.forwards = forwards;
        }

        public List<Double> getVols() {
            return vols;
        }

        public void setVols(List<Double> vols) {
            this.vols = vols;
        }

        public double getR() {
            return r;
        }

        public void setR(double r) {
            this.r = r;
        }

        public List<Double> getQs() {
            return qs;
        }

        public void setQs(List<Double> qs) {
            this.qs = qs;
        }

        public double getT() {
            return T;
        }

        public void setT(double t) {
            this.T = t;
        }
    }
}
