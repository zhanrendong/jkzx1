package tech.tongyu.bct.trade.dao.schema.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.lang.Exception;
import java.lang.String;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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
import tech.tongyu.bct.cm.trade.NonEconomicPartyRole;
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
    name = "bct_trade_bitemporal",
    indexes = {
        	@Index(columnList = "entity_id"),
        	@Index(columnList = "valid_range"),
        	@Index(columnList = "system_range")
        }
)
public class TradeSchema extends BaseBitemporalEntity implements Serializable {
  @Autowired
  @Transient
  private ObjectMapper objectMapper;

  @Column(
      name = "trade_id"
  )
  String tradeId;

  @Column(
      name = "trader"
  )
  String trader;

  @Column(
      name = "position_ids",
      columnDefinition = "uuid[]"
  )
  @Type(
      type = "PGUuidArray"
  )
  UUID[] positionIds;

  @Column(
      name = "non_economic_party_roles",
      columnDefinition = "jsonb"
  )
  @Type(
      type = "PGJson"
  )
  JsonNode nonEconomicPartyRoles;

  @Column(
      name = "trade_date"
  )
  LocalDate tradeDate;

  @Column(
      name = "comment"
  )
  String comment;

  @Column(
      name = "book"
  )
  String book;

  @Transient
  List<TradePositionSchema> positions;

  @ManyToOne
  @JoinColumn(
      name = "entity_id",
      referencedColumnName = "entity_id",
      insertable = false,
      updatable = false
  )
  private TradeSchemaKey tradeSchemaKey;

  public String getTradeId() {
    return tradeId;
  }

  public String getTrader() {
    return trader;
  }

  public List<TradePositionSchema> getPositions() {
    return positions;
  }

  public UUID[] getPositionIds() {
    return positionIds;
  }

  public JsonNode getNonEconomicPartyRoles() {
    return nonEconomicPartyRoles;
  }

  public List<NonEconomicPartyRole> nonEconomicPartyRoles() throws Exception {
    return objectMapper.readValue(nonEconomicPartyRoles.toString(), new TypeReference<List<NonEconomicPartyRole>>(){});
  }

  public LocalDate getTradeDate() {
    return tradeDate;
  }

  public String getComment() {
    return comment;
  }

  public String getBook() {
    return book;
  }

  public void setTradeId(String tradeId) {
    this.tradeId = tradeId;
  }

  public void setTrader(String trader) {
    this.trader = trader;
  }

  public void setPositions(List<TradePositionSchema> positions) {
    this.positions = positions;
  }

  public void setPositionIds(UUID[] positionIds) {
    this.positionIds = positionIds;
  }

  public void setNonEconomicPartyRoles(JsonNode nonEconomicPartyRoles) {
    this.nonEconomicPartyRoles = nonEconomicPartyRoles;
  }

  public void nonEconomicPartyRoles_(List<NonEconomicPartyRole> nonEconomicPartyRoles) throws
      Exception {
    this.nonEconomicPartyRoles = objectMapper.readTree(objectMapper.writeValueAsString(nonEconomicPartyRoles));
  }

  public void setTradeDate(LocalDate tradeDate) {
    this.tradeDate = tradeDate;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void setBook(String book) {
    this.book = book;
  }

  public TradeSchemaKey getTradeSchemaKey() {
    return tradeSchemaKey;
  }

  public void setTradeSchemaKey(TradeSchemaKey tradeSchemaKey) {
    this.tradeSchemaKey = tradeSchemaKey;
  }
}
