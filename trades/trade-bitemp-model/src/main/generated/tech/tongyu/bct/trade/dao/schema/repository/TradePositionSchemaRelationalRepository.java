package tech.tongyu.bct.trade.dao.schema.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import tech.tongyu.bct.trade.dao.schema.schema.TradePositionSchemaRelational;
import tech.tongyu.core.annotation.QueryParam;
import tech.tongyu.core.annotation.QueryTemplate;
import tech.tongyu.core.postgres.BaseRelationalRepository;

@Repository
public interface TradePositionSchemaRelationalRepository extends BaseRelationalRepository<TradePositionSchemaRelational, UUID> {
  @QueryTemplate(
      description = "通过ID获取相应实例"
  )
  Optional<TradePositionSchemaRelational> findByEntityId(
      @QueryParam(description = "实例ID", required = true) final UUID entityId);
}
