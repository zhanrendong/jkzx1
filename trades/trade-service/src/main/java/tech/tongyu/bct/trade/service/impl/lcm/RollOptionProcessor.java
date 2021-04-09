package tech.tongyu.bct.trade.service.impl.lcm;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.feature.AnnualizedFeature;
import tech.tongyu.bct.cm.product.iov.feature.OptionExerciseFeature;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class RollOptionProcessor implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    private static final String ROLL_EXPIRATION_DATE = "expirationDate";

    @Autowired
    public RollOptionProcessor(LCMEventRepo lcmEventRepo) {
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.ROLL;
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        if(isStatusError(position.getLcmEventType())){
            throw new IllegalArgumentException(String.format("持仓编号[%s],当前持仓状态[%s],不能进行展期操作",
                    position.positionId, position.getLcmEventType().description()));
        }
        InstrumentOfValue instrument = position.getAsset().instrumentOfValue();
        if (!(instrument instanceof OptionExerciseFeature)){
            throw new CustomException(String.format("持仓编号[%s],期权结构类型不支持展期操作", position.getPositionId()));
        }
        if (instrument instanceof AnnualizedFeature) {
            if (((AnnualizedFeature) instrument).isAnnualized()) {
                throw new CustomException(String.format("持仓编号[%s],期权结构类型为年化,不能进行展期操作", position.getPositionId()));
            }
        }
        Map<String, Object> eventDetail = eventDto.getEventDetail();
        String expirationDate = (String) eventDetail.get(ROLL_EXPIRATION_DATE);
        ((OptionExerciseFeature) instrument).rollLCMEvent(LocalDate.parse(expirationDate));

        LCMEvent lcmEvent = new LCMEvent();
        BeanUtils.copyProperties(eventDto, lcmEvent);
        lcmEvent.setEventDetail(JsonUtils.mapper.valueToTree(eventDetail));
        lcmEvent.setEventType(eventDto.getLcmEventType());
        lcmEvent.setCashFlow(BigDecimal.ZERO);
        lcmEvent.setPremium(BigDecimal.ZERO);
        lcmEventRepo.save(lcmEvent);

        position.setLcmEventType(eventDto.getLcmEventType());
        BctTradePosition newPosition = new BctTradePosition();
        BeanUtils.copyProperties(position, newPosition);
        return Arrays.asList(newPosition);
    }
}
