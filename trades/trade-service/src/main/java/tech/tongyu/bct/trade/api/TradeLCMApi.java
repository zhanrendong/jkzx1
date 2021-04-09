package tech.tongyu.bct.trade.api;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.auth.dto.ResourceDTO;
import tech.tongyu.bct.auth.service.ResourcePermissionService;
import tech.tongyu.bct.auth.service.ResourceService;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.ProfilingUtils;
import tech.tongyu.bct.trade.dto.event.CashFlowCollectionDTO;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMUnwindAmountDTO;
import tech.tongyu.bct.trade.dto.trade.TradeDTO;
import tech.tongyu.bct.trade.dto.trade.TradePositionParamDTO;
import tech.tongyu.bct.trade.service.PositionLCMService;
import tech.tongyu.bct.trade.service.TradeLCMService;
import tech.tongyu.bct.trade.service.TradeService;
import tech.tongyu.bct.trade.service.impl.transformer.DefaultingRules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum.UPDATE_TRADE;
import static tech.tongyu.bct.auth.enums.ResourceTypeEnum.BOOK;

@Service
public class TradeLCMApi {
    private static Logger logger = LoggerFactory.getLogger(TradeLCMApi.class);

    @Autowired
    TradeService tradeService;
    @Autowired
    TradeLCMService tradeLCMService;
    @Autowired
    PositionLCMService positionLCMService;
    @Autowired
    ResourceService resourceService;
    @Autowired
    ResourcePermissionService resourcePermissionService;

    @Autowired
    public TradeLCMApi(TradeLCMService tradeLCMService) {
        this.tradeLCMService = tradeLCMService;
    }

    @BctMethodInfo(
            description = "恢复历史现金流",
            retDescription = "恢复是否成功",
            retName = "true or exception"
    )
    public Boolean tradeRepairHistoryCashFlow() {
        tradeLCMService.repairHistoryTradeCashFlow();
        return true;
    }

    @BctMethodInfo(
            description = "持仓行权试结算",
            retDescription = "试结算结果",
            retName = "BigDecimal",
            service = "trade-service"
    )
    public BigDecimal tradeExercisePreSettle(
            @BctMethodArg(description = "生命周期事件", argClass = LCMEventTypeEnum.class) String eventType,
            @BctMethodArg(description = "行权试结算持仓编号") String positionId,
            @BctMethodArg(description = "行权事件必需参数") Map<String, Object> eventDetail
    ) {
        if (StringUtils.isBlank(eventType)) {
            throw new CustomException("请输入生命周期事件");
        }
        if (StringUtils.isBlank(positionId)) {
            throw new CustomException("请输入行权试结算持仓编号");
        }
        if (CollectionUtils.isEmpty(eventDetail)) {
            throw new CustomException("请输入行权事件必需参数");
        }
        LCMEventDTO lcmEventDto = new LCMEventDTO();
        lcmEventDto.setPositionId(positionId);
        lcmEventDto.setEventDetail(eventDetail);
        lcmEventDto.setLcmEventType(LCMEventTypeEnum.valueOf(eventType));

        return tradeLCMService.exercisePreSettle(lcmEventDto);
    }

    @BctMethodInfo(
            description = "为交易生成所有未来的生命周期事件",
            retDescription = "生成的生命周期事件",
            retName = "list of LCMNotificationDTO",
            returnClass = LCMNotificationDTO.class,
            service = "trade-service"
    )
    public List<LCMNotificationDTO> tradeLCMEventGenerate(
            @BctMethodArg(description = "交易编号") String tradeId
    ) {
        return tradeLCMService.generateLCMEvents(tradeId, OffsetDateTime.now());
    }

    @BctMethodInfo(
            description = "为多个交易生成所有未来的生命周期事件",
            retDescription = "生成的生命周期事件",
            retName = "list of LCMNotificationDTO",
            returnClass = LCMNotificationDTO.class,
            service = "trade-service"
    )
    public List<LCMNotificationDTO> tradeLCMEventGenerateAll(
            @BctMethodArg(description = "交易编号列表") List<String> tradeIds
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        return tradeIds.stream()
                .flatMap(tradeId -> tradeLCMService.reGenerateLCMEvents(tradeId, now).stream())
                .collect(Collectors.toList());
    }

    @BctMethodInfo(
            description = "从validTime开始，为Trade生成所有生命周期事件并存储到数据库",
            retDescription = "生成的生命周期事件",
            retName = "list of LCMNotificationDTO",
            returnClass = LCMNotificationDTO.class,
            service = "trade-service"
    )
    public List<LCMNotificationDTO> tradeLCMEventReGenerate(
            @BctMethodArg(description = "交易编号") String tradeId,
            @BctMethodArg(description = "重新生成生命周期事件的起始时间") String validTime
    ) {
        LocalDateTime validDateTime = LocalDateTime.parse(validTime);
        OffsetDateTime vtWithDefaultZone = OffsetDateTime.of(validDateTime, DefaultingRules.defaultZone);
        return tradeLCMService.reGenerateLCMEvents(tradeId, vtWithDefaultZone);
    }

    @BctMethodInfo(
            description = "获取所有的生命周期事件",
            retDescription = "所有的生命周期事件",
            retName = "list of LCMNotificationDTO",
            returnClass = LCMNotificationDTO.class,
            service = "trade-service"
    )
    public List<LCMNotificationDTO> trdTradeLCMNotificationList() {
        Collection<ResourceDTO> resources = resourceService.authBookGetCanRead();
        return resources.stream().flatMap(resource ->
                tradeService.listByBookName(resource.getResourceName(), null, null)
                        .stream()
                        .flatMap(tradeId -> tradeLCMService.loadLCMEvents(tradeId).stream())
        ).collect(Collectors.toList());
    }

    @BctMethodInfo(
            description = "根据条件查询生命周期事件",
            retDescription = "符合条件的的生命周期事件",
            retName = "list of LCMNotificationDTO",
            returnClass = LCMNotificationDTO.class,
            service = "trade-service"
    )
    public List<LCMNotificationDTO> traTradeLCMNotificationSearch(
            @BctMethodArg(required = false, description = "开始时间") String after,
            @BctMethodArg(required = false, description = "结束时间") String before,
            @BctMethodArg(required = false, description = "生命周期事件", argClass = LCMEventTypeEnum.class) String notificationEventType
    ) {
        LocalDateTime afterTime = StringUtils.isBlank(after) ? null : LocalDateTime.of(LocalDate.parse(after), LocalTime.MIN);
        LocalDateTime beforeTime = StringUtils.isBlank(before) ? null : LocalDateTime.of(LocalDate.parse(before), LocalTime.MAX);
        LCMEventTypeEnum lcmEventType =
                StringUtils.isBlank(notificationEventType) ? null : LCMEventTypeEnum.valueOf(notificationEventType);

        List<String> tradeIds = resourceService.authBookGetCanRead()
                .stream()
                .flatMap(resource ->
                        tradeService.listByBookName(resource.getResourceName(), null, null)
                                .stream()
                ).collect(Collectors.toList());
        return tradeLCMService.loadLCMEventsSearch(tradeIds, lcmEventType, afterTime, beforeTime);
    }

    @BctMethodInfo(
            description = "处理当前一个Position的生命周期事件",
            retDescription = "处理完生命周期事件的Position",
            retName = "bctTrade",
            service = "trade-service"
    )
    public BctTrade trdTradeLCMEventProcess(
            @BctMethodArg(description = "交易编号") String tradeId,
            @BctMethodArg(description = "交易生命周期事件类型", argClass = LCMEventTypeEnum.class) String eventType,
            @BctMethodArg(description = "持仓编号") String positionId,
            @BctMethodArg(description = "操作人唯一标识userLoginId") String userLoginId,
            @BctMethodArg(description = "Trade中Position的cashFlow") Map<String, Object> eventDetail) {
        LCMEventTypeEnum lcmEventType = LCMEventTypeEnum.valueOf(eventType);

        TradeDTO tradeDTO = tradeService.getByTradeId(tradeId, OffsetDateTime.of(LocalDateTime.now(), DefaultingRules.defaultZone),
                OffsetDateTime.of(LocalDateTime.now(), DefaultingRules.defaultZone));

        if (resourcePermissionService.authCan(BOOK.name(), Lists.newArrayList(tradeDTO.bookName), UPDATE_TRADE.name())
                .stream().noneMatch(permission -> permission)) {
            throw new IllegalArgumentException(String.format("当前用户在交易簿:%s,没有更新交易权限,请联系管理员", tradeDTO.bookName));
        }

        LCMEventDTO positionEventDTO = new LCMEventDTO();
        positionEventDTO.setTradeId(tradeId);
        positionEventDTO.setPositionId(positionId);
        positionEventDTO.setUserLoginId(userLoginId);
        positionEventDTO.setEventDetail(eventDetail);
        positionEventDTO.setLcmEventType(LCMEventTypeEnum.valueOf(eventType));

        return tradeLCMService.processLCMEvent(positionEventDTO);
    }

    @BctMethodInfo(
            description = "根据交易ID和持仓ID获取平仓数量",
            retDescription = "平仓数量",
            retName = "LCMUnwindAmountDTO",
            returnClass = LCMUnwindAmountDTO.class,
            service = "trade-service"
    )
    public LCMUnwindAmountDTO trdTradeLCMUnwindAmountGet(
            @BctMethodArg(description = "交易编号") String tradeId,
            @BctMethodArg(description = "持仓编号") String positionId
    ) {
        if (StringUtils.isBlank(tradeId)) {
            throw new CustomException("请输入交易编号");
        }
        if (StringUtils.isBlank(positionId)) {
            throw new CustomException("请输入持仓编号");
        }
        return tradeLCMService.getUnwindAmounts(tradeId, positionId);
    }

    private static final String TRADE_ID_NAME = "tradeId";

    private static final String POS_ID_NAME = "positionId";

    @BctMethodInfo(
            description = "批量获取平仓数量",
            retDescription = "平仓数量列表",
            retName = "List of LCMUnwindAmountDTOs",
            returnClass = LCMUnwindAmountDTO.class,
            service = "trade-service"
    )
    public List<LCMUnwindAmountDTO> trdTradeLCMUnwindAmountGetAll(
            @BctMethodArg(description = "交易编号与持仓编号列表", argClass = TradePositionParamDTO.class) List<Map<String, Object>> positionIds
    ) {
        List<String> tradeIds = positionIds.stream()
                .map(posMap -> (String) posMap.get(TRADE_ID_NAME)).collect(Collectors.toList());
        StringBuilder msg = new StringBuilder().append("fetch ").append(tradeIds.size()).append(" trades unwind events");
        return ProfilingUtils.timed(msg.toString(), () -> tradeLCMService.getTradesUnwindAmount(tradeIds));
    }


    @BctMethodInfo(
            description = "获取指定交易的生命周期事件集合",
            retDescription = "生命周期事件集合",
            retName = "List of LCMEventDTO",
            returnClass = LCMEventDTO.class,
            service = "trade-service"
    )
    public List<LCMEventDTO> trdTradeLCMEventList(
            @BctMethodArg(description = "交易编号") String tradeId
    ) {
        if (StringUtils.isBlank(tradeId)) throw new IllegalArgumentException("请输入交易编号tradeId");

        return tradeLCMService.listLCMEventsByTradeId(tradeId);
    }

    @BctMethodInfo(
            description = "获取现金流集合",
            retDescription = "现金流集合",
            retName = "List of CashFlowCollectionDTO",
            returnClass = CashFlowCollectionDTO.class,
            service = "trade-service"
    )
    public List<CashFlowCollectionDTO> trdTradeCashFlowCollectionList() {
        return tradeLCMService.listCashFlowCollection();
    }

    @BctMethodInfo(
            description = "获取所有交易的现金流水信息",
            retDescription = "所有交易的现金流水信息",
            retName = "List of LCMEventDTO",
            returnClass = LCMEventDTO.class,
            service = "trade-service"
    )
    public List<LCMEventDTO> trdTradeCashFlowListAll() {
        return tradeLCMService.listAllCashFlow();
    }

    @BctMethodInfo(
            description = "获得当前position支持的所有生命周期事件",
            retDescription = "当前position支持的所有生命周期事件",
            retName = "List of LCMEventTypeEnum",
            returnClass = LCMEventTypeEnum.class,
            service = "trade-service"
    )
    public List<LCMEventTypeEnum> trdPositionLCMEventTypes(
            @BctMethodArg(description = "持仓编号") String positionId
    ) {
        return positionLCMService.getSupportedPositionLCMEventType(positionId, OffsetDateTime.now());
    }

    @BctMethodInfo(
            description = "获得某天所有交易现金流事件",
            retDescription = "该天所有交易现金流事件",
            retName = "List of LCMEventDTO",
            returnClass = LCMEventDTO.class,
            service = "trade-service"
    )
    public List<LCMEventDTO> trdTradeLCMEventSearchByDate(
            @BctMethodArg(description = "日期") String date
    ) {
        return ProfilingUtils.timed("search LCM Events by date", () -> tradeLCMService.listCashFlowsByDate(LocalDate.parse(date)));
    }
}
