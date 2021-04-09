package tech.tongyu.bct.trade.service.impl.transformer;

import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.asset.AssetImpl;
import tech.tongyu.bct.cm.product.iov.CashFlowDirectionEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.CashFlow;
import tech.tongyu.bct.cm.reference.elemental.Party;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;
import tech.tongyu.bct.cm.reference.impl.SimpleParty;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.trade.dto.trade.TradePositionDTO;
import tech.tongyu.bct.trade.dto.trade.product.CashFlowDTO;
import tech.tongyu.bct.trade.service.AssetTransformer;

import java.util.Optional;
import java.util.UUID;

import static tech.tongyu.bct.trade.service.impl.transformer.Utils.oppositePartyRole;

@Component
public class CashFlowTransformer implements AssetTransformer {
    public CashFlowTransformer() {
    }

    @Override
    public Optional<Object> transform(BctTradePosition position) {
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if ((instrument instanceof CashFlow)) {
            return Optional.of(toDTO(position));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Asset<InstrumentOfValue>> transform(TradePositionDTO dto) {
        if (ProductTypeEnum.CASH_FLOW.equals(dto.productType)) {
            return Optional.of(toCashFlow(dto));
        } else {
            return Optional.empty();
        }
    }

    private AssetImpl toCashFlow(TradePositionDTO dto) {

        CashFlowDTO asset = JsonUtils.mapper.convertValue(dto.asset, CashFlowDTO.class);
        InstrumentOfValuePartyRoleTypeEnum partyRole = asset.paymentDirection == CashFlowDirectionEnum.PAY ?
                InstrumentOfValuePartyRoleTypeEnum.BUYER : InstrumentOfValuePartyRoleTypeEnum.SELLER;

        // TODO(http://jira.tongyu.tech:8080/browse/OTMS-1394): 暂时的placeholder，其他服务实现以后再完善
        Party counterparty = new SimpleParty(UUID.randomUUID(), dto.getCounterPartyCode(), dto.getCounterPartyName());
        Party party = new SimpleParty(UUID.randomUUID(), "party", "party");

        CashFlow iov = new CashFlow(ProductTypeEnum.CASH_FLOW, asset.paymentAmount, asset.paymentDirection,
                asset.settlementDate, CurrencyUnit.CNY, asset.paymentState);
        return new AssetImpl(iov, party, partyRole, counterparty, oppositePartyRole(partyRole));
    }

    private CashFlowDTO toDTO(BctTradePosition position) {
        CashFlow vo = (CashFlow) position.asset().instrumentOfValue();
        return new CashFlowDTO(vo.paymentDirection(), vo.currency.name, vo.paymentAmount, vo.settlementDate, vo.paymentState);
    }

}
