package tech.tongyu.bct.trade.dao.schema;

import tech.tongyu.bct.cm.trade.NonEconomicPartyRole;
import tech.tongyu.core.annotation.Bitemporal;
import tech.tongyu.core.annotation.BitemporalKey;
import tech.tongyu.core.annotation.BitemporalMapping;
import tech.tongyu.core.annotation.Relational;

import javax.persistence.Column;
import java.time.LocalDate;
import java.util.List;

@Bitemporal(table = "bct_trade", schema = "trade_service")
@Relational(table = "bct_trade", schema = "trade_service")
public class TradeSchema {
    @BitemporalKey
    @Column(name = "trade_id")
    public String tradeId;

    @Column(name = "trader")
    public String trader;

    @BitemporalMapping(name = "positions", column = "position_ids", isList = true)
    public List<TradePositionSchema> positions;

    @BitemporalMapping(name = "nonEconomicPartyRoles", column = "non_economic_party_roles", isEntity = false)
    public List<NonEconomicPartyRole> nonEconomicPartyRoles;

    @Column(name = "trade_date")
    public LocalDate tradeDate;

    @Column(name = "comment")
    public String comment;

    @Column(name = "book")
    public String book;
}
