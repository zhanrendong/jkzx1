package tech.tongyu.bct.trade.service.impl.lcm;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionExerciseFeature;
import tech.tongyu.bct.cm.product.iov.feature.PremiumFeature;
import tech.tongyu.bct.cm.product.iov.feature.SettlementFeature;
import tech.tongyu.bct.cm.product.iov.feature.SettlementTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.market.dto.QuoteDTO;
import tech.tongyu.bct.market.dto.QuoteFieldEnum;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.trade.dao.dbo.LCMEvent;
import tech.tongyu.bct.trade.dao.repo.LCMEventRepo;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMExpirationNotificationDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationInfoDTO;
import tech.tongyu.bct.trade.service.LCMProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ExpirationProcessor implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    private MarketDataService marketDataService;

    @Autowired
    public ExpirationProcessor(LCMEventRepo lcmEventRepo, MarketDataService marketDataService) {
        this.lcmEventRepo = lcmEventRepo;
        this.marketDataService = marketDataService;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.EXPIRATION;
    }

    @Override
    @Transactional
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        checkPositionStatus(position);
        InstrumentOfValue instrument = position.getAsset().instrumentOfValue();
        BigDecimal premium = ((PremiumFeature) instrument).actualPremium();
        LocalDate endDate = ((PremiumFeature) instrument).endDate();
        LocalDate nowDate = LocalDate.now();
        if (endDate.compareTo(nowDate) > 0) {
            throw new CustomException("只允许对已经到期的持仓做到期操作");
        }
        if (InstrumentOfValuePartyRoleTypeEnum.BUYER.equals(position.partyRole())){
            premium = premium.negate();
        }
        LCMEvent lcmEvent = new LCMEvent();
        BeanUtils.copyProperties(eventDto, lcmEvent);
        lcmEvent.setPaymentDate(getPaymentDate(eventDto.getEventDetail()));
        lcmEvent.setEventType(eventDto.getLcmEventType());
        lcmEvent.setCashFlow(BigDecimal.ZERO);
        lcmEvent.setPremium(premium);
        lcmEventRepo.save(lcmEvent);

        position.setLcmEventType(eventDto.getLcmEventType());
        BctTradePosition newPosition = new BctTradePosition();
        BeanUtils.copyProperties(position, newPosition);
        return Arrays.asList(newPosition);
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        InstrumentOfValue iov = asset.instrumentOfValue();
        Map<String, InstrumentOfValuePartyRoleTypeEnum> partyRoleTypeEnumMap = asset.legalPartyRoles().stream()
                .collect(Collectors.toMap(key -> key.party().partyCode(), value -> value.roleType()));
        if (iov instanceof OptionExerciseFeature) {
            OptionExerciseFeature exerciseFeature = (OptionExerciseFeature) iov;
            String instrumentId = ((OptionExerciseFeature) iov).underlyer().instrumentId();
            QuoteDTO quoteDto = marketDataService.listLatestQuotes(Arrays.asList(instrumentId))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new CustomException(String.format("获取标的物现价失败,instrumentId:%s", instrumentId)));

            Double nowPrice = quoteDto.getFields().get(QuoteFieldEnum.LAST);
            LocalDate expirationDate = exerciseFeature.absoluteExpirationDate();
            LocalDate settlementDate = ((SettlementFeature) iov).settlementDate();
            SettlementTypeEnum settlementType = ((SettlementFeature) iov).settlementType();
            return Arrays.asList(
                    new LCMExpirationNotificationDTO(
                            partyRoleTypeEnumMap,
                            LCMEventTypeEnum.EXPIRATION,
                            iov.productType(),
                            instrumentId,
                            Objects.isNull(nowPrice) ? BigDecimal.ZERO : BigDecimal.valueOf(nowPrice),
                            resolveDateTime(expirationDate),
                            resolveDateTime(expirationDate),
                            resolveDateTime(settlementDate),
                            settlementType));
            // TODO (http://jira.tongyu.tech:8080/browse/OTMS-1401): 如何为American Option生成行权提醒？
        }
        return Collections.emptyList();
    }

    private void checkPositionStatus(BctTradePosition position){
        if (isStatusError(position.getLcmEventType())){
            throw new CustomException(String.format("持仓编号[%s],当前持仓状态[%s],不能进行到期操作",
                    position.positionId, position.getLcmEventType().description()));
        }
    }

    private LocalDateTime resolveDateTime(LocalDate expirationDate) {
        return LocalDateTime.of(expirationDate, LocalTime.MIDNIGHT);
    }
}
