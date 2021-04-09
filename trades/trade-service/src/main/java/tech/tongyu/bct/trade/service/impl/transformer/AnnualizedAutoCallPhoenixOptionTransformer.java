package tech.tongyu.bct.trade.service.impl.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.asset.AssetImpl;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.feature.SettlementTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedAutoCallPhoenixOption;
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
import tech.tongyu.bct.trade.dto.trade.product.AnnualizedAutoCallPhoenixOptionDTO;
import tech.tongyu.bct.trade.service.AssetTransformer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static tech.tongyu.bct.trade.service.impl.transformer.Utils.*;

@Component
public class AnnualizedAutoCallPhoenixOptionTransformer implements AssetTransformer {

    private MarketDataService marketDataService;

    @Autowired
    public AnnualizedAutoCallPhoenixOptionTransformer(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @Override
    public Optional<Object> transform(BctTradePosition position) {
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (instrument instanceof AnnualizedAutoCallPhoenixOption) {
            return Optional.of(toDTO(position));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Asset<InstrumentOfValue>> transform(TradePositionDTO dto) {
        if (ProductTypeEnum.AUTOCALL_PHOENIX.equals(dto.productType)) {
            return Optional.of(toCM(dto));
        } else {
            return Optional.empty();
        }
    }

    private AnnualizedAutoCallPhoenixOptionDTO toDTO(BctTradePosition position) {
        AnnualizedAutoCallPhoenixOption vo = (AnnualizedAutoCallPhoenixOption) position.asset().instrumentOfValue();

        return new AnnualizedAutoCallPhoenixOptionDTO(position.partyRole(), vo.specifiedPrice, vo.underlyer.instrumentId(),
                position.counterparty.partyCode(), vo.underlyerMultiplier, vo.participationRate, vo.initialSpot,
                UnitEnum.valueOf(vo.notionalAmount.unit.name()), (BigDecimal)vo.notionalAmount.value,
                UnitEnum.valueOf(vo.premium.unit.name()), (BigDecimal)vo.premium.value, (BigDecimal)vo.frontPremium.value,
                (BigDecimal)vo.minimumPremium.value, vo.annualized, vo.daysInYear, vo.term, vo.settlementDate, vo.effectiveDate,
                vo.absoluteExpirationDate(), vo.knockDirection, UnitEnum.valueOf(vo.barrier.unit.name()),
                (BigDecimal)vo.barrier.value, vo.knockOutObservationStep, (BigDecimal)vo.couponBarrier.value,
                vo.couponPayment, vo.autoCallPaymentType, vo.fixingObservations, vo.fixingPaymentDates, vo.knockedIn, vo.knockInDate,
                UnitEnum.valueOf(vo.knockInStrike.unit.name()), (BigDecimal)vo.knockInStrike.value,
                UnitEnum.valueOf(vo.knockInBarrier.unit.name()), (BigDecimal)vo.knockInBarrier.value, vo.knockInOptionType,
                vo.knockInObservationStep, vo.knockInObservationDates
                , vo.notionalAmountValue(), vo.notionalLotAmountValue(), vo.actualPremium(), vo.annValRatio
        );
    }

    private AssetImpl toCM(TradePositionDTO dto) {

        AnnualizedAutoCallPhoenixOptionDTO asset = JsonUtils.mapper.convertValue(dto.asset, AnnualizedAutoCallPhoenixOptionDTO.class);

        Optional<InstrumentDTO> instrument = marketDataService.getInstrument(asset.underlyerInstrumentId);
        if (!instrument.isPresent()) {
            throw new CustomException(String.format("Cannot find instrument: %s", asset.underlyerInstrumentId));
        }
        if (AutoCallPaymentTypeEnum.FIXED.equals(asset.autoCallPaymentType)){
            throw new CustomException("AutoCall凤凰式不支持到期未敲出固定收益");
        }

        InstrumentDTO instrumentDTO = instrument.get();
        InstrumentOfValue underlyer = fromDTO(instrumentDTO);
        InstrumentOfValuePartyRoleTypeEnum partyRole = asset.direction;
        InstrumentAssetClassTypeEnum assetClassType = underlyer.assetClassType();
        InstrumentOfValuePartyRoleTypeEnum counterPartyRole = oppositePartyRole(partyRole);

        UnitOfValue<BigDecimal> notional = toUnitOfValue(asset.notionalAmountType, asset.notionalAmount);
        UnitOfValue<BigDecimal> minimumPremium = toUnitOfValue(asset.premiumType, asset.minimumPremium);
        UnitOfValue<BigDecimal> frontPremium = toUnitOfValue(asset.premiumType, asset.frontPremium);
        UnitOfValue<BigDecimal> premium = toUnitOfValue(asset.premiumType, asset.premium);

        UnitOfValue<BigDecimal> barrier = toUnitOfValue(asset.barrierType, asset.barrier);
        UnitOfValue<BigDecimal> couponBarrier = toUnitOfValue(asset.barrierType, asset.couponBarrier);

        UnitOfValue<BigDecimal> knockInStrike = toUnitOfValue(asset.knockInStrikeType, asset.knockInStrike);
        UnitOfValue<BigDecimal> knockInBarrier = toUnitOfValue(asset.knockInBarrierType, asset.knockInBarrier);

        // TODO(http://jira.tongyu.tech:8080/browse/OTMS-1394): 暂时的placeholder，其他服务实现以后再完善
        Party counterparty = new SimpleParty(UUID.randomUUID(), dto.getCounterPartyCode(), dto.getCounterPartyName());
        Party party = new SimpleParty(UUID.randomUUID(), "party", "party");

        AnnualizedAutoCallPhoenixOption iov = new AnnualizedAutoCallPhoenixOption(underlyer, ProductTypeEnum.AUTOCALL_PHOENIX,
                assetClassType, asset.specifiedPrice, ExerciseTypeEnum.EUROPEAN, asset.underlyerMultiplier, asset.participationRate,
                asset.initialSpot, notional, premium, frontPremium, minimumPremium, asset.annualized, asset.daysInYear,
                asset.term, SettlementTypeEnum.CASH, asset.settlementDate, asset.effectiveDate, asset.expirationDate,
                asset.effectiveDate, new AbsoluteDate(asset.expirationDate), null, asset.knockDirection,
                barrier, asset.knockOutObservationStep, couponBarrier, asset.couponPayment, asset.autoCallPaymentType,
                asset.fixingObservations, asset.fixingPaymentDates, asset.knockedIn, asset.knockInDate, asset.knockInObservationStep,
                asset.knockInOptionType, knockInStrike, knockInBarrier, asset.knockInObservationDates, asset.annValRatio);

        return new AssetImpl(
                iov,
                party,
                partyRole,
                counterparty,
                counterPartyRole);
    }
}
