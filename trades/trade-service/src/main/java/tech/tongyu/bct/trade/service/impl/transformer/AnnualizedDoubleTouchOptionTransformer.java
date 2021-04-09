package tech.tongyu.bct.trade.service.impl.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.asset.AssetImpl;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.feature.SettlementTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedDoubleTouchOption;
import tech.tongyu.bct.cm.reference.elemental.Party;
import tech.tongyu.bct.cm.reference.impl.SimpleParty;
import tech.tongyu.bct.cm.reference.impl.UnitEnum;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.market.dto.InstrumentDTO;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.trade.dto.trade.TradePositionDTO;
import tech.tongyu.bct.trade.dto.trade.product.AnnualizedDoubleTouchOptionDTO;
import tech.tongyu.bct.trade.service.AssetTransformer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static tech.tongyu.bct.trade.service.impl.transformer.Utils.fromDTO;
import static tech.tongyu.bct.trade.service.impl.transformer.Utils.toUnitOfValue;

@Component
public class AnnualizedDoubleTouchOptionTransformer implements AssetTransformer {

    private MarketDataService marketDataService;

    @Autowired
    public AnnualizedDoubleTouchOptionTransformer(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @Override
    public Optional<Object> transform(BctTradePosition position) {
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (instrument instanceof AnnualizedDoubleTouchOption) {
            return Optional.of(toDTO(position));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Asset<InstrumentOfValue>> transform(TradePositionDTO dto) {
        if (ProductTypeEnum.DOUBLE_TOUCH.equals(dto.productType) || ProductTypeEnum.DOUBLE_NO_TOUCH.equals(dto.productType)) {
            return Optional.of(toCM(dto));
        } else {
            return Optional.empty();
        }
    }

    private AnnualizedDoubleTouchOptionDTO toDTO(BctTradePosition position) {
        AnnualizedDoubleTouchOption iov = (AnnualizedDoubleTouchOption) position.asset.instrumentOfValue();
        return new AnnualizedDoubleTouchOptionDTO(position.partyRole(), position.counterparty.partyCode(), iov.underlyer.instrumentId(),
                iov.underlyerMultiplier, iov.specifiedPrice, iov.initialSpot, iov.participationRate,
                UnitEnum.valueOf(iov.notionalAmount.unit.name()), (BigDecimal)iov.notionalAmount.value,
                UnitEnum.valueOf(iov.premium.unit.name()), (BigDecimal)iov.premium.value, (BigDecimal)iov.frontPremium.value,
                (BigDecimal)iov.minimumPremium.value, iov.annualized, iov.daysInYear, iov.term, iov.settlementDate,
                iov.effectiveDate, iov.absoluteExpirationDate(), iov.touched, UnitEnum.valueOf(iov.lowBarrier.unit.name()),
                (BigDecimal)iov.lowBarrier.value, (BigDecimal)iov.highBarrier.value,
                iov.rebateType, UnitEnum.valueOf(iov.rebate.unit.name()), (BigDecimal)iov.rebate.value
                , iov.notionalAmountValue(), iov.notionalLotAmountValue(), iov.actualPremium(), iov.annValRatio
        );
    }

    private AssetImpl toCM(TradePositionDTO dto) {
        AnnualizedDoubleTouchOptionDTO asset = JsonUtils.mapper.convertValue(dto.asset, AnnualizedDoubleTouchOptionDTO.class);
        Optional<InstrumentDTO> instrument = marketDataService.getInstrument(asset.underlyerInstrumentId);
        if (!instrument.isPresent()){
            throw new CustomException(String.format("Cannot find instrument: %s", asset.underlyerInstrumentId));
        }
        InstrumentDTO instrumentDto = instrument.get();
        InstrumentOfValue underlyer = fromDTO(instrumentDto);

        InstrumentAssetClassTypeEnum assetClassType = underlyer.assetClassType();
        InstrumentOfValuePartyRoleTypeEnum partyRole = asset.direction;
        InstrumentOfValuePartyRoleTypeEnum counterpartyRole = null;
        if (partyRole.equals(InstrumentOfValuePartyRoleTypeEnum.BUYER)) {
            counterpartyRole = InstrumentOfValuePartyRoleTypeEnum.SELLER;
        } else if (partyRole.equals(InstrumentOfValuePartyRoleTypeEnum.SELLER)) {
            counterpartyRole = InstrumentOfValuePartyRoleTypeEnum.BUYER;
        } else {
            throw new IllegalArgumentException(String.format("不支持的期权交易对手类型:%s", asset.direction));
        }
        // TODO(http://jira.tongyu.tech:8080/browse/OTMS-1394): 暂时的placeholder，其他服务实现以后再完善
        Party counterparty = new SimpleParty(UUID.randomUUID(), dto.getCounterPartyCode(), dto.getCounterPartyName());
        Party party = new SimpleParty(UUID.randomUUID(), "party", "party");

        UnitOfValue<BigDecimal> notional = toUnitOfValue(asset.notionalAmountType, asset.notionalAmount);
        UnitOfValue<BigDecimal> premium = toUnitOfValue(asset.premiumType, asset.premium);
        UnitOfValue<BigDecimal> minimumPremium;
        UnitOfValue<BigDecimal> frontPremium;
        if (asset.annualized){
            minimumPremium = toUnitOfValue(asset.premiumType, asset.minimumPremium);
            frontPremium = toUnitOfValue(asset.premiumType, asset.frontPremium);

        }else{
            minimumPremium = toUnitOfValue(asset.premiumType, BigDecimal.ZERO);
            frontPremium = toUnitOfValue(asset.premiumType, BigDecimal.ZERO);
        }

        UnitOfValue<BigDecimal> lowBarrier = toUnitOfValue(asset.barrierType, asset.lowBarrier);
        UnitOfValue<BigDecimal> highBarrier = toUnitOfValue(asset.barrierType, asset.highBarrier);
        UnitOfValue<BigDecimal> rebate = toUnitOfValue(asset.paymentType, asset.payment);
        ProductTypeEnum productType;
        if (asset.touched){
            productType = ProductTypeEnum.DOUBLE_TOUCH;
        }else {
            productType = ProductTypeEnum.DOUBLE_NO_TOUCH;
        }
        AnnualizedDoubleTouchOption iov = new AnnualizedDoubleTouchOption(underlyer, asset.underlyerMultiplier, productType,
                assetClassType, asset.specifiedPrice, ExerciseTypeEnum.AMERICAN, asset.initialSpot, asset.participationRate,
                notional, frontPremium, minimumPremium, premium, asset.annualized, asset.daysInYear, asset.term,
                SettlementTypeEnum.CASH, asset.settlementDate, asset.effectiveDate, asset.expirationDate, asset.effectiveDate,
                new AbsoluteDate(asset.expirationDate), null,asset.touched, lowBarrier, highBarrier,
                asset.rebateType, rebate, asset.annValRatio);

        return new AssetImpl(
                iov,
                party,
                partyRole,
                counterparty,
                counterpartyRole);
    }

}
