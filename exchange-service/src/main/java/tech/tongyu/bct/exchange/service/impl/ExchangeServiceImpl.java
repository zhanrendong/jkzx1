package tech.tongyu.bct.exchange.service.impl;

import com.google.common.collect.Lists;
import io.vavr.Tuple4;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.exchange.dao.dbo.local.ExchangeTradePortfolio;
import tech.tongyu.bct.exchange.dao.dbo.local.PositionRecord;
import tech.tongyu.bct.exchange.dao.dbo.local.PositionSnapshot;
import tech.tongyu.bct.exchange.dao.dbo.local.TradeRecord;
import tech.tongyu.bct.exchange.dao.repo.intel.local.ExchangeTradePortfolioRepo;
import tech.tongyu.bct.exchange.dao.repo.intel.local.PositionRecordRepo;
import tech.tongyu.bct.exchange.dao.repo.intel.local.PositionSnapshotRepo;
import tech.tongyu.bct.exchange.dao.repo.intel.local.TradeRecordRepo;
import tech.tongyu.bct.exchange.dto.*;
import tech.tongyu.bct.exchange.service.ExchangeService;
import tech.tongyu.bct.market.dto.*;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.pricing.common.Diagnostic;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.commodity.CommodityFutures;
import tech.tongyu.bct.quant.library.priceable.commodity.CommoditySpot;
import tech.tongyu.bct.quant.library.priceable.commodity.CommodityVanillaAmerican;
import tech.tongyu.bct.quant.library.priceable.commodity.CommodityVanillaEuropean;
import tech.tongyu.bct.quant.library.priceable.common.flag.ExerciseTypeEnum;
import tech.tongyu.bct.quant.library.priceable.equity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static tech.tongyu.bct.exchange.common.Constant.ONE_NEGATIVE;

@Service
public class ExchangeServiceImpl implements ExchangeService {
    private TradeRecordRepo tradeRecordRepo;
    private MarketDataService marketDataService;
    private PositionRecordRepo positionRecordRepo;
    private PositionSnapshotRepo positionSnapshotRepo;
    private ExchangeTradePortfolioRepo tradePortfolioRepo;

    @Autowired
    public ExchangeServiceImpl(TradeRecordRepo tradeRecordRepo, MarketDataService marketDataService,
                               PositionRecordRepo positionRecordRepo, PositionSnapshotRepo positionSnapshotRepo,
                               ExchangeTradePortfolioRepo tradePortfolioRepo) {
        this.tradeRecordRepo = tradeRecordRepo;
        this.marketDataService = marketDataService;
        this.positionRecordRepo = positionRecordRepo;
        this.positionSnapshotRepo = positionSnapshotRepo;
        this.tradePortfolioRepo = tradePortfolioRepo;
    }

    @Override
    public List<TradeRecordDTO> findAllTradeRecord() {
        return tradeRecordRepo.findAll()
                .stream()
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> fuzzyQueryInstrumentsInTradeRecords(String criteria) {
        List<String> result = tradeRecordRepo.findAllInstrumentId();
        if (StringUtils.isNotBlank(criteria)) {
            result = result.stream()
                    .filter(item -> item.contains(criteria))
                    .map(i -> new Pair<>(i.equals(criteria) ? -1 : i.indexOf(criteria), i))
                    .sorted(Comparator.comparing(Pair::getKey))
                    .map(Pair::getValue)
                    .collect(Collectors.toList());
        }
        return result;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TradeRecordDTO saveTradeRecordWithNewTransaction(TradeRecordDTO tradeRecordDto) {
        return saveTradeRecord(tradeRecordDto);
    }

    @Override
    @Transactional
    public TradeRecordDTO saveTradeRecordWithoutNewTransaction(TradeRecordDTO tradeRecordDto) {
        return saveTradeRecord(tradeRecordDto);
    }

    @Override
    public Optional<TradeRecordDTO> findTradeRecordByTradeId(String tradeId) {
        Optional<TradeRecord> tradeRecord = tradeRecordRepo.findByTradeId(tradeId);
        if (tradeRecord.isPresent()) {
            return Optional.ofNullable(transToDto(tradeRecord.get()));
        }
        return Optional.empty();
    }

    @Override
    public PositionRecordDTO savePosition(PositionRecordDTO positionRecordDTO) {
        PositionRecord positionRecord = positionRecordRepo.save(transToDbo(positionRecordDTO));
        return transToDto(positionRecord);
    }

    @Override
    public List<TradeRecordDTO> searchTradeRecord(List<String> instrumentIds, LocalDateTime startTime, LocalDateTime endTime) {
        List<TradeRecord> records;
        if (CollectionUtils.isEmpty(instrumentIds)) {
            records = tradeRecordRepo.findAllByDealTimeBetween(startTime, endTime);
        } else {
            records = tradeRecordRepo.findAllByInstrumentIdInAndDealTimeBetween(instrumentIds, startTime, endTime);
        }
        return records.stream()
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PositionRecordDTO> searchPositionRecord(LocalDate searchDate) {
        Map<String, PositionRecord> instrumentMap = new HashMap<>();

        positionRecordRepo.findAll().stream()
                .filter(v -> !searchDate.isBefore(v.getDealDate()))
                .forEach(record -> {
                    String instrumentId = record.getInstrumentId();
                    String bookId = record.getBookId();
                    String uniqueId = instrumentId + ":" + bookId;
                    PositionRecord positionRecord = instrumentMap.get(uniqueId);
                    if (positionRecord == null) {
                        positionRecord = record;
                    } else {
                        add(positionRecord, record);
                    }
                    instrumentMap.put(uniqueId, positionRecord);
                });
        return instrumentMap.values().stream()
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Tuple4<List<Diagnostic>, List<Position>, List<InstrumentDTO>, List<PositionPortfolioRecordDTO>> searchPositionRecord(List<String> portfolioNames, List<String> books, LocalDate searchDate) {
        List<PositionPortfolioRecordDTO> records = searchGroupedPositionRecord(portfolioNames, books, searchDate);

        return searchPositionRecord(records);
    }

    @Override
    public Tuple4<List<Diagnostic>, List<Position>, List<InstrumentDTO>, List<PositionPortfolioRecordDTO>> searchPositionRecord(List<PositionPortfolioRecordDTO> records) {

        List<Position> success = Lists.newArrayList();
        List<Diagnostic> failed = Lists.newArrayList();
        List<InstrumentDTO> instrumentInfos = Lists.newArrayList();
        List<PositionPortfolioRecordDTO> positionRecords = Lists.newArrayList();

        for (PositionPortfolioRecordDTO record : records) {
            Optional<InstrumentDTO> instrument = marketDataService.getInstrument(record.getInstrumentId());
            if (!instrument.isPresent()) {
                failed.add(Diagnostic.of(record.getInstrumentId(), Diagnostic.Type.ERROR,
                        String.format("场内持仓转定价模型：无法找到场内标的物 %s ", record.getInstrumentId())));
                continue;
            }
            InstrumentDTO info = instrument.get();
            InstrumentInfo instrumentInfo = info.getInstrumentInfo();
            Priceable priceable;
            String underlyerInstrumentId = info.getInstrumentId();

            switch (info.getAssetClass()) {
                case COMMODITY:
                    switch (info.getInstrumentType()) {
                        case SPOT:
                            priceable = new CommoditySpot(underlyerInstrumentId);
                            break;
                        case FUTURES:
                            priceable = new CommodityFutures(underlyerInstrumentId);
                            break;
                        case FUTURES_OPTION:
                            CommodityFuturesOptionInfo futuresOptionInfo = (CommodityFuturesOptionInfo) instrumentInfo;
                            underlyerInstrumentId = futuresOptionInfo.getUnderlyerInstrumentId();
                            CommodityFutures commodityFutures = new CommodityFutures(underlyerInstrumentId);
                            if (futuresOptionInfo.getExerciseType() == ExerciseTypeEnum.AMERICAN) {
                                priceable = new CommodityVanillaAmerican<>(commodityFutures, futuresOptionInfo.getStrike(),
                                        LocalDateTime.of(futuresOptionInfo.getExpirationDate(), futuresOptionInfo.getExpirationTime()),
                                        futuresOptionInfo.getOptionType());
                            } else {
                                priceable = new CommodityVanillaEuropean<>(commodityFutures, futuresOptionInfo.getStrike(),
                                        LocalDateTime.of(futuresOptionInfo.getExpirationDate(), futuresOptionInfo.getExpirationTime()),
                                        futuresOptionInfo.getOptionType());
                            }
                            break;
                        default:
                            failed.add(Diagnostic.of(record.getInstrumentId(), Diagnostic.Type.ERROR,
                                    String.format("场内持仓转定价模型：无法识别场内标的类型 %s ", info.getInstrumentType())));
                            continue;
                    }
                    break;
                case EQUITY:
                    switch (info.getInstrumentType()) {
                        case INDEX:
                            priceable = new EquityIndex(underlyerInstrumentId);
                            break;
                        case STOCK:
                            priceable = new EquityStock(underlyerInstrumentId);
                            break;
                        case FUTURES:
                        case INDEX_FUTURES:
                            priceable = new EquityIndexFutures(underlyerInstrumentId);
                            break;
                        case INDEX_OPTION:
                            EquityIndexOptionInfo indexOptionInfo = (EquityIndexOptionInfo) instrumentInfo;
                            underlyerInstrumentId = indexOptionInfo.getUnderlyerInstrumentId();
                            EquityIndex equityIndex = new EquityIndex(underlyerInstrumentId);
                            if (indexOptionInfo.getExerciseType() == ExerciseTypeEnum.AMERICAN) {
                                priceable = new EquityVanillaAmerican<>(equityIndex, indexOptionInfo.getStrike(),
                                        LocalDateTime.of(indexOptionInfo.getExpirationDate(), indexOptionInfo.getExpirationTime()),
                                        indexOptionInfo.getOptionType());
                            } else {
                                priceable = new EquityVanillaEuropean<>(equityIndex, indexOptionInfo.getStrike(),
                                        LocalDateTime.of(indexOptionInfo.getExpirationDate(), indexOptionInfo.getExpirationTime()),
                                        indexOptionInfo.getOptionType());
                            }
                            break;
                        case STOCK_OPTION:
                            EquityStockOptionInfo stockOptionInfo = (EquityStockOptionInfo) instrumentInfo;
                            underlyerInstrumentId = stockOptionInfo.getUnderlyerInstrumentId();
                            EquityStock equityStock = new EquityStock(underlyerInstrumentId);
                            if (stockOptionInfo.getExerciseType() == ExerciseTypeEnum.AMERICAN) {
                                priceable = new EquityVanillaAmerican<>(equityStock, stockOptionInfo.getStrike(),
                                        LocalDateTime.of(stockOptionInfo.getExpirationDate(), stockOptionInfo.getExpirationTime()),
                                        stockOptionInfo.getOptionType());
                            } else {
                                priceable = new EquityVanillaEuropean<>(equityStock, stockOptionInfo.getStrike(),
                                        LocalDateTime.of(stockOptionInfo.getExpirationDate(), stockOptionInfo.getExpirationTime()),
                                        stockOptionInfo.getOptionType());
                            }
                            break;
                        default:
                            failed.add(Diagnostic.of(record.getInstrumentId(), Diagnostic.Type.ERROR,
                                    String.format("场内持仓转定价模型：无法识别场内标的类型 %s ", info.getInstrumentType())));
                            continue;
                    }
                    break;
                default:
                    failed.add(Diagnostic.of(record.getInstrumentId(), Diagnostic.Type.ERROR,
                            String.format("场内持仓转定价模型：无法识别场内标的资产类型 %s ", info.getAssetClass())));
                    continue;
            }
            instrumentInfos.add(info);
            positionRecords.add(record);
            success.add(new Position(String.format("book_%s_instrument_%s_portfolio_%s", record.getBookId(), info.getInstrumentId(),
                    record.getPortfolioName()), record.getNetPosition().doubleValue() * info.getInstrumentInfo().multiplier(),
                    priceable));
        }
        return new Tuple4<>(failed, success, instrumentInfos, positionRecords);
    }

    @Override
    public List<PositionRecordDTO> searchPositionRecordGroupByInstrumentId(LocalDate searchDate, Set<String> bookNames) {
        Map<String, PositionRecord> instrumentMap = new HashMap<>();
        positionRecordRepo.findAll()
                .stream()
                .filter(v -> !searchDate.isBefore(v.getDealDate()))
                .filter(v -> bookNames.contains(v.getBookId()))
                .forEach(record -> {
                    String instrumentId = record.getInstrumentId();
                    PositionRecord positionRecord = instrumentMap.get(instrumentId);
                    if (positionRecord == null) {
                        positionRecord = record;
                    } else {
                        add(positionRecord, record);
                    }
                    instrumentMap.put(instrumentId, positionRecord);
                });
        return instrumentMap.values()
                .stream()
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    //this method is invoked by pricing Api
    public List<PositionPortfolioRecordDTO> searchGroupedPositionRecord(List<String> portfolioNames,
                                                                        List<String> bookNames,
                                                                        LocalDate searchDate) {
        PositionPortfolioRecordDTO.Key key = (CollectionUtils.isEmpty(portfolioNames) && !CollectionUtils.isEmpty(bookNames))
                ? PositionPortfolioRecordDTO.Key.BOOK_INSTRUMENT : PositionPortfolioRecordDTO.Key.PRICING_OTHERS;
        return getGroupedPositionRecord(portfolioNames, bookNames, searchDate, key);
    }

    @Override
    //this method is invoked by exchange Api
    public List<PositionPortfolioRecordDTO> searchGroupedPositionRecord(List<String> portfolioNames,
                                                                        List<String> books,
                                                                        String searchDate,
                                                                        List<String> readableBooks) {

        List<String> bookNames = readableBooks.stream()
                .filter(b -> CollectionUtils.isEmpty(books) || books.contains(b)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(bookNames)) {
            return Lists.newArrayList();
        }

        PositionPortfolioRecordDTO.Key key = PositionPortfolioRecordDTO.Key.PORTFOLIO_BOOK_INSTRUMENT;
        if (!CollectionUtils.isEmpty(portfolioNames) && CollectionUtils.isEmpty(books)) {
            key = PositionPortfolioRecordDTO.Key.PORTFOLIO_INSTRUMENT;
        }
        if (CollectionUtils.isEmpty(portfolioNames) && !CollectionUtils.isEmpty(books)) {
            key = PositionPortfolioRecordDTO.Key.BOOK_INSTRUMENT;
        }

        LocalDate parsedSearchDate = null;
        if (!StringUtils.isBlank(searchDate)) {
            parsedSearchDate = DateTimeUtils.parseToLocalDate(searchDate);
            key = PositionPortfolioRecordDTO.Key.PORTFOLIO_INSTRUMENT;
        }

        return getGroupedPositionRecord(portfolioNames, bookNames, parsedSearchDate, key);
    }

    private void add(PositionRecord addTo, PositionRecord addFrom) {
        addTo.setMarketValue(addFrom.getMarketValue().add(addTo.getMarketValue()));
        addTo.setTotalPnl(addFrom.getTotalPnl().add(addTo.getTotalPnl()));
        addTo.setLongPosition(addFrom.getLongPosition().add(addTo.getLongPosition()));
        addTo.setNetPosition(addFrom.getNetPosition().add(addTo.getNetPosition()));
        addTo.setShortPosition(addFrom.getShortPosition().add(addTo.getShortPosition()));
        addTo.setTotalBuy(addFrom.getTotalBuy().add(addTo.getTotalBuy()));
        addTo.setTotalSell(addFrom.getTotalSell().add(addTo.getTotalSell()));
        addTo.setHistoryBuyAmount(addFrom.getHistoryBuyAmount().add(addTo.getHistoryBuyAmount()));
        addTo.setHistorySellAmount(addFrom.getHistorySellAmount().add(addTo.getHistorySellAmount()));
    }

    @Override
    public List<PositionSnapshotDTO> findAllPositionSnapshot() {
        return positionSnapshotRepo.findAll()
                .stream()
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PositionSnapshotDTO> findPositionSnapshotGroupByInstrumentId() {
        Map<String, PositionSnapshot> instrumentMap = new HashMap<>();
        positionSnapshotRepo.findAll().forEach(snapshot -> {
            String instrumentId = snapshot.getInstrumentId();
            PositionSnapshot snapshotValue = instrumentMap.get(instrumentId);
            if (Objects.isNull(snapshotValue)) {
                snapshotValue = snapshot;
            } else {
                snapshotValue.setTotalPnl(snapshot.getTotalPnl().add(snapshotValue.getTotalPnl()));
                snapshotValue.setMarketValue(snapshot.getMarketValue().add(snapshotValue.getMarketValue()));
                snapshotValue.setNetPosition(snapshot.getNetPosition().add(snapshotValue.getNetPosition()));
                snapshotValue.setLongPosition(snapshot.getLongPosition().add(snapshotValue.getLongPosition()));
                snapshotValue.setShortPosition(snapshot.getShortPosition().add(snapshotValue.getShortPosition()));
                snapshotValue.setTotalBuy(snapshot.getTotalBuy().add(snapshotValue.getTotalBuy()));
                snapshotValue.setTotalSell(snapshot.getTotalSell().add(snapshotValue.getTotalSell()));
                snapshotValue.setHistoryBuyAmount(snapshot.getHistoryBuyAmount().add(snapshotValue.getHistoryBuyAmount()));
                snapshotValue.setHistorySellAmount(snapshot.getHistorySellAmount().add(snapshotValue.getHistorySellAmount()));
            }
            instrumentMap.put(instrumentId, snapshotValue);
        });

        return instrumentMap.values()
                .stream()
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void savePositionSnapshotByTradeRecords(List<TradeRecordDTO> tradeRecordDtoList) {
        tradeRecordDtoList.forEach(tradeRecordDto -> {
            InstrumentOfValuePartyRoleTypeEnum direction = tradeRecordDto.getDirection();
            OpenCloseEnum openClose = tradeRecordDto.getOpenClose();
            BigDecimal multiplier = tradeRecordDto.getMultiplier();
            BigDecimal dealAmount = tradeRecordDto.getDealAmount();
            BigDecimal dealPrice = tradeRecordDto.getDealPrice();
            BigDecimal shortPosition = BigDecimal.ZERO;
            BigDecimal longPosition = BigDecimal.ZERO;
            BigDecimal totalSell = BigDecimal.ZERO;
            BigDecimal totalBuy = BigDecimal.ZERO;
            BigDecimal sellAmount = BigDecimal.ZERO;
            BigDecimal buyAmount = BigDecimal.ZERO;
            switch (direction) {
                case SELLER:
                    totalSell = dealAmount;
                    sellAmount = dealAmount.multiply(dealPrice).multiply(multiplier);
                    if (openClose == OpenCloseEnum.OPEN) {
                        shortPosition = dealAmount;
                    } else {
                        longPosition = dealAmount.multiply(ONE_NEGATIVE);
                    }
                    break;
                case BUYER:
                    totalBuy = dealAmount;
                    buyAmount = dealAmount.multiply(dealPrice).multiply(multiplier);
                    if (openClose == OpenCloseEnum.OPEN) {
                        longPosition = dealAmount;
                    } else {
                        shortPosition = dealAmount.multiply(ONE_NEGATIVE);
                    }
                    break;
            }
            String bookId = tradeRecordDto.getBookId();
            String instrumentId = tradeRecordDto.getInstrumentId();
            // 场内持仓记录
            Optional<PositionRecord> recordOptional = positionRecordRepo.findByBookIdAndInstrumentIdAndDealDate(bookId,
                    instrumentId, tradeRecordDto.getDealTime().toLocalDate());
            PositionRecord positionRecord = new PositionRecord();
            if (recordOptional.isPresent()) {
                positionRecord = recordOptional.get();
            }
            positionRecord.setBookId(bookId);
            positionRecord.setInstrumentId(instrumentId);
            positionRecord.setDealDate(tradeRecordDto.getDealTime().toLocalDate());
            positionRecord.setTotalBuy(positionRecord.getTotalBuy().add(totalBuy));
            positionRecord.setTotalSell(positionRecord.getTotalSell().add(totalSell));
            positionRecord.setHistoryBuyAmount(positionRecord.getHistoryBuyAmount().add(buyAmount));
            positionRecord.setHistorySellAmount(positionRecord.getHistorySellAmount().add(sellAmount));
            positionRecord.setLongPosition(positionRecord.getLongPosition().add(longPosition));
            positionRecord.setShortPosition(positionRecord.getShortPosition().add(shortPosition));
            positionRecord.setNetPosition(positionRecord.getNetPosition().add(longPosition).subtract(shortPosition));
            // 场内持仓总量
            Optional<PositionSnapshot> snapshotOptional = positionSnapshotRepo.findByBookIdAndInstrumentId(bookId, instrumentId);
            PositionSnapshot positionSnapshot = new PositionSnapshot();
            if (snapshotOptional.isPresent()) {
                positionSnapshot = snapshotOptional.get();
            }
            positionSnapshot.setBookId(bookId);
            positionSnapshot.setInstrumentId(instrumentId);
            positionSnapshot.setTotalBuy(positionSnapshot.getTotalBuy().add(totalBuy));
            positionSnapshot.setTotalSell(positionSnapshot.getTotalSell().add(totalSell));
            positionSnapshot.setHistoryBuyAmount(positionSnapshot.getHistoryBuyAmount().add(buyAmount));
            positionSnapshot.setHistorySellAmount(positionSnapshot.getHistorySellAmount().add(sellAmount));
            positionSnapshot.setLongPosition(positionSnapshot.getLongPosition().add(longPosition));
            positionSnapshot.setShortPosition(positionSnapshot.getShortPosition().add(shortPosition));
            positionSnapshot.setNetPosition(positionSnapshot.getNetPosition().add(longPosition).subtract(shortPosition));
            //TODO http://jira.tongyu.tech:8080/browse/OTMS-2039 现价
            Double nowPrice = 0D;
            Optional<QuoteDTO> quoteOptional = marketDataService.getQuote(instrumentId, InstanceEnum.CLOSE,
                    positionRecord.getDealDate(), ZoneId.systemDefault());
            if (quoteOptional.isPresent()) {
                QuoteDTO quoteDto = quoteOptional.get();
                Map<QuoteFieldEnum, Double> fields = quoteDto.getFields();
                nowPrice = fields.get(QuoteFieldEnum.CLOSE);
            }
            positionRecord.setMarketValue(positionRecord.getNetPosition()
                    .multiply(BigDecimal.valueOf(nowPrice)).multiply(multiplier));
            positionRecord.setTotalPnl(positionRecord.getMarketValue().add(positionRecord.getHistorySellAmount())
                    .subtract(positionRecord.getHistoryBuyAmount()));

            positionSnapshot.setMarketValue(positionSnapshot.getNetPosition()
                    .multiply(BigDecimal.valueOf(nowPrice)).multiply(multiplier));
            positionSnapshot.setTotalPnl(positionSnapshot.getMarketValue().add(positionSnapshot.getHistorySellAmount())
                    .subtract(positionSnapshot.getHistoryBuyAmount()));

            positionRecordRepo.save(positionRecord);
            positionSnapshotRepo.save(positionSnapshot);
        });
    }

    private TradeRecordDTO transToDto(TradeRecord tradeRecord) {
        UUID uuid = tradeRecord.getUuid();
        TradeRecordDTO tradeRecordDto = new TradeRecordDTO();
        BeanUtils.copyProperties(tradeRecord, tradeRecordDto);
        tradeRecordDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        List<String> Portfolios = tradePortfolioRepo.findByTradeId(tradeRecordDto.getTradeId()).stream().
                map(ExchangeTradePortfolio::getPortfolioName).distinct().collect(Collectors.toList());
        tradeRecordDto.setPortfolioNames(Portfolios);

        return tradeRecordDto;
    }

    private TradeRecord transToDbo(TradeRecordDTO tradeRecordDto) {
        String uuid = tradeRecordDto.getUuid();
        TradeRecord tradeRecord = new TradeRecord();
        BeanUtils.copyProperties(tradeRecordDto, tradeRecord);
        tradeRecord.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));

        return tradeRecord;
    }

    private PositionRecord transToDbo(PositionRecordDTO positionRecordDTO) {
        String uuid = positionRecordDTO.getUuid();
        PositionRecord positionRecord = new PositionRecord();
        BeanUtils.copyProperties(positionRecordDTO, positionRecord);
        positionRecord.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return positionRecord;
    }

    private PositionRecordDTO transToDto(PositionRecord positionRecord) {
        UUID uuid = positionRecord.getUuid();
        PositionRecordDTO positionRecordDTO = new PositionRecordDTO();
        BeanUtils.copyProperties(positionRecord, positionRecordDTO);
        positionRecordDTO.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        return positionRecordDTO;
    }

    private PositionSnapshotDTO transToDto(PositionSnapshot positionSnapshot) {
        UUID uuid = positionSnapshot.getUuid();
        PositionSnapshotDTO positionSnapshotDto = new PositionSnapshotDTO();
        BeanUtils.copyProperties(positionSnapshot, positionSnapshotDto);
        positionSnapshotDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());

        return positionSnapshotDto;
    }

    private List<PositionPortfolioRecordDTO> getGroupedPositionRecord(List<String> portfolioNames,
                                                                      List<String> bookNames,
                                                                      LocalDate searchDate,
                                                                      PositionPortfolioRecordDTO.Key key) {
        Map<String, TradeRecord> tradeRecords = tradeRecordRepo.findAll()
                .stream()
                .filter(i -> (CollectionUtils.isEmpty(bookNames) || bookNames.contains(i.getBookId()))
                        && (searchDate == null || !searchDate.isBefore(i.getDealTime().toLocalDate())))
                .collect(Collectors.toMap(TradeRecord::getTradeId, v -> v, (k1, k2) -> k1));

        List<PositionPortfolioRecordDTO> positionPortfolioRecords;
        if (key == PositionPortfolioRecordDTO.Key.BOOK_INSTRUMENT) {
            positionPortfolioRecords = tradeRecords.values().stream()
                    .map(item -> transTradeRecord(item, null, key))
                    .collect(Collectors.toList());
        } else {
            List<ExchangeTradePortfolio> tradePortfolios = tradePortfolioRepo.findAll();
            positionPortfolioRecords = tradePortfolios.stream()
                    .filter(i -> tradeRecords.keySet().contains(i.getTradeId()) &&
                            (CollectionUtils.isEmpty(portfolioNames) || portfolioNames.contains(i.getPortfolioName())))
                    .map(tp -> transTradeRecord(tradeRecords.get(tp.getTradeId()), tp.getPortfolioName(), key))
                    .collect(Collectors.toList());
            if (key == PositionPortfolioRecordDTO.Key.PRICING_OTHERS && CollectionUtils.isEmpty(portfolioNames)) {
                Set<String> tradesWithPortfolio = tradePortfolios.stream().map(ExchangeTradePortfolio::getTradeId).collect(Collectors.toSet());
                Set<String> tradesWithoutPortfolio = tradeRecords.keySet();
                tradesWithoutPortfolio.removeAll(tradesWithPortfolio);
                List<PositionPortfolioRecordDTO> positionPortfolioRecordWithoutPortfolio = tradesWithoutPortfolio.stream()
                        .map(tradeId -> transTradeRecord(tradeRecords.get(tradeId), null, key)).collect(Collectors.toList());
                positionPortfolioRecords.addAll(positionPortfolioRecordWithoutPortfolio);
            }
        }

        return positionPortfolioRecords.stream()
                .collect(Collectors.groupingBy(item -> Optional.ofNullable(item.getPortfolioName()),
                        Collectors.groupingBy(item -> Optional.ofNullable(item.getBookId()),
                                Collectors.groupingBy(item -> Optional.ofNullable(item.getInstrumentId())))))
                .values()
                .stream()
                .flatMap(item -> item.values().stream().flatMap(inner -> inner.values().stream()))
                .map(records -> {
                    PositionPortfolioRecordDTO base = records.get(0);
                    records.remove(0);
                    records.forEach(item -> base.add(item));
                    return base;
                }).sorted(Comparator.comparing(PositionPortfolioRecordDTO::getPortfolioName, Comparator.nullsLast(String::compareTo)).
                        thenComparing(PositionPortfolioRecordDTO::getBookId, Comparator.nullsLast(String::compareTo)).
                        thenComparing(PositionPortfolioRecordDTO::getInstrumentId, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    private TradeRecordDTO saveTradeRecord(TradeRecordDTO tradeRecordDto) {
        String instrumentId = tradeRecordDto.getInstrumentId();
        Integer multiplier = 1;
        Optional<InstrumentDTO> instrument = marketDataService.getInstrument(instrumentId);
        if (instrument.isPresent()) {
            multiplier = instrument.get().getInstrumentInfo().multiplier();
            if (Objects.isNull(multiplier)) multiplier = 1;
        } else {
            throw new IllegalArgumentException("标的物: " + instrumentId + "不存在");
        }
        tradeRecordDto.setMultiplier(BigDecimal.valueOf(multiplier));
        TradeRecord tradeRecord = tradeRecordRepo.save(transToDbo(tradeRecordDto));

        return transToDto(tradeRecord);
    }

    private PositionPortfolioRecordDTO transTradeRecord(TradeRecord trade, String portfolioName, PositionPortfolioRecordDTO.Key key) {
        InstrumentOfValuePartyRoleTypeEnum direction = trade.getDirection();
        OpenCloseEnum openClose = trade.getOpenClose();
        BigDecimal multiplier = trade.getMultiplier();
        BigDecimal dealAmount = trade.getDealAmount();
        BigDecimal dealPrice = trade.getDealPrice();
        BigDecimal shortPosition = BigDecimal.ZERO;
        BigDecimal longPosition = BigDecimal.ZERO;
        BigDecimal totalSell = BigDecimal.ZERO;
        BigDecimal totalBuy = BigDecimal.ZERO;
        BigDecimal sellAmount = BigDecimal.ZERO;
        BigDecimal buyAmount = BigDecimal.ZERO;
        if (direction == InstrumentOfValuePartyRoleTypeEnum.SELLER) {
            totalSell = dealAmount;
            sellAmount = dealAmount.multiply(dealPrice).multiply(multiplier);
            if (openClose == OpenCloseEnum.OPEN) {
                shortPosition = dealAmount;
            } else {
                longPosition = dealAmount.multiply(ONE_NEGATIVE);
            }
        } else {
            totalBuy = dealAmount;
            buyAmount = dealAmount.multiply(dealPrice).multiply(multiplier);
            if (openClose == OpenCloseEnum.OPEN) {
                longPosition = dealAmount;
            } else {
                shortPosition = dealAmount.multiply(ONE_NEGATIVE);
            }
        }
        Double nowPrice = 0D;
        String instrumentId = trade.getInstrumentId();
        Optional<QuoteDTO> quoteOptional = marketDataService.getQuote(instrumentId, InstanceEnum.CLOSE,
                trade.getDealTime().toLocalDate(), ZoneId.systemDefault());
        if (quoteOptional.isPresent()) {
            QuoteDTO quoteDto = quoteOptional.get();
            Map<QuoteFieldEnum, Double> fields = quoteDto.getFields();
            nowPrice = fields.get(QuoteFieldEnum.CLOSE);
        }

        BigDecimal marketValue = longPosition.subtract(shortPosition).
                multiply(BigDecimal.valueOf(nowPrice)).multiply(multiplier);
        BigDecimal totalPnl = marketValue.add(sellAmount).subtract(buyAmount);

        return new PositionPortfolioRecordDTO(portfolioName,
                key == PositionPortfolioRecordDTO.Key.PORTFOLIO_INSTRUMENT ? null : trade.getBookId(),
                trade.getInstrumentId(), longPosition, shortPosition, longPosition.subtract(shortPosition),
                totalSell, totalBuy, buyAmount, sellAmount, marketValue, totalPnl);
    }
}
