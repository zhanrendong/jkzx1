package tech.tongyu.bct.trade.dao.schema.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.String;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.beans.factory.annotation.Autowired;
import tech.tongyu.core.postgres.BaseRelationalEntity;
import tech.tongyu.core.postgres.type.PGJson;
import tech.tongyu.core.postgres.type.PGUuidArray;

@Entity
@Table(
    schema = "trade_service",
    name = "trade_position_schema",
    uniqueConstraints = {
        	@UniqueConstraint(columnNames={"position_id"})
        }
)
@Inheritance(
    strategy = InheritanceType.TABLE_PER_CLASS
)
@TypeDefs({
    	@TypeDef(name = "PGJson", typeClass = PGJson.class),
    	@TypeDef(name = "PGUuidArray", typeClass = PGUuidArray.class)
    })
public class TradePositionSchemaRelational extends BaseRelationalEntity {
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
}
