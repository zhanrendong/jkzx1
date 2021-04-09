package tech.tongyu.bct.trade.dao.schema.repository;

import java.lang.Class;
import java.lang.Exception;
import java.lang.String;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.trade.dao.schema.model.TradePositionSchema;
import tech.tongyu.bct.trade.dao.schema.model.TradePositionSchemaKey;
import tech.tongyu.bct.trade.dao.schema.model.TradeSchema;
import tech.tongyu.bct.trade.dao.schema.model.TradeSchemaKey;
import tech.tongyu.core.annotation.BitemporalKey;
import tech.tongyu.core.annotation.UniqueIndex;
import tech.tongyu.core.annotation.UniqueIndexes;
import tech.tongyu.core.postgres.BitemporalRepository;

@Repository
public class TradeSchemaRepository extends BitemporalRepository<TradeSchema, UUID> {
  private static TradeSchemaRepository dynamicProxy;

  @Autowired
  private TradeSchemaKeyRepository tradeSchemaKeyRepository;

  @Autowired
  private TradePositionSchemaRepository tradePositionSchemaRepository;

  @Autowired
  private TradePositionSchemaKeyRepository tradePositionSchemaKeyRepository;

  public TradeSchemaRepository(@Qualifier("bct_trade") Class domainClass,
      EntityManager entityManager) {
    super(domainClass, entityManager);
  }

  @PostConstruct
  public void init() {
    dynamicProxy = this;
  }

  @Transactional
  public void upsert(TradeSchema entity, int status, UUID applicationEventId, OffsetDateTime valid)
      throws Exception {

    OffsetDateTime now = OffsetDateTime.now();

    TradeSchemaKey tradeSchemaKey = new TradeSchemaKey();
    List<TradeSchemaKey> tradeSchemaKeyList = dynamicProxy.tradeSchemaKeyRepository.key(entity);
    if (tradeSchemaKeyList.isEmpty()) {
    	tradeSchemaKey.setEntityId(UUID.randomUUID());
    	tradeSchemaKey.setTradeId(entity.getTradeId());
    } else {
    	tradeSchemaKey = tradeSchemaKeyList.get(0);
    }
    dynamicProxy.tradeSchemaKeyRepository.save(tradeSchemaKey);

    Field[] positionsFields = Class.forName("tech.tongyu.bct.trade.dao.schema.TradePositionSchema").getDeclaredFields();
    Class positionsClass = TradePositionSchema.class;
    Class _positionsClass = TradePositionSchemaKey.class;

    if (entity.getPositionIds() == null && entity.getPositions() != null) {
    	List<UUID> positionIds = new ArrayList();
    	for(TradePositionSchema tradePositionSchema: entity.getPositions()) {
    		TradePositionSchemaKey tradePositionSchemaKey = new TradePositionSchemaKey();
    		List<TradePositionSchemaKey> tradePositionSchemaKeyList = dynamicProxy.tradePositionSchemaKeyRepository.key(tradePositionSchema);
    		if (tradePositionSchemaKeyList.isEmpty()) {
    			tradePositionSchemaKey.setEntityId(UUID.randomUUID());
    			for(Field field: positionsFields) {
    				if (field.getAnnotation(BitemporalKey.class) != null || field.getAnnotation(UniqueIndex.class) != null || field.getAnnotation(UniqueIndexes.class) != null) {
    					Field f1 = positionsClass.getDeclaredField(field.getName());
    					f1.setAccessible(true);
    					Field f2 = _positionsClass.getDeclaredField(field.getName());
    					f2.setAccessible(true);
    					f2.set(tradePositionSchemaKey, f1.get(tradePositionSchema));
    				}
    			}
    		} else {
    			tradePositionSchemaKey = tradePositionSchemaKeyList.get(0);
    			for(Field field: positionsFields) {
    				if (field.getAnnotation(UniqueIndex.class) != null || field.getAnnotation(UniqueIndexes.class) != null) {
    					Field f1 = positionsClass.getDeclaredField(field.getName());
    					f1.setAccessible(true);
    					Field f2 = _positionsClass.getDeclaredField(field.getName());
    					f2.setAccessible(true);
    					f2.set(tradePositionSchemaKey, f1.get(tradePositionSchema));
    				}
    			}
    		}
    		dynamicProxy.tradePositionSchemaKeyRepository.save(tradePositionSchemaKey);

    		tradePositionSchema.setEntityId(tradePositionSchemaKey.getEntityId());
    		dynamicProxy.tradePositionSchemaRepository.upsert(tradePositionSchema, status, applicationEventId, valid);
    		positionIds.add(tradePositionSchemaKey.getEntityId());
    	}
    	entity.setPositionIds(positionIds.toArray((new UUID[positionIds.size()])));
    }

    entity.setEntityId(tradeSchemaKey.getEntityId());
    insert(entity, status, applicationEventId, valid);
  }

  @Transactional
  public List<TradeSchema> findTradeSchemaByPositionIds(UUID uuid, Timestamp valid,
      Timestamp system) {
    String sql = "SELECT t.* FROM bct_trade_bitemporal t WHERE t.position_ids @> ARRAY[?] AND t.valid_range @> ? \\:\\:timestamptz AND t.system_range @> ? \\:\\:timestamptz";
    Query query = entityManager.createNativeQuery(sql, domainClass);
    query.setParameter(1, uuid);
    query.setParameter(2, valid);
    query.setParameter(3, system);
    return query.getResultList();
  }
}
