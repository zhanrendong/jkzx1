package tech.tongyu.bct.quant.library.pricer;

import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.common.impl.QuantlibCalcResultsBlackBasket;
import tech.tongyu.bct.quant.library.numerics.black.Ratio;
import tech.tongyu.bct.quant.library.numerics.black.Spread;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.basket.RatioVanillaEuropean;
import tech.tongyu.bct.quant.library.priceable.common.product.basket.SpreadDigitalCash;
import tech.tongyu.bct.quant.library.priceable.common.product.basket.SpreadVanillaEuropean;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasBasketUnderlyer;

import java.util.List;
import java.util.stream.Collectors;

public class BasketBlackPricer {
    public static QuantlibCalcResultsBlackBasket calc(
            List<CalcTypeEnum> requests,
            Priceable priceable,
            List<Double> underlyerPrices,
            List<Double> vols,
            double tau,
            double r,
            List<Double> qs,
            List<List<Double>> correlations,
            double T
    ) {
        if (!(priceable instanceof RatioVanillaEuropean)
                && !(priceable instanceof SpreadVanillaEuropean)
                && !(priceable instanceof SpreadDigitalCash)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    "quantlib: 多资产标的期权仅支持欧式比例价差和价差。输入为："
                            + priceable.getClass().getSimpleName());
        }
        if (underlyerPrices.size() != 2 || vols.size() != 2 || qs.size() != 2
                || correlations.size() != 2 || correlations.get(0).size() != 2 || correlations.get(1).size() != 2) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    "quantlib: 比例价差只能有两个标的");
        }
        // inputs
        double spot1 = underlyerPrices.get(0);
        double spot2 = underlyerPrices.get(1);
        double weight1 = (priceable instanceof RatioVanillaEuropean) ?
                1.0 : (priceable instanceof SpreadVanillaEuropean ?
                ((SpreadVanillaEuropean)priceable).getWeight1()
                : ((SpreadDigitalCash)priceable).getWeight1());
        double weight2 = (priceable instanceof RatioVanillaEuropean) ?
                1.0 : (priceable instanceof SpreadVanillaEuropean ?
                ((SpreadVanillaEuropean)priceable).getWeight2() : ((SpreadDigitalCash)priceable).getWeight2());
        double strike = (priceable instanceof RatioVanillaEuropean) ? ((RatioVanillaEuropean) priceable).getStrike()
                : (priceable instanceof SpreadVanillaEuropean ?
                ((SpreadVanillaEuropean) priceable).getStrike() : ((SpreadDigitalCash)priceable).getStrike());
        OptionTypeEnum optionTypeEnum = (priceable instanceof RatioVanillaEuropean) ?
                ((RatioVanillaEuropean) priceable).getOptionType()
                : (priceable instanceof SpreadVanillaEuropean ?
                ((SpreadVanillaEuropean) priceable).getOptionType() : ((SpreadDigitalCash)priceable).getOptionType());
        double vol1 = vols.get(0);
        double vol2 = vols.get(1);
        double q1 = qs.get(0);
        double q2 = qs.get(1);
        double rho = correlations.get(0).get(1);
        // outputs
        String positionId = null;
        double quantity = 1.;
        List<String> underlyerInstrumentIds = ((HasBasketUnderlyer) priceable).underlyers().stream()
                .map(u -> ((ExchangeListed)u).getInstrumentId())
                .collect(Collectors.toList());
        Double price = null;
        List<Double> deltas = null;
        List<List<Double>> gammas = null;
        List<Double> vegas = null;
        Double theta = null;
        Double rhoR = null;
        List<List<Double>> cegas = null;
        // code below should be refactored
        // now is a hack since there are only two multi-asset products
        for (CalcTypeEnum req : requests) {
            if (priceable instanceof RatioVanillaEuropean) {
                switch (req) {
                    case PRICE:
                        price = Ratio.price(spot1, spot2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum);
                        break;
                    case DELTA:
                        deltas = Ratio.delta(spot1, spot2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum);
                        break;
                    case GAMMA:
                        gammas = Ratio.gamma(spot1, spot2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum);
                        break;
                    case VEGA:
                        vegas = Ratio.vega(spot1, spot2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum);
                        break;
                    case THETA:
                        theta = -Ratio.theta(spot1, spot2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum);
                        break;
                    case RHO_R:
                        rhoR = Ratio.rhoR(spot1, spot2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum);
                        break;
                    case CEGA:
                        cegas = Ratio.cega(spot1, spot2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum);
                        break;
                }
            } else if (priceable instanceof SpreadVanillaEuropean){
                switch (req) {
                    case PRICE:
                        price = Spread.price(spot1, weight1, spot2, weight2,
                                strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum, false);
                        break;
                    case DELTA:
                        deltas = Spread.delta(spot1, weight1, spot2, weight2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum, false);
                        break;
                    case GAMMA:
                        gammas = Spread.gamma(spot1, weight1, spot2, weight2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum, false);
                        break;
                    case VEGA:
                        vegas = Spread.vega(spot1, weight1, spot2, weight2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum, false);
                        break;
                    case THETA:
                        theta = -Spread.theta(spot1, weight1, spot2, weight2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum, false);
                        break;
                    case RHO_R:
                        rhoR = Spread.rhoR(spot1, weight1, spot2, weight2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum, false);
                        break;
                    case CEGA:
                        cegas = Spread.cega(spot1, weight1, spot2, weight2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum, false);
                        break;
                }
            } else {
                double payment = ((SpreadDigitalCash)priceable).getPayment();
                switch (req) {
                    case PRICE:
                        price = payment * Spread.price(spot1, weight1, spot2, weight2,
                                strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum, true);
                        break;
                    case DELTA:
                        deltas = DoubleUtils.scaleList(Spread.delta(spot1, weight1, spot2, weight2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum, true), payment);
                        break;
                    case GAMMA:
                        gammas = DoubleUtils.scaleMatrix(Spread.gamma(spot1, weight1, spot2, weight2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum, true), payment);
                        break;
                    case VEGA:
                        vegas = DoubleUtils.scaleList(Spread.vega(spot1, weight1, spot2, weight2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum, true), payment);
                        break;
                    case THETA:
                        theta = -payment * Spread.theta(spot1, weight1, spot2, weight2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum, true);
                        break;
                    case RHO_R:
                        rhoR = payment * Spread.rhoR(spot1, weight1, spot2, weight2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum, true);
                        break;
                    case CEGA:
                        cegas = DoubleUtils.scaleMatrix(Spread.cega(spot1, weight1, spot2, weight2, strike,
                                vol1, vol2,
                                r, q1, q2, tau, rho, optionTypeEnum, true), payment);
                        break;
                }
            }
        }
        return new QuantlibCalcResultsBlackBasket(positionId, quantity, underlyerInstrumentIds,
                price, deltas, gammas, vegas, theta, rhoR, cegas, underlyerPrices, vols, r, qs);
    }
}
