package tech.tongyu.bct.trade.dao.schema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.trade.dao.schema.model.TradeSchema;
import tech.tongyu.bct.trade.dao.schema.model.TradeSchemaKey;
import tech.tongyu.bct.trade.dao.schema.repository.TradeSchemaKeyRepository;
import tech.tongyu.bct.trade.dao.schema.repository.TradeSchemaRepository;
import tech.tongyu.core.postgres.BaseBitemporalService;

@Service
public class TradeSchemaService implements BaseBitemporalService<TradeSchema, TradeSchemaKey, TradeSchemaKeyRepository, TradeSchemaRepository> {
  @Autowired
  public TradeSchemaRepository tradeSchemaRepository;

  @Autowired
  public TradeSchemaKeyRepository tradeSchemaKeyRepository;

  public TradeSchemaKeyRepository keyRepo() {
    return tradeSchemaKeyRepository;
  }

  public TradeSchemaRepository bitempRepo() {
    return tradeSchemaRepository;
  }
}
