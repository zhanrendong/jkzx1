package tech.tongyu.bct.quant.library.pricer;

import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.impl.BlackResults;
import tech.tongyu.bct.quant.library.numerics.black.*;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.cash.CashFlows;
import tech.tongyu.bct.quant.library.priceable.cash.CashPayment;
import tech.tongyu.bct.quant.library.priceable.commodity.*;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.Forward;
import tech.tongyu.bct.quant.library.priceable.equity.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.vavr.API.*;
import static io.vavr.Predicates.anyOf;
import static io.vavr.Predicates.instanceOf;

public class BlackPricer {

    public static Double calcBlackScholesAnalytic(CalcTypeEnum request,
                                                  Priceable priceable,
                                                  double underlyerPrice,
                                                  double vol,
                                                  double tau,
                                                  double r,
                                                  double q,
                                                  double T) {
        return Match(priceable).of(
                Case($(instanceOf(CashPayment.class)),v -> BlackScholes.calcCash(request, v.getAmount(), tau, r)),
                Case($(instanceOf(Forward.class)), v -> BlackScholes.calcForward(request,
                        underlyerPrice, v.getStrike(), tau, r, q)),
                Case($(anyOf(instanceOf(EquityVanillaEuropean.class),
                        instanceOf(CommodityVanillaEuropean.class))), v -> BlackScholes.calc(request,
                        underlyerPrice, v.getStrike(), vol, tau, r, q, v.getOptionType())),
                Case($(anyOf(instanceOf(EquityVanillaAmerican.class),
                        instanceOf(CommodityVanillaAmerican.class))), v -> American.calc(request,
                        v.getOptionType(), underlyerPrice, v.getStrike(), vol, tau, r, q)),
                Case($(anyOf(instanceOf(EquityDigitalCash.class), instanceOf(CommodityDigitalCash.class))),
                        v -> v.getPayment() * Digital.calc(request,
                        underlyerPrice, v.getStrike(), vol, tau, r, q, T, v.getOptionType(), true)),
                Case($(anyOf(instanceOf(EquityVerticalSpread.class), instanceOf(CommodityVerticalSpread.class))),
                        v -> (v.getOptionType() == OptionTypeEnum.CALL ? 1 : -1)
                                * (BlackScholes.calc(request, underlyerPrice, v.getStrikeLow(),
                                vol, tau, r, q, v.getOptionType())
                                - BlackScholes.calc(request, underlyerPrice, v.getStrikeHigh(),
                                vol, tau, r, q, v.getOptionType()))),
                Case($(anyOf(instanceOf(EquityKnockOutContinuous.class),
                        instanceOf(CommodityKnoutOutContinuous.class))),
                        v -> KnockOut.calc(request, v.getOptionType(), v.getBarrierDirection(), v.getBarrierType(),
                                v.getRebateType(), v.getBarrier(), v.getRebate(), underlyerPrice, v.getStrike(),
                                vol, tau, r, q)),
                Case($(anyOf(instanceOf(EquityDoubleKnockOutContinuous.class),
                        instanceOf(CommodityDoubleKnockOutContinuous.class))),
                        v -> DoubleKnockOut.calc(request, v.getOptionType(), v.getRebateType(), v.getHighBarrier(),
                                v.getLowBarrier(), v.getHighRebate(), v.getLowRebate(), underlyerPrice, v.getStrike(),
                                vol, tau, r, q)),
                Case($(anyOf(instanceOf(EquityOneTouch.class), instanceOf(CommodityOneTouch.class))),
                        v -> Touch.calc(request, v.effectiveBarrier(vol), v.getBarrierDirection(), BarrierTypeEnum.OUT,
                                v.getRebate(), 0, v.getRebateType(), underlyerPrice, vol, tau, r, q)),
                Case($(anyOf(instanceOf(EquityNoTouch.class), instanceOf(CommodityNoTouch.class))),
                        v -> Touch.calc(request, v.getBarrier(), v.getBarrierDirection(), BarrierTypeEnum.OUT,
                                0, v.getPayment(), RebateTypeEnum.PAY_AT_EXPIRY, underlyerPrice, vol, tau, r, q)),
                Case($(anyOf(instanceOf(EquityDoubleTouch.class), instanceOf(CommodityDoubleTouch.class))),
                        v -> DoubleTouch.calc(request, v.getRebateType(), v.getLowBarrier(), v.getHighBarrier(),
                                v.getLowRebate(), v.getHighRebate(), v.getNoTouchRebate(),
                                underlyerPrice, vol, tau, r, q)),
                Case($(anyOf(instanceOf(EquityAsianFixedStrikeArithmetic.class),
                        instanceOf(CommodityAsianFixedStrikeArithmetic.class))),
                        v -> Asian.calc(request, v.getOptionType(), v.getStrike(), v.getFixings(),
                                v.observationDatesToExpiryTime(), v.getWeights(), underlyerPrice, vol, tau, r, q)),
                Case($(), o -> {throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("定价：BlackScholes解析方法不支持的产品类型 %s", priceable.getPriceableTypeEnum()));})
        );
    }

    public static BlackResults calcBlackScholesAnalytic(List<CalcTypeEnum> requests,
                                                       Priceable priceable,
                                                       double underlyerPrice,
                                                       double vol, double tau, double r, double q, double T) {
        BlackResults results = new BlackResults();
        requests.forEach(req -> {
            Double result = calcBlackScholesAnalytic(req, priceable, underlyerPrice, vol, tau, r, q, T);
            if (!Objects.isNull(result) && !result.isNaN() && !result.isInfinite()) {
                results.setResult(req, result);
            }
        });
        if (requests.contains(CalcTypeEnum.THETA) && !Objects.isNull(results.getTheta())) {
            Double theta = results.getTheta();
            if (!theta.isNaN() && !theta.isInfinite()) {
                results.setResult(CalcTypeEnum.THETA, -theta);
            }
        }
        results.setUnderlyerPrice(underlyerPrice);
        results.setVol(vol);
        results.setR(r);
        results.setQ(q);
        return results;
    }

    public static BlackResults calcBlack76Analytic(List<CalcTypeEnum> requests,
                                                       Priceable priceable,
                                                       double underlyerPrice,
                                                       double vol, double tau, double r, double T) {
        BlackResults results = calcBlackScholesAnalytic(requests, priceable, underlyerPrice, vol, tau, r, r, T);
        if (requests.contains(CalcTypeEnum.RHO_R)) {
            Double rhoR = results.getRhoR();
            if (!Objects.isNull(rhoR) && !rhoR.isNaN() && !rhoR.isInfinite()) {
                rhoR = rhoR + calcBlackScholesAnalytic(CalcTypeEnum.RHO_Q, priceable,
                        underlyerPrice, vol, tau, r, r, T);
                results.setResult(CalcTypeEnum.RHO_R, rhoR);
            }
        }
        return results;
    }

    public static BlackResults calcExchangeListed(double underlyerPrice) {
        BlackResults results = new BlackResults();
        results.setResult(CalcTypeEnum.PRICE, underlyerPrice);
        results.setResult(CalcTypeEnum.DELTA, 1.);
        results.setResult(CalcTypeEnum.GAMMA, 0.);
        results.setResult(CalcTypeEnum.VEGA, 0.);
        results.setResult(CalcTypeEnum.THETA, 0.);
        results.setResult(CalcTypeEnum.RHO_R, 0.);
        results.setResult(CalcTypeEnum.RHO_Q, 0.);
        results.setUnderlyerPrice(underlyerPrice);
        return results;
    }

    public static BlackResults calcCash(double amount) {
        BlackResults results = new BlackResults();
        results.setResult(CalcTypeEnum.PRICE, amount);
        results.setResult(CalcTypeEnum.DELTA, 0.);
        results.setResult(CalcTypeEnum.GAMMA, 0.);
        results.setResult(CalcTypeEnum.VEGA, 0.);
        results.setResult(CalcTypeEnum.THETA, 0.);
        results.setResult(CalcTypeEnum.RHO_R, 0.);
        results.setResult(CalcTypeEnum.RHO_Q, 0.);
        return results;
    }

    public static BlackResults calcCashFlows(CashFlows cashFlows) {
        double price = cashFlows.getCashPayments().stream()
                .mapToDouble(c -> BlackScholes.calcCash(CalcTypeEnum.PRICE, c.getAmount())).sum();
        BlackResults results = new BlackResults();
        results.setResult(CalcTypeEnum.PRICE, price);
        results.setResult(CalcTypeEnum.DELTA, 0.);
        results.setResult(CalcTypeEnum.GAMMA, 0.);
        results.setResult(CalcTypeEnum.VEGA, 0.);
        results.setResult(CalcTypeEnum.THETA, 0.);
        results.setResult(CalcTypeEnum.RHO_R, 0.);
        results.setResult(CalcTypeEnum.RHO_Q, 0.);
        return results;
    }

    public static BlackResults calcCash(double amount, double tau, double r) {
        BlackResults results = new BlackResults();
        results.setResult(CalcTypeEnum.PRICE, BlackScholes.calcCash(CalcTypeEnum.PRICE, amount, tau, r));
        results.setResult(CalcTypeEnum.DELTA, 0.);
        results.setResult(CalcTypeEnum.GAMMA, 0.);
        results.setResult(CalcTypeEnum.VEGA, 0.);
        results.setResult(CalcTypeEnum.THETA, BlackScholes.calcCash(CalcTypeEnum.THETA, amount, tau, r));
        results.setResult(CalcTypeEnum.RHO_R, BlackScholes.calcCash(CalcTypeEnum.RHO_R, amount, tau, r));
        results.setResult(CalcTypeEnum.RHO_Q, 0.);
        return results;
    }
}
