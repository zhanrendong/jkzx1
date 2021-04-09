package tech.tongyu.bct.pricing.api;

//import com.google.common.base.Stopwatch;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.common.api.response.RpcResponseWithDiagnostics;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.common.util.ProfilingUtils;
import tech.tongyu.bct.exchange.dto.PositionPortfolioRecordDTO;
import tech.tongyu.bct.exchange.service.ExchangeService;
import tech.tongyu.bct.market.dto.InstrumentDTO;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.pricing.api.response.OnExchangePricingResponse;
import tech.tongyu.bct.pricing.api.response.OnExchangeResults;
import tech.tongyu.bct.pricing.common.Diagnostic;
import tech.tongyu.bct.pricing.convert.BctTradeToQuantModel;
import tech.tongyu.bct.pricing.scenario.ScenarioResult;
import tech.tongyu.bct.pricing.scenario.SpotScenarioResult;
import tech.tongyu.bct.pricing.service.PricingService;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.common.QuantlibCalcResults;
import tech.tongyu.bct.quant.library.common.impl.BlackResults;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.equity.EquityStock;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasBasketUnderlyer;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;
import tech.tongyu.bct.quant.service.cache.QuantlibObjectCache;
import tech.tongyu.bct.trade.dto.trade.TradeDTO;
import tech.tongyu.bct.trade.dto.trade.TradePositionDTO;
import tech.tongyu.bct.trade.service.PositionService;
import tech.tongyu.bct.trade.service.TradeService;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@Service
public class PricingApi {
    private final PricingService pricingService;
    private final TradeService tradeService;
    private final PositionService positionService;
    private final MarketDataService marketDataService;
    private final ExchangeService exchangeService;

    public static class PricingResponse
            implements RpcResponseWithDiagnostics<List<QuantlibCalcResults>, List<Diagnostic>> {
        @BctField(
                description = "定价结果",
                isCollection = true,
                componentClass = QuantlibCalcResults.class
        )
        private final List<QuantlibCalcResults> results;
        @BctField(
                description = "诊断",
                isCollection = true,
                componentClass = Diagnostic.class
        )
        private final List<Diagnostic> diagnostics;

        public PricingResponse(List<QuantlibCalcResults> results, List<Diagnostic> diagnostics) {
            this.results = results;
            this.diagnostics = diagnostics;
        }

        @Override
        public List<QuantlibCalcResults> getResult() {
            return results;
        }

        @Override
        public List<Diagnostic> getDiagnostics() {
            return diagnostics;
        }
    }

    public static class ScenarioResponse
            implements RpcResponseWithDiagnostics<List<ScenarioResult>, List<Diagnostic>> {
        @BctField(
                description = "情景分析结果",
                isCollection = true,
                componentClass = ScenarioResult.class
        )
        private final List<ScenarioResult> result;
        @BctField(
                description = "诊断",
                isCollection = true,
                componentClass = Diagnostic.class
        )
        private final List<Diagnostic> diagnostics;

        public ScenarioResponse(List<ScenarioResult> results, List<Diagnostic> diagnostics) {
            this.result = results;
            this.diagnostics = diagnostics;
        }

        @Override
        public List<ScenarioResult> getResult() {
            return result;
        }

        @Override
        public List<Diagnostic> getDiagnostics() {
            return diagnostics;
        }
    }

    public static class ScenarioWithPriceChange
            implements RpcResponseWithDiagnostics<List<Map<String, Object>>, List<Diagnostic>> {
        @BctField(
                description = "标的物价格变动情景分析结果"
        )
        private final List<Map<String, Object>> result;
        @BctField(
                description = "诊断",
                isCollection = true,
                componentClass = Diagnostic.class
        )
        private final List<Diagnostic> diagnostics;

        public ScenarioWithPriceChange(List<Map<String, Object>> result, List<Diagnostic> diagnostics) {
            this.result = result;
            this.diagnostics = diagnostics;
        }

        @Override
        public List<Map<String, Object>> getResult() {
            return result;
        }

        @Override
        public List<Diagnostic> getDiagnostics() {
            return diagnostics;
        }
    }

    @Autowired
    public PricingApi(
            PricingService pricingService, TradeService tradeService,
            PositionService positionService, MarketDataService marketDataService,
            ExchangeService exchangeService) {
        this.pricingService = pricingService;
        this.tradeService = tradeService;
        this.positionService = positionService;
        this.marketDataService = marketDataService;
        this.exchangeService = exchangeService;
    }

    @BctMethodInfo(
            description = "对交易定价",
            retName = "pricing",
            retDescription = "返回给定交易的定价结果",
            returnClass = PricingResponse.class,
            service = "pricing-service"
    )
    public PricingResponse prcPrice(
            @BctMethodArg(description = "定价请求", required = false) List<String> requests,
            @BctMethodArg(description = "待定价交易", required = false) List<String> tradeIds,
            @BctMethodArg(description = "定价环境名称", required = false) String pricingEnvironmentId,
            @BctMethodArg(description = "估值日", required = false) String valuationDateTime,
            @BctMethodArg(description = "估值日时区", required = false) String timezone
    ) {
        List<CalcTypeEnum> reqs = getPricingRequests(requests);
        List<BctTrade> allTrades;
        allTrades = ProfilingUtils.timed("loading trades", () -> {
            if (Objects.isNull(tradeIds) || tradeIds.size() == 0) {
                return tradeService.findAll();
            } else {
                return tradeService.listByTradeIds(tradeIds);
            }
        });
        // 过滤已经到期/行权/完全平仓交易
        allTrades = allTrades.stream()
                .filter(trade ->
                        !trade.getPositions()
                                .stream()
                                .allMatch(position -> LCMEventTypeEnum.UNWIND.equals(position.getLcmEventType()) ||
                                        LCMEventTypeEnum.EXERCISE.equals(position.getLcmEventType()) ||
                                        LCMEventTypeEnum.EXPIRATION.equals(position.getLcmEventType()))
                ).collect(Collectors.toList());

        ZonedDateTime t = DateTimeUtils.parse(valuationDateTime, timezone);
        final List<BctTrade> trades = allTrades;
        Tuple2<List<Diagnostic>, List<Position>> converted =
                ProfilingUtils.timed("convert trades", () -> BctTradeToQuantModel.fromTrades(trades));
        List<Diagnostic> failed = new ArrayList<>(converted._1);
        Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> r =
                ProfilingUtils.timed("pricing",
                        () -> pricingService.price(reqs, converted._2, pricingEnvironmentId, t.toLocalDateTime(), t.getZone()));
        failed.addAll(r._1);
        return new PricingResponse(r._2, failed);
    }

    @BctMethodInfo(
            description = "场内交易定价",
            retName = "pricing",
            retDescription = "对指定交易簿/投资组合中的场内交易定价",
            returnClass = OnExchangePricingResponse.class,
            service = "pricing-service"
    )
    public OnExchangePricingResponse prcPriceOnExchange(
            @BctMethodArg(description = "定价请求", required = false) List<String> requests,
            @BctMethodArg(description = "待定价交易簿", required = false) List<String> books,
            @BctMethodArg(description = "待定价投资组合", required = false) List<String> portfolios,
            @BctMethodArg(description = "定价环境名称", required = false) String pricingEnvironmentId,
            @BctMethodArg(description = "估值日", required = false) String valuationDateTime,
            @BctMethodArg(description = "估值日时区", required = false) String timezone
    ) {
        List<CalcTypeEnum> reqs = getPricingRequests(requests);
        ZonedDateTime t = DateTimeUtils.parse(valuationDateTime, timezone);
        Tuple4<List<Diagnostic>, List<Position>, List<InstrumentDTO>, List<PositionPortfolioRecordDTO>> exchangeTraded =
                exchangeService.searchPositionRecord(
                        Objects.isNull(portfolios) ? new ArrayList<>() : portfolios,
                        Objects.isNull(books) ? new ArrayList<>() : books,
                        t.toLocalDate());
        List<Diagnostic> failed = exchangeTraded._1;

        Map<String, Tuple2<InstrumentDTO, PositionPortfolioRecordDTO>> info = new HashMap<>();
        List<Position> mvPositions = new ArrayList<>();
        for (int i = 0; i < exchangeTraded._3.size(); ++i) {
            String positionId = exchangeTraded._2.get(i).getPositionId();
            InstrumentDTO instrumentDTO = exchangeTraded._3.get(i);
            PositionPortfolioRecordDTO positionRecordDTO = exchangeTraded._4.get(i);

            if (!info.containsKey(positionId)) {
                info.put(positionId, Tuple.of(instrumentDTO, positionRecordDTO));
            }

            mvPositions.add(new Position(positionId,
                    exchangeTraded._2.get(i).getQuantity(),
                    new EquityStock(exchangeTraded._3.get(i).getInstrumentId())));
        }
        Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> mvResults = pricingService
                .price(reqs, mvPositions, pricingEnvironmentId, t.toLocalDateTime(), t.getZone());
        failed.addAll(mvResults._1);
        Map<String, Double> marketValues = mvResults._2.stream()
                .collect(Collectors.toMap(
                        QuantlibCalcResults::positionId,
                        r -> r.getValue(CalcTypeEnum.PRICE).orElse(Double.NaN)));

        List<Position> positions = exchangeTraded._2;
        Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> r =
                pricingService.price(reqs, positions, pricingEnvironmentId, t.toLocalDateTime(), t.getZone());
        failed.addAll(r._1);

        List<OnExchangeResults> s = new ArrayList<>();
        for (int i = 0; i < r._2.size(); ++i) {
            if (r._2.get(i) instanceof BlackResults) {
                BlackResults blackResults = (BlackResults) r._2.get(i);
                String positionId = blackResults.getPositionId();
                InstrumentDTO instrumentDTO = info.get(blackResults.getPositionId())._1;
                String instrumentId = instrumentDTO.getInstrumentId();
                InstrumentDTO underlyerInstrumentDTO = instrumentId.equals(blackResults.getUnderlyerInstrumentId()) ?
                        instrumentDTO
                        : marketDataService.getInstrument(blackResults.getUnderlyerInstrumentId()).orElse(null);
                if (Objects.isNull(underlyerInstrumentDTO)) {
                    failed.add(Diagnostic.of(positionId, Diagnostic.Type.ERROR, String.format(
                            "合约信息: 无法找到场内期权 %s 标的物 %s 的合约信息",
                            instrumentId, blackResults.getUnderlyerInstrumentId())));
                    continue;
                }
                InstrumentDTO baseContractInstrumentDTO = (Objects.isNull(blackResults.getBaseContractInstrumentId())
                        || blackResults.getBaseContractInstrumentId().isEmpty()) ?
                        instrumentDTO :
                        marketDataService.getInstrument(blackResults.getBaseContractInstrumentId()).orElse(null);
                if (Objects.isNull(baseContractInstrumentDTO)) {
                    failed.add(Diagnostic.of(positionId, Diagnostic.Type.ERROR, String.format(
                            "合约信息: 无法找到场内期权 %s 基础合约 %s 的合约信息",
                            instrumentId, blackResults.getBaseContractInstrumentId())));
                    continue;
                }
                PositionPortfolioRecordDTO positionRecordDTO = info.get(blackResults.getPositionId())._2;
                OnExchangeResults onExchangeResults = new OnExchangeResults(blackResults.getPositionId(),
                        instrumentDTO, underlyerInstrumentDTO, baseContractInstrumentDTO,
                        positionRecordDTO, blackResults);
                onExchangeResults.setMarketValue(marketValues.getOrDefault(positionId, null));
                s.add(onExchangeResults);
            } else {
                failed.add(Diagnostic.of(r._2.get(i).positionId(),
                        Diagnostic.Type.ERROR, "定价结果类型非BlackResults"));
            }
        }

        // add those that fail to price. we show only position info without market value etc.
        Set<String> calculated = s.stream().map(OnExchangeResults::getPositionId).collect(Collectors.toSet());
        for (String pos : info.keySet()) {
            if (calculated.contains(pos)) {
                continue;
            }
            // pricing failed or getting instrument quote failed
            InstrumentDTO instrumentDTO = info.get(pos)._1;
            String instrumentId = instrumentDTO.getInstrumentId();
            String underlyerInstrumentId = instrumentDTO.toInstrumentInfoDTO().getUnderlyerInstrumentId();
            if (Objects.isNull(underlyerInstrumentId)) {
                underlyerInstrumentId = instrumentId;
            }
            InstrumentDTO underlyerInstrumentDTO;
            if (instrumentId.equals(underlyerInstrumentId)) {
                underlyerInstrumentDTO = instrumentDTO;
            } else {
                Optional<InstrumentDTO> dto = marketDataService.getInstrument(underlyerInstrumentId);
                if (dto.isPresent()) {
                    underlyerInstrumentDTO = dto.get();
                } else {
                    failed.add(Diagnostic.of(pos, Diagnostic.Type.ERROR, String.format(
                            "合约信息: 无法找到场内期权 %s 标的物 %s 的合约信息", instrumentId, underlyerInstrumentId)));
                    continue;
                }
            }
            PositionPortfolioRecordDTO positionRecordDTO = info.get(pos)._2;
            s.add(new OnExchangeResults(pos, instrumentDTO, underlyerInstrumentDTO, positionRecordDTO));
        }

        return new OnExchangePricingResponse(s, failed);
    }

    @BctMethodInfo(
            description = "对期权定价",
            retName = "pricing",
            retDescription = "返回给定交易的定价结果",
            returnClass = PricingResponse.class,
            service = "pricing-service",
            excelType = BctExcelTypeEnum.Json, tags = {BctApiTagEnum.Excel})
    public PricingResponse prcPricePriceable(
            @BctMethodArg(description = "定价请求", excelType = BctExcelTypeEnum.ArrayString, required = false)
                    List<String> requests,
            @BctMethodArg(required = false) String positionId,
            @BctMethodArg(description = "待定价交易") String priceable,
            @BctMethodArg(description = "定价环境名称", required = false) String pricingEnvironmentId,
            @BctMethodArg(description = "估值日", excelType = BctExcelTypeEnum.DateTime, required = false)
                    String valuationDateTime,
            @BctMethodArg(description = "估值日时区", required = false) String timezone
    ) {
        List<CalcTypeEnum> reqs = getPricingRequests(requests);
        Priceable p = (Priceable) QuantlibObjectCache.Instance.getMayThrow(priceable);
        Position position = new Position(Objects.isNull(positionId) ? "test" : positionId, 1.0, p);

        ZonedDateTime t = DateTimeUtils.parse(valuationDateTime, timezone);

        List<Diagnostic> failed = new ArrayList<>();
        Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> r =
                pricingService.price(reqs, Arrays.asList(position), pricingEnvironmentId, t.toLocalDateTime(), t.getZone());
        failed.addAll(r._1);
        return new PricingResponse(r._2, failed);
    }

    @BctMethodInfo(
            description = "获取交易",
            retName = "Load",
            retDescription = "获得交易要素",
            service = "pricing-service",
            tags = {BctApiTagEnum.Excel}, excelType = BctExcelTypeEnum.Table)
    public List<Map<String, Object>> prcTradeLoad(
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString) List<String> tradeIds
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        List<BctTrade> trades = tradeIds.stream().distinct()
                .map(id -> tradeService.getBctTradeByTradeId(id, now, now))
                .collect(Collectors.toList());
        Map<String, String> positionIdToTradeId = new HashMap<>();

        for (BctTrade trade : trades) {
            String tId = trade.tradeId;
            for (BctTradePosition bctTradePosition : trade.positions) {
                String pId = bctTradePosition.positionId;
                if (!positionIdToTradeId.containsKey(pId)) {
                    positionIdToTradeId.put(pId, tId);
                }
            }
        }

        Tuple2<List<Diagnostic>, List<Position>> converted = BctTradeToQuantModel.fromTrades(trades);
        return converted._2.stream()
                .map(p -> {
                    Map<String, Object> onePosition = new HashMap<>();
                    onePosition.put("tradeId", positionIdToTradeId.get(p.getPositionId()));
                    onePosition.put("positionId", p.getPositionId());
                    onePosition.put("quantity", p.getQuantity());
                    QuantlibObjectCache.Instance.put(p.getPriceable(), p.getPositionId());
                    return onePosition;
                })
                .collect(Collectors.toList());
    }

    @BctMethodInfo(
            description = "交易标的物价格变动情景分析",
            retName = "result",
            retDescription = "情景分析结果",
            returnClass = ScenarioWithPriceChange.class,
            service = "pricing-service"
    )
    public ScenarioWithPriceChange prcSpotScenarios(
            @BctMethodArg(description = "交易ID", required = false) List<String> trades,
            @BctMethodArg(description = "交易簿", required = false) List<String> books,
            @BctMethodArg(description = "投资组合", required = false) List<String> portfolios,
            @BctMethodArg(description = "定价请求", required = false) List<String> requests,
            @BctMethodArg(description = "标的物", required = false) List<String> underlyers,
            @BctMethodArg(description = "定价环境名称", required = false) String pricingEnvironmentId,
            @BctMethodArg(description = "估值日", required = false) String valuationDateTime,
            @BctMethodArg(description = "最小值") Number min,
            @BctMethodArg(description = "最大值") Number max,
            @BctMethodArg(description = "情景数") Number num,
            @BctMethodArg(description = "是否百分比?") boolean isPercentage,
            @BctMethodArg(description = "估值日时区", required = false) String timezone
    ) {
        ZonedDateTime t = DateTimeUtils.parse(valuationDateTime, timezone);
        List<CalcTypeEnum> reqs = getPricingRequests(requests);
        // get trades
        //   if both books and trades are not given, get all trades
        List<BctTrade> allTrades = ProfilingUtils.timed("scenario loading trades:", () -> {
            List<BctTrade> loadedTrades;
            if (!Objects.isNull(portfolios) && !portfolios.isEmpty()) {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID, "情景分析：暂不支持投资组合");
            }
            if ((Objects.isNull(books) || books.isEmpty()) && (Objects.isNull(trades) || trades.isEmpty())) {
                loadedTrades = tradeService.findAll();
            } else {
                List<String> tradeIds = Objects.isNull(trades) ? new ArrayList<>() : trades;
                OffsetDateTime now = OffsetDateTime.now();
                if (!Objects.isNull(books)) {
                    tradeIds = books.stream().flatMap(b -> tradeService.listByBookName(b, now, now).stream())
                            .collect(Collectors.toList());
                }
                loadedTrades = tradeIds.stream().distinct()
                        .map(id -> tradeService.getBctTradeByTradeId(id, t.toOffsetDateTime(), t.toOffsetDateTime()))
                        .collect(Collectors.toList());
            }

            // filter exercised/unwound/expired positions
            return loadedTrades.stream()
                    .filter(trade ->
                            !trade.getPositions()
                                    .stream()
                                    .allMatch(position -> LCMEventTypeEnum.UNWIND.equals(position.getLcmEventType()) ||
                                            LCMEventTypeEnum.EXERCISE.equals(position.getLcmEventType()) ||
                                            LCMEventTypeEnum.EXPIRATION.equals(position.getLcmEventType()))
                    ).collect(Collectors.toList());

        });
        Tuple2<List<Diagnostic>, List<Position>> converted = BctTradeToQuantModel.fromTrades(allTrades);
        List<Diagnostic> failed = new ArrayList<>(converted._1);

        // add underlyer trades from exchange-service if book ids are given
        if (!Objects.isNull(books)) {
            /*List<PositionRecordDTO> ps = exchangeService.searchPositionRecord(t.toLocalDate());
            for (PositionRecordDTO dto : ps) {
                if (!books.contains(dto.getBookId())) {
                    continue;
                }
                String instrumentId = dto.getInstrumentId();
                Optional<InstrumentDTO> info = marketDataService.getInstrument(instrumentId);
                if (!info.isPresent()) {
                    failed.add(Diagnostic.of(instrumentId, Diagnostic.Type.ERROR,
                            String.format("情景分析：无法找到场内标的物 %s ", instrumentId)));
                    continue;
                }
                converted._2.add(new Position(
                        String.format("book_%s_underlyer_%s",
                                dto.getBookId(), instrumentId),
                        dto.getNetPosition().doubleValue() * info.get().getInstrumentInfo().multiplier(),
                        new EquityStock(instrumentId)));
            }*/
            Tuple4<List<Diagnostic>, List<Position>, List<InstrumentDTO>, List<PositionPortfolioRecordDTO>> exchangeTraded =
                    exchangeService.searchPositionRecord(portfolios, books, t.toLocalDate());
            converted._2.addAll(exchangeTraded._2);
            failed.addAll(exchangeTraded._1);
        }

        Set<String> toBump;
        if (CollectionUtils.isEmpty(underlyers)) {
            // default to all underlyers
            toBump = ProfilingUtils.timed("spot scenario - get all underlyers", () -> {
                Set<String> bump_underlyers = new HashSet<>();
                for (Position p : converted._2) {
                    if (p.getPriceable() instanceof HasUnderlyer) {
                        if (((HasUnderlyer) p.getPriceable()).getUnderlyer() instanceof ExchangeListed) {
                            bump_underlyers.add(((ExchangeListed) ((HasUnderlyer) p.getPriceable())
                                    .getUnderlyer()).getInstrumentId());
                        } else {
                            failed.add(Diagnostic.of(p.getPositionId(), Diagnostic.Type.WARNING,
                                    String.format("情景分析：position %s 标的物不是场内交易品种", p.getPositionId())));
                        }
                    } else if (p.getPriceable() instanceof HasBasketUnderlyer) {
                        bump_underlyers.addAll(((HasBasketUnderlyer) p.getPriceable()).underlyers().stream()
                                .filter(u -> u instanceof ExchangeListed)
                                .map(u -> ((ExchangeListed) u).getInstrumentId()).collect(Collectors.toList()));
                    } else {
                        if (p.getPriceable() instanceof ExchangeListed) {
                            bump_underlyers.add(((ExchangeListed) p.getPriceable()).getInstrumentId());
                        } else {
                            failed.add(Diagnostic.of(p.getPositionId(), Diagnostic.Type.WARNING,
                                    String.format("情景分析：position %s 既不是场内交易品种也没有标的物", p.getPositionId())));
                        }
                    }
                }
                return bump_underlyers;
            });
        } else {
            toBump = new HashSet<>(underlyers);
        }

        Tuple2<List<Diagnostic>, List<ScenarioResult>> spotScenarioResults = pricingService.spotScenarios(reqs, converted._2,
                pricingEnvironmentId, new ArrayList<>(toBump),
                min.doubleValue(), max.doubleValue(), num.intValue(), isPercentage,
                t.toLocalDateTime(), t.getZone());
        failed.addAll(spotScenarioResults._1);

        // to show price change we have to convert quantlib result into a map ...
        List<Map<String, Object>> withPriceChange = getPriceChanges(spotScenarioResults._2);

        return new ScenarioWithPriceChange(withPriceChange, failed);
    }

    @BctMethodInfo(
            description = "标的物情景分析",
            retName = "result",
            retDescription = "对指定标的物价格情景分析",
            returnClass = ScenarioResponse.class,
            service = "pricing-service",
            excelType = BctExcelTypeEnum.DataDict, tags = {BctApiTagEnum.Excel})
    public ScenarioResponse prcPriceableSpotScenarios(
            @BctMethodArg String priceable,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString, required = false) List<String> requests,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString, required = false) List<String> underlyers,
            @BctMethodArg(required = false) String pricingEnvironmentId,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime, required = false) String valuationDateTime,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number min,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number max,
            @BctMethodArg(excelType = BctExcelTypeEnum.Double) Number num,
            @BctMethodArg(excelType = BctExcelTypeEnum.Boolean) boolean isPercentage,
            @BctMethodArg(required = false) String timezone
    ) {
        ZonedDateTime t = DateTimeUtils.parse(valuationDateTime, timezone);
        List<CalcTypeEnum> reqs = getPricingRequests(requests);
        // get priceables
        Priceable p = (Priceable) QuantlibObjectCache.Instance.getMayThrow(priceable);
        List<Diagnostic> failed = new ArrayList<>();
        Set<String> toBump;
        if (Objects.isNull(underlyers)) {
            // default to all underlyers
            toBump = new HashSet<>();

            if (p instanceof HasUnderlyer) {
                if (((HasUnderlyer) p).getUnderlyer() instanceof ExchangeListed) {
                    toBump.add(((ExchangeListed) ((HasUnderlyer) p)
                            .getUnderlyer()).getInstrumentId());
                } else {
                    failed.add(Diagnostic.of("position", Diagnostic.Type.WARNING,
                            "情景分析：标的物不是场内交易品种"));
                }
            } else if (p instanceof HasBasketUnderlyer) {
                toBump.addAll(((HasBasketUnderlyer) p).underlyers().stream()
                        .filter(u -> u instanceof ExchangeListed)
                        .map(u -> ((ExchangeListed) u).getInstrumentId()).collect(Collectors.toList()));
            } else if (p instanceof ExchangeListed) {
                toBump.add(((ExchangeListed) p).getInstrumentId());
            } else {
                failed.add(Diagnostic.of("position", Diagnostic.Type.ERROR, "情景分析：无法识别标的物"));
            }
        } else {
            toBump = new HashSet<>(underlyers);
        }
        Position position = new Position("position", 1., p);
        Tuple2<List<Diagnostic>, List<ScenarioResult>> r = pricingService.spotScenarios(reqs,
                Collections.singletonList(position),
                pricingEnvironmentId, new ArrayList<>(toBump),
                min.doubleValue(), max.doubleValue(), num.intValue(), isPercentage,
                t.toLocalDateTime(), t.getZone());
        failed.addAll(r._1);
        return new ScenarioResponse(r._2, failed);
    }

    @BctMethodInfo(
            description = "情景分析",
            retName = "result",
            retDescription = "对指定标的物价格，波动率，无风险利率进行情景分析",
            returnClass = ScenarioResponse.class,
            service = "pricing-service",
            excelType = BctExcelTypeEnum.DataDict, tags = {BctApiTagEnum.Excel})
    public ScenarioResponse prcBlackScenarios(
            @BctMethodArg(description = "交易ID", required = false) List<String> trades,
            @BctMethodArg(description = "交易簿", required = false) List<String> books,
            @BctMethodArg(description = "投资组合", required = false) List<String> portfolios,
            @BctMethodArg(description = "定价请求", required = false) List<String> requests,
            @BctMethodArg(description = "标的物", required = false) List<String> underlyers,
            @BctMethodArg(description = "定价环境名称", required = false) String pricingEnvironmentId,
            @BctMethodArg(description = "估值日", required = false) String valuationDateTime,
            @BctMethodArg(description = "标的物价格增量最小值", required = false) Number spotBumpMin,
            @BctMethodArg(description = "标的物价格增量最大值", required = false) Number spotBumpMax,
            @BctMethodArg(description = "标的物价格增量情景数", required = false) Number spotBumpNum,
            @BctMethodArg(description = "标的物价格增量是否百分比?", required = false) Boolean isSpotBumpPercentage,
            @BctMethodArg(description = "波动率增量最小值", required = false) Number volBumpMin,
            @BctMethodArg(description = "波动率增量最大值", required = false) Number volBumpMax,
            @BctMethodArg(description = "波动率增量情景数", required = false) Number volBumpNum,
            @BctMethodArg(description = "波动率增量是否百分比?", required = false) Boolean isVolBumpPercentage,
            @BctMethodArg(description = "无风险利率增量最小值", required = false) Number rBumpMin,
            @BctMethodArg(description = "无风险利率增量最大值", required = false) Number rBumpMax,
            @BctMethodArg(description = "无风险利率增量情景数", required = false) Number rBumpNum,
            @BctMethodArg(description = "无风险利率增量是否百分比?", required = false) Boolean isRBumpPercentage,
            @BctMethodArg(description = "估值日时区", required = false) String timezone
    ) {
        ZonedDateTime t = DateTimeUtils.parse(valuationDateTime, timezone);
        List<CalcTypeEnum> reqs = getPricingRequests(requests);
        // get trades
        //Stopwatch stopwatch = Stopwatch.createStarted();
        //   if both books and trades are not given, get all trades
        List<BctTrade> allTrades;
        if (!Objects.isNull(portfolios) && !portfolios.isEmpty()) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "情景分析：暂不支持投资组合");
        }
        if ((Objects.isNull(books) || books.isEmpty()) && (Objects.isNull(trades) || trades.isEmpty())) {
            allTrades = tradeService.findAll();
        } else {
            List<String> tradeIds = Objects.isNull(trades) ? new ArrayList<>() : trades;
            OffsetDateTime now = OffsetDateTime.now();
            if (!Objects.isNull(books)) {
                tradeIds = books.stream().flatMap(b -> tradeService.listByBookName(b, now, now).stream())
                        .collect(Collectors.toList());
            }
            allTrades = tradeIds.stream().distinct()
                    .map(id -> tradeService.getBctTradeByTradeId(id, t.toOffsetDateTime(), t.toOffsetDateTime()))
                    .collect(Collectors.toList());
        }

        // filter exercised/unwound/expired positions
        allTrades = allTrades.stream()
                .filter(trade ->
                        !trade.getPositions()
                                .stream()
                                .allMatch(position -> LCMEventTypeEnum.UNWIND.equals(position.getLcmEventType()) ||
                                        LCMEventTypeEnum.EXERCISE.equals(position.getLcmEventType()) ||
                                        LCMEventTypeEnum.EXPIRATION.equals(position.getLcmEventType()))
                ).collect(Collectors.toList());

        Tuple2<List<Diagnostic>, List<Position>> converted = BctTradeToQuantModel.fromTrades(allTrades);
        List<Diagnostic> failed = new ArrayList<>(converted._1);

        // add underlyer trades from exchange-service if book ids are given
        if (!Objects.isNull(books)) {
            Tuple4<List<Diagnostic>, List<Position>, List<InstrumentDTO>, List<PositionPortfolioRecordDTO>> exchangeTraded =
                    exchangeService.searchPositionRecord(portfolios, books, t.toLocalDate());
            converted._2.addAll(exchangeTraded._2);
            failed.addAll(exchangeTraded._1);
        }
        //stopwatch.stop();
        //System.out.println("loading trades: " + stopwatch);

        //stopwatch.reset();
        //stopwatch.start();
        Set<String> toBump;
        if (CollectionUtils.isEmpty(underlyers)) {
            // default to all underlyers
            toBump = new HashSet<>();
            for (Position p : converted._2) {
                if (p.getPriceable() instanceof HasUnderlyer) {
                    if (((HasUnderlyer) p.getPriceable()).getUnderlyer() instanceof ExchangeListed) {
                        toBump.add(((ExchangeListed) ((HasUnderlyer) p.getPriceable())
                                .getUnderlyer()).getInstrumentId());
                    } else {
                        failed.add(Diagnostic.of(p.getPositionId(), Diagnostic.Type.WARNING,
                                String.format("情景分析：position %s 标的物不是场内交易品种", p.getPositionId())));
                    }
                } else if (p.getPriceable() instanceof HasBasketUnderlyer) {
                    toBump.addAll(((HasBasketUnderlyer) p.getPriceable()).underlyers().stream()
                            .filter(u -> u instanceof ExchangeListed)
                            .map(u -> ((ExchangeListed) u).getInstrumentId()).collect(Collectors.toList()));
                } else {
                    if (p.getPriceable() instanceof ExchangeListed) {
                        toBump.add(((ExchangeListed) p.getPriceable()).getInstrumentId());
                    } else {
                        failed.add(Diagnostic.of(p.getPositionId(), Diagnostic.Type.WARNING,
                                String.format("情景分析：position %s 既不是场内交易品种也没有标的物", p.getPositionId())));
                    }
                }
            }
        } else {
            toBump = new HashSet<>(underlyers);
        }
        //stopwatch.stop();
        //System.out.println("getting underlyers to bump: " + stopwatch);

        Tuple2<List<Diagnostic>, List<ScenarioResult>> r = pricingService.scenarios(reqs, converted._2,
                pricingEnvironmentId, new ArrayList<>(toBump),
                Objects.isNull(spotBumpMin) ? 0. : spotBumpMin.doubleValue(),
                Objects.isNull(spotBumpMax) ? 0. : spotBumpMax.doubleValue(),
                Objects.isNull(spotBumpNum) ? 1 : spotBumpNum.intValue(),
                Objects.isNull(isSpotBumpPercentage) || isSpotBumpPercentage,
                Objects.isNull(volBumpMin) ? 0. : volBumpMin.doubleValue(),
                Objects.isNull(volBumpMax) ? 0. : volBumpMax.doubleValue(),
                Objects.isNull(volBumpNum) ? 1 : volBumpNum.intValue(),
                Objects.isNull(isVolBumpPercentage) || isVolBumpPercentage,
                Objects.isNull(rBumpMin) ? 0. : rBumpMin.doubleValue(),
                Objects.isNull(rBumpMax) ? 0. : rBumpMax.doubleValue(),
                Objects.isNull(rBumpNum) ? 1 : rBumpNum.intValue(),
                Objects.isNull(isRBumpPercentage) || isRBumpPercentage,
                t.toLocalDateTime(), t.getZone());
        failed.addAll(r._1);
        return new ScenarioResponse(r._2, failed);
    }

    @BctMethodInfo(
            description = "试定价",
            retName = "result",
            retDescription = "试定价结果",
            returnClass = PricingResponse.class,
            service = "pricing-service"
    )
    public PricingResponse prcTrial(
            @BctMethodArg() Map<String, Object> trade,
            @BctMethodArg(required = false) List<String> requests,
            @BctMethodArg(required = false) Number underlyerPrice,
            @BctMethodArg(required = false) Number vol,
            @BctMethodArg(required = false) Number r,
            @BctMethodArg(required = false) Number q,
            @BctMethodArg(required = false) String pricingEnvironmentId,
            @BctMethodArg(required = false) String valuationDateTime,
            @BctMethodArg(required = false) String timezone
    ) {
        ZonedDateTime t = DateTimeUtils.parse(valuationDateTime, timezone);
        List<Diagnostic> failed = new ArrayList<>();
        BctTrade bctTrade;
        try {
            TradeDTO dto = JsonUtils.mapper.convertValue(trade, TradeDTO.class);
            bctTrade = tradeService.fromDTO(dto, t.toOffsetDateTime());
        } catch (Exception e) {
            failed.add(Diagnostic.of("TRADE", Diagnostic.Type.ERROR, "试定价：无法解析交易"));
            return new PricingResponse(new ArrayList<>(), failed);
        }
        Tuple2<List<Diagnostic>, List<Position>> converted = BctTradeToQuantModel.from(bctTrade);
        failed.addAll(converted._1);
        Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> results = pricePositions(converted, requests,
                underlyerPrice, vol, r, q, pricingEnvironmentId, t);
        failed.addAll(results._1);
        return new PricingResponse(results._2, failed);
    }

    @BctMethodInfo(
            description = "试定价",
            retName = "result",
            retDescription = "试定价结果",
            returnClass = PricingResponse.class,
            service = "pricing-service"
    )
    public PricingResponse prcTrialPositions(
            @BctMethodArg() List<Map<String, Object>> positions,
            @BctMethodArg(required = false) List<String> requests,
            @BctMethodArg(required = false) Number underlyerPrice,
            @BctMethodArg(required = false) Number vol,
            @BctMethodArg(required = false) Number r,
            @BctMethodArg(required = false) Number q,
            @BctMethodArg(required = false) String pricingEnvironmentId,
            @BctMethodArg(required = false) String valuationDateTime,
            @BctMethodArg(required = false) String timezone
    ) {
        ZonedDateTime t = DateTimeUtils.parse(valuationDateTime, timezone);
        List<Diagnostic> failed = new ArrayList<>();
        List<BctTradePosition> bctPositions;
        try {
            bctPositions = positions.stream()
                    .map(p -> {
                        TradePositionDTO pos = JsonUtils.mapper.convertValue(p, TradePositionDTO.class);
                        pos.setCounterPartyCode("test");
                        pos.setCounterPartyName("test");
                        pos.setBookName("test");
                        pos.setPositionId("test");
                        pos.setUserLoginId("test");
                        return positionService.fromDTO(pos);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            failed.add(Diagnostic.of("POSITIONS", Diagnostic.Type.ERROR, "试定价：无法解析交易"));
            return new PricingResponse(new ArrayList<>(), failed);
        }
        Tuple2<List<Diagnostic>, List<Position>> converted = BctTradeToQuantModel.fromPositions("preTrade", bctPositions);
        failed.addAll(converted._1);
        Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> results = pricePositions(converted, requests,
                underlyerPrice, vol, r, q, pricingEnvironmentId, t);
        failed.addAll(results._1);
        return new PricingResponse(results._2, failed);
    }

    @BctMethodInfo(
            description = "篮子标的物期权试定价",
            retName = "result",
            retDescription = "试定价结果",
            returnClass = PricingResponse.class,
            service = "pricing-service"
    )
    public PricingResponse prcTrialBasketUnderlyerOption(
            @BctMethodArg() Map<String, Object> position,
            @BctMethodArg(required = false) List<String> requests,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString) List<String> underlyers,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble, required = false) List<Number> underlyerPrices,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble, required = false) List<Number> vols,
            @BctMethodArg(required = false) Number r,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble, required = false) List<Number> qs,
            @BctMethodArg(excelType = BctExcelTypeEnum.Matrix, required = false) List<List<Number>> corrMatrix,
            @BctMethodArg(required = false) String pricingEnvironmentId,
            @BctMethodArg(required = false) String valuationDateTime,
            @BctMethodArg(required = false) String timezone
    ) {
        ZonedDateTime t = DateTimeUtils.parse(valuationDateTime, timezone);
        List<Diagnostic> failed = new ArrayList<>();
        BctTradePosition bctTrade;
        try {
            TradePositionDTO pos = JsonUtils.mapper.convertValue(position, TradePositionDTO.class);
            pos.setCounterPartyCode("test");
            pos.setCounterPartyName("test");
            pos.setBookName("test");
            pos.setPositionId("test");
            pos.setUserLoginId("test");
            bctTrade = positionService.fromDTO(pos);
        } catch (Exception e) {
            failed.add(Diagnostic.of("TRADE", Diagnostic.Type.ERROR, "试定价：无法解析交易"));
            return new PricingResponse(new ArrayList<>(), failed);
        }
        Tuple2<List<Diagnostic>, List<Position>> converted = BctTradeToQuantModel
                .fromPositions("preTrade", Collections.singletonList(bctTrade));
        failed.addAll(converted._1);
        if (converted._2.size() != 1) {
            failed.add(Diagnostic.of("TRADE", Diagnostic.Type.ERROR, "试定价：篮子标的物期权试定价不支持多腿"));
            return new PricingResponse(new ArrayList<>(), failed);
        }
        Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> results;
        List<CalcTypeEnum> reqs;
        if (Objects.isNull(requests) || requests.isEmpty()) {
            reqs = Arrays.asList(CalcTypeEnum.PRICE, CalcTypeEnum.DELTA, CalcTypeEnum.GAMMA, CalcTypeEnum.VEGA,
                    CalcTypeEnum.THETA, CalcTypeEnum.RHO_R, CalcTypeEnum.RHO_Q, CalcTypeEnum.CEGA);
        } else {
            reqs = new ArrayList<>();
            requests.stream()
                    .map(String::toUpperCase)
                    .distinct()
                    .forEach(x -> {
                        try {
                            reqs.add(CalcTypeEnum.valueOf(x));
                        } catch (Exception e) {
                        }
                    });
        }
        if (Objects.isNull(underlyerPrices) && Objects.isNull(vols) && Objects.isNull(r) && Objects.isNull(qs)) {
            results = pricingService.price(reqs,
                    converted._2, pricingEnvironmentId, t.toLocalDateTime(), t.getZone());
        } else {
            results = pricingService.price(reqs, converted._2.get(0),
                    pricingEnvironmentId,
                    underlyers,
                    DoubleUtils.forceDouble(underlyerPrices),
                    DoubleUtils.forceDouble(vols),
                    r.doubleValue(),
                    DoubleUtils.forceDouble(qs),
                    DoubleUtils.numberToDoubleMatrix(corrMatrix),
                    t.toLocalDateTime(),
                    t.getZone());
        }
        failed.addAll(results._1);
        return new PricingResponse(results._2, failed);
    }

    @BctMethodInfo(
            description = "试定价情景分析",
            retName = "result",
            retDescription = "对指定标的物价格，波动率，无风险利率进行情景分析",
            returnClass = ScenarioResponse.class,
            service = "pricing-service",
            excelType = BctExcelTypeEnum.DataDict, tags = {BctApiTagEnum.Excel})
    public ScenarioResponse prcTrialScenarios(
            @BctMethodArg() List<Map<String, Object>> positions,
            @BctMethodArg(description = "定价请求", required = false) List<String> requests,
            @BctMethodArg(description = "定价环境名称", required = false) String pricingEnvironmentId,
            @BctMethodArg(description = "估值日", required = false) String valuationDateTime,
            @BctMethodArg(description = "标的物价格") double underlyerPrice,
            @BctMethodArg(description = "标的物价格增量最小值", required = false) Number spotBumpMin,
            @BctMethodArg(description = "标的物价格增量最大值", required = false) Number spotBumpMax,
            @BctMethodArg(description = "标的物价格增量情景数", required = false) Number spotBumpNum,
            @BctMethodArg(description = "标的物价格增量是否百分比?", required = false) Boolean isSpotBumpPercentage,
            @BctMethodArg(description = "波动率") double vol,
            @BctMethodArg(description = "波动率增量最小值", required = false) Number volBumpMin,
            @BctMethodArg(description = "波动率增量最大值", required = false) Number volBumpMax,
            @BctMethodArg(description = "波动率增量情景数", required = false) Number volBumpNum,
            @BctMethodArg(description = "波动率增量是否百分比?", required = false) Boolean isVolBumpPercentage,
            @BctMethodArg(description = "无风险利率") double r,
            @BctMethodArg(description = "无风险利率增量最小值", required = false) Number rBumpMin,
            @BctMethodArg(description = "无风险利率增量最大值", required = false) Number rBumpMax,
            @BctMethodArg(description = "无风险利率增量情景数", required = false) Number rBumpNum,
            @BctMethodArg(description = "无风险利率增量是否百分比?", required = false) Boolean isRBumpPercentage,
            @BctMethodArg(description = "分红/融券率", required = false) Number q,
            @BctMethodArg(description = "估值日时区", required = false) String timezone
    ) {
        ZonedDateTime t = DateTimeUtils.parse(valuationDateTime, timezone);
        List<CalcTypeEnum> reqs = getPricingRequests(requests);
        List<Diagnostic> failed = new ArrayList<>();
        List<BctTradePosition> bctPositions;
        try {
            bctPositions = positions.stream()
                    .map(p -> {
                        TradePositionDTO pos = JsonUtils.mapper.convertValue(p, TradePositionDTO.class);
                        pos.setCounterPartyCode("test");
                        pos.setCounterPartyName("test");
                        pos.setBookName("test");
                        pos.setPositionId("test");
                        pos.setUserLoginId("test");
                        return positionService.fromDTO(pos);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            failed.add(Diagnostic.of("POSITIONS", Diagnostic.Type.ERROR, "试定价：无法解析交易"));
            return new ScenarioResponse(new ArrayList<>(), failed);
        }
        Tuple2<List<Diagnostic>, List<Position>> converted = BctTradeToQuantModel.fromPositions("preTrade",
                bctPositions);
        failed.addAll(converted._1);

        Tuple2<List<Diagnostic>, List<ScenarioResult>> result = pricingService.scenarios(
                reqs,
                converted._2,
                pricingEnvironmentId,
                underlyerPrice,
                Objects.isNull(spotBumpMin) ? 0. : spotBumpMin.doubleValue(),
                Objects.isNull(spotBumpMax) ? 0. : spotBumpMax.doubleValue(),
                Objects.isNull(spotBumpNum) ? 1 : spotBumpNum.intValue(),
                Objects.isNull(isSpotBumpPercentage) || isSpotBumpPercentage,
                vol,
                Objects.isNull(volBumpMin) ? 0. : volBumpMin.doubleValue(),
                Objects.isNull(volBumpMax) ? 0. : volBumpMax.doubleValue(),
                Objects.isNull(volBumpNum) ? 1 : volBumpNum.intValue(),
                Objects.isNull(isVolBumpPercentage) || isVolBumpPercentage,
                r,
                Objects.isNull(rBumpMin) ? 0. : rBumpMin.doubleValue(),
                Objects.isNull(rBumpMax) ? 0. : rBumpMax.doubleValue(),
                Objects.isNull(rBumpNum) ? 1 : rBumpNum.intValue(),
                Objects.isNull(isRBumpPercentage) || isRBumpPercentage,
                Objects.nonNull(q) ? q.doubleValue() : null,
                t.toLocalDateTime(), t.getZone());
        failed.addAll(result._1);
        return new ScenarioResponse(result._2, failed);
    }

    private Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> pricePositions(Tuple2<List<Diagnostic>, List<Position>> converted,
                                                                               List<String> requests,
                                                                               Number underlyerPrice,
                                                                               Number vol,
                                                                               Number r,
                                                                               Number q,
                                                                               String pricingEnvironmentId,
                                                                               ZonedDateTime valuationTime
    ) {
        Tuple2<List<Diagnostic>, List<QuantlibCalcResults>> results;
        if (Objects.isNull(underlyerPrice) && Objects.isNull(vol) && Objects.isNull(r) && Objects.isNull(q)) {
            results = pricingService.price(getPricingRequests(requests),
                    converted._2, pricingEnvironmentId, valuationTime.toLocalDateTime(), valuationTime.getZone());
        } else {
            results = pricingService.price(
                    getPricingRequests(requests),
                    converted._2,
                    pricingEnvironmentId,
                    Objects.isNull(underlyerPrice) ? null : underlyerPrice.doubleValue(),
                    Objects.isNull(vol) ? null : vol.doubleValue(),
                    Objects.isNull(r) ? null : r.doubleValue(),
                    Objects.isNull(q) ? null : q.doubleValue(),
                    valuationTime.toLocalDateTime(),
                    valuationTime.getZone());
        }
        return results;
    }

    private static List<CalcTypeEnum> getPricingRequests(List<String> requests) {
        List<CalcTypeEnum> reqs = new ArrayList<>();
        if (Objects.isNull(requests) || requests.size() == 0) {
            reqs.add(CalcTypeEnum.PRICE);
            reqs.add(CalcTypeEnum.DELTA);
            reqs.add(CalcTypeEnum.GAMMA);
            reqs.add(CalcTypeEnum.VEGA);
            reqs.add(CalcTypeEnum.THETA);
            reqs.add(CalcTypeEnum.RHO_R);
            reqs.add(CalcTypeEnum.RHO_Q);
        } else {
            requests.stream()
                    .map(String::toUpperCase)
                    .distinct()
                    .forEach(r -> {
                        try {
                            reqs.add(CalcTypeEnum.valueOf(r));
                        } catch (Exception e) {
                        }
                    });
        }
        return reqs;
    }

    private static List<Map<String, Object>> getPriceChanges(List<ScenarioResult> scenarioResults) {
        // (position id, instrument id, scenario id) is unique
        Map<Tuple2<String, String>, Double> origPriceMap = new HashMap<>();
        for (ScenarioResult scenarioResult : scenarioResults) {
            String positionId = scenarioResult.positionId();
            String scenarioId = scenarioResult.scenarioId();
            String instrumentId = ((SpotScenarioResult) scenarioResult).getBumpedInstrumentId();
            if (scenarioId.equalsIgnoreCase("scenario_100%")) {
                Tuple2<String, String> k = Tuple.of(positionId, instrumentId);
                if (!origPriceMap.containsKey(k)) {
                    ((SpotScenarioResult) scenarioResult).getScenarioResult().getValue(CalcTypeEnum.PRICE)
                            .ifPresent(p -> origPriceMap.put(k, p));
                }
            }
        }
        List<Map<String, Object>> ret = new ArrayList<>();
        for (ScenarioResult scenarioResult : scenarioResults) {
            String positionId = scenarioResult.positionId();
            String instrumentId = ((SpotScenarioResult) scenarioResult).getBumpedInstrumentId();
            Tuple2<String, String> k = Tuple.of(positionId, instrumentId);
            Map<String, Object> s = JsonUtils.mapper.convertValue(scenarioResult, Map.class);
            ((Map<String, Object>) s.get("scenarioResult")).put("pnlChange", null);
            if (origPriceMap.containsKey(k)) {
                double origPrice = origPriceMap.get(k);
                ((SpotScenarioResult) scenarioResult).getScenarioResult().getValue(CalcTypeEnum.PRICE)
                        .ifPresent(p -> ((Map<String, Object>) s.get("scenarioResult")).put("pnlChange", p - origPrice));
            }
            ret.add(s);
        }
        return ret;
    }
}
