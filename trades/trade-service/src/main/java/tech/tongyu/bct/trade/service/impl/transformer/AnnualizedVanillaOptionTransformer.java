package tech.tongyu.bct.trade.service.impl.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.asset.AssetImpl;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.feature.SettlementTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedVanillaOption;
import tech.tongyu.bct.cm.reference.elemental.Party;
import tech.tongyu.bct.cm.reference.impl.*;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.market.dto.InstrumentDTO;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.trade.dto.trade.TradePositionDTO;
import tech.tongyu.bct.trade.dto.trade.product.AnnualizedVanillaOptionDTO;
import tech.tongyu.bct.trade.service.AssetTransformer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static tech.tongyu.bct.trade.service.impl.transformer.Utils.fromDTO;
import static tech.tongyu.bct.trade.service.impl.transformer.Utils.toUnitOfValue;

@Component
public class AnnualizedVanillaOptionTransformer implements AssetTransformer {

    private MarketDataService marketDataService;

    @Autowired
    public AnnualizedVanillaOptionTransformer(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @Override
    public Optional<Object> transform(BctTradePosition position) {
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if ((instrument instanceof AnnualizedVanillaOption)) {
            return Optional.of(toDTO(position));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Asset<InstrumentOfValue>> transform(TradePositionDTO dto) {
        if ((ProductTypeEnum.VANILLA_EUROPEAN.equals(dto.productType) || ProductTypeEnum.VANILLA_AMERICAN.equals(dto.productType))) {
            return Optional.of(toVanillaOption(dto));
        } else {
            return Optional.empty();
        }
    }

    private AssetImpl toVanillaOption(TradePositionDTO dto) {

        AnnualizedVanillaOptionDTO asset = JsonUtils.mapper.convertValue(dto.asset, AnnualizedVanillaOptionDTO.class);

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

        ProductTypeEnum productType;
        switch (asset.exerciseType) {
            case EUROPEAN:
                productType = ProductTypeEnum.VANILLA_EUROPEAN;
                break;
            case AMERICAN:
                productType = ProductTypeEnum.VANILLA_AMERICAN;
                break;
            default:
                throw new RuntimeException("not supported product type");

        }

        // TODO(http://jira.tongyu.tech:8080/browse/OTMS-1394): 暂时的placeholder，其他服务实现以后再完善
        Party counterparty = new SimpleParty(UUID.randomUUID(), dto.getCounterPartyCode(), dto.getCounterPartyName());
        Party party = new SimpleParty(UUID.randomUUID(), "party", "party");

        AnnualizedVanillaOption iov = new AnnualizedVanillaOption(underlyer, asset.underlyerMultiplier,
                asset.optionType, productType, assetClassType, notional,
                asset.participationRate, asset.effectiveDate, asset.expirationDate, asset.daysInYear,
                asset.term, asset.annualized, asset.exerciseType, new AbsoluteDate(asset.expirationDate), null
                , asset.initialSpot, asset.settlementDate, SettlementTypeEnum.CASH, strike, asset.effectiveDate, asset.specifiedPrice,
                frontPremium, minimumPremium, premium, asset.annValRatio);
        return new AssetImpl(
                iov,
                party,
                partyRole,
                counterparty,
                counterpartyRole);
    }

    private AnnualizedVanillaOptionDTO toDTO(BctTradePosition position) {
        AnnualizedVanillaOption vo = (AnnualizedVanillaOption) position.asset().instrumentOfValue();
        return new AnnualizedVanillaOptionDTO(vo.exerciseType, position.partyRole(), vo.underlyer.instrumentId(),
                vo.initialSpot, (BigDecimal) vo.strike.value(), UnitEnum.valueOf(vo.strike.unit().name()),
                vo.specifiedPrice(), vo.settlementDate, vo.term, vo.annualized, vo.daysInYear, vo.participationRate,
                vo.optionType, (BigDecimal) vo.notionalAmount.value(), UnitEnum.valueOf(vo.notionalAmount.unit().name()),
                vo.underlyerMultiplier, vo.absoluteExpirationDate(), position.counterparty.partyCode(),
                vo.effectiveDate, UnitEnum.valueOf(vo.premium().unit.name()),
                (BigDecimal) vo.frontPremium().value, (BigDecimal) vo.premium().value, (BigDecimal) vo.minimumPremium().value
                , vo.notionalAmountValue(), vo.notionalLotAmountValue(), vo.actualPremium(), vo.annValRatio
        );
    }

}
