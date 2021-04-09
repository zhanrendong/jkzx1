package tech.tongyu.bct.trade.dao.schema.repository;

import java.lang.Class;
import java.lang.Exception;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.trade.dao.schema.model.TradePositionSchema;
import tech.tongyu.bct.trade.dao.schema.model.TradePositionSchemaKey;
import tech.tongyu.core.postgres.BitemporalRepository;

@Repository
public class TradePositionSchemaRepository extends BitemporalRepository<TradePositionSchema, UUID> {
  private static TradePositionSchemaRepository dynamicProxy;

  @Autowired
  private TradePositionSchemaKeyRepository tradePositionSchemaKeyRepository;

  public TradePositionSchemaRepository(@Qualifier("trade_position_schema") Class domainClass,
      EntityManager entityManager) {
    super(domainClass, entityManager);
  }

  @PostConstruct
  public void init() {
    dynamicProxy = this;
  }

  @Transactional
  public void upsert(TradePositionSchema entity, int status, UUID applicationEventId,
      OffsetDateTime valid) throws Exception {

    OffsetDateTime now = OffsetDateTime.now();

    TradePositionSchemaKey tradePositionSchemaKey = new TradePositionSchemaKey();
    List<TradePositionSchemaKey> tradePositionSchemaKeyList = dynamicProxy.tradePositionSchemaKeyRepository.key(entity);
    if (tradePositionSchemaKeyList.isEmpty()) {
    	tradePositionSchemaKey.setEntityId(UUID.randomUUID());
    	tradePositionSchemaKey.setPositionId(entity.getPositionId());
    } else {
    	tradePositionSchemaKey = tradePositionSchemaKeyList.get(0);
    }
    dynamicProxy.tradePositionSchemaKeyRepository.save(tradePositionSchemaKey);

    entity.setEntityId(tradePositionSchemaKey.getEntityId());
    insert(entity, status, applicationEventId, valid);
  }
}
