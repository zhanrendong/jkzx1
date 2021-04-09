package tech.tongyu.bct.trade.dao.schema.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.lang.String;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.beans.factory.annotation.Autowired;
import tech.tongyu.core.postgres.BaseBitemporalEntity;
import tech.tongyu.core.postgres.type.PGJson;
import tech.tongyu.core.postgres.type.PGUuidArray;

@Entity
@Inheritance(
    strategy = InheritanceType.TABLE_PER_CLASS
)
@TypeDefs({
    	@TypeDef(name = "PGJson", typeClass = PGJson.class),
    	@TypeDef(name = "PGUuidArray", typeClass = PGUuidArray.class)
    })
@Table(
    schema = "trade_service",
    name = "trade_position_schema_bitemporal",
    indexes = {
        	@Index(columnList = "entity_id"),
        	@Index(columnList = "valid_range"),
        	@Index(columnList = "system_range")
        }
)
public class TradePositionSchema extends BaseBitemporalEntity implements Serializable {
  @Autowired
  @Transient
  private ObjectMapper objectMapper;

  @Column(
      name = "book_name"
  )
  String bookName;

  @Column(
      name = "position_id"
  )
  String positionId;

  @Column(
      name = "quantity"
  )
  BigDecimal quantity;

  @Column(
      columnDefinition = "jsonb",
      name = "asset"
  )
  @Type(
      type = "PGJson"
  )
  JsonNode asset;

  @Column(
      columnDefinition = "jsonb",
      name = "counterparty"
  )
  @Type(
      type = "PGJson"
  )
  JsonNode counterparty;

  @Column(
      columnDefinition = "jsonb",
      name = "position_account"
  )
  @Type(
      type = "PGJson"
  )
  JsonNode positionAccount;

  @Column(
      columnDefinition = "jsonb",
      name = "counterparty_account"
  )
  @Type(
      type = "PGJson"
  )
  JsonNode counterpartyAccount;

  @ManyToOne
  @JoinColumn(
      name = "entity_id",
      referencedColumnName = "entity_id",
      insertable = false,
      updatable = false
  )
  private TradePositionSchemaKey tradePositionSchemaKey;

  public String getBookName() {
    return bookName;
  }

  public String getPositionId() {
    return positionId;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public JsonNode getAsset() {
    return asset;
  }

  public JsonNode getCounterparty() {
    return counterparty;
  }

  public JsonNode getPositionAccount() {
    return positionAccount;
  }

  public JsonNode getCounterpartyAccount() {
    return counterpartyAccount;
  }

  public void setBookName(String bookName) {
    this.bookName = bookName;
  }

  public void setPositionId(String positionId) {
    this.positionId = positionId;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public void setAsset(JsonNode asset) {
    this.asset = asset;
  }

  public void setCounterparty(JsonNode counterparty) {
    this.counterparty = counterparty;
  }

  public void setPositionAccount(JsonNode positionAccount) {
    this.positionAccount = positionAccount;
  }

  public void setCounterpartyAccount(JsonNode counterpartyAccount) {
    this.counterpartyAccount = counterpartyAccount;
  }

  public TradePositionSchemaKey getTradePositionSchemaKey() {
    return tradePositionSchemaKey;
  }

  public void setTradePositionSchemaKey(TradePositionSchemaKey tradePositionSchemaKey) {
    this.tradePositionSchemaKey = tradePositionSchemaKey;
  }
}
