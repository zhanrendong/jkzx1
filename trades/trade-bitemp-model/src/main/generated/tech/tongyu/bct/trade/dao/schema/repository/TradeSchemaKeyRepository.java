package tech.tongyu.bct.trade.dao.schema.repository;

import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import tech.tongyu.bct.trade.dao.schema.model.TradeSchema;
import tech.tongyu.bct.trade.dao.schema.model.TradeSchemaKey;
import tech.tongyu.core.postgres.RelationalRepository;

@Repository
public class TradeSchemaKeyRepository extends RelationalRepository<TradeSchemaKey, UUID> {
  public TradeSchemaKeyRepository(@Qualifier("bct_trade_key") Class domainClass,
      EntityManager entityManager) {
    super(domainClass, entityManager);
  }

  public List<TradeSchemaKey> key(TradeSchema entity) {
    return findAll(new Specification<TradeSchemaKey>() {
    	@Override
    	public Predicate toPredicate(Root<TradeSchemaKey> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    		List<Predicate> list = new ArrayList();
    		list.add(cb.equal(root.get("tradeId").as(String.class), entity.getTradeId()));
    		Predicate[] p = new Predicate[list.size()];
    		query.where(cb.and(list.toArray(p)));
    		return query.getRestriction();
    	}
    });
  }

  public List<TradeSchemaKey> keys(List<TradeSchema> entities) {
    return findAll(new Specification<TradeSchemaKey>() {
    	@Override
    	public Predicate toPredicate(Root<TradeSchemaKey> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    		List<Predicate> predicateList = entities.stream().map(entity -> {
    			List<Predicate> list = new ArrayList();
    			list.add(cb.equal(root.get("tradeId").as(String.class), entity.getTradeId()));
    			Predicate[] predicates = new Predicate[list.size()];
    			return cb.and(list.toArray(predicates));
    		}).collect(Collectors.toList());
    		Predicate[] predicateArray = new Predicate[predicateList.size()];
    		query.where(cb.or(predicateList.toArray(predicateArray)));
    		return query.getRestriction();
    	}
    });
  }
}
