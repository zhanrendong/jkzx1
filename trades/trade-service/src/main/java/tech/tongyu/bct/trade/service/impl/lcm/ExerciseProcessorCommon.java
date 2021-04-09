package tech.tongyu.bct.trade.service.impl.lcm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.NotionalFeature;
import tech.tongyu.bct.cm.product.iov.feature.OptionExerciseFeature;
import tech.tongyu.bct.cm.product.iov.feature.PremiumFeature;
import tech.tongyu.bct.cm.reference.elemental.Party;
import tech.tongyu.bct.cm.reference.impl.UnitEnum;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.NonEconomicPartyRole;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.cm.trade.impl.SalesPartyRole;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.document.dto.PositionDocumentDTO;
import tech.tongyu.bct.trade.dao.dbo.LCMEvent;
import tech.tongyu.bct.trade.dao.repo.LCMEventRepo;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.service.impl.transformer.Utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static tech.tongyu.bct.document.service.TradeDocumentService.tradeSettleTopic;

public abstract class ExerciseProcessorCommon {

    /** 原名义本金 */
    protected static final String NOTIONAL_OLD_VALUE = "notionalOldValue";
    // TODO http://jira.tongyu.tech:8080/browse/OTMS-1852
    protected static final String UNDERLYER_PRICE = "underlyerPrice";

    protected static final String SETTLE_AMOUNT = "settleAmount";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private LCMEventRepo lcmEventRepo;

    public boolean canPreSettle(){
        return false;
    }

    public abstract LCMEventTypeEnum eventType();

    public abstract BigDecimal preSettle(BctTradePosition position, LCMEventDTO eventDto);

    protected void checkEuropeanOptionExpirationDate(String positionId, InstrumentOfValue instrument){
        if (instrument instanceof OptionExerciseFeature) {
            LocalDate nowDate = LocalDate.now();
            LocalDate expirationDate = ((OptionExerciseFeature) instrument).absoluteExpirationDate();
            if (nowDate.isBefore(expirationDate)) {
                throw new CustomException(
                        String.format("持仓编号:[%s],当前持仓为欧式,还未到到期,到期日:[%s],不能进行行权操作",
                                positionId, expirationDate.toString()));
            }
        }
    }

    protected BigDecimal getPremiumValueByPartyRole(BigDecimal premium, InstrumentOfValuePartyRoleTypeEnum partyRole){
        if (InstrumentOfValuePartyRoleTypeEnum.BUYER.equals(partyRole)){
            premium = premium.negate();
        }
        return premium;
    }

    protected BigDecimal getCashFlowValueByPartyRole(BigDecimal cashFlow, InstrumentOfValuePartyRoleTypeEnum partyRole){
        if (InstrumentOfValuePartyRoleTypeEnum.SELLER.equals(partyRole)){
            cashFlow = cashFlow.negate();
        }
        return cashFlow;
    }

    protected BigDecimal checkPartyRoleType(BigDecimal cashFlow, InstrumentOfValuePartyRoleTypeEnum partyRoleType) {
        switch (partyRoleType) {
            case BUYER:
                break;
            case SELLER:
                cashFlow = cashFlow.negate();
                break;
            default:
                throw new IllegalArgumentException("超出行权买卖角色");
        }
        return cashFlow;
    }

    protected void sendPositionDoc(BctTrade trade, BctTradePosition position){
        PositionDocumentDTO positionDocDto = new PositionDocumentDTO();
        Optional<NonEconomicPartyRole> nonEconomicPartyRole = trade.getNonEconomicPartyRoles()
                .stream()
                .findAny();
        if (nonEconomicPartyRole.isPresent()){
            SalesPartyRole salesPartyRole = (SalesPartyRole) nonEconomicPartyRole.get();
            positionDocDto.setSalesName(salesPartyRole.salesName);
        }
        Party counterParty = position.getCounterparty();
        positionDocDto.setPartyName(counterParty.partyName());
        InstrumentOfValue instrument = position.asset().instrumentOfValue();
        if (instrument instanceof OptionExerciseFeature){
            LocalDate expirationDate = ((OptionExerciseFeature) instrument).absoluteExpirationDate();
            positionDocDto.setExpirationDate(expirationDate);
        }
        positionDocDto.setPositionId(position.getPositionId());
        positionDocDto.setBookName(trade.getBookName());
        positionDocDto.setTradeId(trade.getTradeId());
        redisTemplate.convertAndSend(tradeSettleTopic, JsonUtils.objectToJsonString(positionDocDto));
    }

    protected BigDecimal getUnderlyerPrice(Map<String, Object> eventDetail){
        String underlyerPriceStr = (String) eventDetail.get(UNDERLYER_PRICE);
        if (StringUtils.isBlank(underlyerPriceStr)) {
            throw new IllegalArgumentException("请输入标的物价格");
        }
        return new BigDecimal(underlyerPriceStr);
    }

    protected BigDecimal getInitialPremium(BctTradePosition position){
        UnitEnum notionalUnit = null;
        BigDecimal initialNotional = BigDecimal.ZERO;
        InstrumentOfValue instrument = position.getAsset().instrumentOfValue();
        if (instrument instanceof NotionalFeature){
            UnitOfValue<BigDecimal> notionalAmount = ((NotionalFeature) instrument).notionalAmount();
            notionalUnit = UnitEnum.valueOf(notionalAmount.unit().name());
            initialNotional = notionalAmount.value();
        }
        // 获取初始名义本金
        Optional<LCMEvent> lcmEventOptional = lcmEventRepo.findByPositionIdAndEventTypeInOrderByCreatedAt(
                position.getPositionId(), Arrays.asList(LCMEventTypeEnum.UNWIND, LCMEventTypeEnum.UNWIND_PARTIAL))
                .stream()
                .findFirst();
        if (lcmEventOptional.isPresent()){
            LCMEvent lcmEventQuery = lcmEventOptional.get();
            Map<String, Object> eventDetailQuery = JsonUtils.mapper.convertValue(lcmEventQuery.getEventDetail(), Map.class);
            String notionalOleValue = (String) eventDetailQuery.get(NOTIONAL_OLD_VALUE);
            initialNotional = new BigDecimal(notionalOleValue);
        }
        BigDecimal premium = BigDecimal.ZERO;
        if (Objects.nonNull(notionalUnit) && Objects.nonNull(initialNotional)){
            UnitOfValue<BigDecimal> unitInitialNotional = Utils.toUnitOfValue(notionalUnit, initialNotional);
            premium = ((PremiumFeature) instrument).actualInitialPremium(unitInitialNotional);
            premium = getPremiumValueByPartyRole(premium, position.partyRole());
        }
        return premium;
    }
}
