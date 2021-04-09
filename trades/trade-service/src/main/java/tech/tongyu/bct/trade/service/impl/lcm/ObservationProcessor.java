package tech.tongyu.bct.trade.service.impl.lcm;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.FixingFeature;
import tech.tongyu.bct.cm.product.iov.feature.UnderlyerFeature;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.market.dto.QuoteDTO;
import tech.tongyu.bct.market.dto.QuoteFieldEnum;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.trade.dao.dbo.LCMEvent;
import tech.tongyu.bct.trade.dao.repo.LCMEventRepo;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationInfoDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMObservationNotificationDTO;
import tech.tongyu.bct.trade.service.LCMProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ObservationProcessor implements LCMProcessor{

    private LCMEventRepo lcmEventRepo;

    private MarketDataService marketDataService;

    private static final String OBSERVATION_DATE = "observationDate";

    private static final String OBSERVATION_PRICE = "observationPrice";

    @Autowired
    public ObservationProcessor(LCMEventRepo lcmEventRepo, MarketDataService marketDataService) {
        this.lcmEventRepo = lcmEventRepo;
        this.marketDataService = marketDataService;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.OBSERVE;
    }

    @Override
    @Transactional
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        if (isStatusError(position.lcmEventType)) {
            throw new IllegalArgumentException(String.format("持仓编号:(%s),当前持仓已经行权|平仓|到期，不能进行该操作",
                    position.positionId));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof FixingFeature)) {
            throw new CustomException(String.format("当前持仓不支持观察生命周期事件,持仓编号:%s,期权类型:%s",
                    position.getPositionId(), instrument.getClass()));
        }

        Map<String, Object> eventDetail = eventDto.getEventDetail();
        String observationDateStr = (String) eventDetail.get(OBSERVATION_DATE);
        String observationPriceStr = (String) eventDetail.get(OBSERVATION_PRICE);
        LocalDate observationDate = LocalDate.parse(observationDateStr);
        BigDecimal observationPrice = new BigDecimal(observationPriceStr);
        Map<LocalDate, BigDecimal> fixingObservations = ((FixingFeature) instrument).fixingObservations();
        if (!fixingObservations.containsKey(observationDate)){
            throw new CustomException(String.format("操作持仓不包含当前观察日期:%s", observationDateStr));
        }
        ((FixingFeature) instrument).observationLCMEvent(observationDate, observationPrice);

        LCMEvent lcmEvent = new LCMEvent();
        BeanUtils.copyProperties(eventDto, lcmEvent);

        lcmEvent.setEventDetail(JsonUtils.mapper.valueToTree(eventDetail));
        lcmEvent.setEventType(eventDto.getLcmEventType());
        lcmEvent.defaultNumberValue();
        lcmEventRepo.save(lcmEvent);

        BctTradePosition newPosition = new BctTradePosition();
        BeanUtils.copyProperties(position, newPosition);
        return Arrays.asList(newPosition);
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        LocalDate nowDate = LocalDate.now();
        InstrumentOfValue iov = asset.instrumentOfValue();
        Map<String, InstrumentOfValuePartyRoleTypeEnum> partyRoleMap = asset.legalPartyRoles().stream()
                .collect(Collectors.toMap(key -> key.party().partyCode(), value -> value.roleType()));
        if (iov instanceof FixingFeature && iov instanceof UnderlyerFeature){
            Map<LocalDate, BigDecimal> fixingObservations = ((FixingFeature) iov).fixingObservations();
            if (CollectionUtils.isEmpty(fixingObservations)){
                return new ArrayList<>();
            }
            String instrumentId = ((FixingFeature) iov).underlyer().instrumentId();
            BigDecimal nowPrice = getLatestInstrumentPrice(instrumentId);
            return fixingObservations.keySet()
                    .stream()
                    .filter(observationDate -> nowDate.isEqual(observationDate) || nowDate.isBefore(observationDate))
                    .map(observationDate -> new LCMObservationNotificationDTO(
                            partyRoleMap,
                            LCMEventTypeEnum.OBSERVE,
                            iov.productType(),
                            instrumentId,
                            nowPrice,
                            observationDate.atStartOfDay(),
                            observationDate))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private BigDecimal getLatestInstrumentPrice(String instrumentId){
        QuoteDTO quoteDto = marketDataService.listLatestQuotes(Arrays.asList(instrumentId))
                .stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(String.format("获取标的物现价失败,instrumentId:[%s]", instrumentId)));
        Double nowPrice = quoteDto.getFields().get(QuoteFieldEnum.LAST);
        return BigDecimal.valueOf(nowPrice);
    }
}
