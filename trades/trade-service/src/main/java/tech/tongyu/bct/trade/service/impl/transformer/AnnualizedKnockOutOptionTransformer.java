package tech.tongyu.bct.trade.service.impl.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.asset.AssetImpl;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.feature.SettlementTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedKnockOutOption;
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
import tech.tongyu.bct.trade.dto.trade.product.AnnualizedKnockOutOptionDTO;
import tech.tongyu.bct.trade.service.AssetTransformer;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static tech.tongyu.bct.trade.service.impl.transformer.Utils.fromDTO;
import static tech.tongyu.bct.trade.service.impl.transformer.Utils.toUnitOfValue;

@Component
public class AnnualizedKnockOutOptionTransformer implements AssetTransformer {

    MarketDataService marketDataService;

    @Autowired
    public AnnualizedKnockOutOptionTransformer(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @Override
    public Optional<Object> transform(BctTradePosition position) {
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (instrument instanceof AnnualizedKnockOutOption) {
            return Optional.of(toDTO(position));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Asset<InstrumentOfValue>> transform(TradePositionDTO dto) {
        if (ProductTypeEnum.BARRIER.equals(dto.productType)) {
            return Optional.of(toCM(dto));
        } else {
            return Optional.empty();
        }
    }

    private AnnualizedKnockOutOptionDTO toDTO(BctTradePosition position) {
        AnnualizedKnockOutOption vo = (AnnualizedKnockOutOption) position.asset().instrumentOfValue();
        return new AnnualizedKnockOutOptionDTO(position.partyRole(), vo.specifiedPrice, vo.optionType,
                vo.underlyer.instrumentId(), position.counterparty.partyCode(), vo.underlyerMultiplier, vo.participationRate,
                vo.initialSpot, UnitEnum.valueOf(vo.notionalAmount.unit.name()), (BigDecimal)vo.notionalAmount.value,
                UnitEnum.valueOf(vo.premium.unit.name()), (BigDecimal)vo.premium.value, (BigDecimal)vo.frontPremium.value,
                (BigDecimal)vo.minimumPremium.value, vo.annualized, vo.daysInYear, vo.term, vo.settlementDate, vo.effectiveDate,
                vo.absoluteExpirationDate(), UnitEnum.valueOf(vo.strike.unit.name()), (BigDecimal)vo.strike.value,
                vo.observationType, vo.knockDirection, UnitEnum.valueOf(vo.barrier.unit.name()), (BigDecimal)vo.barrier.value,
                Objects.isNull(vo.barrierShift) ? null : (BigDecimal) vo.barrierShift.value , vo.knockOutObservationStep,
                vo.fixingObservations, vo.rebateType, UnitEnum.valueOf(vo.rebate.unit.name()), (BigDecimal)vo.rebate.value
                , vo.notionalAmountValue(), vo.notionalLotAmountValue(), vo.actualPremium(), vo.annValRatio
        );
    }

    private AssetImpl toCM(TradePositionDTO dto) {

        AnnualizedKnockOutOptionDTO asset = JsonUtils.mapper.convertValue(dto.asset, AnnualizedKnockOutOptionDTO.class);

        Optional<InstrumentDTO> instrument = marketDataService.getInstrument(asset.underlyerInstrumentId);

        if (!instrument.isPresent()) {
            throw new CustomException(String.format("Cannot find instrument: %s", asset.underlyerInstrumentId));
        }
        InstrumentDTO instrumentDTO = instrument.get();
        InstrumentOfValue underlyer = fromDTO(instrumentDTO);
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
        UnitOfValue<BigDecimal> barrier = toUnitOfValue(asset.barrierType, asset.barrier);
        UnitOfValue<BigDecimal> rebate = toUnitOfValue(asset.rebateUnit, asset.rebate);
        UnitOfValue<BigDecimal> barrierShift = null;
        if (Objects.nonNull(asset.barrierShift)){
            barrierShift = toUnitOfValue(asset.barrierType, asset.barrierShift);
        }

        // TODO(http://jira.tongyu.tech:8080/browse/OTMS-1394): 暂时的placeholder，其他服务实现以后再完善
        Party counterparty = new SimpleParty(UUID.randomUUID(), dto.getCounterPartyCode(), dto.getCounterPartyName());
        Party party = new SimpleParty(UUID.randomUUID(), "party", "party");

        AnnualizedKnockOutOption iov = new AnnualizedKnockOutOption(
                underlyer, asset.optionType, ProductTypeEnum.BARRIER, underlyer.assetClassType(), asset.specifiedPrice,
                ExerciseTypeEnum.EUROPEAN, asset.underlyerMultiplier, asset.participationRate, asset.initialSpot,
                notional, premium, frontPremium, minimumPremium, asset.annualized, asset.daysInYear, asset.term,
                SettlementTypeEnum.CASH, asset.settlementDate, asset.effectiveDate, asset.expirationDate, asset.effectiveDate,
                new AbsoluteDate(asset.expirationDate), null, strike, asset.observationType, asset.knockDirection,
                barrier, barrierShift, asset.knockOutObservationStep, asset.fixingObservations, asset.rebateType, rebate,
                asset.annValRatio);

        return new AssetImpl(
                iov,
                party,
                partyRole,
                counterparty,
                counterpartyRole);
    }


}

