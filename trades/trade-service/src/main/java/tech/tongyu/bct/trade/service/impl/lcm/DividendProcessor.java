package tech.tongyu.bct.trade.service.impl.lcm;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.eqd.ListedEquityInstrument;
import tech.tongyu.bct.cm.product.iov.feature.NotionalFeature;
import tech.tongyu.bct.cm.product.iov.feature.SingleStrikeFeature;
import tech.tongyu.bct.cm.product.iov.feature.UnderlyerFeature;
import tech.tongyu.bct.cm.reference.impl.UnitEnum;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.trade.dao.dbo.LCMEvent;
import tech.tongyu.bct.trade.dao.repo.LCMEventRepo;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationInfoDTO;
import tech.tongyu.bct.trade.service.LCMProcessor;
import tech.tongyu.bct.trade.service.impl.transformer.Utils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class DividendProcessor implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    private static final String strikeUnit = "strikeUnit";

    private static final String strikeValue = "strikeValue";

    private static final String notionalUnit = "notionalUnit";

    private static final String notionalValue = "notionalValue";

    @Autowired
    public DividendProcessor(LCMEventRepo lcmEventRepo) {
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.DIVIDEND;
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        LCMEventTypeEnum status = position.getLcmEventType();
        if (isStatusError(status)) {
            throw new IllegalArgumentException(
                    String.format("持仓编号:(%s)当前持仓状态为%s,不能进行展期操作", status, position.positionId));
        }
        InstrumentOfValue instrument = position.getAsset().instrumentOfValue();
        if (instrument instanceof UnderlyerFeature
                && instrument instanceof NotionalFeature
                && instrument instanceof SingleStrikeFeature) {
            InstrumentOfValue underlyer = ((UnderlyerFeature) instrument).underlyer();
            if (underlyer instanceof ListedEquityInstrument) {
                Map<String, Object> eventDetail = eventDto.getEventDetail();
                String strikeUnitStr = (String) eventDetail.get(strikeUnit);
                String strikeValueStr = (String) eventDetail.get(strikeValue);
                String notionalUnitStr = (String) eventDetail.get(notionalUnit);
                String notionalValueStr = (String) eventDetail.get(notionalValue);
                UnitOfValue<BigDecimal> strike = Utils.toUnitOfValue(UnitEnum.valueOf(strikeUnitStr),
                        new BigDecimal(strikeValueStr));
                UnitOfValue<BigDecimal> notional = Utils.toUnitOfValue(UnitEnum.valueOf(notionalUnitStr),
                        new BigDecimal(notionalValueStr));
                ((SingleStrikeFeature) instrument).strikeChangeLCMEvent(strike);
                ((NotionalFeature) instrument).notionalChangeLCMEvent(notional);

            } else {
                throw new CustomException(String.format("标的物（%s）不支持分红事件", underlyer.instrumentId()));
            }
        } else {
            throw new CustomException(String.format("%s不支持分红事件处理", instrument.productType()));
        }

        LCMEvent lcmEvent = new LCMEvent();
        BeanUtils.copyProperties(eventDto, lcmEvent);
        lcmEvent.setEventType(eventDto.getLcmEventType());
        lcmEvent.defaultNumberValue();
        lcmEventRepo.save(lcmEvent);

        position.setLcmEventType(eventDto.getLcmEventType());
        BctTradePosition newPosition = new BctTradePosition();
        BeanUtils.copyProperties(position, newPosition);
        return Arrays.asList(newPosition);
    }

}
