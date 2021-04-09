package tech.tongyu.bct.trade.service.impl.lcm;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.feature.EffectiveDateFeature;
import tech.tongyu.bct.cm.product.iov.feature.KnockInFeature;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.trade.dao.dbo.LCMEvent;
import tech.tongyu.bct.trade.dao.repo.LCMEventRepo;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationInfoDTO;
import tech.tongyu.bct.trade.service.LCMProcessor;

import java.time.LocalDate;
import java.util.*;

@Component
public class KnockInProcessor implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    private static final String KNOCK_IN_DATE = "knockInDate";

    @Autowired
    public KnockInProcessor(LCMEventRepo lcmEventRepo) {
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.KNOCK_IN;
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {

        if (isStatusError(position.lcmEventType)) {
            throw new IllegalArgumentException(String.format("持仓编号:(%s),当前持仓已经行权|平仓|到期，不能进行该操作",
                    position.positionId));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof KnockInFeature)){
            throw new CustomException(String.format("期权结构[%s]不支持敲入操作", instrument.getClass()));
        }
        Boolean isKnockIn = ((KnockInFeature) instrument).knockedIn();
        if (Objects.nonNull(isKnockIn) && isKnockIn){
            throw new CustomException(String.format("持仓编号[%s],已经敲入,不能重复执行", position.getPositionId()));
        }
        LocalDate effectiveDate = ((EffectiveDateFeature) instrument).effectiveDate();
        LocalDate nowDate = LocalDate.now();
        if (nowDate.isBefore(effectiveDate)){
            throw new CustomException(String.format("敲入日期早于起始日,起始日:[%s],敲入日:[%s]",
                    effectiveDate.toString(), nowDate.toString()));
        }
        ((KnockInFeature) instrument).updateKnockInDate(nowDate);
        Map<String, Object> eventDetail = eventDto.getEventDetail();
        eventDetail.put(KNOCK_IN_DATE, nowDate.toString());

        LCMEvent lcmEvent = new LCMEvent();
        BeanUtils.copyProperties(eventDto, lcmEvent);
        lcmEvent.setEventDetail(JsonUtils.mapper.valueToTree(eventDetail));
        lcmEvent.setEventType(eventDto.getLcmEventType());
        lcmEvent.defaultNumberValue();
        lcmEventRepo.save(lcmEvent);

        position.setLcmEventType(eventDto.getLcmEventType());
        BctTradePosition newPosition = new BctTradePosition();
        BeanUtils.copyProperties(position, newPosition);
        return Arrays.asList(newPosition);
    }

}
