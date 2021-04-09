package tech.tongyu.bct.trade.dao.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.Type;
import tech.tongyu.core.annotation.Bitemporal;
import tech.tongyu.core.annotation.BitemporalKey;
import tech.tongyu.core.annotation.Relational;

import javax.persistence.Column;
import java.math.BigDecimal;

@Bitemporal(schema = "trade_service")
@Relational(schema = "trade_service")
public class TradePositionSchema {

    @Column
    public String bookName;

    @BitemporalKey
    @Column
    public String positionId;

    @Column
    public BigDecimal quantity;

    @Column(name = "asset", columnDefinition = "jsonb")
    @Type(type = "PGJson")
    public JsonNode asset;

    @Column(name = "counterparty", columnDefinition = "jsonb")
    @Type(type = "PGJson")
    public JsonNode counterparty;

    @Column(name = "position_account", columnDefinition = "jsonb")
    @Type(type = "PGJson")
    public JsonNode positionAccount;

    @Column(name = "counterparty_account", columnDefinition = "jsonb")
    @Type(type = "PGJson")
    public JsonNode counterpartyAccount;
}
