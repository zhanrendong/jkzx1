package tech.tongyu.bct.trade.service.impl.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.asset.AssetImpl;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.feature.SettlementTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.SpreadsInstrument;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedSpreadsOption;
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
import tech.tongyu.bct.trade.dto.trade.product.SpreadsOptionDTO;
import tech.tongyu.bct.trade.service.AssetTransformer;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static tech.tongyu.bct.trade.service.impl.transformer.Utils.fromDTO;
import static tech.tongyu.bct.trade.service.impl.transformer.Utils.toUnitOfValue;

@Component
public class SpreadsOptionTransformer implements AssetTransformer {

    private MarketDataService marketDataService;

    @Autowired
    public SpreadsOptionTransformer(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @Override
    public Optional<Object> transform(BctTradePosition position) {
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if ((instrument instanceof AnnualizedSpreadsOption)) {
            return Optional.of(toDTO(position));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Asset<InstrumentOfValue>> transform(TradePositionDTO dto) {
        if ((ProductTypeEnum.SPREAD_EUROPEAN == dto.productType ||
                ProductTypeEnum.RATIO_SPREAD_EUROPEAN == dto.productType)) {
            return Optional.of(toSpreadsOption(dto));
        } else {
            return Optional.empty();
        }
    }

    private AssetImpl toSpreadsOption(TradePositionDTO dto) {
        SpreadsOptionDTO asset = JsonUtils.mapper.convertValue(dto.asset, SpreadsOptionDTO.class);

        List<BasketInstrumentConstituent> constituents = asset.constituents.stream().map(item -> {
            String instrumentId = (String) item.get(SpreadsInstrument.INSTRUMENT_ID_KEY);
            InstrumentDTO instrument = marketDataService.getInstrument(instrumentId).orElseThrow(
                    () -> new CustomException(String.format("Cannot find instrument: %s", instrumentId)));
            BigDecimal multiplier = new BigDecimal(item.get(SpreadsInstrument.MULTIPIER_KEY).toString());
            BigDecimal initialSpot = new BigDecimal(item.get(SpreadsInstrument.INITIAL_SPOT_KEY).toString());
            BigDecimal weight = new BigDecimal(item.get(SpreadsInstrument.WEIGHT_KEY).toString());
            InstrumentOfValue underlyer = fromDTO(instrument);
            return new tech.tongyu.bct.cm.product.iov.impl.BasketInstrumentConstituent(
                    underlyer, weight, multiplier, initialSpot);
        }).collect(Collectors.toList());

        StringJoiner basketId = new StringJoiner(SpreadsInstrument.BASKET_ID_JOINER);
        constituents.stream().forEach(item -> basketId.add(item.instrument().instrumentId()));
        SpreadsInstrument underlyer = new SpreadsInstrument(basketId.toString(), constituents);

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
        UnitOfValue<BigDecimal> minimumPremium = asset.annualized ? toUnitOfValue(asset.premiumType, asset.minimumPremium)
                : toUnitOfValue(asset.premiumType, BigDecimal.ZERO);
        UnitOfValue<BigDecimal> frontPremium = asset.annualized ? toUnitOfValue(asset.premiumType, asset.frontPremium)
                : toUnitOfValue(asset.premiumType, BigDecimal.ZERO);

        ProductTypeEnum productType = dto.productType;


        // TODO(http://jira.tongyu.tech:8080/browse/OTMS-1394): 暂时的placeholder，其他服务实现以后再完善
        Party counterparty = new SimpleParty(UUID.randomUUID(), dto.getCounterPartyCode(), dto.getCounterPartyName());
        Party party = new SimpleParty(UUID.randomUUID(), "party", "party");

        AnnualizedSpreadsOption iov = new AnnualizedSpreadsOption(underlyer, asset.optionType, productType, assetClassType, notional,
                asset.participationRate, asset.effectiveDate, asset.expirationDate, asset.daysInYear,
                asset.term, asset.annualized, asset.exerciseType, new AbsoluteDate(asset.expirationDate), null,
                asset.settlementDate, SettlementTypeEnum.CASH, strike, asset.effectiveDate, asset.specifiedPrice,
                frontPremium, minimumPremium, premium, asset.annValRatio);
        return new AssetImpl(iov, party, partyRole, counterparty, counterpartyRole);
    }

    private SpreadsOptionDTO toDTO(BctTradePosition position) {
        AnnualizedSpreadsOption vo = (AnnualizedSpreadsOption) position.asset().instrumentOfValue();
        return new SpreadsOptionDTO(vo.exerciseType, position.partyRole(), vo.productType,
                (BigDecimal) vo.strike.value(), UnitEnum.valueOf(vo.strike.unit().name()), vo.specifiedPrice(),
                vo.settlementDate, vo.term, vo.annualized, vo.daysInYear, vo.participationRate, vo.optionType,
                (BigDecimal) vo.notionalAmount.value(), UnitEnum.valueOf(vo.notionalAmount.unit().name()),
                vo.absoluteExpirationDate(), position.counterparty.partyCode(), vo.effectiveDate,
                UnitEnum.valueOf(vo.premium().unit.name()), (BigDecimal) vo.frontPremium().value,
                (BigDecimal) vo.premium().value, (BigDecimal) vo.minimumPremium().value,
                (SpreadsInstrument) vo.underlyer, vo.annValRatio);
    }

}
