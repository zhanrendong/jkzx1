package tech.tongyu.bct.pricing.service;

import io.vavr.Tuple2;
import tech.tongyu.bct.pricing.common.Diagnostic;
import tech.tongyu.bct.pricing.scenario.ScenarioResult;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.QuantlibCalcResults;
import tech.tongyu.bct.quant.library.priceable.Position;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public interface PricingService {
    String SCHEMA = "pricingService";

    Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> price(
            List<CalcTypeEnum> requests, List<Position> positions, String pricingEnvironmentId,
            LocalDateTime valuationDatetime, ZoneId timezone);

    Tuple2<List<Diagnostic>, List<ScenarioResult>> spotScenarios(
            List<CalcTypeEnum> requests, List<Position> positions, String pricingEnvironmentId,
            List<String> underlyers, double min, double max, int num, boolean isPercentage,
            LocalDateTime valuationDateTime, ZoneId timezone);

    Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> price(
            List<CalcTypeEnum> requests, List<Position> positions, String pricingEnvironmentId,
            Double underlyerPrice, Double vol, Double r, Double q,
            LocalDateTime valuationDateTime, ZoneId timezone);

    Tuple2<List<Diagnostic>, List<ScenarioResult>> scenarios(
            List<CalcTypeEnum> requests,
            List<Position> positions,
            String pricingEnvironmentId,
            List<String> underlyers,
            double spotBumpMin, double spotBumpMax, int spotBumpNumber, boolean isSpotBumpPercent,
            double volBumpMin, double volBumpMax, int volBumpNumber, boolean isVolBumpPercent,
            double rBumpMin, double rBumpMax, int rBumpNumber, boolean isRBumpPercent,
            LocalDateTime valuationDateTime,
            ZoneId timezone);

    Tuple2<List<Diagnostic>, List<ScenarioResult>> scenarios(
            List<CalcTypeEnum> requests,
            List<Position> positions,
            String pricingEnvironmentId,
            double spot,
            double spotBumpMin, double spotBumpMax, int spotBumpNumber, boolean isSpotBumpPercent,
            double vol,
            double volBumpMin, double volBumpMax, int volBumpNumber, boolean isVolBumpPercent,
            double r,
            double rBumpMin, double rBumpMax, int rBumpNumber, boolean isRBumpPercent,
            Double q,
            LocalDateTime valuationDateTime,
            ZoneId timezone);

    Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> price(
            List<CalcTypeEnum> requests, Position position, String pricingEnvironmentId,
            List<String> underlyers,
            List<Double> underlyerPrices, List<Double> vols, double r, List<Double> qs,
            List<List<Double>> corrMatrix,
            LocalDateTime valuationDateTime, ZoneId timezone);
}
