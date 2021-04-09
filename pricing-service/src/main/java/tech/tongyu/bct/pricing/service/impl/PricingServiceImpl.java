package tech.tongyu.bct.pricing.service.impl;

//import com.google.common.base.Stopwatch;

import com.google.common.collect.Lists;
import io.vavr.*;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.Locator;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.common.util.ProfilingUtils;
import tech.tongyu.bct.market.service.CorrelationService;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.model.dto.ModelLocator;
import tech.tongyu.bct.model.dto.ModelTypeEnum;
import tech.tongyu.bct.model.service.ModelService;
import tech.tongyu.bct.pricing.common.CorrelationMatrixLocator;
import tech.tongyu.bct.pricing.common.Diagnostic;
import tech.tongyu.bct.pricing.common.QuoteFieldLocator;
import tech.tongyu.bct.pricing.common.config.PricingConfig;
import tech.tongyu.bct.pricing.common.config.SingleAssetPricingConfig;
import tech.tongyu.bct.pricing.common.config.impl.CustomProductModelXYPricingConfig;
import tech.tongyu.bct.pricing.common.config.impl.OptionOnBasketPricingConfig;
import tech.tongyu.bct.pricing.common.config.impl.OptionWithBaseContractPricingConfig;
import tech.tongyu.bct.pricing.common.descriptor.PositionDescriptor;
import tech.tongyu.bct.pricing.dto.BaseContractDTO;
import tech.tongyu.bct.pricing.environment.PricingEnvironment;
import tech.tongyu.bct.pricing.scenario.*;
import tech.tongyu.bct.pricing.service.BaseContractService;
import tech.tongyu.bct.pricing.service.PricingEnvironmentDataService;
import tech.tongyu.bct.pricing.service.PricingService;
import tech.tongyu.bct.quant.library.common.*;
import tech.tongyu.bct.quant.library.common.impl.BlackResults;
import tech.tongyu.bct.quant.library.common.impl.ModelXYPricer;
import tech.tongyu.bct.quant.library.common.impl.ModelXYPricerParams;
import tech.tongyu.bct.quant.library.common.impl.QuantlibCalcResultsBlackBasket;
import tech.tongyu.bct.quant.library.market.curve.DiscountingCurve;
import tech.tongyu.bct.quant.library.market.curve.impl.PwlfDiscountingCurve;
import tech.tongyu.bct.quant.library.market.vol.ImpliedVolSurface;
import tech.tongyu.bct.quant.library.market.vol.impl.AtmPwcVolSurface;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.cash.CashPayment;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.HasFixings;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;
import tech.tongyu.bct.quant.library.pricer.GenericPricer;
import tech.tongyu.bct.quant.service.cache.QuantlibObjectCache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PricingServiceImpl implements PricingService {
    private static Logger logger = LoggerFactory.getLogger(PricingServiceImpl.class);

    private final BaseContractService baseContractService;
    private final MarketDataService marketDataService;
    private final ModelService modelService;
    private final CorrelationService correlationService;
    private final PricingEnvironmentDataService pricingEnvironmentDataService;

    @Autowired
    public PricingServiceImpl(BaseContractService baseContractService, MarketDataService marketDataService,
                              ModelService modelService,
                              CorrelationService correlationService,
                              PricingEnvironmentDataService pricingEnvironmentDataService) {
        this.baseContractService = baseContractService;
        this.marketDataService = marketDataService;
        this.modelService = modelService;
        this.correlationService = correlationService;
        this.pricingEnvironmentDataService = pricingEnvironmentDataService;
    }

    @Override
    public Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> price(
            List<CalcTypeEnum> requests, List<Position> positions, String pricingEnvironmentId,
            LocalDateTime valuationDatetime, ZoneId timezone) {
        //  prepare for pricing:
        //    1. decompose all positions
        //    2. plan
        //    3. gather market data/models
        Tuple3<List<Diagnostic>, Map<Locator, Object>, List<Tuple2<Position, PricingConfig>>> prepared =
                ProfilingUtils.timed("prepared", () -> prepare(positions, pricingEnvironmentId, valuationDatetime, timezone));
        //    add failed
        List<Diagnostic> failed = prepared._1;
        //    this is the market (quotes and models)
        Map<Locator, Object> market = prepared._2;
        //    valid positions
        List<Tuple2<Position, PricingConfig>> toPrice = prepared._3;
        //    pricing
        Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> priced =
                ProfilingUtils.timed("pricing with market", () -> priceWithMarketData(requests,
                        toPrice, market, valuationDatetime));
        failed.addAll(priced._1);
        return Tuple.of(failed, priced._2);
    }

    private PricingEnvironment getPricingEnvironment(String pricingEnvironmentId, LocalDateTime valuationDatetime) {
        PricingEnvironment pricingEnvironment;
        Map<String, BaseContractDTO> baseContractMap =
                baseContractService.buildLookupTable(null, valuationDatetime.toLocalDate());
        if (Objects.isNull(pricingEnvironmentId) || pricingEnvironmentId.isEmpty()) {
            pricingEnvironment = PricingEnvironment.getDefaultPricingEnvironment(baseContractMap);
        } else {
            pricingEnvironment = PricingEnvironment
                    .from(pricingEnvironmentDataService.load(pricingEnvironmentId), baseContractMap);
        }
        return pricingEnvironment;
    }

    private Tuple2<List<Diagnostic>, List<Tuple2<Position, PricingConfig>>> plan(List<Position> positions,
                                                                                 PricingEnvironment pricingEnvironment,
                                                                                 LocalDateTime valuationDatetime,
                                                                                 ZoneId timezone) {
        // prepare a list to store all failures
        List<Diagnostic> failed = new ArrayList<>();
        // filter out those that have expired or are expiring
        List<Position> active = new ArrayList<>();
        // check fixings
        ListIterator iter = positions.listIterator();
        while (iter.hasNext()) {
            Position position = (Position) iter.next();
            if (position.getPriceable() instanceof HasFixings
                    && !((HasFixings) position.getPriceable()).hasSufficientFixings(valuationDatetime)) {
                failed.add(Diagnostic.of(position.getPositionId(), Diagnostic.Type.WARNING,
                        String.format("Position %s 缺少观察结果", position.getPositionId())));
                iter.remove(); // remove failed position, prevent it from sneaking in active positions
            }
        }
        List<Position> decomposed = positions.stream()
                .map(Position::decompose)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        for (Position position : decomposed) {
            if (position.getPriceable() instanceof HasExpiry
                    && ((HasExpiry) position.getPriceable()).getExpiry()
                    .isAfter(valuationDatetime)) {
                active.add(position);
            } else if (position.getPriceable() instanceof ExchangeListed) {
                active.add(position);
            } else if (position.getPriceable() instanceof CashPayment
                    && !((CashPayment) position.getPriceable()).getPaymentDate()
                    .isBefore(valuationDatetime.toLocalDate())) {
                active.add(position);
            } else {
                failed.add(Diagnostic.of(position.getPositionId(), Diagnostic.Type.WARNING,
                        String.format("Position %s 期权当日过期或已过期, 或现金已经支付", position.getPositionId())));
            }
        }
        // start
        // 1. plan
        //      get all necessary info about the pricer: market data (quotes, vol etc.), pricer and pricing params
        List<Tuple2<Position, PricingConfig>> configured = new ArrayList<>();
        for (Position p : active) {
            try {
                PricingConfig pricingConfig = pricingEnvironment.plan(
                        PositionDescriptor.getPositionDescriptor(p), valuationDatetime, timezone);
                Tuple2<Position, PricingConfig> ret = Tuple.of(p, pricingConfig);
                configured.add(ret);
            } catch (Exception e) {
                failed.add(Diagnostic.of(p.getPositionId(),
                        Diagnostic.Type.ERROR, "无法对position进行定价配置: " + e.getMessage()));
            }
        }
        return Tuple.of(failed, configured);
    }

    private Tuple3<List<Diagnostic>, Map<Locator, Object>, List<Tuple2<Position, PricingConfig>>> prepare(
            List<Position> positions, String pricingEnvironmentId, LocalDateTime valuationDatetime, ZoneId timezone) {
        PricingEnvironment pricingEnvironment = getPricingEnvironment(pricingEnvironmentId, valuationDatetime);

        Tuple2<List<Diagnostic>, List<Tuple2<Position, PricingConfig>>> planned = ProfilingUtils.timed("\t planning:",
                () -> plan(positions, pricingEnvironment, valuationDatetime, timezone));

        List<Diagnostic> failed = planned._1;
        List<Tuple2<Position, PricingConfig>> configured = planned._2;

        // 2. collect market data
        //    those without quotes and models have been filtered out
        Tuple3<List<Diagnostic>, Map<Locator, Object>, List<Tuple2<Position, PricingConfig>>> built =
                ProfilingUtils.timed("build marketdata", () -> buildMarketData(configured, valuationDatetime, timezone));
        //    add failed
        failed.addAll(built._1);
        return Tuple.of(failed, built._2, built._3);
    }

    private List<Supplier<QuantlibCalcResults>> planCalculations(
            List<CalcTypeEnum> requests,
            List<Tuple2<Position, PricingConfig>> toPrice,
            Map<Locator, Object> market,
            LocalDateTime valuationDateTime
    ) {
        return toPrice.stream()
                .map(p -> {
                        if (p._2 instanceof OptionWithBaseContractPricingConfig) {
                            Tuple5<Double, ImpliedVolSurface, DiscountingCurve, Double, LocalDate> mkt =
                                    getMarketDataWithBaseContract(market, (OptionWithBaseContractPricingConfig) p._2);
                        return (Supplier<QuantlibCalcResults>) () ->
                                GenericPricer.qlSingleAssetOptionCalcWithBaseContract(requests,
                                    p._1.getPriceable(), valuationDateTime,
                                    mkt._1, mkt._2, mkt._3, mkt._4, mkt._5, p._2.pricer());
                        } else if (p._2 instanceof CustomProductModelXYPricingConfig) {
                            Tuple2<Double, ModelXYPricer> mkt = getMarketDataForModelXY(market,
                                    (CustomProductModelXYPricingConfig) p._2);
                        return (Supplier<QuantlibCalcResults>) () ->
                                GenericPricer.qlSingleAssetOptionCalc(requests, p._1.getPriceable(),
                                    valuationDateTime,
                                    mkt._1, null, null, null, mkt._2);
                        } else if (p._2 instanceof OptionOnBasketPricingConfig) {
                            Tuple5<List<Double>, List<ImpliedVolSurface>,
                                    DiscountingCurve, List<DiscountingCurve>, List<List<Double>>> mkt =
                                    getMarketDataForBasket(market, (OptionOnBasketPricingConfig)p._2);
                        return (Supplier<QuantlibCalcResults>) () ->
                                GenericPricer.multiAssetOptionCalc(requests, p._1.getPriceable(),
                                    valuationDateTime, mkt._1, mkt._2, mkt._3, mkt._4, mkt._5, p._2.pricer());
                        } else {
                            Tuple4<Double, ImpliedVolSurface, DiscountingCurve, DiscountingCurve> mkt =
                                    getMarketData(market, (SingleAssetPricingConfig) p._2);
                        return (Supplier<QuantlibCalcResults>) () ->
                                GenericPricer.qlSingleAssetOptionCalc(
                                    requests, p._1.getPriceable(),
                                    valuationDateTime,
                                    mkt._1, mkt._2, mkt._3, mkt._4, p._2.pricer());
                        }
                }).collect(Collectors.toList());
                    }

    private List<QuantlibCalcResults> execComputations(List<Supplier<QuantlibCalcResults>> tasks) {
        List<QuantlibCalcResults> ret = new ArrayList<>();
        // batch 5000 tasks
        int taskNum = 10000;
        List<List<Supplier<QuantlibCalcResults>>> subTasks = Lists.partition(tasks, taskNum);
        for (int i = 0; i < subTasks.size(); ++i) {
            long start = System.currentTimeMillis();
            List<CompletableFuture<QuantlibCalcResults>> futures = subTasks.get(i).stream()
                    .map(t -> CompletableFuture.supplyAsync(t).exceptionally(ex -> null))
                .collect(Collectors.toList());
            List<QuantlibCalcResults> results = futures.stream().map(CompletableFuture::join)
                .collect(Collectors.toList());
            ret.addAll(results);
            long end = System.currentTimeMillis();
            logger.info("finish pricing "+results.size()+" tasks in "+(end-start)/1000.0+" seconds");
        }
        return ret;
    }

    private Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> cleanResults(
            List<Tuple2<Position, PricingConfig>> toPrice,
            List<QuantlibCalcResults> results) {
        List<Diagnostic> failed = new ArrayList<>();
        List<QuantlibCalcResults> calcResults = new ArrayList<>();
        // this is a hack!
        // we will collect those with the same position ids, knowing they come from decomposition
        Map<String, QuantlibCalcResults> tmp = new HashMap<>();
        for (int i = 0; i < toPrice.size(); ++i) {
            Position p = toPrice.get(i)._1;
            String positionId = p.getPositionId();
            PricingConfig c = toPrice.get(i)._2;
            if (Objects.isNull(results.get(i))) {
                failed.add(Diagnostic.of(p.getPositionId(), Diagnostic.Type.ERROR,
                        String.format("Quantlib定价position %s 失败", p.getPositionId())));
            } else {
                // below is a mess
                // now we have two different results: single asset and multi
                // they cannot be mixed
                QuantlibCalcResults r = results.get(i);
                if (r instanceof BlackResults) {
                    r.scale(p.getQuantity());
                    if (toPrice.get(i)._2 instanceof OptionWithBaseContractPricingConfig) {
                        ((BlackResults) r)
                                .setBaseContractInstrumentId(
                                        ((OptionWithBaseContractPricingConfig) toPrice.get(i)._2)
                                                .getBaseContractPriceLocator().getInstrumentId());
                    }
                    ((BlackResults) r).setPositionId(p.getPositionId());
                    if (c instanceof SingleAssetPricingConfig) {
                        QuoteFieldLocator locator = ((SingleAssetPricingConfig) c).underlyerPrice();
                        if (!Objects.isNull(locator)) {
                            ((BlackResults) r).setUnderlyerInstrumentId(locator.getInstrumentId());
                        }
                    }
                } else if (r instanceof QuantlibCalcResultsBlackBasket){
                    r = ((QuantlibCalcResultsBlackBasket) r)
                            .scaleBy(p.getQuantity())
                            .addPositionId(positionId);
                } else {
                    failed.add(Diagnostic.of(p.getPositionId(), Diagnostic.Type.ERROR,
                            String.format("无法识别Quantlib定价结果: position %s ", p.getPositionId())));
                    continue;
                }

                // this is a hack!
                if (p.decomposed()) {
                    if (tmp.containsKey(positionId)) {
                        // have to handle two cases: single and multi asset
                        if (r instanceof BlackResults) {
                            // add new result
                            BlackResults orig = (BlackResults) tmp.get(positionId);
                            // do not change quantity (it is from pre-decomposition position)
                            if (!Objects.isNull(orig.getPrice()) && !Objects.isNull(((BlackResults) r).getPrice())) {
                                orig.setResult(CalcTypeEnum.PRICE,
                                        (orig.getPrice() + ((BlackResults) r).getPrice()) / orig.quantity());
                            }
                            if (!Objects.isNull(orig.getDelta()) && !Objects.isNull(((BlackResults) r).getDelta())) {
                                orig.setResult(CalcTypeEnum.DELTA,
                                        (orig.getDelta() + ((BlackResults) r).getDelta()) / orig.quantity());
                            }
                            if (!Objects.isNull(orig.getGamma()) && !Objects.isNull(((BlackResults) r).getGamma())) {
                                orig.setResult(CalcTypeEnum.GAMMA,
                                        (orig.getGamma() + ((BlackResults) r).getGamma()) / orig.quantity());
                            }
                            if (!Objects.isNull(orig.getVega()) && !Objects.isNull(((BlackResults) r).getVega())) {
                                orig.setResult(CalcTypeEnum.VEGA,
                                        (orig.getVega() + ((BlackResults) r).getVega()) / orig.quantity());
                            }
                            if (!Objects.isNull(orig.getTheta()) && !Objects.isNull(((BlackResults) r).getTheta())) {
                                orig.setResult(CalcTypeEnum.THETA,
                                        (orig.getTheta() + ((BlackResults) r).getTheta()) / orig.quantity());
                            }
                            if (!Objects.isNull(orig.getRhoR()) && !Objects.isNull(((BlackResults) r).getRhoR())) {
                                orig.setResult(CalcTypeEnum.RHO_R,
                                        (orig.getRhoR() + ((BlackResults) r).getRhoR()) / orig.quantity());
                            }
                            if (!Objects.isNull(orig.getRhoQ()) && !Objects.isNull(((BlackResults) r).getRhoQ())) {
                                orig.setResult(CalcTypeEnum.RHO_Q,
                                        (orig.getRhoQ() + ((BlackResults) r).getRhoQ()) / orig.quantity());
                            }
                            if (!Objects.isNull(orig.getBaseContractDelta())
                                    && !Objects.isNull(((BlackResults) r).getBaseContractDelta())) {
                                orig.setBaseContractDelta(orig.getBaseContractDelta()
                                        + ((BlackResults) r).getBaseContractDelta());
                            }
                            if (!Objects.isNull(orig.getBaseContractGamma())
                                    && !Objects.isNull(((BlackResults) r).getBaseContractGamma())) {
                                orig.setBaseContractGamma(orig.getBaseContractGamma()
                                        + ((BlackResults) r).getBaseContractGamma());
                            }
                            if (!Objects.isNull(orig.getBaseContractTheta())
                                    && !Objects.isNull(((BlackResults) r).getBaseContractTheta())) {
                                orig.setBaseContractTheta(orig.getBaseContractTheta()
                                        + ((BlackResults) r).getBaseContractTheta());
                            }
                            if (!Objects.isNull(orig.getBaseContractRhoR())
                                    && !Objects.isNull(((BlackResults) r).getBaseContractRhoR())) {
                                orig.setBaseContractRhoR(orig.getBaseContractRhoR()
                                        + ((BlackResults) r).getBaseContractRhoR());
                            }
                        } else {
                            QuantlibCalcResults orig = tmp.get(positionId);
                            if (!(orig instanceof QuantlibCalcResultsBlackBasket)) {
                                failed.add(Diagnostic.of(positionId, Diagnostic.Type.ERROR,
                                        String.format("Position %s 分解后类型不一致：单一标的与篮子标的同时出现", positionId)));
                                continue;
                            }
                            tmp.put(positionId, ((QuantlibCalcResultsBlackBasket)orig)
                                    .sumWithoutQuantity((QuantlibCalcResultsBlackBasket)r));
                        }
                    } else {
                        //   from decomposition, so quantity has to be the undecomposed one's
                        // put the first decomposed position into the map
                        if (r instanceof BlackResults) {
                            ((BlackResults) r).setQuantity(p.getQuantityBeforeDecomposition());
                            tmp.put(positionId, r);
                        } else {
                            tmp.put(positionId,
                                    ((QuantlibCalcResultsBlackBasket)r)
                                            .changeQuantity(p.getQuantityBeforeDecomposition()));
                        }

                    }
                } else {
                    calcResults.add(r);
                }
            }
        }
        if (!tmp.isEmpty()) {
            tmp.forEach((k, v) -> calcResults.add(v));
        }
        return Tuple.of(failed, calcResults);
    }

    private Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> priceWithMarketData(
            List<CalcTypeEnum> requests,
            List<Tuple2<Position, PricingConfig>> toPrice,
            Map<Locator, Object> market,
            LocalDateTime valuationDateTime) {
        // 1. pricing
        List<QuantlibCalcResults> results = execComputations(
                planCalculations(requests, toPrice, market, valuationDateTime));
        // 2. collecting results
        return cleanResults(toPrice, results);
    }

    private Double extractUnderlyerPrice(Map<Locator, Object> market, Locator locator) {
        if (!Objects.isNull(locator) && market.containsKey(locator)) {
            return (Double) market.get(locator);
        } else {
            return null;
        }
    }

    private <T> T extractModel(Map<Locator, Object> market, Locator locator) {
        if (!Objects.isNull(locator) && market.containsKey(locator)) {
            return (T) QuantlibObjectCache.Instance.get((String) market.get(locator));
        } else {
            return null;
        }
    }

    private Tuple4<Double, ImpliedVolSurface, DiscountingCurve, DiscountingCurve> getMarketData(
            Map<Locator, Object> market, SingleAssetPricingConfig config
    ) {
        // underlyer
        Double underlyerPrice = extractUnderlyerPrice(market, config.underlyerPrice());
        // vol
        ImpliedVolSurface volSurface = extractModel(market, config.impliedVolSurface());
        // discounting
        DiscountingCurve discountingCurve = extractModel(market, config.discountingCurve());
        // dividend
        DiscountingCurve dividendCurve = extractModel(market, config.dividendCurve());
        return Tuple.of(underlyerPrice, volSurface, discountingCurve, dividendCurve);
    }

    private Tuple5<Double, ImpliedVolSurface, DiscountingCurve, Double, LocalDate> getMarketDataWithBaseContract(
            Map<Locator, Object> market, OptionWithBaseContractPricingConfig config
    ) {
        // underlyer
        Double underlyerPrice = extractUnderlyerPrice(market, config.underlyerPrice());
        // vol
        ImpliedVolSurface volSurface = extractModel(market, config.impliedVolSurface());
        // discounting
        DiscountingCurve discountingCurve = extractModel(market, config.discountingCurve());
        // base contract price
        Double baseContractPrice = extractUnderlyerPrice(market, config.getBaseContractPriceLocator());
        // base contract maturity
        LocalDate baseContractMaturity = config.getBaseContractMaturity();
        return Tuple.of(underlyerPrice, volSurface, discountingCurve, baseContractPrice, baseContractMaturity);
    }

    private Tuple2<Double, ModelXYPricer> getMarketDataForModelXY(Map<Locator, Object> market,
                                                                  CustomProductModelXYPricingConfig config) {
        // underlyer
        Double underlyerPrice = extractUnderlyerPrice(market, config.underlyerPrice());
        // model xy
        String modelId = (String) market.get(config.models().get(0));
        return Tuple.of(underlyerPrice, new ModelXYPricer(new ModelXYPricerParams(modelId)));
    }

    private Tuple5<List<Double>, List<ImpliedVolSurface>, DiscountingCurve, List<DiscountingCurve>, List<List<Double>>>
    getMarketDataForBasket(Map<Locator, Object> market, OptionOnBasketPricingConfig config) {
        // underlyers
        List<Double> underlyerPrices = config.underlyerPrices().stream()
                .map(k -> extractUnderlyerPrice(market, k))
                .collect(Collectors.toList());
        // vol surfaces
        List<ImpliedVolSurface> volSurfaceList = config.impliedVolSurfaces().stream()
                .map(k -> (ImpliedVolSurface) extractModel(market, k))
                .collect(Collectors.toList());
        // discounting curve
        DiscountingCurve discountingCurve = extractModel(market, config.discountingCurve());
        // dividend curves
        List<DiscountingCurve> dividendCurves = config.dividendCurves().stream()
                .map(k -> (DiscountingCurve) extractModel(market, k))
                .collect(Collectors.toList());
        // correlation matrix
        List<List<Double>> corrMatrix = (List<List<Double>>) market.get(config.correlatinMatrix());
        return Tuple.of(underlyerPrices, volSurfaceList, discountingCurve, dividendCurves, corrMatrix);
    }

    private Tuple3<List<Diagnostic>, Map<Locator, Object>,
            List<Tuple2<Position, PricingConfig>>> buildMarketData(
            List<Tuple2<Position, PricingConfig>> configured, LocalDateTime valuationDateTime, ZoneId timezone) {
        List<Diagnostic> failed = new ArrayList<>();
        List<Tuple2<Position, PricingConfig>> valid = new ArrayList<>();

        Set<QuoteFieldLocator> quoteFieldLocators = new HashSet<>();
        Set<ModelLocator> modelLocators = new HashSet<>();
        Set<CorrelationMatrixLocator> corrLocators = new HashSet<>();

        for (Tuple2<Position, PricingConfig> c : configured) {
            quoteFieldLocators.addAll(c._2.quotes());
            modelLocators.addAll(c._2.models());
            if (c._2 instanceof OptionOnBasketPricingConfig) {
                corrLocators.add(((OptionOnBasketPricingConfig) c._2).correlatinMatrix());
            }
        }

        // get quotes
        Map<QuoteFieldLocator, Double> quotes = marketDataService.listQuotesByField(
                new ArrayList<>(quoteFieldLocators));
        // get models
        Map<ModelLocator, String> models = modelService.loadBatch(new ArrayList<>(modelLocators));
        // get correlation matrices
        Map<CorrelationMatrixLocator, List<List<Double>>> corrMatrices = new HashMap<>();
        for (CorrelationMatrixLocator c : corrLocators) {
            try {
                corrMatrices.put(c, correlationService.correlationMatrix(c.getInstrumentIds()));
            } catch (Exception e) {
            }

        }

        // filter out those positions missing either quotes or models
        for (Tuple2<Position, PricingConfig> c : configured) {
            // check quotes
            boolean pass = true;
            for (QuoteFieldLocator l : c._2.quotes()) {
                if (!quotes.containsKey(l)) {
                    pass = false;
                    failed.add(Diagnostic.of(c._1.getPositionId(),
                            Diagnostic.Type.ERROR,
                            String.format("无法找到position %s 标的物行情: %s, %s, %s, %s",
                                    c._1.getPositionId(), l.getInstrumentId(),
                                    l.getValuationDate(), l.getInstance(), l.getField())));
                    break;
                }
            }
            // check models
            if (pass) {
                for (ModelLocator l : c._2.models()) {
                    if (!models.containsKey(l)) {
                        pass = false;
                        failed.add(Diagnostic.of(c._1.getPositionId(),
                                Diagnostic.Type.ERROR,
                                String.format("无法找到position %s 定价需要的模型: %s, %s, %s, %s",
                                        c._1.getPositionId(), l.getUnderlyer(), l.getModelName(),
                                        l.getValuationDate(), l.getInstance())));
                        break;
                    }
                }
            }
            // check correlations
            if (pass && c._2 instanceof OptionOnBasketPricingConfig) {
                if (!corrMatrices.containsKey(((OptionOnBasketPricingConfig) c._2).correlatinMatrix())) {
                    pass = false;
                    failed.add(Diagnostic.of(c._1.getPositionId(),
                            Diagnostic.Type.ERROR,
                            String.format("无法找到position %s 定价需要的相关性系数矩阵 %s", c._1.getPositionId(),
                                    ((OptionOnBasketPricingConfig) c._2).correlatinMatrix().toString())));
                }
            }
            if (pass) {
                valid.add(c);
            }
        }
        Map<Locator, Object> market = new HashMap<>(quotes);
        models.forEach(market::put);
        corrMatrices.forEach(market::put);
        return Tuple.of(failed, market, valid);
    }

    // below are scenario related methods
    @Override
    public Tuple2<List<Diagnostic>, List<ScenarioResult>> spotScenarios(
            List<CalcTypeEnum> requests,
            List<Position> positions,
            String pricingEnvironmentId,
            List<String> underlyers,
            double min, double max, int num, boolean isPercentage,
            LocalDateTime valuationDateTime,
            ZoneId timezone) {

        Tuple3<List<Diagnostic>, Map<Locator, Object>, List<Tuple2<Position, PricingConfig>>> prepared = prepare(positions, pricingEnvironmentId, valuationDateTime, timezone);

        List<Diagnostic> failed = prepared._1;

        List<ScenarioResult> spotScenarioResults = new ArrayList<>();
        List<Supplier<QuantlibCalcResults>> tasks = new ArrayList<>();
        List<SpotScenarioDescription> scenarioDescriptions = new ArrayList<>();
        for (String underlyer : underlyers) {
            Tuple3<List<Diagnostic>, List<SpotScenarioDescription>, List<Supplier<QuantlibCalcResults>>> r =
                    oneUnderlyerSpotScenarios(requests,
                            prepared._3, prepared._2, underlyer,
                            min, max, num, isPercentage, valuationDateTime, timezone);
            failed.addAll(r._1);
            scenarioDescriptions.addAll(r._2);
            tasks.addAll(r._3);
        }
        logger.info("create "+tasks.size()+" pricing tasks");
        // actual computation
        List<QuantlibCalcResults> results = execComputations(tasks);

        // clean the results: add decomposed positions etc.
        Tuple3<List<Diagnostic>, List<SpotScenarioDescription>, List<QuantlibCalcResults>> cleaned =
                cleanScenarioResults(
                scenarioDescriptions,
                results);

        List<SpotScenarioDescription> cleanedDescriptions = cleaned._2;
        List<QuantlibCalcResults> cleanedCalcResults = cleaned._3;
        for (int i = 0; i < cleanedDescriptions.size(); ++i) {
            String scenarioId = cleanedDescriptions.get(i).getScenarioId();
            QuantlibCalcResults r = cleanedCalcResults.get(i);
            if (Objects.nonNull(r)) {
                String instrumentId = cleanedDescriptions.get(i).getInstrumentId();
                String positionId = cleanedDescriptions.get(i).getPositionId();
                spotScenarioResults.add(new SpotScenarioResult(positionId,
                        scenarioId, instrumentId, r));
            } else {
                failed.add(Diagnostic.of(scenarioId, Diagnostic.Type.ERROR,
                        String.format("情景分析：%s, position %s quantlib 定价失败",
                                scenarioId, cleanedDescriptions.get(i).getPositionId())));
            }
        }
        return Tuple.of(failed, spotScenarioResults);
    }

    private Tuple3<List<Diagnostic>, List<SpotScenarioDescription>, List<Supplier<QuantlibCalcResults>>>
    oneUnderlyerSpotScenarios(
            List<CalcTypeEnum> requests, List<Tuple2<Position, PricingConfig>> positions,
            Map<Locator, Object> market, String underlyer, double min, double max, int num, boolean isPercentage,
            LocalDateTime valuationDateTime, ZoneId timezone) {
        // limit computation to options on the bumped underlyer
        Tuple2<List<Tuple2<Position, PricingConfig>>, List<Locator>> affected = getAffected(underlyer, positions);
        if (affected._1.size() == 0) {
            return Tuple.of(Collections.singletonList(Diagnostic.of(underlyer, Diagnostic.Type.WARNING,
                    "标的物 " + underlyer + " 无相应交易")),
                    Collections.emptyList(),
                    Collections.emptyList());
        }
        // generate scenarios
        List<Scenario> scenarios = generateSpotScenarios(affected._2, min, max, num, isPercentage);
        return spotScenarioTasks(requests, affected._1, market, scenarios, valuationDateTime);
    }

    private List<Scenario> generateSpotScenarios(List<Locator> keysToBump,
                                                 double min, double max, int num,
                                                 boolean isPercentage) {
        double step = (max - min) / (num - 1);
        Set<Double> spotBumps = IntStream.range(0, num)
                .mapToObj(i -> isPercentage ? min + i * step - 1.0 : min + i * step)
                .collect(Collectors.toSet());
        spotBumps.add(0.);

        return spotBumps.stream()
                .sorted()
                .map(delta -> {
                    List<ScenarioAction> scenarioActions = keysToBump.stream()
                            .map(k -> new SpotScenarioAction(k, delta, isPercentage))
                            .collect(Collectors.toList());
                    String scenarioId = "scenario_" + (isPercentage ? String.valueOf((int) ((delta + 1.0) * 100))
                            : String.valueOf(delta)) + "%";
                    return new Scenario(scenarioId, scenarioActions);
                })
                .collect(Collectors.toList());
    }

    private Tuple3<List<Diagnostic>, List<SpotScenarioDescription>, List<Supplier<QuantlibCalcResults>>>
    spotScenarioTasks(List<CalcTypeEnum> requests,
                                                                          List<Tuple2<Position, PricingConfig>> toPrice,
                                                                          Map<Locator, Object> market,
                                                                          List<Scenario> scenarios,
                                                                          LocalDateTime valuationDateTime) {
        List<Diagnostic> failed = new ArrayList<>();
        List<SpotScenarioDescription> scenarioDescriptions = new ArrayList<>();
        List<Supplier<QuantlibCalcResults>> tasks = new ArrayList<>();
        for (Scenario scenario : scenarios) {
            Either<Diagnostic, Map<Locator, Object>> bumped = scenario.apply(market);
            if (bumped.isRight()) {
                Map<Locator, Object> bumpedMarket = bumped.get();
                    String instrumentId = ((QuoteFieldLocator)((SpotScenarioAction)(scenario.getActions().get(0)))
                            .getKey()).getInstrumentId();
                scenarioDescriptions.addAll(toPrice.stream()
                        .map(p -> new SpotScenarioDescription(scenario.getId(), p._1.getPositionId(), instrumentId, p))
                        .collect(Collectors.toList()));
                tasks.addAll(planCalculations(requests, toPrice, bumpedMarket, valuationDateTime));
            } else {
                failed.add(Diagnostic.of(scenario.getId(), Diagnostic.Type.ERROR,
                        String.format("定价: 生成标的物情景失败：%s", scenario.getId())));
            }
        }
        return Tuple.of(failed, scenarioDescriptions, tasks);
    }

    private boolean hasUnderlyer(String instrumentId, PricingConfig config) {
        if (config instanceof SingleAssetPricingConfig) {
            if (Objects.isNull(((SingleAssetPricingConfig) config).underlyerPrice())) {
                return false;
            }
            return instrumentId.equals(((SingleAssetPricingConfig) config).underlyerPrice().getInstrumentId());
        } else if (config instanceof OptionOnBasketPricingConfig) {
            // multi-asset/basket underlyer
            return ((OptionOnBasketPricingConfig) config).getUnderlyerPrices().stream()
                    .map(QuoteFieldLocator::getInstrumentId)
                    .collect(Collectors.toList())
                    .contains(instrumentId);
        } else {
            // WARNING: here assumes the type of config
            return false;
        }
    }

    private Locator getLocator(PricingConfig config, String instrumentToBump) {
        if (config instanceof SingleAssetPricingConfig) {
            return ((SingleAssetPricingConfig) config).underlyerPrice();
        } else if (config instanceof OptionOnBasketPricingConfig) {
            for (QuoteFieldLocator l : ((OptionOnBasketPricingConfig) config).getUnderlyerPrices()) {
                if (l.getInstrumentId().equalsIgnoreCase(instrumentToBump)) {
                    return l;
                }
            }
        } else {
            return null;
        }
        return null;
    }

    private Tuple2<List<Tuple2<Position, PricingConfig>>, List<Locator>> getAffected(
            String instrumentToBump, List<Tuple2<Position, PricingConfig>> positions) {
        List<Tuple2<Position, PricingConfig>> affected = positions.stream()
                .filter(p -> hasUnderlyer(instrumentToBump, p._2))
                .collect(Collectors.toList());
        List<Locator> locators = affected.stream()
                .map(p -> getLocator(p._2, instrumentToBump))
                .distinct()
                .collect(Collectors.toList());
        return Tuple.of(affected, locators);
    }

    private Tuple2<List<Tuple2<Position, PricingConfig>>, List<Locator>> getVolAffected(
            String instrumentToBump, List<Tuple2<Position, PricingConfig>> positions) {
        List<Tuple2<Position, PricingConfig>> affected = new ArrayList<>();
        Set<Locator> locators = new HashSet<>();
        for (Tuple2<Position, PricingConfig> t : positions) {
            PricingConfig config = t._2;
            if (config instanceof SingleAssetPricingConfig) {
                Locator locator = ((SingleAssetPricingConfig) config).impliedVolSurface();
                if (Objects.nonNull(locator) &&
                        instrumentToBump.equalsIgnoreCase(((ModelLocator) locator).getUnderlyer())) {
                    affected.add(t);
                    locators.add(locator);
                }
            } else if (config instanceof OptionOnBasketPricingConfig) {
                for (ModelLocator l : ((OptionOnBasketPricingConfig) config).getImpliedVolSurfaces()) {
                    if (instrumentToBump.equalsIgnoreCase(l.getUnderlyer())) {
                        affected.add(t);
                        locators.add(l);
                    }
                }
            }
        }
        return Tuple.of(affected, locators.stream().distinct().collect(Collectors.toList()));
    }

    private Tuple2<List<Tuple2<Position, PricingConfig>>, List<Locator>> getDiscountingAffected(
            String instrumentToBump, List<Tuple2<Position, PricingConfig>> positions) {
        List<Tuple2<Position, PricingConfig>> affected = new ArrayList<>();
        Set<Locator> locators = new HashSet<>();
        for (Tuple2<Position, PricingConfig> t : positions) {
            PricingConfig config = t._2;
            Locator locator = null;
            if (config instanceof SingleAssetPricingConfig) {
                locator = ((SingleAssetPricingConfig) config).discountingCurve();

            } else if (config instanceof OptionOnBasketPricingConfig) {
                locator = ((OptionOnBasketPricingConfig) config).getDiscountingCurve();
            }
            if (Objects.nonNull(locator)) {
                affected.add(t);
                locators.add(locator);
            }
        }
        return Tuple.of(affected, locators.stream().distinct().collect(Collectors.toList()));
    }

    @Override
    public Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> price(
            List<CalcTypeEnum> requests, List<Position> positions, String pricingEnvironmentId,
            Double underlyerPrice, Double vol, Double r, Double q,
            LocalDateTime valuationDateTime, ZoneId timezone) {
        PricingEnvironment pricingEnvironment = getPricingEnvironment(pricingEnvironmentId, valuationDateTime);

        Tuple2<List<Diagnostic>, List<Tuple2<Position, PricingConfig>>> planned = plan(
                positions, pricingEnvironment, valuationDateTime, timezone);

        List<Diagnostic> failed = planned._1;
        List<Tuple2<Position, PricingConfig>> configured = planned._2;

        // 2. fake market data
        Map<Locator, Object> market = new HashMap<>();
        Set<QuoteFieldLocator> quoteFieldLocators = new HashSet<>();
        Set<ModelLocator> modelLocators = new HashSet<>();
        for (Tuple2<Position, PricingConfig> c : configured) {
            quoteFieldLocators.addAll(c._2.quotes());
            modelLocators.addAll(c._2.models().stream().filter(m -> m != null).collect(Collectors.toList()));
        }
        if (!quoteFieldLocators.isEmpty() && Objects.isNull(underlyerPrice)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "试定价：无标的物价格");
        }
        for (QuoteFieldLocator locator : quoteFieldLocators) {
            market.put(locator, underlyerPrice);
        }
        for (ModelLocator locator : modelLocators) {
            if (locator.getModelType() == ModelTypeEnum.VOL_SURFACE) {
                if (!market.containsKey(locator)) {
                    AtmPwcVolSurface vs = AtmPwcVolSurface.flat(valuationDateTime,
                            underlyerPrice, vol, DateTimeUtils.DAYS_IN_YEAR);
                    String handle = QuantlibObjectCache.Instance.put(vs);
                    market.put(locator, handle);
                }
            }
            if (locator.getModelType() == ModelTypeEnum.RISK_FREE_CURVE) {
                if (!market.containsKey(locator)) {
                    if (Objects.isNull(r)) {
                        throw new CustomException(ErrorCode.INPUT_NOT_VALID, "试定价：无无风险利率");
                    }
                    PwlfDiscountingCurve discountingCurve = PwlfDiscountingCurve.flat(valuationDateTime,
                            r, DateTimeUtils.DAYS_IN_YEAR);
                    String handle = QuantlibObjectCache.Instance.put(discountingCurve);
                    market.put(locator, handle);
                }
            }
            if (locator.getModelType() == ModelTypeEnum.DIVIDEND_CURVE) {
                if (!market.containsKey(locator)) {
                    if (Objects.isNull(q)) {
                        throw new CustomException(ErrorCode.INPUT_NOT_VALID, "试定价：无分红率");
                    }
                    PwlfDiscountingCurve discountingCurve = PwlfDiscountingCurve.flat(valuationDateTime,
                            q, DateTimeUtils.DAYS_IN_YEAR);
                    String handle = QuantlibObjectCache.Instance.put(discountingCurve);
                    market.put(locator, handle);
                }
            }
        }
        Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> results = priceWithMarketData(requests,
                configured, market, valuationDateTime);
        failed.addAll(results._1);
        return Tuple.of(failed, results._2);
    }

    @Override
    public Tuple2<List<Diagnostic>, List<ScenarioResult>> scenarios(
            List<CalcTypeEnum> requests,
            List<Position> positions,
            String pricingEnvironmentId,
            List<String> underlyers,
            double spotBumpMin,
            double spotBumpMax,
            int spotBumpNumber,
            boolean isSpotBumpPercent,
            double volBumpMin,
            double volBumpMax,
            int volBumpNumber,
            boolean isVolBumpPercent,
            double rBumpMin,
            double rBumpMax,
            int rBumpNumber,
            boolean isRBumpPercent,
            LocalDateTime valuationDateTime,
            ZoneId timezone) {
        //  prepare for pricing:
        //    1. decompose all positions
        //    2. plan
        //    3. gather market data/models
        //Stopwatch stopwatch = Stopwatch.createStarted();
        Tuple3<List<Diagnostic>, Map<Locator, Object>, List<Tuple2<Position, PricingConfig>>> prepared =
                prepare(positions, pricingEnvironmentId, valuationDateTime, timezone);
        //    add failed
        List<Diagnostic> failed = prepared._1;
        //    this is the market (quotes and models)
        Map<Locator, Object> market = prepared._2;
        //    valid positions
        List<Tuple2<Position, PricingConfig>> toPrice = prepared._3;
        //stopwatch.stop();
        //System.out.println("preparing: " + stopwatch);
        //stopwatch.reset();
        //stopwatch.start();
        //  base line
        List<Supplier<QuantlibCalcResults>> tasks = (planCalculations(requests, toPrice, market, valuationDateTime));
        //    tag base line calculations
        List<SpotScenarioDescription> descriptions = toPrice.stream().map(p -> new SpotScenarioDescription(
                "baseline", p._1.getPositionId(), "", p))
                .collect(Collectors.toList());
        //  for each underlyer, generate computation tasks
        for (String underlyer : underlyers) {
            Tuple3<List<Diagnostic>, List<SpotScenarioDescription>, List<Supplier<QuantlibCalcResults>>> builtTasks
                    = buildScenarioTasks(requests, toPrice, market, underlyer,
                    spotBumpMin, spotBumpMax, spotBumpNumber, isSpotBumpPercent,
                    volBumpMin, volBumpMax, volBumpNumber, isVolBumpPercent,
                    rBumpMin, rBumpMax, rBumpNumber, isRBumpPercent, valuationDateTime, timezone);
            tasks.addAll(builtTasks._3);
            descriptions.addAll(builtTasks._2);
        }
        //stopwatch.stop();
        //System.out.println("generating tasks: " + stopwatch);
        //stopwatch.reset();
        //stopwatch.start();
        // run comptuations
        List<QuantlibCalcResults> calcResults = execComputations(tasks);
        //stopwatch.stop();
        //System.out.println("computing: " + stopwatch);
        // clean the results: need to sum decomposed positions etc.
        Tuple3<List<Diagnostic>, List<SpotScenarioDescription>, List<QuantlibCalcResults>> cleaned =
                cleanScenarioResults(descriptions, calcResults);
        failed.addAll(cleaned._1);
        List<SpotScenarioDescription> cleanedDescriptions = cleaned._2;
        List<QuantlibCalcResults> cleanedCalcResults = cleaned._3;
        // go through the results and collect into final return values
        //   collect base line results
        Map<String, BlackScenarioResult> baselineResults = new HashMap<>();
        for (int i = 0; i < cleanedDescriptions.size(); ++i) {
            if (cleanedDescriptions.get(i).getScenarioId().equalsIgnoreCase("baseline")) {
                QuantlibCalcResults r = cleanedCalcResults.get(i);
                if (r instanceof QuantlibCalcResultsBlack) {
                    baselineResults.put(r.positionId(), BlackScenarioResult.from(descriptions.get(i).getInstrumentId(),
                            "original", (QuantlibCalcResultsBlack) r));
                } else if (r instanceof QuantlibCalcResultsBlackBasket) {
                    baselineResults.put(r.positionId(), BlackScenarioResult.from(descriptions.get(i).getInstrumentId(),
                            "original", (QuantlibCalcResultsBlackBasket) r));
                } else {
                    String positionId = descriptions.get(i).getPositionId();
                    failed.add(Diagnostic.of(positionId, Diagnostic.Type.ERROR,
                            "定价: position "
                                    + positionId + "quantlib定价失败"));
                }
            }
        }
        List<ScenarioResult> scenarioResults = new ArrayList<>();
        for (int i = 0; i < cleanedDescriptions.size(); ++i) {
            if (cleanedDescriptions.get(i).getScenarioId().equalsIgnoreCase("baseline")) {
                continue;
            }
            QuantlibCalcResults r = cleanedCalcResults.get(i);
            if (r instanceof QuantlibCalcResultsBlack) {
                scenarioResults.add(new BlackScenarioResult(
                        cleanedDescriptions.get(i).getPositionId(),
                        cleanedDescriptions.get(i).getScenarioId(),
                        cleanedDescriptions.get(i).getInstrumentId(),
                        r.quantity(),
                        r.getValue(CalcTypeEnum.PRICE).orElse(0.),
                        r.getValue(CalcTypeEnum.DELTA).orElse(0.),
                        r.getValue(CalcTypeEnum.GAMMA).orElse(0.),
                        r.getValue(CalcTypeEnum.VEGA).orElse(0.),
                        r.getValue(CalcTypeEnum.THETA).orElse(0.),
                        r.getValue(CalcTypeEnum.RHO_R).orElse(0.),
                        ((QuantlibCalcResultsBlack) r).underlyerPrice().orElse(0.),
                        ((QuantlibCalcResultsBlack) r).vol().orElse(0.),
                        ((QuantlibCalcResultsBlack) r).r().orElse(0.),
                        baselineResults.get(r.positionId())
                ));
            } else if (r instanceof QuantlibCalcResultsBlackBasket) {
                List<String> instrumentIds = ((QuantlibCalcResultsBlackBasket) r).getUnderlyerInstrumentIds();
                String underlyer = cleanedDescriptions.get(i).getInstrumentId();
                int idx = instrumentIds.indexOf(underlyer);
                if (idx == -1) {
                    failed.add(Diagnostic.of(r.positionId(), Diagnostic.Type.ERROR,
                            "定价: 情景 " + cleanedDescriptions.get(i).getScenarioId() + " 多标的期权position "
                                    + r.positionId() + " 标的物不包含情景标的" + underlyer));
                    continue;
                }
                scenarioResults.add(new BlackScenarioResult(
                        cleanedDescriptions.get(i).getPositionId(),
                        cleanedDescriptions.get(i).getScenarioId(),
                        underlyer,
                        ((QuantlibCalcResultsBlackBasket) r).getQuantity(),
                        ((QuantlibCalcResultsBlackBasket) r).getPrice(),
                        ((QuantlibCalcResultsBlackBasket) r).getDeltas().get(idx),
                        ((QuantlibCalcResultsBlackBasket) r).getGammas().get(idx).get(idx),
                        ((QuantlibCalcResultsBlackBasket) r).getVegas().get(idx),
                        ((QuantlibCalcResultsBlackBasket) r).getTheta(),
                        ((QuantlibCalcResultsBlackBasket) r).getRhoR(),
                        ((QuantlibCalcResultsBlackBasket) r).getUnderlyerPrices().get(idx),
                        ((QuantlibCalcResultsBlackBasket) r).getVols().get(idx),
                        ((QuantlibCalcResultsBlackBasket) r).getR(),
                        baselineResults.get(r.positionId())
                ));
            } else {
                failed.add(Diagnostic.of(cleanedDescriptions.get(i).getScenarioId(), Diagnostic.Type.ERROR,
                        "定价: 情景 " + cleanedDescriptions.get(i).getScenarioId() + " position "
                                + cleanedDescriptions.get(i).getPositionId() + "定价返回类型未知"));
            }

        }
        return Tuple.of(failed, scenarioResults);
    }

    private Tuple3<List<Diagnostic>, List<SpotScenarioDescription>, List<Supplier<QuantlibCalcResults>>>
    buildScenarioTasks(
            List<CalcTypeEnum> requests,
            List<Tuple2<Position, PricingConfig>> toPrice,
            Map<Locator, Object> market,
            String underlyer,
            double spotBumpMin, double spotBumpMax, int spotBumpNumber, boolean isSpotBumpPercent,
            double volBumpMin, double volBumpMax, int volBumpNumber, boolean isVolBumpPercent,
            double rBumpMin, double rBumpMax, int rBumpNumber, boolean isRBumpPercent,
            LocalDateTime valuationDateTime,
            ZoneId timezone) {
        List<Diagnostic> failed = new ArrayList<>();
        // results
        List<Supplier<QuantlibCalcResults>> tasks = new ArrayList<>();
        List<SpotScenarioDescription> descriptions = new ArrayList<>();
        // pricing
        //   get dependent positions and their pricing configs
        //Stopwatch stopwatch = Stopwatch.createStarted();
        Tuple2<List<Tuple2<Position, PricingConfig>>, List<Locator>> spotAffected = getAffected(underlyer, toPrice);
        //stopwatch.stop();
        //System.out.println("    getting spot dependent positions: " + stopwatch);
        //stopwatch.reset();
        //stopwatch.start();
        Tuple2<List<Tuple2<Position, PricingConfig>>, List<Locator>> volAffected = getVolAffected(underlyer, toPrice);
        //stopwatch.stop();
        //System.out.println("    getting vol dependent positions: " + stopwatch);
        //stopwatch.reset();
        //stopwatch.start();
        Tuple2<List<Tuple2<Position, PricingConfig>>, List<Locator>> discountingAffected = getDiscountingAffected(
                underlyer, toPrice);
        //stopwatch.stop();
        //System.out.println("    getting r dependent positions: " + stopwatch);
        //stopwatch.reset();
        //stopwatch.start();
        // generate tasks
        List<Double> spotBumps = generateRange(spotBumpMin, spotBumpMax, spotBumpNumber);
        List<Double> volBumps = generateRange(volBumpMin, volBumpMax, volBumpNumber);
        List<Double> rBumps = generateRange(rBumpMin, rBumpMax, rBumpNumber);
        List<Scenario> scenarios = new ArrayList<>();
        List<Tuple3<Double, Double, Double>> bumps = new ArrayList<>();
        for (int i = 0; i < spotBumpNumber; ++i) {
            for (int j = 0; j < volBumpNumber; ++j) {
                for (int k = 0; k < rBumpNumber; ++k) {
                    double spotDelta = spotBumps.get(i);
                    double volDelta = volBumps.get(j);
                    double rDelta = rBumps.get(k);
                    if (DoubleUtils.smallEnough(spotDelta, DoubleUtils.SMALL_NUMBER) &&
                            DoubleUtils.smallEnough(volDelta, DoubleUtils.SMALL_NUMBER) &&
                            DoubleUtils.smallEnough(rDelta, DoubleUtils.SMALL_NUMBER)) {
                        // this is base line (all bumps are 0)
                        continue;
                    }
                    bumps.add(Tuple.of(spotDelta, volDelta, rDelta));
                    List<ScenarioAction> actions = new ArrayList<>();
                    if (DoubleUtils.nonZero(spotDelta, DoubleUtils.SMALL_NUMBER)) {
                        actions.addAll(spotAffected._2.stream()
                                .map(l -> new SpotScenarioAction(l, spotDelta, isSpotBumpPercent))
                                .collect(Collectors.toList()));
                    }
                    if (DoubleUtils.nonZero(volDelta, DoubleUtils.SMALL_NUMBER)) {
                        actions.addAll(volAffected._2.stream()
                                .map(l -> new VolScenarioAction(l, isVolBumpPercent, volDelta))
                                .collect(Collectors.toList()));
                    }
                    if (DoubleUtils.nonZero(rDelta, DoubleUtils.SMALL_NUMBER)) {
                        actions.addAll(discountingAffected._2.stream()
                                .map(l -> new CurveScenarioAction(l, isRBumpPercent, rDelta))
                                .collect(Collectors.toList()));
                    }
                    String scenarioId = String.format("scenario_%s_%d_%d_%d", underlyer, i, j, k);
                    scenarios.add(new Scenario(scenarioId, actions));
                }
            }
        }
        //stopwatch.stop();
        //System.out.println("  generating scenarios: " + stopwatch);
        //stopwatch.reset();
        //stopwatch.start();
        for (int i = 0; i < scenarios.size(); ++i) {
            Scenario scenario = scenarios.get(i);
            Tuple3<Double, Double, Double> aBump = bumps.get(i);
            Either<Diagnostic, Map<Locator, Object>> bumped = scenario.apply(market);
            if (bumped.isRight()) {
                Map<Locator, Object> bumpedMarket = bumped.get();
                // price only those affected
                // positions
                Map<String, Tuple2<Position, PricingConfig>> affected = new HashMap<>();
                if (DoubleUtils.nonZero(aBump._1, DoubleUtils.SMALL_NUMBER)) {
                    for (Tuple2<Position, PricingConfig> t : spotAffected._1) {
                        if (!affected.containsKey(t._1.getPositionId())) {
                            affected.put(t._1.getPositionId(), t);
                        }
                    }
                }
                if (DoubleUtils.nonZero(aBump._2, DoubleUtils.SMALL_NUMBER)) {
                    for (Tuple2<Position, PricingConfig> t : volAffected._1) {
                        if (!affected.containsKey(t._1.getPositionId())) {
                            affected.put(t._1.getPositionId(), t);
                        }
                    }
                }
                if (DoubleUtils.nonZero(aBump._3, DoubleUtils.SMALL_NUMBER)) {
                    for (Tuple2<Position, PricingConfig> t : discountingAffected._1) {
                        if (!affected.containsKey(t._1.getPositionId())) {
                            affected.put(t._1.getPositionId(), t);
                        }
                    }
                }
                List<Tuple2<Position, PricingConfig>> affectedToPrice = new ArrayList<>(affected.values());
                tasks.addAll(planCalculations(requests,
                        affectedToPrice, bumpedMarket, valuationDateTime));
                descriptions.addAll(affectedToPrice.stream()
                        .map(a -> new SpotScenarioDescription(scenario.getId(), a._1.getPositionId(), underlyer, a))
                        .collect(Collectors.toList()));
            } else {
                failed.add(Diagnostic.of(scenario.getId(), Diagnostic.Type.ERROR,
                        String.format("定价: 生成标的物情景失败：%s", scenario.getId())));
            }
        }
        //stopwatch.stop();
        //System.out.println("  generating tasks: " + stopwatch);
        return Tuple.of(failed, descriptions, tasks);
    }

    private List<Double> generateRange(double min, double max, int n) {
        double delta = n == 1 ? max - min : (max - min) / (n - 1);
        return IntStream.range(0, n)
                .mapToObj(i -> min + i * delta)
                .collect(Collectors.toList());
    }

    @Override
    public Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> price(List<CalcTypeEnum> requests, Position position,
                                                                     String pricingEnvironmentId,
                                                                     List<String> underlyers,
                                                                     List<Double> underlyerPrices,
                                                                     List<Double> vols,
                                                                     double r,
                                                                     List<Double> qs,
                                                                     List<List<Double>> corrMatrix,
                                                                     LocalDateTime valuationDateTime,
                                                                     ZoneId timezone) {

        PricingEnvironment pricingEnvironment = getPricingEnvironment(pricingEnvironmentId, valuationDateTime);

        Tuple2<List<Diagnostic>, List<Tuple2<Position, PricingConfig>>> planned = plan(
                Collections.singletonList(position), pricingEnvironment, valuationDateTime, timezone);

        List<Diagnostic> failed = planned._1;
        List<Tuple2<Position, PricingConfig>> configured = planned._2;

        if (configured.size() == 0) {
            return Tuple.of(failed, new ArrayList<>());
        }

        if (!(configured.get(0)._2 instanceof OptionOnBasketPricingConfig)) {
            failed.add(Diagnostic.of(position.getPositionId(), Diagnostic.Type.ERROR, String.format(
                    "试定价: 配置position %s 失败。类型非篮子标的。", position.getPositionId()
            )));
            return Tuple.of(failed, new ArrayList<>());
        }

        OptionOnBasketPricingConfig config = (OptionOnBasketPricingConfig) configured.get(0)._2;

        // 2. fake market data
        Map<Locator, Object> market = new HashMap<>();
        //    correlation matrix
        //      check first to make sure the order of underlyers match the option's
        if (config.correlatinMatrix().getInstrumentIds().size() != underlyers.size()) {
            failed.add(Diagnostic.of(position.getPositionId(), Diagnostic.Type.ERROR, String.format(
                    "试定价: 期权标的物数量 %s 不等于输入标的物数量 %s",
                    config.correlatinMatrix().getInstrumentIds().size(),
                    underlyers.size()
            )));
            return Tuple.of(failed, new ArrayList<>());
        }
        for (int i = 0; i < underlyers.size(); ++i) {
            if (!underlyers.get(i).equalsIgnoreCase(config.correlatinMatrix().getInstrumentIds().get(i))) {
                failed.add(Diagnostic.of(position.getPositionId(), Diagnostic.Type.ERROR, String.format(
                        "试定价: 第 %s 个期权标的物 %s 与输入标的物不符",
                        i,
                        config.correlatinMatrix().getInstrumentIds().get(i)
                )));
                return Tuple.of(failed, new ArrayList<>());
            }
        }
        market.put(config.correlatinMatrix(), corrMatrix);
        //    underlyer indcies
        Map<String, Integer> underlyeIndexes = new HashMap<>();
        for (int i = 0; i < underlyers.size(); ++i) {
            underlyeIndexes.put(underlyers.get(i), i);
        }
        //    underlyers
        for (QuoteFieldLocator locator : config.getUnderlyerPrices()) {
            market.put(locator, underlyerPrices.get(underlyeIndexes.get(locator.getInstrumentId())));
        }
        //    vols
        for (ModelLocator locator : config.getImpliedVolSurfaces()) {
            double vol = vols.get(underlyeIndexes.get(locator.getUnderlyer()));
            if (!market.containsKey(locator)) {
                AtmPwcVolSurface vs = AtmPwcVolSurface.flat(valuationDateTime,
                        underlyerPrices.get(underlyeIndexes.get(locator.getUnderlyer())),
                        vol, DateTimeUtils.DAYS_IN_YEAR);
                String handle = QuantlibObjectCache.Instance.put(vs);
                market.put(locator, handle);
            }
        }
        //    risk free curve
        PwlfDiscountingCurve discountingCurve = PwlfDiscountingCurve.flat(valuationDateTime,
                r, DateTimeUtils.DAYS_IN_YEAR);
        String handle = QuantlibObjectCache.Instance.put(discountingCurve);
        market.put(config.discountingCurve(), handle);
        //    dividend curves
        if (Objects.nonNull(qs)) {
            for (ModelLocator locator : config.dividendCurves()) {
                double q = qs.get(underlyeIndexes.get(locator.getUnderlyer()));
                PwlfDiscountingCurve dividendCurve = PwlfDiscountingCurve.flat(valuationDateTime,
                        q, DateTimeUtils.DAYS_IN_YEAR);
                market.put(locator, QuantlibObjectCache.Instance.put(dividendCurve));
            }
        }

        Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> results = priceWithMarketData(requests,
                configured, market, valuationDateTime);
        failed.addAll(results._1);
        return Tuple.of(failed, results._2);
    }

    @Override
    public Tuple2<List<Diagnostic>, List<ScenarioResult>> scenarios(
            List<CalcTypeEnum> requests,
            List<Position> positions,
            String pricingEnvironmentId,
            double spot,
            double spotBumpMin,
            double spotBumpMax,
            int spotBumpNumber,
            boolean isSpotBumpPercent,
            double vol,
            double volBumpMin,
            double volBumpMax,
            int volBumpNumber,
            boolean isVolBumpPercent,
            double r,
            double rBumpMin,
            double rBumpMax,
            int rBumpNumber,
            boolean isRBumpPercent,
            Double q,
            LocalDateTime valuationDateTime,
            ZoneId timezone) {
        // exclude basket and those without underlyers
        for (Position position : positions) {
            if (!(position.getPriceable() instanceof HasUnderlyer)) {
                return Tuple.of(
                        Collections.singletonList(Diagnostic.of(
                                position.getPositionId(), Diagnostic.Type.ERROR,
                                String.format("position %s 无标的物", position.getPositionId()))),
                        Collections.emptyList());
            }
        }

        PricingEnvironment pricingEnvironment = getPricingEnvironment(pricingEnvironmentId, valuationDateTime);

        Tuple2<List<Diagnostic>, List<Tuple2<Position, PricingConfig>>> planned = plan(
                positions, pricingEnvironment, valuationDateTime, timezone);

        List<Diagnostic> failed = planned._1;
        List<Tuple2<Position, PricingConfig>> configured = planned._2;

        // 1. make sure all positions have the same underlyer
        QuoteFieldLocator underlyerLocator = null;
        for (Tuple2<Position, PricingConfig> p : configured) {
            PricingConfig config = p._2;
            if (!(config instanceof SingleAssetPricingConfig)) {
                return Tuple.of(
                        Collections.singletonList(Diagnostic.of(
                                p._1.getPositionId(), Diagnostic.Type.ERROR,
                                String.format("position %s 无标的物", p._1.getPositionId()))),
                        Collections.emptyList());
            }
            QuoteFieldLocator l = ((SingleAssetPricingConfig) config).underlyerPrice();
            if (Objects.isNull(underlyerLocator)) {
                underlyerLocator = l;
            }
            if (!underlyerLocator.equals(l)) {
                return Tuple.of(
                        Collections.singletonList(Diagnostic.of(
                                p._1.getPositionId(), Diagnostic.Type.ERROR,
                                String.format("position %s 标的物与其他position不同", p._1.getPositionId()))),
                        Collections.emptyList());
            }
        }

        // 2. fake market data
        Map<Locator, Object> market = new HashMap<>();
        Set<QuoteFieldLocator> quoteFieldLocators = new HashSet<>();
        Set<ModelLocator> modelLocators = new HashSet<>();
        for (Tuple2<Position, PricingConfig> c : configured) {
            quoteFieldLocators.addAll(c._2.quotes());
            modelLocators.addAll(c._2.models().stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
        for (QuoteFieldLocator locator : quoteFieldLocators) {
            market.put(locator, spot);
        }
        for (ModelLocator locator : modelLocators) {
            if (locator.getModelType() == ModelTypeEnum.VOL_SURFACE) {
                if (!market.containsKey(locator)) {
                    AtmPwcVolSurface vs = AtmPwcVolSurface.flat(valuationDateTime,
                            spot, vol, DateTimeUtils.DAYS_IN_YEAR);
                    String handle = QuantlibObjectCache.Instance.put(vs);
                    market.put(locator, handle);
                }
            }
            if (locator.getModelType() == ModelTypeEnum.RISK_FREE_CURVE) {
                if (!market.containsKey(locator)) {
                    PwlfDiscountingCurve discountingCurve = PwlfDiscountingCurve.flat(valuationDateTime,
                            r, DateTimeUtils.DAYS_IN_YEAR);
                    String handle = QuantlibObjectCache.Instance.put(discountingCurve);
                    market.put(locator, handle);
                }
            }
            if (locator.getModelType() == ModelTypeEnum.DIVIDEND_CURVE) {
                if (!market.containsKey(locator) && Objects.nonNull(q)) {
                    PwlfDiscountingCurve discountingCurve = PwlfDiscountingCurve.flat(valuationDateTime,
                            q, DateTimeUtils.DAYS_IN_YEAR);
                    String handle = QuantlibObjectCache.Instance.put(discountingCurve);
                    market.put(locator, handle);
                }
            }
        }
        // 3. scenarios
        Tuple3<List<Diagnostic>, List<SpotScenarioDescription>, List<Supplier<QuantlibCalcResults>>> built
                = buildScenarioTasks(requests, planned._2, market, underlyerLocator.getInstrumentId(),
                spotBumpMin, spotBumpMax, spotBumpNumber, isSpotBumpPercent,
                volBumpMin, volBumpMax, volBumpNumber, isVolBumpPercent,
                rBumpMin, rBumpMax, rBumpNumber, isRBumpPercent, valuationDateTime, timezone);
        failed.addAll(built._1);
        // run comptuations
        List<QuantlibCalcResults> calcResults = execComputations(built._3);
        // clean the results
        Tuple3<List<Diagnostic>, List<SpotScenarioDescription>, List<QuantlibCalcResults>> cleaned =
                cleanScenarioResults(built._2, calcResults);
        failed.addAll(cleaned._1);
        List<ScenarioResult> scenarioResults = new ArrayList<>();
        List<SpotScenarioDescription> cleanedDescriptions = cleaned._2;
        List<QuantlibCalcResults> cleanedCalcResults = cleaned._3;
        for (int i = 0; i < cleanedDescriptions.size(); ++i) {
            if (cleanedDescriptions.get(i).getScenarioId().equalsIgnoreCase("baseline")) {
                continue;
            }
            QuantlibCalcResults result = cleanedCalcResults.get(i);
            if (result instanceof QuantlibCalcResultsBlack) {
                scenarioResults.add(new BlackScenarioResult(
                        cleanedDescriptions.get(i).getPositionId(),
                        cleanedDescriptions.get(i).getScenarioId(),
                        cleanedDescriptions.get(i).getInstrumentId(),
                        result.quantity(),
                        result.getValue(CalcTypeEnum.PRICE).orElse(0.),
                        result.getValue(CalcTypeEnum.DELTA).orElse(0.),
                        result.getValue(CalcTypeEnum.GAMMA).orElse(0.),
                        result.getValue(CalcTypeEnum.VEGA).orElse(0.),
                        result.getValue(CalcTypeEnum.THETA).orElse(0.),
                        result.getValue(CalcTypeEnum.RHO_R).orElse(0.),
                        ((QuantlibCalcResultsBlack) result).underlyerPrice().orElse(0.),
                        ((QuantlibCalcResultsBlack) result).vol().orElse(0.),
                        ((QuantlibCalcResultsBlack) result).r().orElse(0.),
                        null
                ));
            } else if (result instanceof QuantlibCalcResultsBlackBasket) {
                List<String> instrumentIds = ((QuantlibCalcResultsBlackBasket) result).getUnderlyerInstrumentIds();
                String underlyer = cleanedDescriptions.get(i).getInstrumentId();
                int idx = instrumentIds.indexOf(underlyer);
                if (idx == -1) {
                    failed.add(Diagnostic.of(result.positionId(), Diagnostic.Type.ERROR,
                            "定价: 情景 " + cleanedDescriptions.get(i).getScenarioId() + " 多标的期权position "
                                    + result.positionId() + " 标的物不包含情景标的" + underlyer));
                    continue;
                }
                scenarioResults.add(new BlackScenarioResult(
                        cleanedDescriptions.get(i).getPositionId(),
                        cleanedDescriptions.get(i).getScenarioId(),
                        underlyer,
                        ((QuantlibCalcResultsBlackBasket) result).getQuantity(),
                        ((QuantlibCalcResultsBlackBasket) result).getPrice(),
                        ((QuantlibCalcResultsBlackBasket) result).getDeltas().get(idx),
                        ((QuantlibCalcResultsBlackBasket) result).getGammas().get(idx).get(idx),
                        ((QuantlibCalcResultsBlackBasket) result).getVegas().get(idx),
                        ((QuantlibCalcResultsBlackBasket) result).getTheta(),
                        ((QuantlibCalcResultsBlackBasket) result).getRhoR(),
                        ((QuantlibCalcResultsBlackBasket) result).getUnderlyerPrices().get(idx),
                        ((QuantlibCalcResultsBlackBasket) result).getVols().get(idx),
                        ((QuantlibCalcResultsBlackBasket) result).getR(),
                        null
                ));
            } else {
                failed.add(Diagnostic.of(cleanedDescriptions.get(i).getScenarioId(), Diagnostic.Type.ERROR,
                        "定价: 情景 " + cleanedDescriptions.get(i).getScenarioId() + " position "
                                + cleanedDescriptions.get(i).getPositionId() + "定价返回类型未知"));
            }

        }
        return Tuple.of(failed, scenarioResults);
    }

    // a hack!
    //   scenario has multiple layers:
    //     scenario -> positions -> decomposed positions
    //       for each scenario: sum over decomposed position
    //   requires uniqueness of (scenario id, underlyer, position id)
    //     position id may repeat because of decomposition
    //   also there may be errors to filter
    // this version does not handle the case
    private Tuple3<List<Diagnostic>, List<SpotScenarioDescription>, List<QuantlibCalcResults>> cleanScenarioResults(
            List<SpotScenarioDescription> descriptions,
            List<QuantlibCalcResults> results) {
        List<Diagnostic> failed = new ArrayList<>();
        List<SpotScenarioDescription> cleanedDescriptions = new ArrayList<>();
        List<QuantlibCalcResults> cleanedResults = new ArrayList<>();
        // this is a hack!
        // we will collect those with the same (scenario id, underlyer, position id),
        // knowing they come from decomposition
        Map<Tuple3<String, String, String>, QuantlibCalcResults> tmp = new HashMap<>();
        for (int i = 0; i < descriptions.size(); ++i) {
            Tuple2<Position, PricingConfig> toPrice = descriptions.get(i).getToPrice();
            Position p = toPrice._1;
            PricingConfig config = toPrice._2;
            String positionId = p.getPositionId();
            PricingConfig c = toPrice._2;
            Tuple3<String, String, String> key = Tuple.of(
                    descriptions.get(i).getScenarioId(),
                    descriptions.get(i).getInstrumentId(),
                    descriptions.get(i).getPositionId());
            if (Objects.isNull(results.get(i))) {
                failed.add(Diagnostic.of(p.getPositionId(), Diagnostic.Type.ERROR,
                        String.format("Quantlib定价position %s 失败", p.getPositionId())));
            } else {
                // below is a mess
                // now we have two different results: single asset and multi
                // they cannot be mixed
                QuantlibCalcResults r = results.get(i);
                if (r instanceof BlackResults) {
                    r.scale(p.getQuantity());
                    if (config instanceof OptionWithBaseContractPricingConfig) {
                        ((BlackResults) r)
                                .setBaseContractInstrumentId(
                                        ((OptionWithBaseContractPricingConfig) config)
                                                .getBaseContractPriceLocator().getInstrumentId());
                    }
                    ((BlackResults) r).setPositionId(p.getPositionId());
                    if (c instanceof SingleAssetPricingConfig) {
                        QuoteFieldLocator locator = ((SingleAssetPricingConfig) c).underlyerPrice();
                        if (!Objects.isNull(locator)) {
                            ((BlackResults) r).setUnderlyerInstrumentId(locator.getInstrumentId());
                        }
                    }
                } else if (r instanceof QuantlibCalcResultsBlackBasket) {
                    r = ((QuantlibCalcResultsBlackBasket) r)
                            .scaleBy(p.getQuantity())
                            .addPositionId(positionId);
                } else {
                    failed.add(Diagnostic.of(p.getPositionId(), Diagnostic.Type.ERROR,
                            String.format("无法识别Quantlib定价结果: position %s ", p.getPositionId())));
                    continue;
                }

                // this is a hack!
                if (p.decomposed()) {
                    if (tmp.containsKey(key)) {
                        // have to handle two cases: single and multi asset
                        if (r instanceof BlackResults) {
                            // add new result
                            BlackResults orig = (BlackResults) tmp.get(key);
                            // do not change quantity (it is from pre-decomposition position)
                            if (!Objects.isNull(orig.getPrice()) && !Objects.isNull(((BlackResults) r).getPrice())) {
                                orig.setResult(CalcTypeEnum.PRICE,
                                        (orig.getPrice() + ((BlackResults) r).getPrice()) / orig.quantity());
                            }
                            if (!Objects.isNull(orig.getDelta()) && !Objects.isNull(((BlackResults) r).getDelta())) {
                                orig.setResult(CalcTypeEnum.DELTA,
                                        (orig.getDelta() + ((BlackResults) r).getDelta()) / orig.quantity());
                            }
                            if (!Objects.isNull(orig.getGamma()) && !Objects.isNull(((BlackResults) r).getGamma())) {
                                orig.setResult(CalcTypeEnum.GAMMA,
                                        (orig.getGamma() + ((BlackResults) r).getGamma()) / orig.quantity());
                            }
                            if (!Objects.isNull(orig.getVega()) && !Objects.isNull(((BlackResults) r).getVega())) {
                                orig.setResult(CalcTypeEnum.VEGA,
                                        (orig.getVega() + ((BlackResults) r).getVega()) / orig.quantity());
                            }
                            if (!Objects.isNull(orig.getTheta()) && !Objects.isNull(((BlackResults) r).getTheta())) {
                                orig.setResult(CalcTypeEnum.THETA,
                                        (orig.getTheta() + ((BlackResults) r).getTheta()) / orig.quantity());
                            }
                            if (!Objects.isNull(orig.getRhoR()) && !Objects.isNull(((BlackResults) r).getRhoR())) {
                                orig.setResult(CalcTypeEnum.RHO_R,
                                        (orig.getRhoR() + ((BlackResults) r).getRhoR()) / orig.quantity());
                            }
                            if (!Objects.isNull(orig.getRhoQ()) && !Objects.isNull(((BlackResults) r).getRhoQ())) {
                                orig.setResult(CalcTypeEnum.RHO_Q,
                                        (orig.getRhoQ() + ((BlackResults) r).getRhoQ()) / orig.quantity());
                            }
                            if (!Objects.isNull(orig.getBaseContractDelta())
                                    && !Objects.isNull(((BlackResults) r).getBaseContractDelta())) {
                                orig.setBaseContractDelta(orig.getBaseContractDelta()
                                        + ((BlackResults) r).getBaseContractDelta());
                            }
                            if (!Objects.isNull(orig.getBaseContractGamma())
                                    && !Objects.isNull(((BlackResults) r).getBaseContractGamma())) {
                                orig.setBaseContractGamma(orig.getBaseContractGamma()
                                        + ((BlackResults) r).getBaseContractGamma());
                            }
                            if (!Objects.isNull(orig.getBaseContractTheta())
                                    && !Objects.isNull(((BlackResults) r).getBaseContractTheta())) {
                                orig.setBaseContractTheta(orig.getBaseContractTheta()
                                        + ((BlackResults) r).getBaseContractTheta());
                            }
                            if (!Objects.isNull(orig.getBaseContractRhoR())
                                    && !Objects.isNull(((BlackResults) r).getBaseContractRhoR())) {
                                orig.setBaseContractRhoR(orig.getBaseContractRhoR()
                                        + ((BlackResults) r).getBaseContractRhoR());
                            }
                        } else {
                            QuantlibCalcResults orig = tmp.get(key);
                            if (!(orig instanceof QuantlibCalcResultsBlackBasket)) {
                                failed.add(Diagnostic.of(positionId, Diagnostic.Type.ERROR,
                                        String.format("Position %s 分解后类型不一致：单一标的与篮子标的同时出现", positionId)));
                                continue;
                            }
                            tmp.put(key, ((QuantlibCalcResultsBlackBasket) orig)
                                    .sumWithoutQuantity((QuantlibCalcResultsBlackBasket) r));
                        }
                    } else {
                        //   from decomposition, so quantity has to be the undecomposed one's
                        // put the first decomposed position into the map
                        if (r instanceof BlackResults) {
                            ((BlackResults) r).setQuantity(p.getQuantityBeforeDecomposition());
                            tmp.put(key, r);
                        } else {
                            tmp.put(key,
                                    ((QuantlibCalcResultsBlackBasket) r)
                                            .changeQuantity(p.getQuantityBeforeDecomposition()));
                        }

                    }
                } else {
                    tmp.put(key, r);
                }
            }
        }
        for(Map.Entry<Tuple3<String, String, String>, QuantlibCalcResults> e : tmp.entrySet()) {
            cleanedDescriptions.add(
                    new SpotScenarioDescription(e.getKey()._1, e.getKey()._3, e.getKey()._2,
                            null));
            cleanedResults.add(e.getValue());
        }
        return Tuple.of(failed, cleanedDescriptions, cleanedResults);
    }
}
