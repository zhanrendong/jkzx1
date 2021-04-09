package tech.tongyu.bct.trade.service.impl.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.asset.AssetImpl;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.feature.SettlementTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedDoubleSharkFinOption;
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
import tech.tongyu.bct.trade.dto.trade.product.AnnualizedDoubleSharkFinOptionDTO;
import tech.tongyu.bct.trade.service.AssetTransformer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static tech.tongyu.bct.trade.service.impl.transformer.Utils.fromDTO;
import static tech.tongyu.bct.trade.service.impl.transformer.Utils.toUnitOfValue;

@Component
public class AnnualizedSharkFinOptionTransformer implements AssetTransformer {

    MarketDataService marketDataService;

    @Autowired
    public AnnualizedSharkFinOptionTransformer(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @Override
    public Optional<Object> transform(BctTradePosition position) {
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (instrument instanceof AnnualizedDoubleSharkFinOption) {
            return Optional.of(toDTO(position));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Asset<InstrumentOfValue>> transform(TradePositionDTO dto) {
        if (ProductTypeEnum.DOUBLE_SHARK_FIN.equals(dto.productType)) {
            return Optional.of(toCM(dto));
        } else {
            return Optional.empty();
        }
    }

    private AnnualizedDoubleSharkFinOptionDTO toDTO(BctTradePosition position) {
        AnnualizedDoubleSharkFinOption vo = (AnnualizedDoubleSharkFinOption) position.asset().instrumentOfValue();
        return new AnnualizedDoubleSharkFinOptionDTO(position.partyRole(), vo.specifiedPrice, vo.underlyer.instrumentId(),
                position.counterparty.partyCode(), vo.underlyerMultiplier, vo.initialSpot,
                UnitEnum.valueOf(vo.notionalAmount.unit.name()), (BigDecimal)vo.notionalAmount.value,
                UnitEnum.valueOf(vo.premium.unit.name()), (BigDecimal)vo.premium.value, (BigDecimal)vo.frontPremium.value,
                (BigDecimal)vo.minimumPremium.value, vo.annualized, vo.daysInYear,vo.term,
                vo.settlementDate, vo.effectiveDate, vo.absoluteExpirationDate(),
                vo.observationType, vo.lowParticipationRate, vo.highParticipationRate,
                UnitEnum.valueOf(vo.lowStrike.unit.name()), (BigDecimal)vo.lowStrike.value, (BigDecimal)vo.highStrike.value,
                UnitEnum.valueOf(vo.lowBarrier.unit.name()), (BigDecimal)vo.lowBarrier.value, (BigDecimal)vo.highBarrier.value,
                vo.rebateType, UnitEnum.valueOf(vo.lowRebate.unit.name()), (BigDecimal)vo.lowRebate.value, (BigDecimal)vo.highRebate.value
                , vo.notionalAmountValue(), vo.notionalLotAmountValue(), vo.actualPremium(), vo.annValRatio
        );
    }

    private AssetImpl toCM(TradePositionDTO dto) {

        AnnualizedDoubleSharkFinOptionDTO asset =
                JsonUtils.mapper.convertValue(dto.asset, AnnualizedDoubleSharkFinOptionDTO.class);

        Optional<InstrumentDTO> instrument = marketDataService.getInstrument(asset.underlyerInstrumentId);

        if (!instrument.isPresent()) {
            throw new CustomException(String.format("Cannot find instrument: %s", asset.underlyerInstrumentId));
        }
        InstrumentDTO instrumentDTO = instrument.get();
        InstrumentOfValue underlyer = fromDTO(instrumentDTO);
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
        UnitOfValue<BigDecimal> lowStrike = toUnitOfValue(asset.strikeType, asset.lowStrike);
        UnitOfValue<BigDecimal> highStrike = toUnitOfValue(asset.strikeType, asset.highStrike);

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

        UnitOfValue<BigDecimal> lowRebate = toUnitOfValue(asset.rebateUnit, asset.lowRebate);
        UnitOfValue<BigDecimal> highRebate = toUnitOfValue(asset.rebateUnit, asset.highRebate);

        // TODO(http://jira.tongyu.tech:8080/browse/OTMS-1394): 暂时的placeholder，其他服务实现以后再完善
        Party counterparty = new SimpleParty(UUID.randomUUID(), dto.getCounterPartyCode(), dto.getCounterPartyName());
        Party party = new SimpleParty(UUID.randomUUID(), "party", "party");

        AnnualizedDoubleSharkFinOption iov = new AnnualizedDoubleSharkFinOption(underlyer, asset.underlyerMultiplier,
                ProductTypeEnum.DOUBLE_SHARK_FIN, assetClassType, asset.specifiedPrice, ExerciseTypeEnum.EUROPEAN,
                asset.initialSpot, notional, frontPremium, minimumPremium, premium,
                asset.annualized, asset.daysInYear, asset.term, SettlementTypeEnum.CASH, asset.settlementDate,
                asset.effectiveDate, asset.expirationDate, asset.effectiveDate, new AbsoluteDate(asset.expirationDate),
                null, asset.observationType, asset.lowParticipationRate, asset.highParticipationRate,
                lowStrike, highStrike, lowBarrier, highBarrier, asset.rebateType, lowRebate, highRebate, asset.annValRatio);

        return new AssetImpl(
                iov,
                party,
                partyRole,
                counterparty,
                counterpartyRole);
    }


}
