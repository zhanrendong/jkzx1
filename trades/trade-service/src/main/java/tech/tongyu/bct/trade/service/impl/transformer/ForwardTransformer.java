package tech.tongyu.bct.trade.service.impl.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.asset.AssetImpl;
import tech.tongyu.bct.cm.product.iov.InstrumentAssetClassTypeEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.SettlementTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.Forward;
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
import tech.tongyu.bct.trade.dto.trade.product.ForwardDTO;
import tech.tongyu.bct.trade.service.AssetTransformer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static tech.tongyu.bct.trade.service.impl.transformer.Utils.fromDTO;
import static tech.tongyu.bct.trade.service.impl.transformer.Utils.oppositePartyRole;
import static tech.tongyu.bct.trade.service.impl.transformer.Utils.toUnitOfValue;

@Component
public class ForwardTransformer implements AssetTransformer {

    private MarketDataService marketDataService;

    @Autowired
    public ForwardTransformer(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @Override
    public Optional<Object> transform(BctTradePosition position) {
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if ((instrument instanceof Forward)) {
            return Optional.of(toDTO(position));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Asset<InstrumentOfValue>> transform(TradePositionDTO dto) {
        if (ProductTypeEnum.FORWARD.equals(dto.productType)) {
            return Optional.of(toForwardsOption(dto));
        } else {
            return Optional.empty();
        }
    }

    private AssetImpl toForwardsOption(TradePositionDTO dto) {

        ForwardDTO asset = JsonUtils.mapper.convertValue(dto.asset, ForwardDTO.class);

        Optional<InstrumentDTO> instrument = marketDataService.getInstrument(asset.underlyerInstrumentId);

        if (!instrument.isPresent()) {
            throw new CustomException(String.format("Cannot find instrument: %s", asset.underlyerInstrumentId));
        }
        InstrumentDTO instrumentDTO = instrument.get();
        InstrumentOfValue underlyer = fromDTO(instrumentDTO);

        InstrumentAssetClassTypeEnum assetClassType = underlyer.assetClassType();
        InstrumentOfValuePartyRoleTypeEnum partyRole = asset.direction;
        InstrumentOfValuePartyRoleTypeEnum counterpartyRole = oppositePartyRole(partyRole);

        UnitOfValue<BigDecimal> strike = toUnitOfValue(asset.strikeType, asset.strike);
        UnitOfValue<BigDecimal> notional = toUnitOfValue(asset.notionalAmountType, asset.notionalAmount);

        UnitOfValue<BigDecimal> premium = toUnitOfValue(asset.premiumType, asset.premium);

        // TODO(http://jira.tongyu.tech:8080/browse/OTMS-1394): 暂时的placeholder，其他服务实现以后再完善
        Party counterparty = new SimpleParty(UUID.randomUUID(), dto.getCounterPartyCode(), dto.getCounterPartyName());
        Party party = new SimpleParty(UUID.randomUUID(), "party", "party");

        Forward iov = new Forward(underlyer, asset.underlyerMultiplier, ProductTypeEnum.FORWARD,
                assetClassType, notional, asset.effectiveDate, asset.expirationDate, new AbsoluteDate(asset.expirationDate),
                null, asset.initialSpot, asset.settlementDate, SettlementTypeEnum.CASH, strike, asset.effectiveDate,
                asset.specifiedPrice, premium);
        return new AssetImpl(
                iov,
                party,
                partyRole,
                counterparty,
                counterpartyRole);
    }

    private ForwardDTO toDTO(BctTradePosition position) {
        Forward vo = (Forward) position.asset().instrumentOfValue();
        return new ForwardDTO(position.partyRole(), vo.underlyer.instrumentId(), vo.initialSpot,
                (BigDecimal) vo.strike.value(), UnitEnum.valueOf(vo.strike.unit().name()), vo.specifiedPrice(),
                vo.settlementDate, (BigDecimal) vo.notionalAmount.value(), UnitEnum.valueOf(vo.notionalAmount.unit().name()),
                vo.underlyerMultiplier, vo.absoluteExpirationDate(), position.counterparty.partyCode(), vo.effectiveDate,
                UnitEnum.valueOf(vo.premium().unit.name()), (BigDecimal) vo.premium().value,vo.annualized
                , vo.notionalAmountValue(), vo.notionalLotAmountValue(), vo.actualPremium()
        );
    }

}
