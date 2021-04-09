package tech.tongyu.bct.trade.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.feature.InitialSpotFeature;
import tech.tongyu.bct.cm.product.iov.feature.NotionalFeature;
import tech.tongyu.bct.cm.product.iov.feature.OptionExerciseFeature;
import tech.tongyu.bct.cm.product.iov.feature.UnderlyerFeature;
import tech.tongyu.bct.cm.reference.impl.UnitEnum;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.TradeStatusEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.common.util.ProfilingUtils;
import tech.tongyu.bct.common.util.TimeUtils;
import tech.tongyu.bct.market.dto.InstanceEnum;
import tech.tongyu.bct.market.dto.QuoteDTO;
import tech.tongyu.bct.market.dto.QuoteFieldEnum;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.trade.dao.dbo.*;
import tech.tongyu.bct.trade.dao.repo.*;
import tech.tongyu.bct.trade.dto.TradeEventTypeEnum;
import tech.tongyu.bct.trade.dto.event.CashFlowCollectionDTO;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationInfoDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMUnwindAmountDTO;
import tech.tongyu.bct.trade.manager.PositionManager;
import tech.tongyu.bct.trade.manager.TradeManager;
import tech.tongyu.bct.trade.manager.TradePositionManager;
import tech.tongyu.bct.trade.service.*;
import tech.tongyu.bct.trade.service.impl.lcm.ExerciseProcessorCommon;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TradeLCMServiceImpl implements TradeLCMService {

    @PersistenceContext
    private EntityManager entityManager;

    private static Logger logger = LoggerFactory.getLogger(TradeLCMServiceImpl.class);

    List<LCMCompositeProcessor> assetLCMProcessors;

    TradePositionIndexRepo tradePositionIndexRepo;

    TradePositionManager tradePositionManager;

    LCMNotificationRepo notificationEventRepo;

    TradeEventComponent tradeEventComponent;

    PositionLCMService positionLCMService;

    TradeCashFlowRepo tradeCashFlowRepo;

    PositionManager positionManager;

    TradeEventRepo tradeEventRepo;

    TradeService tradeService;

    TradeManager tradeManager;

    LCMEventRepo lcmEventRepo;

    MarketDataService marketDataService;

    @Autowired
    public TradeLCMServiceImpl(LCMEventRepo lcmEventRepo,
                               TradeService tradeService,
                               MarketDataService marketDataService,
                               TradeManager tradeManager,
                               TradeEventRepo tradeEventRepo,
                               PositionManager positionManager,
                               TradeCashFlowRepo tradeCashFlowRepo,
                               PositionLCMService positionLCMService,
                               TradeEventComponent tradeEventComponent,
                               LCMNotificationRepo notificationEventRepo,
                               TradePositionManager tradePositionManager,
                               TradePositionIndexRepo tradePositionIndexRepo,
                               List<LCMCompositeProcessor> assetLCMProcessors) {
        this.lcmEventRepo = lcmEventRepo;
        this.tradeService = tradeService;
        this.marketDataService = marketDataService;
        this.tradeManager = tradeManager;
        this.tradeEventRepo = tradeEventRepo;
        this.positionManager = positionManager;
        this.tradeCashFlowRepo = tradeCashFlowRepo;
        this.positionLCMService = positionLCMService;
        this.assetLCMProcessors = assetLCMProcessors;
        this.tradeEventComponent = tradeEventComponent;
        this.tradePositionManager = tradePositionManager;
        this.notificationEventRepo = notificationEventRepo;
        this.tradePositionIndexRepo = tradePositionIndexRepo;
    }

    @Override
    @Transactional
    public void repairHistoryTradeCashFlow() {
        Set<UUID> uuidSet = new HashSet<>();
        lcmEventRepo.findAll().stream()
                .filter(lcmEvent -> Objects.isNull(lcmEvent.getPremium()) || Objects.isNull(lcmEvent.getCashFlow()))
                .map(lcmEvent -> {
                    List<TradeCashFlow> cashFlows = tradeCashFlowRepo.findByTradeIdAndPositionIdAndLcmEventType
                            (lcmEvent.getTradeId(), lcmEvent.getPositionId(), lcmEvent.getEventType());
                    BigDecimal premium = null;
                    BigDecimal cashFlow = null;
                    if (CollectionUtils.isNotEmpty(cashFlows)){
                        TradeCashFlow tradeCashFlow = null;
                        if (cashFlows.size() == 1){
                            tradeCashFlow = cashFlows.get(0);
                        }else{
                            for (TradeCashFlow tradeCash : cashFlows){
                                if (!uuidSet.contains(tradeCash.getUuid())){
                                    tradeCashFlow = tradeCash;
                                    uuidSet.add(tradeCash.getUuid());
                                    break;
                                }
                            }
                        }
                        if (Objects.nonNull(tradeCashFlow)){
                            premium = tradeCashFlow.getPremium();
                            cashFlow = tradeCashFlow.getCashFlow();
                        }
                    }
                    if (Objects.isNull(lcmEvent.getCashFlow())){
                        lcmEvent.setCashFlow(Objects.isNull(cashFlow) ? BigDecimal.ZERO : cashFlow);
                    }
                    if (Objects.isNull(lcmEvent.getPremium())){
                        lcmEvent.setPremium(Objects.isNull(premium) ? BigDecimal.ZERO : premium);
                    }
                    return lcmEventRepo.save(lcmEvent);
                }).collect(Collectors.toList());
    }

    @Override
    public List<LCMEventDTO> listAllCashFlow() {
        return lcmEventRepo.findAll()
                .parallelStream()
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CashFlowCollectionDTO> listCashFlowCollection(){
        Map<String, CashFlowCollectionDTO> cashFlowMap = new HashMap<>();
        lcmEventRepo.findAll().forEach(lcmEvent -> {
            CashFlowCollectionDTO collectionDto = cashFlowMap.get(lcmEvent.getPositionId());
            if (Objects.isNull(collectionDto)){
                collectionDto = new CashFlowCollectionDTO(lcmEvent.getTradeId(), lcmEvent.getPositionId());
            }
            switch(lcmEvent.getEventType()){
                case OPEN:
                    collectionDto.setOpen(collectionDto.open.add(lcmEvent.getCashFlow()));
                    break;
                case AMEND:
                    collectionDto.setOpen(collectionDto.open.add(lcmEvent.getPremium()));
                    collectionDto.setOther(collectionDto.other.add(lcmEvent.getCashFlow()));
                    break;
                case UNWIND:
                case UNWIND_PARTIAL:
                    collectionDto.setUnwind(collectionDto.unwind.add(lcmEvent.getCashFlow()));
                    break;
                default:
                    collectionDto.setSettle(collectionDto.settle.add(lcmEvent.getCashFlow()));
                    break;
            }
            cashFlowMap.put(lcmEvent.getPositionId(), collectionDto);
        });
        return cashFlowMap.values()
                .stream()
                .peek(collectionDto -> {
                    LCMUnwindAmountDTO unwindDto = getUnwindAmounts(collectionDto.positionId, collectionDto.tradeId);
                    BigDecimal initialValue = unwindDto.getInitialValue();
                    BigDecimal historyValue = unwindDto.getHistoryValue();
                    BigDecimal initialNumber = initialValue;
                    BigDecimal unwindNumber = historyValue;
                    if (UnitEnum.CNY.equals(unwindDto.getValueUnit())){
                        if (BigDecimal.ZERO.compareTo(unwindDto.getInitialSpot()) == 0){
                            collectionDto.setErrorMessage(String.format("交易编号[%s],持仓编号[%s]合约期初价格为0,请修正合约信息",
                                    collectionDto.tradeId, collectionDto.positionId));
                        }
                        if (StringUtils.isBlank(collectionDto.errorMessage)) {
                            BigDecimal scale = unwindDto.getInitialSpot().multiply(unwindDto.getUnderlyerMultiplier());
                            initialNumber = initialValue.divide(scale, 10, BigDecimal.ROUND_DOWN);
                            unwindNumber = historyValue.divide(scale, 10, BigDecimal.ROUND_DOWN);
                        }
                    }
                    collectionDto.setInitialNumber(initialNumber);
                    collectionDto.setUnwindNumber(unwindNumber);
                }).collect(Collectors.toList());
    }

    @Override
    public Boolean lcmEventExistByUUID(UUID uuid) {
        return lcmEventRepo.existsById(uuid);
    }

    @Override
    public List<LCMEventDTO> listCashFlowsByTradeId(String tradeId) {
        if (StringUtils.isBlank(tradeId)) {
            throw new IllegalArgumentException("请输入交易编号tradeId");
        }
        return lcmEventRepo.findAllByTradeId(tradeId)
                .stream()
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LCMEventDTO> listCashFlowsByDate(LocalDate date) {
        Instant start = ZonedDateTime.of(
                LocalDateTime.of(date,LocalTime.MIN), ZoneId.of(TimeUtils.DEFAULT_TIME_ZONE_STRING)).toInstant();
        Instant end = ZonedDateTime.of(
                LocalDateTime.of(date,LocalTime.MAX), ZoneId.of(TimeUtils.DEFAULT_TIME_ZONE_STRING)).toInstant();

        return lcmEventRepo.findByCreatedAtBetween(start, end)
                .stream()
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LCMEventDTO> listLCMEventsByTradeId(String tradeId) {
        if (StringUtils.isBlank(tradeId)) {
            throw new IllegalArgumentException("请输入交易编号tradeId");
        }
        return lcmEventRepo.findAllByTradeId(tradeId)
                .stream()
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LCMEventDTO> listLCMEventsByPositionId(String positionId) {
        if (StringUtils.isBlank(positionId)) {
            throw new IllegalArgumentException("请输入positionId");
        }
        return lcmEventRepo.findAllByPositionId(positionId)
                .stream()
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LCMEventDTO> listLCMEventsByPositionIds(List<String> positionIds) {
        if(positionIds == null || positionIds.size() == 0) {
            throw new IllegalArgumentException("请输入positionIds");
        }

        positionIds.forEach(positionId -> {
            if (StringUtils.isBlank(positionId)) {
                throw new IllegalArgumentException("请输入positionIds");
            }
        });

        return lcmEventRepo.findAllByPositionIdIn(positionIds)
                .stream()
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LCMEventDTO> listLCMEventsByTradeIdAndPositionId(String tradeId, String positionId) {
        if (StringUtils.isBlank(tradeId)) {
            throw new IllegalArgumentException("请输入交易编号tradeId");
        }
        if (StringUtils.isBlank(positionId)) {
            throw new IllegalArgumentException("请输入positionId");
        }
        return lcmEventRepo.findAllByTradeIdAndPositionId(tradeId, positionId)
                .stream()
                .map(this::transToDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<LCMNotificationDTO> generateLCMEvents(String tradeId, OffsetDateTime validTime) {
        OffsetDateTime transactionTime = OffsetDateTime.now();
        BctTrade bctTrade = tradeService.getBctTradeByTradeId(tradeId, validTime, transactionTime);
        return Arrays.asList(bctTrade)
                .stream()
                .flatMap(trade -> trade.positions
                        .parallelStream()
                        .flatMap(position ->
                                positionLCMService.generatePositionLCMEvents(position)
                                        .stream()
                                        .map(eventInfo ->
                                                new LCMNotificationDTO(
                                                        UUID.randomUUID().toString(),
                                                        tradeId,
                                                        position.positionId,
                                                        eventInfo.notificationTime,
                                                        eventInfo.notificationEventType,
                                                        eventInfo)
                                        )
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<LCMNotificationDTO> reGenerateLCMEvents(String tradeId, OffsetDateTime validTime) {
        List<LCMNotification> oldNotifications = notificationEventRepo.findAllByTradeId(tradeId);
        notificationEventRepo.deleteInBatch(oldNotifications);
        List<LCMNotification> newNotifications = generateLCMEvents(tradeId, validTime)
                .stream()
                .map(this::toDBO)
                .collect(Collectors.toList());
        return notificationEventRepo.saveAll(newNotifications)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LCMNotificationDTO> loadLCMEvents(String tradeId) {
        return notificationEventRepo.findAllByTradeId(tradeId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LCMNotificationDTO> loadLCMEventsSearch(List<String> tradeIds, LCMEventTypeEnum notificationEventType,
                                                        LocalDateTime start, LocalDateTime end) {

        List<LCMNotification> notifications;
        if (Objects.isNull(notificationEventType)){
            notifications = notificationEventRepo.findByTradeIdInAndNotificationTimeBetween(tradeIds, start, end);
        }else {
            notifications = notificationEventRepo.findByTradeIdInAndNotificationEventTypeAndNotificationTimeBetween(tradeIds, notificationEventType,
                    start, end);
        }
        notifications.forEach(notification -> entityManager.detach(notification));
        return notifications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LCMNotificationDTO> loadLCMEventsBefore(String tradeId, LocalDateTime before) {
        return notificationEventRepo.findAllByTradeId(tradeId)
                .stream()
                .map(this::toDTO)
                .filter(lcmNotification -> lcmNotification.eventInfo.notificationTime.isBefore(before))
                .collect(Collectors.toList());
    }

    @Override
    public List<LCMNotificationDTO> loadLCMEventsAfter(String tradeId, LocalDateTime after) {

        return null;
    }


    @Override
    public List<LCMNotificationDTO> loadLCMEventsBetween(String tradeId, LocalDateTime before, LocalDateTime after) {
        return null;
    }

    /** 原名义本金 */
    private static final String NOTIONAL_OLD_VALUE = "notionalOldValue";

    @Override
    public List<LCMUnwindAmountDTO> getTradesUnwindAmount(List<String> tradeIds) {
        List<BctTrade> trades = ProfilingUtils.timed("fetch trades", () -> tradeService.listByTradeIds(tradeIds));
        List<String> positionIds = trades.stream().flatMap(trade -> trade.positionIds.stream()).collect(Collectors.toList());
        List<LCMEvent> events = ProfilingUtils.timed("fetch events", () -> lcmEventRepo.findAllByPositionIdIn(positionIds));
        Map<String, List<LCMEvent>> posEventMap = events.stream().collect(Collectors.groupingBy(key -> key.getPositionId()));
        List<LCMUnwindAmountDTO> unwindAmounts =
                ProfilingUtils.timed("compute unwind amounts",
                        () -> trades.parallelStream().flatMap(trade ->
                                trade.positions.parallelStream().map(pos -> {
                                    Optional<LCMEvent> lcmEventOptional =
                                            posEventMap.getOrDefault(pos.positionId, Collections.emptyList())
                                                    .stream()
                                                    .filter(event -> event.getEventType() == LCMEventTypeEnum.UNWIND ||
                                                            event.getEventType() == LCMEventTypeEnum.UNWIND_PARTIAL)
                                                    .min(new Comparator<LCMEvent>() {
                                                        @Override
                                                        public int compare(LCMEvent o1, LCMEvent o2) {
                                                            return o1.getCreatedAt().compareTo(o2.getCreatedAt());
                                                        }
                                                    });
                                    return computeUnwindAmounts(trade.getTradeId(), pos, lcmEventOptional);
                                })).collect(Collectors.toList()));
        return unwindAmounts;
    }

    @Override
    public LCMUnwindAmountDTO getUnwindAmounts(String tradeId, String positionId) {
        if (StringUtils.isBlank(positionId)){
            throw new CustomException("请输入持仓编号positionId");
        }
        BctTradePosition position = positionManager.getByPositionId(positionId)
                .orElseThrow(() -> new CustomException(String.format("持仓数据不存在,positionId:%s", positionId)));
        Optional<LCMEvent> lcmEventOptional = lcmEventRepo.findByPositionIdAndEventTypeInOrderByCreatedAt(positionId,
                Arrays.asList(LCMEventTypeEnum.UNWIND, LCMEventTypeEnum.UNWIND_PARTIAL))
                .stream()
                .findFirst();
        return computeUnwindAmounts(tradeId, position, lcmEventOptional);
    }

    private LCMUnwindAmountDTO computeUnwindAmounts(String tradeId, BctTradePosition position, Optional<LCMEvent> lcmEventOptional) {
        String positionId = position.getPositionId();
        LCMUnwindAmountDTO lcmUnwindAmountDto = new LCMUnwindAmountDTO();
        lcmUnwindAmountDto.setTradeId(tradeId);
        lcmUnwindAmountDto.setPositionId(positionId);
        InstrumentOfValue instrument = position.getAsset().instrumentOfValue();
        BigDecimal remainValue = BigDecimal.ZERO;
        if (instrument instanceof NotionalFeature){
            UnitOfValue<BigDecimal> notional = ((NotionalFeature) instrument).notionalAmount();
            remainValue = notional.value;
            lcmUnwindAmountDto.setValueUnit(UnitEnum.valueOf(notional.unit.name()));
            BigDecimal underlyerMultiplier = ((NotionalFeature) instrument).underlyerMultiplier();
            lcmUnwindAmountDto.setUnderlyerMultiplier(Objects.isNull(underlyerMultiplier)
                    || BigDecimal.ZERO.compareTo(underlyerMultiplier) == 0
                    ? BigDecimal.ONE : underlyerMultiplier);
        }
        if (instrument instanceof InitialSpotFeature){
            lcmUnwindAmountDto.setInitialSpot(((InitialSpotFeature) instrument).initialSpot());
        }
        lcmUnwindAmountDto.setRemainValue(remainValue);
        BigDecimal initialValue;
        if (lcmEventOptional.isPresent()){
            LCMEvent lcmEvent = lcmEventOptional.get();
            Map<String, Object> eventDetail = JsonUtils.mapper.convertValue(lcmEvent.getEventDetail(), Map.class);
            String notionalOleValue = (String) eventDetail.get(NOTIONAL_OLD_VALUE);
            initialValue = new BigDecimal(notionalOleValue);
        }else {
            initialValue = remainValue;
        }
        lcmUnwindAmountDto.setInitialValue(initialValue);
        BigDecimal historyValue = BigDecimal.ZERO;
        if (initialValue.compareTo(remainValue) > 0){
            historyValue = initialValue.subtract(remainValue);
        }
        lcmUnwindAmountDto.setHistoryValue(historyValue);

        return lcmUnwindAmountDto;
    }

    @Override
    public BigDecimal exercisePreSettle(LCMEventDTO eventDto) {
        if (!(LCMEventTypeEnum.EXERCISE.equals(eventDto.getLcmEventType()) ||
                LCMEventTypeEnum.SETTLE.equals(eventDto.getLcmEventType()) ||
                LCMEventTypeEnum.KNOCK_OUT.equals(eventDto.getLcmEventType()) ||
                LCMEventTypeEnum.EXPIRATION.equals(eventDto.getLcmEventType()) ||
                LCMEventTypeEnum.SNOW_BALL_EXERCISE.equals(eventDto.getLcmEventType()))){
            throw new CustomException("当前只支持行权,敲出,雪球过期行权事件能够提前计算结算金额");
        }
        BctTradePosition position = positionManager.getByPositionId(eventDto.getPositionId())
                .orElseThrow(() -> new CustomException(String.format("持仓数据不存在,positionId:%s", eventDto.getPositionId())));
        List<LCMProcessor> candidateProcessors = assetLCMProcessors.parallelStream()
                .flatMap(p -> p.process(position, eventDto.getLcmEventType()).stream())
                .collect(Collectors.toList());
        if (candidateProcessors.size() > 1) {
            StringBuffer candidateNames = new StringBuffer();
            candidateProcessors.forEach(c -> candidateNames.append(c.getClass()).append(","));
            throw new IllegalStateException(String.format("为Asset(%s)的 %s 事件找到%s个Processor:%s",
                    position.asset.getClass(), eventDto.getLcmEventType(), candidateProcessors.size(), candidateNames.toString()));
        }
        if(CollectionUtils.isEmpty(candidateProcessors)){
            throw new IllegalStateException(String.format("未找到Asset(%s)的 %s 事件所需的Processor:", position.asset.getClass(), eventDto.getLcmEventType()));
        }
        BigDecimal settleAmount = BigDecimal.ZERO;
        LCMProcessor exerciseProcessor = candidateProcessors.get(0);
        if (exerciseProcessor instanceof ExerciseProcessorCommon
                && ((ExerciseProcessorCommon) exerciseProcessor).canPreSettle()){
            settleAmount = ((ExerciseProcessorCommon) exerciseProcessor).preSettle(position, eventDto);
        }
        return settleAmount;
    }

    @Override
    @Transactional
    public BctTrade processLCMEvent(LCMEventDTO eventDto) {
        BctTrade bctTrade = tradePositionManager.getByTradeId(eventDto.getTradeId())
                .orElseThrow(() -> new CustomException(String.format("无法找到tradeId为%s的交易", eventDto.getTradeId())));
        BctTradePosition bctPosition = bctTrade.getPositions()
                .stream()
                .filter(position -> eventDto.getPositionId().equals(position.getPositionId()))
                .findAny()
                .orElseThrow(() -> new CustomException(String.format("无法找到positionId为%s的交易", eventDto.getPositionId())));

        return doProcessLCMEvent(bctTrade, bctPosition, eventDto);
    }

    @Override
    public List<LCMEventTypeEnum> getSupportedTradeLCMEventType(String tradeId, OffsetDateTime validTime) {
        OffsetDateTime transactionTime = OffsetDateTime.now();
        BctTrade bctTrade = tradeService.getBctTradeByTradeId(tradeId, validTime, transactionTime);
        return Arrays.asList(bctTrade)
                .stream()
                .flatMap(trade ->
                        trade.positions
                                .stream()
                                .flatMap(p -> positionLCMService.getSupportedPositionLCMEventType(p).stream()))
                .collect(Collectors.toList());
    }

    private Map<String, BigDecimal> findInstrumentPriceMap(List<String> instrumentIds) {
        LocalDate nowDate = LocalDate.now();
        Map<String, BigDecimal> instrumentPriceMap = new HashMap<>();
        instrumentIds.forEach(instrumentId -> {
            BigDecimal price = null;
            Optional<QuoteDTO> quoteClose = marketDataService.getQuote(instrumentId, InstanceEnum.CLOSE, nowDate, ZoneId.systemDefault());
            if (quoteClose.isPresent() && nowDate.isEqual(quoteClose.get().getValuationDate())) {
                price = BigDecimal.valueOf(quoteClose.get().getFields().get(QuoteFieldEnum.CLOSE));
            } else {
                Optional<QuoteDTO> quoteRealTime = marketDataService.getQuote(instrumentId, InstanceEnum.INTRADAY, nowDate, ZoneId.systemDefault());
                if (quoteRealTime.isPresent()) {
                    price = BigDecimal.valueOf(quoteRealTime.get().getFields().get(QuoteFieldEnum.LAST));
                }
            }
            instrumentPriceMap.put(instrumentId, price);
        });
        return instrumentPriceMap;
    }

    private BctTrade doProcessLCMEvent(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        List<LCMProcessor> candidateProcessors = assetLCMProcessors.parallelStream()
                .flatMap(p -> p.process(position, eventDto.getLcmEventType()).stream())
                .collect(Collectors.toList());
        if (candidateProcessors.size() > 1) {
            StringBuffer candidateNames = new StringBuffer();
            candidateProcessors.forEach(c -> candidateNames.append(c.getClass()).append(","));
            throw new IllegalStateException(String.format("为Asset(%s)的 %s 事件找到%s个Processor:%s",
                    position.asset.getClass(), eventDto.getLcmEventType(), candidateProcessors.size(), candidateNames.toString()));
        }
        LCMProcessor eventProcessor = candidateProcessors.get(0);
        logger.info("准备生成,持仓编号[{}]生命周期事件类型[{}]", position.getPositionId(), eventDto.getLcmEventType());
        List<BctTradePosition> newPositions = eventProcessor.process(trade, position, eventDto);
        if (newPositions.size() == 1 && newPositions.get(0).equals(position)) {
            return trade;
        }
        newPositions.forEach(this::savePositionAndIndex);
        saveTradeAndEvent(eventDto.getUserLoginId(), trade, newPositions);

        OffsetDateTime vt = OffsetDateTime.now();
        OffsetDateTime tt = OffsetDateTime.now();
        return Optional.of(tradeService.getBctTradeByTradeId(trade.tradeId, vt, tt))
                .orElseThrow(() -> new IllegalArgumentException(String.format("无法找到tradeId为%s的交易", trade.tradeId)));
    }

    private void saveTradeAndEvent(String userLoginId, BctTrade bctTrade, List<BctTradePosition> positions) {
        List<String> positionIds = bctTrade.getPositionIds();
        List<String> newPositionIds = positions.stream()
                .map(BctTradePosition::getPositionId)
                .collect(Collectors.toList());
        List<String> positionIdComb = Stream
                .concat(positionIds.stream(), newPositionIds.stream())
                .distinct().collect(Collectors.toList());
        bctTrade.setPositionIds(positionIdComb);

        TradeEvent tradeEvent = generateTradeEvent(bctTrade, positions, newPositionIds);
        tradeEvent.setUserLoginId(userLoginId);
        tradeEventRepo.save(tradeEvent);
        tradeManager.upsert(bctTrade);
    }

    private TradeEvent generateTradeEvent(BctTrade bctTrade, List<BctTradePosition> positions, List<String> newPositionIds){
        TradeEvent tradeEvent = new TradeEvent();
        BeanUtils.copyProperties(bctTrade, tradeEvent);

        boolean newClosed = positions.stream()
                .allMatch(this::isPositionFinished);
        boolean oldClosed = false;
        if (newClosed){
            oldClosed = bctTrade.getPositions()
                    .stream()
                    .filter(position -> !newPositionIds.contains(position.getPositionId()))
                    .allMatch(this::isPositionFinished);
        }
        if (newClosed && oldClosed){
            bctTrade.setTradeStatus(TradeStatusEnum.CLOSED);
            tradeEvent.setEventType(TradeEventTypeEnum.EARLY_TERMINATE_TRADE_EVENT);
            return tradeEvent;
        }
        tradeEvent.setEventType(TradeEventTypeEnum.LIFE_CYCLE_EVENT);
        return tradeEvent;
    }

    private Boolean isPositionFinished(BctTradePosition position){
        LCMEventTypeEnum nowLcmEvent = position.getLcmEventType();
        return  LCMEventTypeEnum.EXPIRATION.equals(nowLcmEvent) ||
                LCMEventTypeEnum.KNOCK_OUT.equals(nowLcmEvent) ||
                LCMEventTypeEnum.EXERCISE.equals(nowLcmEvent) ||
                LCMEventTypeEnum.SETTLE.equals(nowLcmEvent) ||
                LCMEventTypeEnum.UNWIND.equals(nowLcmEvent);
    }

    private void savePositionAndIndex(BctTradePosition bctPosition) {
        Optional<TradePositionIndex> indexOptional = tradePositionIndexRepo.findByPositionId(bctPosition.getPositionId());
        if (indexOptional.isPresent()){
            TradePositionIndex tradePositionIndex = indexOptional.get();
            tradePositionIndex.setLcmEventType(bctPosition.getLcmEventType());
            if (LCMEventTypeEnum.ROLL.equals(bctPosition.getLcmEventType())
                    || LCMEventTypeEnum.AMEND.equals(bctPosition.getLcmEventType())){
                InstrumentOfValue instrument = bctPosition.getAsset().instrumentOfValue();
                if (instrument instanceof OptionExerciseFeature){
                    tradePositionIndex.setExpirationDate(((OptionExerciseFeature) instrument).absoluteExpirationDate());
                }
                if (instrument instanceof UnderlyerFeature){
                    tradePositionIndex.setInstrumentId(((UnderlyerFeature) instrument).underlyer().instrumentId());
                }
            }
            tradePositionIndexRepo.save(tradePositionIndex);
        }
        positionManager.upsert(bctPosition);
    }

    private LCMEventDTO transToDto(LCMEvent lcmEvent){
        UUID uuid = lcmEvent.getUuid();
        Instant instant = lcmEvent.getCreatedAt();
        LCMEventDTO eventDto = new LCMEventDTO();
        BeanUtils.copyProperties(lcmEvent, eventDto);

        Map<String, Object> eventDetail = JsonUtils.mapper.convertValue(lcmEvent.getEventDetail(), Map.class);
        eventDto.setCreatedAt(Objects.isNull(instant) ? null : TimeUtils.instantTransToLocalDateTime(instant));
        eventDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        eventDto.setLcmEventType(lcmEvent.getEventType());
        eventDto.setEventDetail(eventDetail);
        return eventDto;
    }

    private LCMNotification toDBO(LCMNotificationDTO dto) {
        JsonNode jsonNode = JsonUtils.mapper.valueToTree(dto);
        LCMNotification notification = new LCMNotification();
        notification.setTradeId(dto.tradeId);
        notification.setPositionId(dto.positionId);
        notification.setNotificationTime(dto.notificationTime);
        notification.setNotificationEventType(dto.notificationEventType);
        notification.setNotificationInfo(jsonNode.get(LCMNotificationDTO.eventInfoFieldName));
        return notification;
    }

    private LCMNotificationDTO toDTO(LCMNotification dbo) {
        LCMNotificationInfoDTO eventInfo =
                (LCMNotificationInfoDTO) JsonUtils.jsonNodeWithTypeTagToObject(dbo.getNotificationInfo());
        return new LCMNotificationDTO(dbo.getUuid().toString(), dbo.getTradeId(), dbo.getPositionId(),
                dbo.getNotificationTime(), dbo.getNotificationEventType(), eventInfo);
    }
}
