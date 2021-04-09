package tech.tongyu.bct.trade.service.impl.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.asset.AssetImpl;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.feature.SettlementTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedEagleOption;
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
import tech.tongyu.bct.trade.dto.trade.product.AnnualizedEagleOptionDTO;
import tech.tongyu.bct.trade.service.AssetTransformer;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static tech.tongyu.bct.trade.service.impl.transformer.Utils.fromDTO;
import static tech.tongyu.bct.trade.service.impl.transformer.Utils.toUnitOfValue;

@Component
public class AnnualizedEagleOptionTransformer implements AssetTransformer {

    MarketDataService marketDataService;

    @Autowired
    public AnnualizedEagleOptionTransformer(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @Override
    public Optional<Object> transform(BctTradePosition position) {
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (instrument instanceof AnnualizedEagleOption) {
            return Optional.of(toDTO(position));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Asset<InstrumentOfValue>> transform(TradePositionDTO dto) {
        if (ProductTypeEnum.EAGLE.equals(dto.productType)) {
            return Optional.of(toCM(dto));
        } else {
            return Optional.empty();
        }
    }

    private AnnualizedEagleOptionDTO toDTO(BctTradePosition position) {
        AnnualizedEagleOption iov = (AnnualizedEagleOption) position.asset.instrumentOfValue();
        return new AnnualizedEagleOptionDTO(position.partyRole(), position.counterparty.partyCode(),
                iov.underlyer.instrumentId(), iov.underlyerMultiplier, iov.specifiedPrice, iov.initialSpot,
                UnitEnum.valueOf(iov.notionalAmount.unit.name()), (BigDecimal)iov.notionalAmount.value,
                UnitEnum.valueOf(iov.premium.unit.name()), (BigDecimal)iov.premium.value, (BigDecimal)iov.frontPremium.value,
                (BigDecimal)iov.minimumPremium.value, iov.annualized, iov.daysInYear, iov.term, iov.settlementDate,
                iov.effectiveDate, iov.absoluteExpirationDate(), UnitEnum.valueOf(iov.strike1.unit.name()),
                (BigDecimal)iov.strike1.value, (BigDecimal)iov.strike2.value, (BigDecimal)iov.strike3.value, (BigDecimal)iov.strike4.value,
                iov.participationRate1, iov.participationRate2
                , iov.notionalAmountValue(), iov.notionalLotAmountValue(), iov.actualPremium(), iov.annValRatio
        );
    }

    private AssetImpl toCM(TradePositionDTO dto) {
        AnnualizedEagleOptionDTO asset = JsonUtils.mapper.convertValue(dto.asset, AnnualizedEagleOptionDTO.class);
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

        UnitOfValue<BigDecimal> strike1 = toUnitOfValue(asset.strikeType, asset.strike1);
        UnitOfValue<BigDecimal> strike2 = toUnitOfValue(asset.strikeType, asset.strike2);
        UnitOfValue<BigDecimal> strike3 = toUnitOfValue(asset.strikeType, asset.strike3);
        // 參與率1*(行权价2 - 行权价1) = 參與率2*(行权价4 - 行权价3)
        BigDecimal strike4Value = asset.strike4;
        if (Objects.isNull(strike4Value)){
            BigDecimal multiplier1 = asset.participationRate1.multiply(asset.strike2.subtract(asset.strike1));
            strike4Value = multiplier1.divide(asset.participationRate2, 10, BigDecimal.ROUND_DOWN).add(asset.strike3);
        }
        UnitOfValue<BigDecimal> strike4 = toUnitOfValue(asset.strikeType, strike4Value);

        AnnualizedEagleOption iov = new AnnualizedEagleOption(underlyer, asset.underlyerMultiplier, ProductTypeEnum.EAGLE,
                assetClassType, asset.specifiedPrice, ExerciseTypeEnum.EUROPEAN, asset.initialSpot, notional, frontPremium,
                minimumPremium, premium, asset.annualized, asset.daysInYear, asset.term, SettlementTypeEnum.CASH,
                asset.settlementDate, asset.effectiveDate, asset.expirationDate, asset.effectiveDate, new AbsoluteDate(asset.expirationDate),
                null, strike1, strike2, strike3, strike4, asset.participationRate1, asset.participationRate2, asset.annValRatio);

        return new AssetImpl(
                iov,
                party,
                partyRole,
                counterparty,
                counterpartyRole);
    }
}
