package tech.tongyu.bct.trade.service.impl.lcm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.feature.PremiumFeature;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.trade.dao.dbo.LCMEvent;
import tech.tongyu.bct.trade.dao.repo.LCMEventRepo;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationInfoDTO;
import tech.tongyu.bct.trade.service.LCMProcessor;

import java.math.BigDecimal;
import java.util.*;

@Component
public class OpenOptionPositionProcessor implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    private static String ASSET = "asset";
    private static Logger logger = LoggerFactory.getLogger(OpenOptionPositionProcessor.class);

    @Autowired
    public OpenOptionPositionProcessor(LCMEventRepo lcmEventRepo) {
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.OPEN;
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        InstrumentOfValue instrument = position.getAsset().instrumentOfValue();
        BigDecimal premium = BigDecimal.ZERO;
        BigDecimal cashFlow = BigDecimal.ZERO;
        if (instrument instanceof PremiumFeature){
            premium = ((PremiumFeature) instrument).actualPremium();
            cashFlow = premium;
        }
        // 根据买卖方向判断现金流和权力金变化
        switch (position.partyRole()){
            case BUYER:
                cashFlow = cashFlow.negate();
                break;
            case SELLER:
                premium = premium.negate();
                break;
        }
        Map<String, Object> eventDetail = new HashMap<>();
        eventDetail.put(ASSET, instrument);

        LCMEvent lcmEvent = new LCMEvent();
        BeanUtils.copyProperties(eventDto, lcmEvent);
        lcmEvent.setEventDetail(JsonUtils.mapper.valueToTree(eventDetail));
        lcmEvent.setPaymentDate(getPaymentDate(eventDetail));
        lcmEvent.setEventType(eventDto.getLcmEventType());
        lcmEvent.setPositionId(position.getPositionId());
        lcmEvent.setCashFlow(cashFlow);
        lcmEvent.setPremium(premium);
        LCMEvent saved = lcmEventRepo.save(lcmEvent);

        logger.info(String.format("成功创建开仓事件,唯一标识[%s]交易[%s],持仓[%s],生命周期事件[%s]", saved.getUuid().toString(),
                lcmEvent.getTradeId(), lcmEvent.getPositionId(), lcmEvent.getEventType().toString()));
        BctTradePosition newPosition = new BctTradePosition();
        BeanUtils.copyProperties(position, newPosition);
        return Arrays.asList(newPosition);
    }

}
