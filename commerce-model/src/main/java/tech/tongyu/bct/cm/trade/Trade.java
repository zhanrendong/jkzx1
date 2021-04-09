package tech.tongyu.bct.cm.trade;

import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface Trade {
    List<Position<Asset<InstrumentOfValue>>> positions();

    List<NonEconomicPartyRole> nonEconomicPartyRoles();

    LocalDate tradeDate();

    Optional<String> comment();
}
