package tech.tongyu.bct.trade.service;

import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.trade.dto.trade.TradePositionDTO;

import java.util.Optional;

@Component
public interface AssetTransformer {

    Optional<Object> transform(BctTradePosition position);

    Optional<Asset<InstrumentOfValue>> transform(TradePositionDTO dto);


}
