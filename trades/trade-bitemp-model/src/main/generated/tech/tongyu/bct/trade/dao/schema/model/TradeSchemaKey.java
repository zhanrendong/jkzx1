package tech.tongyu.bct.trade.dao.schema.model;

import java.io.Serializable;
import java.lang.String;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import tech.tongyu.core.postgres.BaseRelationalEntity;

@Entity
@Table(
    schema = "trade_service",
    name = "bct_trade_key",
    uniqueConstraints = {
        	@UniqueConstraint(columnNames={"trade_id"})
        }
)
@Inheritance(
    strategy = InheritanceType.TABLE_PER_CLASS
)
public class TradeSchemaKey extends BaseRelationalEntity implements Serializable {
  @Column(
      name = "trade_id",
      nullable = false
  )
  public String tradeId;

  public void setTradeId(String tradeId) {
    this.tradeId = tradeId;
  }

  public String getTradeId() {
    return tradeId;
  }
}
