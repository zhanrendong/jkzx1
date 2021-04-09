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
    name = "trade_position_schema_key",
    uniqueConstraints = {
        	@UniqueConstraint(columnNames={"position_id"})
        }
)
@Inheritance(
    strategy = InheritanceType.TABLE_PER_CLASS
)
public class TradePositionSchemaKey extends BaseRelationalEntity implements Serializable {
  @Column(
      name = "position_id",
      nullable = false
  )
  public String positionId;

  public void setPositionId(String positionId) {
    this.positionId = positionId;
  }

  public String getPositionId() {
    return positionId;
  }
}
