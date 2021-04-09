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
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedVerticalSpreadOption;
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
import tech.tongyu.bct.trade.dto.trade.product.AnnualizedVerticalSpreadOptionDTO;
import tech.tongyu.bct.trade.service.AssetTransformer;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static tech.tongyu.bct.trade.service.impl.transformer.Utils.fromDTO;
import static tech.tongyu.bct.trade.service.impl.transformer.Utils.toUnitOfValue;

@Component
public class AnnualizedVerticalSpreadOptionTransformer implements AssetTransformer {

    private MarketDataService marketDataService;

    @Autowired
    public AnnualizedVerticalSpreadOptionTransformer(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @Override
    public Optional<Object> transform(BctTradePosition position) {
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if ((instrument instanceof AnnualizedVerticalSpreadOption)) {
            return Optional.of(toDTO(position));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Asset<InstrumentOfValue>> transform(TradePositionDTO dto) {
        if ((ProductTypeEnum.VERTICAL_SPREAD.equals(dto.productType))) {
            return Optional.of(toCM(dto));
        } else {
            return Optional.empty();
        }
    }

    private AssetImpl toCM(TradePositionDTO dto) {

        AnnualizedVerticalSpreadOptionDTO asset = JsonUtils.mapper.convertValue(dto.asset, AnnualizedVerticalSpreadOptionDTO.class);

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
        List<UnitOfValue<BigDecimal>> strikes = new ArrayList<>();
        strikes.add(lowStrike);
        strikes.add(highStrike);
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

        ProductTypeEnum productType = ProductTypeEnum.VERTICAL_SPREAD;

        // TODO(http://jira.tongyu.tech:8080/browse/OTMS-1394): 暂时的placeholder，其他服务实现以后再完善
        Party counterparty = new SimpleParty(UUID.randomUUID(), dto.getCounterPartyCode(), dto.getCounterPartyName());
        Party party = new SimpleParty(UUID.randomUUID(), "party", "party");

        AnnualizedVerticalSpreadOption iov = new AnnualizedVerticalSpreadOption(underlyer, asset.underlyerMultiplier,
                asset.optionType, productType, assetClassType, notional,
                asset.participationRate, asset.effectiveDate, asset.expirationDate, asset.daysInYear,
                asset.term, asset.annualized, asset.exerciseType, new AbsoluteDate(asset.expirationDate), null
                , asset.initialSpot, asset.settlementDate, SettlementTypeEnum.CASH, strikes, asset.effectiveDate, asset.specifiedPrice,
                frontPremium, minimumPremium, premium, asset.annValRatio);
        return new AssetImpl(
                iov,
                party,
                partyRole,
                counterparty,
                counterpartyRole);
    }

    private AnnualizedVerticalSpreadOptionDTO toDTO(BctTradePosition position) {
        AnnualizedVerticalSpreadOption vo = (AnnualizedVerticalSpreadOption) position.asset().instrumentOfValue();
        UnitEnum strikeType = UnitEnum.valueOf(((UnitOfValue<BigDecimal>) vo.strikes.get(0)).unit().name());
        List<BigDecimal> strikes =
                (List<BigDecimal>) vo.strikes.stream().map(strike -> ((UnitOfValue<BigDecimal>) strike).value())
                        .collect(Collectors.toList());
        if (strikes.size() != 2) {
            throw new RuntimeException("number of strike is not 2");
        }
        Collections.sort(strikes);
        BigDecimal lowStrike = strikes.get(0);
        BigDecimal highStrike = strikes.get(1);
        return new AnnualizedVerticalSpreadOptionDTO(vo.exerciseType, position.partyRole(), vo.underlyer.instrumentId(),
                vo.initialSpot, strikeType, lowStrike, highStrike,
                vo.specifiedPrice(), vo.settlementDate, vo.term, vo.annualized, vo.daysInYear, vo.participationRate,
                vo.optionType, (BigDecimal) vo.notionalAmount.value(), UnitEnum.valueOf(vo.notionalAmount.unit().name()),
                vo.underlyerMultiplier, vo.absoluteExpirationDate(), position.counterparty.partyCode(),
                vo.effectiveDate, UnitEnum.valueOf(vo.premium().unit.name()),
                (BigDecimal) vo.frontPremium().value, (BigDecimal) vo.premium().value, (BigDecimal) vo.minimumPremium().value
                , vo.notionalAmountValue(), vo.notionalLotAmountValue(), vo.actualPremium(), vo.annValRatio
        );
    }

}
