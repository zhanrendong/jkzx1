package tech.tongyu.bct.trade.dao.schema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.trade.dao.schema.model.TradePositionSchema;
import tech.tongyu.bct.trade.dao.schema.model.TradePositionSchemaKey;
import tech.tongyu.bct.trade.dao.schema.repository.TradePositionSchemaKeyRepository;
import tech.tongyu.bct.trade.dao.schema.repository.TradePositionSchemaRepository;
import tech.tongyu.core.postgres.BaseBitemporalService;

@Service
public class TradePositionSchemaService implements BaseBitemporalService<TradePositionSchema, TradePositionSchemaKey, TradePositionSchemaKeyRepository, TradePositionSchemaRepository> {
  @Autowired
  public TradePositionSchemaRepository tradePositionSchemaRepository;

  @Autowired
  public TradePositionSchemaKeyRepository tradePositionSchemaKeyRepository;

  public TradePositionSchemaKeyRepository keyRepo() {
    return tradePositionSchemaKeyRepository;
  }

  public TradePositionSchemaRepository bitempRepo() {
    return tradePositionSchemaRepository;
  }
}
