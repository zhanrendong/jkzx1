package tech.tongyu.bct.trade.service.impl.lcm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.feature.NotionalFeature;
import tech.tongyu.bct.cm.reference.impl.UnitEnum;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;
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
import tech.tongyu.bct.trade.service.impl.transformer.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class UnwindProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    LCMEventRepo lcmEventRepo;

    /** 平仓手数 */
    private static final String UNWIND_LOT = "unWindLot";
    /** 平仓金额 */
    private static final String UNWIND_AMOUNT = "unWindAmount";
    /** 平仓单价 */
    private static final String UNWIND_LOT_VALUE = "unWindLotValue";
    /** 平仓总价 */
    private static final String UNWIND_AMOUNT_VALUE = "unWindAmountValue";
    /** 原名义本金 */
    private static final String NOTIONAL_OLD_VALUE = "notionalOldValue";

    @Autowired
    public UnwindProcessor(LCMEventRepo lcmEventRepo) {
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public BigDecimal preSettle(BctTradePosition position, LCMEventDTO eventDto) {
        return BigDecimal.ZERO;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.UNWIND;
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        if(isStatusError(position.lcmEventType)){
            throw new IllegalArgumentException(String.format("持仓编号[%s],持仓状态[%s],不支持平仓操作",
                      position.positionId, position.lcmEventType.description()));
        }
        Map<String, Object> eventDetail = eventDto.getEventDetail();
        String amountValue = (String) eventDetail.get(UNWIND_AMOUNT_VALUE);
        if (StringUtils.isBlank(amountValue)){
            throw new CustomException("请输入平仓金额unWindAmountValue");
        }
        // 获取现金流
        BigDecimal cashFlow = new BigDecimal(amountValue);
        BigDecimal notionalChange = BigDecimal.ZERO;
        BigDecimal notionalOld = BigDecimal.ZERO;
        UnitEnum notionalUnit = null;

        LCMEvent lcmEvent =  new LCMEvent();
        BeanUtils.copyProperties(eventDto, lcmEvent);
        InstrumentOfValue instrument = position.getAsset().instrumentOfValue();
        if (instrument instanceof NotionalFeature){
            UnitOfValue<BigDecimal> notionalAmount = ((NotionalFeature) instrument).notionalAmount();
            notionalUnit = UnitEnum.valueOf(notionalAmount.unit.name());
            notionalOld = notionalAmount.value;
            if (UnitEnum.LOT.equals(notionalUnit)){
                String lotChange = (String) eventDetail.get(UNWIND_LOT);
                if (StringUtils.isBlank(lotChange)){
                    throw new CustomException("请输入平仓手数");
                }
                notionalChange = new BigDecimal(lotChange);
            }else{
                String amountChange = (String) eventDetail.get(UNWIND_AMOUNT);
                if (StringUtils.isBlank(amountChange)){
                    throw new CustomException("请输入平仓金额");
                }
                notionalChange = new BigDecimal(amountChange);
            }

            int changeCompareResult = notionalChange.compareTo(notionalOld);
            if (changeCompareResult > 0){
                throw new CustomException(String.format("平仓金额超出可平仓金额,可平仓金额[%s],平仓金额[%s]",
                        notionalOld.toPlainString(), notionalChange.toPlainString()));
            }
            if (changeCompareResult < 0){
                lcmEvent.setEventType(LCMEventTypeEnum.UNWIND_PARTIAL);
                position.setLcmEventType(LCMEventTypeEnum.UNWIND_PARTIAL);
            }
            if (changeCompareResult == 0){
                lcmEvent.setEventType(LCMEventTypeEnum.UNWIND);
                position.setLcmEventType(LCMEventTypeEnum.UNWIND);
            }
            eventDetail.put(NOTIONAL_OLD_VALUE, notionalOld.toPlainString());
            eventDto.setEventDetail(eventDetail);
        }
        BigDecimal premium = BigDecimal.ZERO;
        if (LCMEventTypeEnum.UNWIND.equals(lcmEvent.getEventType())){
            premium = getInitialPremium(position);
        }
        UnitOfValue<BigDecimal> newNotional = Utils.toUnitOfValue(notionalUnit, notionalOld.subtract(notionalChange));
        ((NotionalFeature) instrument).notionalChangeLCMEvent(newNotional);

        // 保存现金流和权力金变化
        lcmEvent.setEventDetail(JsonUtils.mapper.valueToTree(eventDetail));
        lcmEvent.setPaymentDate(getPaymentDate(eventDetail));
        lcmEvent.setCashFlow(cashFlow);
        lcmEvent.setPremium(premium);
        lcmEventRepo.save(lcmEvent);

        if (LCMEventTypeEnum.UNWIND.equals(position.getLcmEventType())){
            sendPositionDoc(trade, position);
        }
        BctTradePosition newPosition = new BctTradePosition();
        BeanUtils.copyProperties(position, newPosition);
        return Arrays.asList(newPosition);

    }

}
