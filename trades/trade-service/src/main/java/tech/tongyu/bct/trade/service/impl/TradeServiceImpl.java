package tech.tongyu.bct.trade.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionExerciseFeature;
import tech.tongyu.bct.cm.product.iov.feature.UnderlyerFeature;
import tech.tongyu.bct.cm.reference.elemental.Party;
import tech.tongyu.bct.cm.reference.impl.SimpleParty;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.NonEconomicPartyRole;
import tech.tongyu.bct.cm.trade.TradeStatusEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.cm.trade.impl.SalesPartyRole;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.document.dto.TradeDocumentDTO;
import tech.tongyu.bct.trade.dao.dbo.LCMEvent;
import tech.tongyu.bct.trade.dao.dbo.TradeEvent;
import tech.tongyu.bct.trade.dao.dbo.TradePositionIndex;
import tech.tongyu.bct.trade.dao.repo.LCMEventRepo;
import tech.tongyu.bct.trade.dao.repo.TradeEventRepo;
import tech.tongyu.bct.trade.dao.repo.TradePositionIndexRepo;
import tech.tongyu.bct.trade.dto.TradeEventTypeEnum;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.trade.TradeDTO;
import tech.tongyu.bct.trade.dto.trade.TradePositionDTO;
import tech.tongyu.bct.trade.dto.trade.TradePositionIndexDTO;
import tech.tongyu.bct.trade.manager.TradeManager;
import tech.tongyu.bct.trade.manager.TradePositionManager;
import tech.tongyu.bct.trade.service.BookService;
import tech.tongyu.bct.trade.service.PositionService;
import tech.tongyu.bct.trade.service.TradeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static tech.tongyu.bct.document.service.TradeDocumentService.tradeOpenTopic;

@Service
public class TradeServiceImpl implements TradeService {

    BookService bookService;

    TradeManager tradeManager;

    LCMEventRepo lcmEventRepo;

    RedisTemplate redisTemplate;

    TradeEventRepo tradeEventRepo;

    PositionService positionService;

    TradeEventComponent tradeEventComponent;

    TradePositionManager tradePositionManager;

    TradePositionIndexRepo tradePositionIndexRepo;

    @Autowired
    public TradeServiceImpl(BookService bookService,
                            TradeManager tradeManager,
                            LCMEventRepo lcmEventRepo,
                            RedisTemplate redisTemplate,
                            TradeEventRepo tradeEventRepo,
                            PositionService positionService,
                            TradeEventComponent tradeEventComponent,
                            TradePositionManager tradePositionManager,
                            TradePositionIndexRepo tradePositionIndexRepo) {

        this.bookService = bookService;
        this.tradeManager = tradeManager;
        this.lcmEventRepo = lcmEventRepo;
        this.redisTemplate = redisTemplate;
        this.tradeEventRepo = tradeEventRepo;
        this.positionService = positionService;
        this.tradeEventComponent = tradeEventComponent;
        this.tradePositionManager = tradePositionManager;
        this.tradePositionIndexRepo = tradePositionIndexRepo;
    }

    @Override
    public void deleteAll() {
        tradePositionIndexRepo.deleteAll();
        tradePositionManager.deleteAll();
        lcmEventRepo.deleteAll();
    }

    @Override
    public List<BctTrade> listByTradeIds(List<String> tradeIds) {
        return tradeIds.parallelStream()
                .map(tradeId -> getBctTradeByTradeId(tradeId, null, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<BctTrade> findAll() {
        return tradePositionManager.findAll();
    }

    @Override
    public List<TradeDTO> findByTradeStatus(TradeStatusEnum tradeStatus) {
        return tradeManager.findByTradeStatus(tradeStatus)
                .parallelStream()
                .map(trade -> transToTradeDTO(trade, null, null))
                .collect(Collectors.toList());
    }

    @Override
    public void generateHistoryTradeIndex() {
        search(new HashMap<>()).forEach(tradeDto -> tradeDto.getPositions().forEach(positionDto -> {
            positionService.createPositionIndex(tradeDto, positionDto);
        }));
    }

    @Override
    public List<BctTrade> listBySimilarTradeId(String similarTradeId) {
        return tradeManager.findBySimilarTradeId(similarTradeId);
    }

    @Override
    public List<String> listTradeIdByCounterPartyName(String counterPartyName) {
        if (StringUtils.isBlank(counterPartyName)) {
            throw new CustomException("请输入交易对手counterPartyName");
        }
        return tradePositionIndexRepo.findByCounterPartyName(counterPartyName)
                .stream()
                .map(TradePositionIndex::getTradeId)
                .distinct()
                .collect(Collectors.toList());
    }

    private enum TradeStatus {
        EXPIRATION, EXPIRATION_TODAY, OPEN_TODAY, UNWIND_TODAY, TERMINIATED_TODAY
    }

    @Override
    public List<TradeDTO> search(Map<String, String> searchDetail) {
        BctTrade bctTrade = new BctTrade();
        bctTrade.setBookName(searchDetail.get("bookName"));
        bctTrade.setTradeId(searchDetail.get("tradeId"));
        String tradeDate = searchDetail.get("tradeDate");
        if (StringUtils.isNotBlank(tradeDate)) {
            bctTrade.setTradeDate(LocalDate.parse(tradeDate));
        }
        List<BctTrade> bctTrades = tradeManager.findByExample(bctTrade);
        String partyCode = searchDetail.get("counterPartyCode");
        String salesName = searchDetail.get("salesName");

        if (StringUtils.isNotBlank(salesName) || StringUtils.isNotBlank(partyCode)) {
            bctTrades = bctTrades.parallelStream()
                    .filter(t -> (StringUtils.isBlank(salesName) || isSalesEquals(t, salesName)) &&
                            (StringUtils.isBlank(partyCode) || positionService.isCounterPartyEquals(t.getPositionIds(), partyCode)))
                    .collect(Collectors.toList());
        }

        String status = searchDetail.get("status");
        if (StringUtils.isNoneBlank(status)) {
            TradeStatus tradeStatus = TradeStatus.valueOf(status);
            if (tradeStatus.equals(TradeStatus.EXPIRATION_TODAY)) {
                bctTrades = filterExpirationToday(bctTrades);
            } else if (tradeStatus.equals(TradeStatus.EXPIRATION)) {
                bctTrades = filterExpiration(bctTrades);
            } else if (tradeStatus.equals(TradeStatus.UNWIND_TODAY)) {
                bctTrades = filterUnwindToday(bctTrades);
            } else if (tradeStatus.equals(TradeStatus.OPEN_TODAY)) {
                bctTrades = filterOpenToday(bctTrades);
            } else {
                throw new CustomException("不支持的交易状态[" + status + "]");
            }
        }

        return bctTrades.parallelStream()
                .map(trade -> transToTradeDTO(trade, null, null))
                .collect(Collectors.toList());
    }

    private List<BctTrade> filterExpirationToday(List<BctTrade> trades) {
        return trades.stream().filter(trade ->
                trade.getPositionIds().stream()
                        .map(positionService::getBctPositionById)
                        .anyMatch(position -> {
                            InstrumentOfValue instrument = position.asset.instrumentOfValue();
                            if (!(instrument instanceof OptionExerciseFeature)) return false;

                            LocalDate localDate = ((OptionExerciseFeature) instrument).absoluteExpirationDate();
                            return localDate.equals(LocalDate.now());
                        })).collect(Collectors.toList());
    }

    private List<BctTrade> filterExpiration(List<BctTrade> trades) {
        return trades.stream().filter(trade -> {
            boolean alreadyExercise = lcmEventRepo.findAllByTradeId(trade.getTradeId())
                    .stream()
                    .anyMatch(v -> v.getEventType().equals(LCMEventTypeEnum.EXERCISE));
            if (alreadyExercise) return false;

            return trade.getPositionIds().stream()
                    .map(positionService::getBctPositionById)
                    .anyMatch(position -> {
                        InstrumentOfValue instrument = position.asset.instrumentOfValue();
                        if (!(instrument instanceof OptionExerciseFeature)) return false;

                        LocalDate expirationDate = ((OptionExerciseFeature) instrument).absoluteExpirationDate();
                        return expirationDate.isBefore(LocalDate.now());
                    });
        }).collect(Collectors.toList());
    }

    private List<BctTrade> filterUnwindToday(List<BctTrade> trades) {
        return trades.stream().filter(trade ->
                lcmEventRepo.findAllByTradeId(trade.getTradeId())
                        .stream()
                        .anyMatch(v -> {
                            if (!v.getEventType().equals(LCMEventTypeEnum.UNWIND) && !v.getEventType().equals(LCMEventTypeEnum.UNWIND_PARTIAL))
                                return false;
                            LocalDate unwindDate = LocalDateTime.ofInstant(v.getCreatedAt(), DateTimeUtils.BCT_DEFAULT_TIMEZONE).toLocalDate();
                            return unwindDate.equals(LocalDate.now());
                        })).collect(Collectors.toList());
    }

    private List<BctTrade> filterOpenToday(List<BctTrade> trades) {
        return trades.stream()
                .filter(trade -> trade.getTradeDate().equals(LocalDate.now()))
                .collect(Collectors.toList());

    }

    @Override
    @Transactional
    public void create(TradeDTO tradeDto, OffsetDateTime validTime) {
        generatePositionIds(tradeDto);
        TradeEvent tradeEvent = new TradeEvent();
        tradeEvent.setTradeId(tradeDto.getTradeId());
        tradeEvent.setUserLoginId(tradeDto.getTrader());
        tradeEvent.setEventType(TradeEventTypeEnum.NEW_TRADE_EVENT);
        BctTrade bctTrade = transToBctTrade(tradeDto);
        bctTrade.setTradeStatus(TradeStatusEnum.LIVE);
        LCMEventDTO positionEvent = new LCMEventDTO();
        BeanUtils.copyProperties(tradeEvent, positionEvent);

//            tradeEventRepo.save(tradeEvent);
        tradeManager.upsert(bctTrade);
        tradeDto.positions.forEach(position -> positionService.createPosition(tradeDto, position, positionEvent));
    }

    private void sendTradeDoc(TradeDTO trade, String partyName) {
        TradeDocumentDTO tradeDocDto = new TradeDocumentDTO();
        tradeDocDto.setPartyName(partyName);
        tradeDocDto.setTradeId(trade.getTradeId());
        tradeDocDto.setBookName(trade.getBookName());
        tradeDocDto.setSalesName(trade.getSalesName());
        tradeDocDto.setTradeDate(trade.getTradeDate());
        redisTemplate.convertAndSend(tradeOpenTopic, JsonUtils.objectToJsonString(tradeDocDto));
    }

    private void generatePositionIds(TradeDTO tradeDto) {
        String tradeId = tradeDto.getTradeId();
        List<TradePositionDTO> positions = tradeDto.getPositions();
        if (positions.isEmpty()) {
            throw new IllegalArgumentException(String.format("交易编号:%s,请录入持仓数据", tradeDto.tradeId));
        }
        for (int i = 0; i < positions.size(); i++) {
            TradePositionDTO positionDto = positions.get(i);
            positionDto.setPositionId(tradeId + "_" + i);
        }
    }


    @Override
    @Transactional
    public void deleteByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        if (StringUtils.isBlank(tradeId)) throw new IllegalArgumentException("请输入交易编号:tradeId");
        tradePositionManager.deleteByTradeId(tradeId, validTime, transactionTime);
        tradePositionIndexRepo.deleteByTradeId(tradeId);
        lcmEventRepo.deleteByTradeId(tradeId);
    }

    @Override
    public TradeDTO getByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        if (StringUtils.isBlank(tradeId)) {
            throw new IllegalArgumentException("请输入交易编号tradeId");
        }
        Optional<BctTrade> tradeOptional = tradeManager.getByTradeId(tradeId, validTime, transactionTime);
        return tradeOptional.map(bctTrade -> transToTradeDTO(bctTrade, null, null))
                .orElseThrow(() -> new IllegalArgumentException(String.format("交易编号:%s,数据不存在", tradeId)));
    }

    @Override
    public BctTrade getBctTradeByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        if (StringUtils.isBlank(tradeId)) {
            throw new IllegalArgumentException("请输入交易编号tradeId");
        }
        return tradeManager.getByTradeId(tradeId, validTime, transactionTime)
                .map(this::setBctPositions)
                .orElseThrow(() -> new IllegalArgumentException(String.format("交易编号:%s,数据不存在", tradeId)));
    }

    @Override
    public List<String> listByBookName(String bookName, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        if (StringUtils.isBlank(bookName)) {
            throw new IllegalArgumentException("请输入交易簿名称bookName");
        }
        List<BctTrade> bctTrades = tradeManager.findByBookName(bookName);
        return bctTrades.stream()
                .map(bctTrade -> bctTrade.getTradeId())
                .collect(Collectors.toList());
    }


    @Override
    public List<TradeDTO> getTradeByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        return null;
    }

    @Override
    public List<TradeDTO> getTradeByBook(String bookName, OffsetDateTime validTime) {
        return null;
    }

    @Override
    public List<TradeDTO> getTradeByCounterparty(String counterpartyId, OffsetDateTime validTime) {
        return null;
    }

    @Override
    public List<TradeDTO> getTradeBySales(String salesId, OffsetDateTime validTime) {
        return null;
    }

    @Override
    public List<TradeDTO> getTradeByTradeDate(LocalDate tradeDate, OffsetDateTime validTime) {
        return null;
    }

    @Override
    public List<TradeDTO> getExpiringTrades(String trader, OffsetDateTime validTime) {
        LocalDate today = LocalDate.now();
        return tradeManager.findByTrader(trader)
                .stream()
                .filter(bctTrade -> positionService.isPositionExpired(bctTrade.getPositionIds(), today))
                .map(bctTrade -> transToTradeDTO(bctTrade, null, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listInstrumentsByBookName(String bookName) {
        OffsetDateTime now = OffsetDateTime.now();
        return tradePositionManager.findByBookName(bookName, now, now).stream().flatMap(t -> t.positions().stream())
                .map(pos -> pos.asset().instrumentOfValue())
                .filter(i -> i instanceof UnderlyerFeature)
                .map(i -> ((UnderlyerFeature) i).underlyer().instrumentId())
                .collect(Collectors.toList());
    }

    @Override
    public BctTrade fromDTO(TradeDTO tradeDto, OffsetDateTime validTime) {
        generatePositionIds(tradeDto);
        List<TradePositionDTO> positionDTOS = tradeDto.positions;
        List<BctTradePosition> positions = positionDTOS.stream()
                .map(p -> positionService.fromDTO(p)).collect(Collectors.toList());
        BctTrade bctTrade = transToBctTrade(tradeDto);
        bctTrade.positions = positions;
        bctTrade.setTradeStatus(TradeStatusEnum.LIVE);
        return bctTrade;
    }

    @Override
    public List<TradeDTO> findByTradeIds(List<String> tradeIds) {
        return tradeManager.findByTradeIds(tradeIds)
                .stream()
                .map(trade -> transToTradeDTO(trade, null, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<TradePositionIndexDTO> searchTradeIndexByIndex(TradePositionIndexDTO indexDto, String status) {
        TradePositionIndex index = new TradePositionIndex();
        BeanUtils.copyProperties(indexDto, index);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase();
        List<TradePositionIndex> indexList = tradePositionIndexRepo.findAll(Example.of(index, matcher),
                Sort.by(Sort.Direction.DESC, "tradeDate"));
        if (StringUtils.isNotBlank(status)) {
            TradeStatus tradeStatus = TradeStatus.valueOf(status);
            LocalDate nowDate = LocalDate.now();
            switch (tradeStatus) {
                case OPEN_TODAY:
                    indexList = indexList.stream()
                            .filter(i -> Objects.nonNull(i.getTradeDate()) && nowDate.isEqual(i.getTradeDate()))
                            .collect(Collectors.toList());
                    break;
                case EXPIRATION:
                    List<LCMEventTypeEnum> settleEventTypes = Arrays.asList(LCMEventTypeEnum.EXPIRATION,
                            LCMEventTypeEnum.EXERCISE, LCMEventTypeEnum.UNWIND,
                            LCMEventTypeEnum.SETTLE, LCMEventTypeEnum.KNOCK_OUT);
                    indexList = indexList.stream()
                            .filter(i -> !settleEventTypes.contains(i.getLcmEventType()))
                            .filter(i -> Objects.nonNull(i.getExpirationDate()) && nowDate.isAfter(i.getExpirationDate()))
                            .collect(Collectors.toList());
                    break;
                case UNWIND_TODAY:
                    List<LCMEventTypeEnum> unwindEventTypes = Arrays.asList(LCMEventTypeEnum.UNWIND, LCMEventTypeEnum.UNWIND_PARTIAL);
                    List<String> unwindTradeIds = lcmEventRepo.findByPaymentDateAndEventTypeIn(nowDate, unwindEventTypes)
                            .stream()
                            .map(LCMEvent::getTradeId)
                            .distinct()
                            .collect(Collectors.toList());
                    indexList = indexList.stream()
                            .filter(i -> unwindTradeIds.contains(i.getTradeId()))
                            .collect(Collectors.toList());
                    break;
                case EXPIRATION_TODAY:
                    indexList = indexList.stream()
                            .filter(i -> Objects.nonNull(i.getExpirationDate()) && nowDate.isEqual(i.getExpirationDate()))
                            .collect(Collectors.toList());
                    break;
                case TERMINIATED_TODAY:
                    List<LCMEventTypeEnum> terminatedEventTypes = Arrays.asList(LCMEventTypeEnum.EXPIRATION,
                            LCMEventTypeEnum.EXERCISE, LCMEventTypeEnum.UNWIND,
                            LCMEventTypeEnum.SETTLE, LCMEventTypeEnum.KNOCK_OUT);
                    List<String> terminatedTradeIds = lcmEventRepo.findByPaymentDateAndEventTypeIn(nowDate, terminatedEventTypes)
                            .stream()
                            .map(LCMEvent::getTradeId)
                            .distinct()
                            .collect(Collectors.toList());
                    indexList = indexList.stream()
                            .filter(i -> terminatedTradeIds.contains(i.getTradeId()))
                            .collect(Collectors.toList());
                    break;
                default:
                    throw new CustomException("不支持的交易状态[" + status + "]");
            }
        }
        return indexList.stream()
                .map(this::transToIndexDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TradeDTO> searchByProductTypesAndNotInLcmEvents(List<ProductTypeEnum> productTypes,
                                                                List<LCMEventTypeEnum> lcmEventTypes) {
        LocalDate nowDate = LocalDate.now();
        List<String> tradeIds = tradePositionIndexRepo.findByProductTypeInAndLcmEventTypeNotIn(productTypes, lcmEventTypes)
                .stream()
                .filter(index -> isPositionCanSettleAble(index, nowDate))
                .map(TradePositionIndex::getTradeId)
                .distinct()
                .collect(Collectors.toList());
        return tradeManager.findByTradeIds(tradeIds)
                .stream()
                .map(trade -> transToTradeDTO(trade, lcmEventTypes, productTypes))
                .collect(Collectors.toList());
    }

    @Override
    public void updateTradeStatus(String tradeId, TradeStatusEnum tradeStatus) {
        BctTrade bctTrade = tradeManager.getByTradeId(tradeId)
                .orElseThrow(() -> new CustomException(String.format("交易数据不存在,交易编号:%s", tradeId)));
        bctTrade.setTradeStatus(tradeStatus);
        tradeManager.upsert(bctTrade);
    }

    @Override
    public List<TradeDTO> getByTradeIds(List<String> tradeIds) {
        if (CollectionUtils.isEmpty(tradeIds)) {
            throw new IllegalArgumentException("请输入交易编号tradeId");
        }
        List<BctTrade> tradeOptional = tradeManager.findByTradeIds(tradeIds);
        return tradeOptional.stream()
                .map(bctTrade -> transToTradeDTO(bctTrade, null, null))
                .collect(Collectors.toList());
    }

    private Boolean isSalesEquals(BctTrade bctTrade, String salesName) {
        List<NonEconomicPartyRole> nonEconomicPartyRoles = bctTrade.getNonEconomicPartyRoles();
        for (NonEconomicPartyRole role : nonEconomicPartyRoles) {
            SalesPartyRole salesPartyRole = (SalesPartyRole) role;
            if (salesName.equals(salesPartyRole.salesName)) {
                return true;
            }
        }
        return false;
    }

    private BctTrade setBctPositions(BctTrade bctTrade) {
        List<BctTradePosition> positions = bctTrade.getPositionIds()
                .stream()
                .map(pid -> positionService.getBctPositionById(pid))
                .collect(Collectors.toList());
        bctTrade.setPositions(positions);
        return bctTrade;
    }

    private Boolean isPositionCanSettleAble(TradePositionIndex index, LocalDate nowDate) {
        if (ProductTypeEnum.VANILLA_EUROPEAN.equals(index.getProductType())) {
            LocalDate expirationDate = index.getExpirationDate();
            if (nowDate.isBefore(expirationDate)) {
                return false;
            }
        }
        return true;
    }

    public static BctTrade transToBctTrade(TradeDTO tradeDto) {
        BctTrade bctTrade = new BctTrade();
        BeanUtils.copyProperties(tradeDto, bctTrade);
        List<String> positionIds = tradeDto.positions
                .stream()
                .map(p -> p.getPositionId())
                .collect(Collectors.toList());
        bctTrade.setPositionIds(positionIds);

        List<NonEconomicPartyRole> nonEconomicPartyRoles = generatePartyRoles(tradeDto);
        bctTrade.setNonEconomicPartyRoles(nonEconomicPartyRoles);
        return bctTrade;
    }

    public TradeDTO transToTradeDTO(BctTrade bctTrade, List<LCMEventTypeEnum> lcmEventTypes, List<ProductTypeEnum> productTypes) {
        TradeDTO tradeDTO = new TradeDTO();
        BeanUtils.copyProperties(bctTrade, tradeDTO);
        bctTrade.getNonEconomicPartyRoles()
                .stream()
                .findAny()
                .ifPresent(nonEconomicPartyRole -> {
                    SalesPartyRole salesPartyRole = (SalesPartyRole) nonEconomicPartyRole;
                    SimpleParty party = (SimpleParty) salesPartyRole.branch;
                    tradeDTO.setPartyCode(party.partyCode);
                    tradeDTO.setPartyName(party.partyName);
                    tradeDTO.setSalesCode(salesPartyRole.salesName);
                    tradeDTO.setSalesName(salesPartyRole.salesName);
                    tradeDTO.setSalesCommission(salesPartyRole.salesCommission);
                });

        List<TradePositionDTO> positions = bctTrade.getPositionIds()
                .stream()
                .map(positionService::getByPositionId)
                .filter(p -> (CollectionUtils.isEmpty(productTypes) || productTypes.contains(p.getProductType())) &&
                        (CollectionUtils.isEmpty(lcmEventTypes) || !lcmEventTypes.contains(p.getLcmEventType())))
                .collect(Collectors.toList());
        tradeDTO.setPositions(positions);

        return tradeDTO;
    }

    private TradePositionIndexDTO transToIndexDto(TradePositionIndex index) {
        TradePositionIndexDTO indexDto = new TradePositionIndexDTO();
        BeanUtils.copyProperties(index, indexDto);

        return indexDto;
    }

    // TODO(http://jira.tongyu.tech:8080/browse/OTMS-1394): 暂时的placeholder，其他服务实现以后再完善
    private static List<NonEconomicPartyRole> generatePartyRoles(TradeDTO tradeDto) {
        Party party = new SimpleParty(UUID.randomUUID(), tradeDto.getPartyCode(), tradeDto.getPartyName());
        NonEconomicPartyRole salesPartyRole = new SalesPartyRole(party, tradeDto.getSalesName(), tradeDto.getSalesCommission(),
                null, null);
        return Arrays.asList(salesPartyRole);
    }

}