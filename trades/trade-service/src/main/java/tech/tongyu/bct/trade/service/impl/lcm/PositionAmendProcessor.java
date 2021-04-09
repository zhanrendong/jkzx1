package tech.tongyu.bct.trade.service.impl.lcm;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.PremiumFeature;
import tech.tongyu.bct.cm.reference.elemental.Account;
import tech.tongyu.bct.cm.reference.elemental.Party;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.trade.dao.dbo.LCMEvent;
import tech.tongyu.bct.trade.dao.repo.LCMEventRepo;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationInfoDTO;
import tech.tongyu.bct.trade.dto.trade.TradePositionDTO;
import tech.tongyu.bct.trade.service.AssetTransformer;
import tech.tongyu.bct.trade.service.LCMProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

@Component
public class PositionAmendProcessor implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    private List<AssetTransformer> assetTransformers;

    private static Logger logger = LoggerFactory.getLogger(PositionAmendProcessor.class);
    private static String CASH_FLOW_CHANGE = "cashFlowChange";
    private static String PRODUCT_TYPE = "productType";
    private static String ASSET = "asset";

    @Autowired
    public PositionAmendProcessor(LCMEventRepo lcmEventRepo, List<AssetTransformer> assetTransformers) {
        this.lcmEventRepo = lcmEventRepo;
        this.assetTransformers = assetTransformers;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.AMEND;
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        if (isStatusError(position.getLcmEventType())){
            throw new CustomException(String.format("持仓编号[%s], 当前状态[%s],不能进行修改操作",
                    position.getPositionId(), position.getLcmEventType().description()));
        }
        logger.info("持仓编号[{}]生命周期事件类型[{}]", position.getPositionId(), eventDto.getLcmEventType());

        Map<String, Object> eventDetail = eventDto.getEventDetail();
        String productType = (String) eventDetail.get(PRODUCT_TYPE);
        HashMap assetMap = (HashMap) eventDetail.get(ASSET);
        JsonNode asset = JsonUtils.mapper.valueToTree(assetMap);

        InstrumentOfValue oldInstrument = position.asset.instrumentOfValue();
        BigDecimal oldCashFlow = BigDecimal.ZERO;
        BigDecimal oldPremium = BigDecimal.ZERO;
        if (oldInstrument instanceof PremiumFeature){
            oldPremium = ((PremiumFeature) oldInstrument).actualPremium();
            oldCashFlow = oldPremium;
            if (InstrumentOfValuePartyRoleTypeEnum.SELLER.equals(position.partyRole())){
                oldPremium = oldPremium.negate();
            }
            if (InstrumentOfValuePartyRoleTypeEnum.BUYER.equals(position.partyRole())){
                oldCashFlow = oldCashFlow.negate();
            }
        }

        TradePositionDTO positionDto = transToPositionDto(position);
        positionDto.setProductType(ProductTypeEnum.valueOf(productType));
        positionDto.setAsset(asset);

        Asset<InstrumentOfValue> assetImpl = convertAsset(positionDto);
        position.setAsset(assetImpl);

        InstrumentOfValue newInstrument = assetImpl.instrumentOfValue();
        BigDecimal newCashFlow = BigDecimal.ZERO;
        BigDecimal newPremium = BigDecimal.ZERO;
        if (newInstrument instanceof PremiumFeature){
            newPremium = ((PremiumFeature) newInstrument).actualPremium();
            newCashFlow = newPremium;
            if (InstrumentOfValuePartyRoleTypeEnum.SELLER.equals(position.partyRole())){
                newPremium = newPremium.negate();
            }
            if (InstrumentOfValuePartyRoleTypeEnum.BUYER.equals(position.partyRole())){
                newCashFlow = newCashFlow.negate();
            }
        }
        BigDecimal cashFlowChange = newCashFlow.subtract(oldCashFlow);
        BigDecimal premiumChange = newPremium.subtract(oldPremium);

        LCMEvent lcmEvent = new LCMEvent();
        BeanUtils.copyProperties(eventDto, lcmEvent);
        lcmEvent.setEventDetail(JsonUtils.mapper.valueToTree(eventDetail));
        lcmEvent.setPaymentDate(getPaymentDate(eventDetail));
        lcmEvent.setEventType(eventDto.getLcmEventType());
        lcmEvent.setCashFlow(cashFlowChange);
        lcmEvent.setPremium(premiumChange);
        lcmEventRepo.save(lcmEvent);

        position.setLcmEventType(eventDto.getLcmEventType());
        BctTradePosition newPosition = new BctTradePosition();
        BeanUtils.copyProperties(position, newPosition);
        return Arrays.asList(newPosition);
    }

    private TradePositionDTO transToPositionDto(BctTradePosition bctPosition) {
        TradePositionDTO positionDto = new TradePositionDTO();
        BeanUtils.copyProperties(bctPosition, positionDto);
        Party counterParty = bctPosition.getCounterparty();
        if (counterParty != null) {
            positionDto.setCounterPartyCode(counterParty.partyCode());
            positionDto.setCounterPartyName(counterParty.partyName());
        }
        Account positionAccount = bctPosition.getPositionAccount();
        if (positionAccount != null) {
            positionDto.setPositionAccountCode(positionAccount.accountCode());
            positionDto.setPositionAccountName(positionAccount.accountName());
        }
        Account counterPartyAccount = bctPosition.getCounterpartyAccount();
        if (counterPartyAccount != null) {
            positionDto.setCounterPartyAccountCode(counterPartyAccount.accountCode());
            positionDto.setCounterPartyAccountName(counterPartyAccount.accountName());
        }
        return positionDto;
    }

    private Asset<InstrumentOfValue> convertAsset(TradePositionDTO positionDTO) {
        return assetTransformers.stream()
                .flatMap(transformer -> transformer.transform(positionDTO).map(Stream::of).orElse(Stream.empty()))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("无法找到[%s]的解析器", positionDTO.productType)));
    }


}
