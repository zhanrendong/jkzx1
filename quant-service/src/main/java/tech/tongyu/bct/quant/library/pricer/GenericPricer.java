package tech.tongyu.bct.quant.library.pricer;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;
import tech.tongyu.bct.quant.library.common.QuantlibCalcResults;
import tech.tongyu.bct.quant.library.common.impl.*;
import tech.tongyu.bct.quant.library.financial.date.DayCountBasis;
import tech.tongyu.bct.quant.library.market.curve.DiscountingCurve;
import tech.tongyu.bct.quant.library.market.curve.impl.PwlfDiscountingCurve;
import tech.tongyu.bct.quant.library.market.custom.ModelXY;
import tech.tongyu.bct.quant.library.market.vol.ImpliedVolSurface;
import tech.tongyu.bct.quant.library.numerics.black.BlackScholes;
import tech.tongyu.bct.quant.library.numerics.mc.McPricerSingleAssetLognormal;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.cash.CashFlows;
import tech.tongyu.bct.quant.library.priceable.cash.CashPayment;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.service.cache.QuantlibObjectCache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;
import static tech.tongyu.bct.quant.library.common.CalcTypeEnum.*;

public class GenericPricer {

    private static double getRate(DiscountingCurve discountingCurve, LocalDate expirationDate, DayCountBasis basis) {
        double tau = basis.daycountFraction(discountingCurve.getValuationDate(), expirationDate);
        double df = discountingCurve.df(expirationDate);
        return -FastMath.log(df) / tau;
    }

    public static QuantlibCalcResults qlSingleAssetOptionCalc(
            List<CalcTypeEnum> requests,
            Priceable priceable,
            LocalDateTime valuationDateTime,
            Double underlyerPrice,
            ImpliedVolSurface volSurface,
            DiscountingCurve discountingCurve,
            DiscountingCurve dividendCurve,
            QuantPricerSpec pricer) {
        // special case: just simple exchanged listed product
        if (priceable instanceof ExchangeListed) {
            return BlackPricer.calcExchangeListed(underlyerPrice);
        }
        // special case: a single cash payment
        if (priceable instanceof CashPayment) {
            if (Objects.isNull(discountingCurve)) {
                return BlackPricer.calcCash(((CashPayment) priceable).getAmount());
            }
            LocalDateTime t = LocalDateTime.of(((CashPayment) priceable).getPaymentDate(),
                    discountingCurve.getValuationDateTime().toLocalTime());
            double tau = DateTimeUtils.days(discountingCurve.getValuationDateTime(), t) / DateTimeUtils.DAYS_IN_YEAR;
            double r = -FastMath.log(discountingCurve.df(t)) / tau;
            return BlackPricer.calcCash(((CashPayment) priceable).getAmount(), tau, r);
        }
        // special case: cash flows
        if (priceable instanceof CashFlows) {
            if (Objects.isNull(discountingCurve)) {
                return BlackPricer.calcCashFlows((CashFlows)priceable);
            }
            double price = 0.0;
            double rhoR = 0.0;
            double theta = 0.0;
            for (CashPayment c : ((CashFlows) priceable).getCashPayments()) {
                LocalDateTime t = LocalDateTime.of(c.getPaymentDate(),
                        discountingCurve.getValuationDateTime().toLocalTime());
                double tau = DateTimeUtils.days(discountingCurve.getValuationDateTime(), t)
                        / DateTimeUtils.DAYS_IN_YEAR;
                double r = -FastMath.log(discountingCurve.df(t)) / tau;
                price += BlackScholes.calcCash(PRICE, c.getAmount(), tau, r);
                rhoR += BlackScholes.calcCash(RHO_R, c.getAmount(), tau, r);
                theta += BlackScholes.calcCash(THETA, c.getAmount(), tau, r);
            }
            BlackResults blackResults = new BlackResults();
            blackResults.setResult(PRICE, price);
            blackResults.setResult(RHO_R, rhoR);
            blackResults.setResult(THETA, theta);
            blackResults.setResult(DELTA, 0.);
            blackResults.setResult(GAMMA, 0.);
            blackResults.setResult(VEGA, 0.);
            blackResults.setResult(RHO_Q, 0.);
            return blackResults;
        }
        // other cases
        ImpliedVolSurface volSurfaceRolled = PricerUtils.roll(valuationDateTime, volSurface);
        DiscountingCurve discountingCurveRolled = PricerUtils.roll(valuationDateTime, discountingCurve);
        DiscountingCurve dividendCurveRolled = PricerUtils.roll(valuationDateTime, dividendCurve);
        return Match(pricer).of(
                Case($(instanceOf(BlackMcPricer.class)),
                        p -> McPricerSingleAssetLognormal.mcCalcBlackScholes(requests, priceable,
                                underlyerPrice, volSurfaceRolled, discountingCurveRolled,
                                dividendCurveRolled != null ? dividendCurveRolled : discountingCurveRolled,
                                (BlackMcPricerParams) pricer.getPricerParams())),
                Case($(instanceOf(Black76McPricer.class)),
                        p -> McPricerSingleAssetLognormal.mcCalcBlack76(requests, priceable,
                                underlyerPrice, volSurfaceRolled, discountingCurveRolled,
                                (BlackMcPricerParams) pricer.getPricerParams())),
                Case($(instanceOf(BlackScholesAnalyticPricer.class)),
                        p -> {
                            PricerUtils.BlackPricingParameters params = PricerUtils.extractBlackParameters(
                                    priceable, underlyerPrice,
                                    volSurfaceRolled, discountingCurveRolled, dividendCurveRolled, pricer);
                            BlackResults results = BlackPricer.calcBlackScholesAnalytic(requests, priceable,
                                    underlyerPrice,
                                    params.getVol(), params.getTau(), params.getR(), params.getQ(), params.getT());
                            results.setUnderlyerForward(params.getForward());
                            return results;
                        }),
                Case($(instanceOf(Black76AnalyticPricer.class)),
                        p -> {
                            PricerUtils.BlackPricingParameters params = PricerUtils.extractBlackParameters(
                                    priceable, underlyerPrice,
                                    volSurfaceRolled, discountingCurveRolled, null, pricer);
                            BlackResults results = BlackPricer.calcBlack76Analytic(requests, priceable,
                                    underlyerPrice, params.getVol(), params.getTau(), params.getR(), params.getT());
                            results.setUnderlyerForward(params.getForward());
                            return results;
                        }),
                Case($(instanceOf(ModelXYPricer.class)),
                        p -> {
                            String handle = ((ModelXYPricerParams)pricer.getPricerParams()).getModelId();
                            ModelXY modelXY = (ModelXY) QuantlibObjectCache.Instance.getMayThrow(handle);
                            return CustomPricer.modelXYCalc(requests, modelXY, underlyerPrice, valuationDateTime);
                        }),
                Case($(), o -> {
                    throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                            String.format("定价：不支持产品类型 %s 和定价方法 %s",
                                    priceable.getPriceableTypeEnum(), pricer.getPricerName()));
                }));
    }

    public static QuantlibCalcResults qlSingleAssetOptionCalcWithBaseContract(
            List<CalcTypeEnum> requests,
            Priceable priceable,
            LocalDateTime valuationDateTime,
            double underlyerPrice,
            ImpliedVolSurface volSurface,
            DiscountingCurve discountingCurve,
            double baseContractPrice,
            LocalDate baseContractMaturity,
            QuantPricerSpec pricer) {
        ImpliedVolSurface volSurfaceRolled = PricerUtils.roll(valuationDateTime, volSurface);
        DiscountingCurve discountingCurveRolled = PricerUtils.roll(valuationDateTime, discountingCurve);
        LocalDate valDate = valuationDateTime.toLocalDate();
        double dfq = baseContractPrice / underlyerPrice
                * discountingCurve.df(baseContractMaturity)
                / discountingCurve.df(valDate);
        long T = valDate.until(baseContractMaturity, ChronoUnit.DAYS);
        if (T == 0)
            throw new CustomException("valuation date cannot be the same as base contract maturity");
        Set<CalcTypeEnum> reqToUse = new HashSet<>(requests);
        for (CalcTypeEnum r : requests) {
            if (r == RHO_R || r == THETA)
                reqToUse.add(RHO_Q);
        }
        double q = -FastMath.log(dfq) / T * DateTimeUtils.DAYS_IN_YEAR;
        DiscountingCurve dividendCurve = PwlfDiscountingCurve.flat(valuationDateTime, q, DateTimeUtils.DAYS_IN_YEAR);
        QuantlibCalcResults results = qlSingleAssetOptionCalc(requests, priceable, valuationDateTime,
                underlyerPrice, volSurfaceRolled, discountingCurveRolled, dividendCurve, pricer);
        if (!(results instanceof BlackResults))
            throw new CustomException(ErrorCode.NOT_IMPLEMENTED, "Quantlib: 基础合约仅支持Black-Scholes模型");
        if (results.getValue(DELTA).isPresent()) {
            double delta = ((BlackResults) results).getDelta();
            ((BlackResults) results).setBaseContractDelta(delta * underlyerPrice / baseContractPrice * delta);
        }
        if (results.getValue(GAMMA).isPresent()) {
            double gamma = ((BlackResults) results).getGamma();
            ((BlackResults) results).setBaseContractGamma(gamma *
                    (underlyerPrice * underlyerPrice) / (baseContractPrice * baseContractPrice) * gamma);
        }
        if (results.getValue(THETA).isPresent() && results.getValue(RHO_Q).isPresent()) {
            double theta = ((BlackResults) results).getTheta();
            double rhoQ = ((BlackResults) results).getRhoQ();
            ((BlackResults) results).setBaseContractTheta(theta
                    + rhoQ * FastMath.log(baseContractPrice / underlyerPrice)
                    / (T * T) * (DateTimeUtils.DAYS_IN_YEAR * DateTimeUtils.DAYS_IN_YEAR));
        }
        if (results.getValue(RHO_R).isPresent() && results.getValue(RHO_Q).isPresent()) {
            double rhoQ = ((BlackResults) results).getRhoQ();
            double rhoR = ((BlackResults) results).getRhoR();
            ((BlackResults) results).setBaseContractRhoR(rhoR + rhoQ);
        }
        ((BlackResults) results).setBaseContractPrice(baseContractPrice);
        return results;
    }

    public static QuantlibCalcResults multiAssetOptionCalc(
            List<CalcTypeEnum> requests,
            Priceable priceable,
            LocalDateTime valuationDateTime,
            List<Double> underlyerPrices,
            List<ImpliedVolSurface> volSurfaces,
            DiscountingCurve discountingCurve,
            List<DiscountingCurve> dividendCurves,
            List<List<Double>> correlations,
            QuantPricerSpec pricer
    ) {
        if (!(pricer instanceof BlackScholesAnalyticPricer)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 多资产标的期权仅支持Black-Scholes解析解");
        }
        /*if (!(priceable instanceof RatioVanillaEuropean) && !(priceable instanceof SpreadVanillaEuropean)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "quantlib: 多资产标的期权仅支持欧式期权");
        }
        if (underlyerPrices.size() != 2 || volSurfaces.size() != 2 || dividendCurves.size() != 2
                || correlations.size() != 2 || correlations.get(0).size() != 2 || correlations.get(1).size() != 2) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    "quantlib: (比例)价差只能有两个标的");
        }*/
        PricerUtils.BasketBlackPricingParameters parameters = PricerUtils.extractBasketBlackPricingParameters(
                priceable, underlyerPrices,
                volSurfaces.stream().map(v -> PricerUtils.roll(valuationDateTime, v)).collect(Collectors.toList()),
                PricerUtils.roll(valuationDateTime, discountingCurve),
                dividendCurves.stream().map(c -> PricerUtils.roll(valuationDateTime, c)).collect(Collectors.toList()),
                pricer);
        return BasketBlackPricer.calc(requests, priceable,
                underlyerPrices, parameters.getVols(), parameters.getTau(),
                parameters.getR(), parameters.getQs(), correlations, parameters.getT());
    }
}
