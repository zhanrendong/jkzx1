package tech.tongyu.bct.trade.service.impl.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.asset.AssetImpl;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.feature.SettlementTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedDigitalOption;
import tech.tongyu.bct.cm.reference.elemental.Party;
import tech.tongyu.bct.cm.reference.impl.*;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.market.dto.InstrumentDTO;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.trade.dto.trade.TradePositionDTO;
import tech.tongyu.bct.trade.dto.trade.product.AnnualizedDigitalOptionDTO;
import tech.tongyu.bct.trade.service.AssetTransformer;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static tech.tongyu.bct.trade.service.impl.transformer.Utils.fromDTO;
import static tech.tongyu.bct.trade.service.impl.transformer.Utils.toUnitOfValue;

@Component
public class AnnualizedDigitalOptionTransformer implements AssetTransformer {

    MarketDataService marketDataService;

    @Autowired
    public AnnualizedDigitalOptionTransformer(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @Override
    public Optional<Object> transform(BctTradePosition position) {
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (instrument instanceof AnnualizedDigitalOption) {
            return Optional.of(toDTO(position));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Asset<InstrumentOfValue>> transform(TradePositionDTO dto) {
        if (ProductTypeEnum.DIGITAL.equals(dto.productType)) {
            return Optional.of(toCM(dto));
        } else {
            return Optional.empty();
        }
    }

    private AnnualizedDigitalOptionDTO toDTO(BctTradePosition position) {
        AnnualizedDigitalOption iov = (AnnualizedDigitalOption) position.asset().instrumentOfValue();

        return new AnnualizedDigitalOptionDTO(position.partyRole(), iov.specifiedPrice, iov.exerciseType, iov.underlyer.instrumentId(),
                iov.optionType, position.counterparty.partyCode(), iov.underlyerMultiplier, iov.participationRate,
                iov.initialSpot, UnitEnum.valueOf(iov.notionalAmount.unit.name()), (BigDecimal)iov.notionalAmount.value,
                UnitEnum.valueOf(iov.premium.unit.name()), (BigDecimal)iov.premium.value, (BigDecimal)iov.frontPremium.value,
                (BigDecimal)iov.minimumPremium.value, iov.annualized, iov.daysInYear, iov.term, iov.settlementDate,
                iov.effectiveDate, iov.absoluteExpirationDate(), UnitEnum.valueOf(iov.strike.unit.name()), (BigDecimal)iov.strike.value,
                iov.observationType, iov.rebateType, UnitEnum.valueOf(iov.rebate.unit.name()), (BigDecimal)iov.rebate.value,
                iov.notionalAmountValue(), iov.notionalLotAmountValue(), iov.actualPremium(),
                (BigDecimal)iov.rebate.value, UnitEnum.valueOf(iov.rebate.unit.name()), iov.annValRatio
        );
    }

    private AssetImpl toCM(TradePositionDTO dto) {

        AnnualizedDigitalOptionDTO asset = JsonUtils.mapper.convertValue(dto.asset, AnnualizedDigitalOptionDTO.class);

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
        UnitOfValue<BigDecimal> strike = toUnitOfValue(asset.strikeType, asset.strike);
        UnitOfValue<BigDecimal> notional = toUnitOfValue(asset.notionalAmountType, asset.notionalAmount);

        UnitOfValue<BigDecimal> payment;
        if (Objects.equals(asset.exerciseType, ExerciseTypeEnum.AMERICAN)) {
            payment = toUnitOfValue(asset.rebateUnit, asset.rebate);
        } else {
            payment = toUnitOfValue(asset.paymentType, asset.payment);
        }

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

        // TODO(http://jira.tongyu.tech:8080/browse/OTMS-1394): 暂时的placeholder，其他服务实现以后再完善
        Party counterparty = new SimpleParty(UUID.randomUUID(), dto.getCounterPartyCode(), dto.getCounterPartyName());
        Party party = new SimpleParty(UUID.randomUUID(), "party", "party");

        AnnualizedDigitalOption iov = new AnnualizedDigitalOption(underlyer, asset.optionType, asset.underlyerMultiplier,
                ProductTypeEnum.DIGITAL, assetClassType, asset.specifiedPrice, asset.exerciseType, asset.initialSpot,
                asset.participationRate, notional, frontPremium, minimumPremium, premium, asset.annualized, asset.daysInYear,
                asset.term, SettlementTypeEnum.CASH, asset.settlementDate, asset.effectiveDate, asset.expirationDate,
                asset.effectiveDate, new AbsoluteDate(asset.expirationDate), null, strike, asset.observationType,
                asset.rebateType, payment, asset.annValRatio);
        return new AssetImpl(
                iov,
                party,
                partyRole,
                counterparty,
                counterpartyRole);
    }


}
