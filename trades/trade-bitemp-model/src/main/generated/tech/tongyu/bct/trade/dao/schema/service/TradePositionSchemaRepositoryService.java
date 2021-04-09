package tech.tongyu.bct.trade.dao.schema.service;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.trade.dao.schema.repository.TradePositionSchemaRelationalRepository;
import tech.tongyu.bct.trade.dao.schema.schema.TradePositionSchemaRelational;
import tech.tongyu.core.annotation.QueryApi;
import tech.tongyu.core.annotation.QueryParam;

@Service
public class TradePositionSchemaRepositoryService {
  @Autowired
  public TradePositionSchemaRelationalRepository tradePositionSchemaRelationalRepository;

  @QueryApi(
      description = "通过ID获取相应实例"
  )
  public Optional<TradePositionSchemaRelational> findByEntityIdFromTradePositionSchema(
      @QueryParam(description = "实例ID", required = true) final UUID entityId) {
    return tradePositionSchemaRelationalRepository.findByEntityId(entityId);
  }
}
