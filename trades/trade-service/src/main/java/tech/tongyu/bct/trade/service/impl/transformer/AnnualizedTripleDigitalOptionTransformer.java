package tech.tongyu.bct.trade.service.impl.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.asset.AssetImpl;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.feature.SettlementTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedTripleDigitalOption;
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
import tech.tongyu.bct.trade.dto.trade.product.AnnualizedTripleDigitalOptionDTO;
import tech.tongyu.bct.trade.service.AssetTransformer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static tech.tongyu.bct.trade.service.impl.transformer.Utils.fromDTO;
import static tech.tongyu.bct.trade.service.impl.transformer.Utils.oppositePartyRole;
import static tech.tongyu.bct.trade.service.impl.transformer.Utils.toUnitOfValue;

@Component
public class AnnualizedTripleDigitalOptionTransformer implements AssetTransformer {

    private MarketDataService marketDataService;

    @Autowired
    public AnnualizedTripleDigitalOptionTransformer(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @Override
    public Optional<Object> transform(BctTradePosition position) {
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (instrument instanceof AnnualizedTripleDigitalOption) {
            return Optional.of(toDTO(position));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Asset<InstrumentOfValue>> transform(TradePositionDTO dto) {
        if (ProductTypeEnum.TRIPLE_DIGITAL.equals(dto.productType)) {
            return Optional.of(toCM(dto));
        } else {
            return Optional.empty();
        }
    }

    private AnnualizedTripleDigitalOptionDTO toDTO(BctTradePosition position) {

        AnnualizedTripleDigitalOption vo = (AnnualizedTripleDigitalOption) position.asset().instrumentOfValue();

        return new AnnualizedTripleDigitalOptionDTO(position.partyRole(), vo.specifiedPrice, vo.optionType,
                vo.underlyer.instrumentId(), position.counterparty.partyCode(), vo.underlyerMultiplier, vo.participationRate,
                vo.initialSpot, UnitEnum.valueOf(vo.notionalAmount.unit.name()), (BigDecimal)vo.notionalAmount.value,
                UnitEnum.valueOf(vo.premium.unit.name()), (BigDecimal)vo.premium.value, (BigDecimal)vo.frontPremium.value,
                (BigDecimal)vo.minimumPremium.value, vo.annualized, vo.daysInYear, vo.term, vo.settlementDate,
                vo.effectiveDate, vo.absoluteExpirationDate(), UnitEnum.valueOf(vo.strike1.unit.name()), (BigDecimal)vo.strike1.value,
                (BigDecimal)vo.strike2.value, (BigDecimal)vo.strike3.value, UnitEnum.valueOf(vo.payment1.unit.name()),
                (BigDecimal)vo.payment1.value, (BigDecimal)vo.payment2.value, (BigDecimal)vo.payment3.value
                , vo.notionalAmountValue(), vo.notionalLotAmountValue(), vo.actualPremium(), vo.annValRatio
        );
    }

    private AssetImpl toCM(TradePositionDTO dto) {
        AnnualizedTripleDigitalOptionDTO asset = JsonUtils.mapper.convertValue(dto.asset, AnnualizedTripleDigitalOptionDTO.class);
        Optional<InstrumentDTO> instrument = marketDataService.getInstrument(asset.underlyerInstrumentId);
        if (!instrument.isPresent()) {
            throw new CustomException(String.format("Cannot find instrument: %s", asset.underlyerInstrumentId));
        }
        InstrumentDTO instrumentDTO = instrument.get();
        InstrumentOfValue underlyer = fromDTO(instrumentDTO);
        InstrumentAssetClassTypeEnum assetClassType = underlyer.assetClassType();
        InstrumentOfValuePartyRoleTypeEnum partyRole = asset.direction;
        InstrumentOfValuePartyRoleTypeEnum counterpartyRole = oppositePartyRole(partyRole);

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

        UnitOfValue<BigDecimal> strike1 = toUnitOfValue(asset.strikeType, asset.strike1);
        UnitOfValue<BigDecimal> strike2 = toUnitOfValue(asset.strikeType, asset.strike2);
        UnitOfValue<BigDecimal> strike3 = toUnitOfValue(asset.strikeType, asset.strike3);

        UnitOfValue<BigDecimal> payment1 = toUnitOfValue(asset.paymentType, asset.payment1);
        UnitOfValue<BigDecimal> payment2 = toUnitOfValue(asset.paymentType, asset.payment2);
        UnitOfValue<BigDecimal> payment3 = toUnitOfValue(asset.paymentType, asset.payment3);

        // TODO(http://jira.tongyu.tech:8080/browse/OTMS-1394): 暂时的placeholder，其他服务实现以后再完善
        Party counterparty = new SimpleParty(UUID.randomUUID(), dto.getCounterPartyCode(), dto.getCounterPartyName());
        Party party = new SimpleParty(UUID.randomUUID(), "party", "party");

        AnnualizedTripleDigitalOption iov = new AnnualizedTripleDigitalOption(underlyer, asset.optionType, ProductTypeEnum.TRIPLE_DIGITAL,
                assetClassType, asset.specifiedPrice, ExerciseTypeEnum.EUROPEAN, asset.underlyerMultiplier, asset.participationRate,
                asset.initialSpot, notional, premium, frontPremium, minimumPremium, asset.annualized, asset.daysInYear, asset.term,
                SettlementTypeEnum.CASH, asset.settlementDate, asset.effectiveDate, asset.expirationDate, asset.effectiveDate,
                new AbsoluteDate(asset.expirationDate), null, strike1, strike2, strike3, payment1, payment2, payment3,
                asset.annValRatio);

        return new AssetImpl(
                iov,
                party,
                partyRole,
                counterparty,
                counterpartyRole);
    }
}
