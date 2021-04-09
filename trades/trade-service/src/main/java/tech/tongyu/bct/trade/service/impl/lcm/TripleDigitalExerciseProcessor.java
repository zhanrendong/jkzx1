package tech.tongyu.bct.trade.service.impl.lcm;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedTripleDigitalOption;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class TripleDigitalExerciseProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    @Autowired
    public TripleDigitalExerciseProcessor(LCMEventRepo lcmEventRepo){
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return Lists.newArrayList();
    }

    @Override
    public boolean canPreSettle() {
        return true;
    }

    private void checkPositionStatus(BctTradePosition position, Map<String, Object> eventDTO){
        if (isStatusError(position.lcmEventType)) {
            throw new IllegalArgumentException(String.format("持仓编号[%s],当前持仓状态[%s],不能进行权操作",
                    position.positionId, position.getLcmEventType().description()));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof AnnualizedTripleDigitalOption)) {
            throw new CustomException(String.format("AnnualizedTripleDigitalOption只支持四层阶梯期权行权结算,"
                    + "与Position[%s]的结构类型[%s]不匹配", position.getPositionId(), instrument.getClass()));
        }

        checkEuropeanOptionExpirationDate(position.getPositionId(), instrument);
    }

    @Override
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {

        Map<String, Object> eventDetail = eventDto.getEventDetail();
        checkPositionStatus(position, eventDetail);

        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if(instrument instanceof AnnualizedTripleDigitalOption) {
            String settleAmountStr = (String) eventDetail.get(SETTLE_AMOUNT);
            if (StringUtils.isBlank(settleAmountStr)){
                throw new CustomException("请输入四层阶梯期权结算金额settleAmount");
            }
            BigDecimal settleAmount = new BigDecimal(settleAmountStr);
            BigDecimal premium = getInitialPremium(position);

            LCMEvent lcmEvent = new LCMEvent();
            BeanUtils.copyProperties(eventDto, lcmEvent);
            lcmEvent.setEventDetail(JsonUtils.mapper.valueToTree(eventDetail));
            lcmEvent.setPaymentDate(getPaymentDate(eventDetail));
            lcmEvent.setEventType(eventDto.getLcmEventType());
            lcmEvent.setCashFlow(settleAmount);
            lcmEvent.setPremium(premium);
            lcmEventRepo.save(lcmEvent);

            sendPositionDoc(trade, position);
            position.setLcmEventType(eventDto.getLcmEventType());
            BctTradePosition newPosition = new BctTradePosition();
            BeanUtils.copyProperties(position, newPosition);
            return Arrays.asList(newPosition);
        }
        else {
            throw new CustomException("期权类型转换错误");
        }
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.EXERCISE;
    }

    @Override
    public BigDecimal preSettle(BctTradePosition position, LCMEventDTO eventDto) {
        checkPositionStatus(position, eventDto.getEventDetail());
        AnnualizedTripleDigitalOption annualizedTripleDigitalOption = (AnnualizedTripleDigitalOption) position.asset.instrumentOfValue();
        BigDecimal spot = getUnderlyerPrice(eventDto.getEventDetail());
        BigDecimal payment = BigDecimal.ZERO;
        if(annualizedTripleDigitalOption.optionType().equals(OptionTypeEnum.CALL)){
            if(spot.compareTo(annualizedTripleDigitalOption.strike1Value()) >=0){
                payment = annualizedTripleDigitalOption.payment1Value();
                if(spot.compareTo(annualizedTripleDigitalOption.strike2Value()) >= 0){
                    payment = annualizedTripleDigitalOption.payment2Value();
                    if(spot.compareTo(annualizedTripleDigitalOption.strike3Value()) >= 0){
                        payment = annualizedTripleDigitalOption.payment3Value();
                    }
                }
            }
        }else{
            payment = annualizedTripleDigitalOption.payment1Value();
            if(annualizedTripleDigitalOption.strike1Value().compareTo(spot) <= 0){
                payment = annualizedTripleDigitalOption.payment2Value();
                if(annualizedTripleDigitalOption.strike2Value().compareTo(spot) <= 0){
                    payment = annualizedTripleDigitalOption.payment3Value();
                    if(annualizedTripleDigitalOption.strike3Value().compareTo(spot) <= 0){
                        payment = BigDecimal.ZERO;
                    }
                }
            }
        }
        return payment;
    }
}
