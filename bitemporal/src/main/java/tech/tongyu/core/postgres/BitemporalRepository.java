package tech.tongyu.core.postgres;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.core.postgres.type.TsTzRange;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoRepositoryBean
public class BitemporalRepository<T extends BaseBitemporalEntity, ID extends UUID>
        extends SimpleJpaRepository<T, ID> implements BaseBitemporalRepository<T, ID> {

    private static Logger logger = LoggerFactory.getLogger(BitemporalRepository.class);

    protected final Class<T> domainClass;
    protected final EntityManager entityManager;

    public BitemporalRepository(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        this.domainClass = domainClass;
        this.entityManager = entityManager;
    }

    @Override
    public String table() {
        Table table = this.getDomainClass().getAnnotation(Table.class);
        return table.schema() + "." + table.name();
    }

    @Transactional
    void insertN(T entity) {
        StringBuffer sb = new StringBuffer("entity_id, entity_doc, ");
        Stream.of(domainClass.getDeclaredFields()).filter(f -> f.getAnnotation(Column.class) != null).forEach(f -> sb.append(f.getAnnotation(Column.class).name() + ", "));
        String columns = sb.substring(0, sb.lastIndexOf(","));
        String sql = "insert into " + table().replace("_bitemporal", "") + " " +
                "(" + columns + ")" +
                " select " + columns + " from " + table() + " where entity_id = ? and entity_status = 1 " +
                "and lower(valid_range) = ?  \\:\\:timestamptz " +
                "and lower(system_range) = ?  \\:\\:timestamptz ";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, entity.getEntityId());
        query.setParameter(2, entity.getValidRange().getStart());
        query.setParameter(3, entity.getSystemRange().getStart());
        query.executeUpdate();
    }

    @Transactional
    void deleteN(T entity) {
        String sql = "delete from " + table().replace("_bitemporal", "") + " where entity_id = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, entity.getEntityId());
        query.executeUpdate();
    }

    @Override
    @Transactional
    public UUID insert(T entity, int status, UUID applicationEventId, OffsetDateTime valid_from, OffsetDateTime valid_to) throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime valid_origin = TsTzRange.ORIGIN;
        UUID entityVersionId = UUID.randomUUID();
        T t = find(entity.getEntityId(), valid_from, now);
        entity.setApplicationEventId(applicationEventId);
        if (t!=null) {
            t.setSystemRange(TsTzRange.of(t.getSystemRange().getStart(), now));
            entityManager.persist(t);
            entityManager.flush();
            entityManager.clear();

            valid_origin = t.getValidRange().getStart();
            String sql = "update " + table() + " set system_range = tstzrange(lower(system_range), ?) " +
                    "where entity_id = ? " +
                    "and system_range @> ? \\:\\:timestamptz " +
                    "and lower(valid_range) >= ? \\:\\:timestamptz";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, now);
            query.setParameter(2, entity.getEntityId());
            query.setParameter(3, now);
            query.setParameter(4, valid_origin);
            int updatedRows = query.executeUpdate();
            logger.info("updated " + updatedRows + " row(s)...");
        } else {
            entity.setEntityVersionId(UUID.randomUUID());
            entity.setEntityStatus(0);
            entity.setValidRange(TsTzRange.of(TsTzRange.ORIGIN, TsTzRange.INFINITY));
            entity.setSystemRange(TsTzRange.of(TsTzRange.ORIGIN, now));
            entityManager.persist(entity);
            entityManager.flush();
            entityManager.clear();
        }

        if (valid_origin.isBefore(valid_from)) {
            entity.setEntityVersionId(UUID.randomUUID());
            entity.setEntityStatus(0);
            entity.setValidRange(TsTzRange.of(valid_origin, valid_from));
            entity.setSystemRange(TsTzRange.of(now, TsTzRange.INFINITY));
            entityManager.persist(entity);
            entityManager.flush();
            entityManager.clear();
        }

        if (valid_to.isBefore(TsTzRange.INFINITY)) {
            entity.setEntityVersionId(UUID.randomUUID());
            entity.setEntityStatus(0);
            entity.setValidRange(TsTzRange.of(valid_to, TsTzRange.INFINITY));
            entity.setSystemRange(TsTzRange.of(now, TsTzRange.INFINITY));
            entityManager.persist(entity);
            entityManager.flush();
            entityManager.clear();
        }

        entity.setEntityVersionId(entityVersionId);
        entity.setEntityStatus(status);
        entity.setValidRange(TsTzRange.of(valid_from, valid_to));
        entity.setSystemRange(TsTzRange.of(now, TsTzRange.INFINITY));
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();

        deleteN(entity);
        insertN(entity);
        return entityVersionId;
    }

    @Override
    @Transactional
    public T find(UUID entityId, OffsetDateTime valid, OffsetDateTime system) {
        String sql = "select * from " + table() + " where entity_id = ? " +
                "and valid_range @> ? \\:\\:timestamptz " +
                "and system_range @> ? \\:\\:timestamptz ";
        Query query = entityManager.createNativeQuery(sql, domainClass);
        query.setParameter(1, entityId);
        query.setParameter(2, valid);
        query.setParameter(3, system);
        try {
            T t = (T) query.getSingleResult();
            return t;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @Transactional
    public List<T> find(List<UUID> entityIds, OffsetDateTime valid, OffsetDateTime system) {
        String sql = "select * from " + table() + " where ARRAY[" + entityIds.stream().map(uuid -> "'" + uuid.toString() + "'").collect(Collectors.joining(",")) + "]\\:\\:uuid[] @> ARRAY[entity_id]\\:\\:uuid[] " +
                "and valid_range @> ? \\:\\:timestamptz " +
                "and system_range @> ? \\:\\:timestamptz ";
        Query query = entityManager.createNativeQuery(sql, domainClass);
        query.setParameter(1, valid);
        query.setParameter(2, system);
        try {
            List<T> t = (List<T>) query.getResultList();
            return t;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<T> versions(UUID entityId) {
        String sql = "select * from " + table() + " where entity_id = ?";
        Query query = entityManager.createNativeQuery(sql, domainClass);
        query.setParameter(1, entityId);
        return query.getResultList();
    }

    public Map treeMap(UUID entityId) {
        String sql = "select * from " + table() + " where entity_id = ?";
        Query query = entityManager.createNativeQuery(sql, domainClass);
        query.setParameter(1, entityId);
        List<BaseBitemporalEntity> result = query.getResultList();
        Map map = new HashMap();
        map.put("name", entityId.toString());
        List<Map> children = new ArrayList();
        for (BaseBitemporalEntity t : result) {
            if (null != map.get("system_start")) {
                if (((Long) map.get("system_start")).compareTo(TsTzRange.ORIGIN.toEpochSecond()) == 0
                        && TsTzRange.ORIGIN.compareTo(t.getSystemRange().getStart()) != 0) {
                    map.put("system_start", t.getSystemRange().getStart().toEpochSecond());
                    map.put("system_start_string", t.getSystemRange().getStart().toString());
                } else if (((Long) map.get("system_start")).compareTo(t.getSystemRange().getStart().toEpochSecond()) > 0
                        && TsTzRange.ORIGIN.compareTo(t.getSystemRange().getStart()) != 0) {
                    map.put("system_start", t.getSystemRange().getStart().toEpochSecond());
                    map.put("system_start_string", t.getSystemRange().getStart().toString());
                }
            } else {
                map.put("system_start", t.getSystemRange().getStart().toEpochSecond());
                map.put("system_start_string", t.getSystemRange().getStart().toString());
            }
            if (null != map.get("system_end")) {
                if (((Long) map.get("system_end")).compareTo(TsTzRange.INFINITY.toEpochSecond()) == 0
                        && TsTzRange.INFINITY.compareTo(t.getSystemRange().getEnd()) != 0) {
                    map.put("system_end", t.getSystemRange().getEnd().toEpochSecond());
                    map.put("system_end_string", t.getSystemRange().getEnd().toString());
                } else if (((Long) map.get("system_end")).compareTo(t.getSystemRange().getEnd().toEpochSecond()) < 0
                        && TsTzRange.INFINITY.compareTo(t.getSystemRange().getEnd()) != 0) {
                    map.put("system_end", t.getSystemRange().getEnd().toEpochSecond());
                    map.put("system_end_string", t.getSystemRange().getEnd().toString());
                }
            } else {
                map.put("system_end", t.getSystemRange().getEnd().toEpochSecond());
                map.put("system_end_string", t.getSystemRange().getEnd().toString());
            }
            if (null != map.get("valid_start")) {
                if (((Long) map.get("valid_start")).compareTo(TsTzRange.ORIGIN.toEpochSecond()) == 0
                        && TsTzRange.ORIGIN.compareTo(t.getValidRange().getStart()) != 0) {
                    map.put("valid_start", t.getValidRange().getStart().toEpochSecond());
                    map.put("valid_start_string", t.getValidRange().getStart().toString());
                } else if (((Long) map.get("valid_start")).compareTo(t.getValidRange().getStart().toEpochSecond()) > 0
                        && TsTzRange.ORIGIN.compareTo(t.getValidRange().getStart()) != 0) {
                    map.put("valid_start", t.getValidRange().getStart().toEpochSecond());
                    map.put("valid_start_string", t.getValidRange().getStart().toString());
                }
            } else {
                map.put("valid_start", t.getValidRange().getStart().toEpochSecond());
                map.put("valid_start_string", t.getValidRange().getStart().toString());
            }
            if (null != map.get("valid_end")) {
                if (((Long) map.get("valid_end")).compareTo(TsTzRange.INFINITY.toEpochSecond()) == 0
                        && TsTzRange.INFINITY.compareTo(t.getValidRange().getEnd()) != 0) {
                    map.put("valid_end", t.getValidRange().getEnd().toEpochSecond());
                    map.put("valid_end_string", t.getValidRange().getEnd().toString());
                } else if (((Long) map.get("valid_end")).compareTo(t.getValidRange().getEnd().toEpochSecond()) < 0
                        && TsTzRange.INFINITY.compareTo(t.getValidRange().getEnd()) != 0) {
                    map.put("valid_end", t.getValidRange().getEnd().toEpochSecond());
                    map.put("valid_end_string", t.getValidRange().getEnd().toString());
                }
            } else {
                map.put("valid_end", t.getValidRange().getEnd().toEpochSecond());
                map.put("valid_end_string", t.getValidRange().getEnd().toString());
            }
        }

        if (null != map.get("system_start") && null != map.get("system_end") && null != map.get("valid_start") && null != map.get("valid_end")) {
            if (map.get("system_end").equals(map.get("system_start"))) {
                map.put("system_end", (Long) map.get("system_start") + 1);
            }
            map.put("system_range", ((Long) map.get("system_end") - (Long) map.get("system_start")) * 1.2);
            if (map.get("valid_end").equals(map.get("valid_end"))) {
                map.put("valid_end", (Long) map.get("valid_end") + 1);
            }
            map.put("valid_range", ((Long) map.get("valid_end") - (Long) map.get("valid_start")) * 1.2);
        }
        for (BaseBitemporalEntity t : result) {
            Map m = new HashMap();
            m.put("name", t.getEntityVersionId());
            m.put("r", Double.valueOf(Math.random() * 256).intValue());
            m.put("g", Double.valueOf(Math.random() * 256).intValue());
            m.put("b", Double.valueOf(Math.random() * 256).intValue());
            m.put("entity", t);
            m.put("bitemp_coord", "vt:" + t.getValidRange().toString() + ",\nst:" + t.getSystemRange().toString());
            if (TsTzRange.ORIGIN.compareTo(t.getSystemRange().getStart()) == 0 && TsTzRange.INFINITY.compareTo(t.getSystemRange().getEnd()) == 0) {
                m.put("x", 0);
                m.put("width", 1);
            } else if (TsTzRange.ORIGIN.compareTo(t.getSystemRange().getStart()) == 0) {
                m.put("x", 0);
                m.put("width", (Math.abs((Long) map.get("system_start") - t.getSystemRange().getEnd().toEpochSecond()) + ((Long) map.get("system_end") - (Long) map.get("system_start")) * 0.1) / (Double) map.get("system_range"));
            } else if (TsTzRange.INFINITY.compareTo(t.getSystemRange().getEnd()) == 0) {
                m.put("x", (t.getSystemRange().getStart().toEpochSecond() - (Long) map.get("system_start") + ((Long) map.get("system_end") - (Long) map.get("system_start")) * 0.1) / (Double) map.get("system_range"));
                m.put("width", (Math.abs((Long) map.get("system_end") - t.getSystemRange().getStart().toEpochSecond()) + ((Long) map.get("system_end") - (Long) map.get("system_start")) * 0.1) / (Double) map.get("system_range"));
            } else {
                m.put("x", (t.getSystemRange().getStart().toEpochSecond() - (Long) map.get("system_start") + ((Long) map.get("system_end") - (Long) map.get("system_start")) * 0.1) / (Double) map.get("system_range"));
                m.put("width", Math.abs((t.getSystemRange().getStart().toEpochSecond() - t.getSystemRange().getEnd().toEpochSecond()) / (Double) map.get("system_range")));
            }
            if (TsTzRange.ORIGIN.compareTo(t.getValidRange().getStart()) == 0 && TsTzRange.INFINITY.compareTo(t.getValidRange().getEnd()) == 0) {
                m.put("y", 0);
                m.put("height", 1);
            } else if (TsTzRange.ORIGIN.compareTo(t.getValidRange().getStart()) == 0) {
                m.put("y", ((Long) map.get("valid_end") - t.getValidRange().getEnd().toEpochSecond() + ((Long) map.get("valid_end") - (Long) map.get("valid_start")) * 0.1) / (Double) map.get("valid_range"));
                m.put("height", (Math.abs((Long) map.get("valid_end") - t.getValidRange().getStart().toEpochSecond()) + ((Long) map.get("valid_end") - (Long) map.get("valid_start")) * 0.1) / (Double) map.get("valid_range"));
            } else if (TsTzRange.INFINITY.compareTo(t.getValidRange().getEnd()) == 0) {
                m.put("y", 0);
                m.put("height", (Math.abs((Long) map.get("valid_end") - t.getValidRange().getStart().toEpochSecond()) + ((Long) map.get("valid_end") - (Long) map.get("valid_start")) * 0.1) / (Double) map.get("valid_range"));
            } else {
                m.put("y", ((Long) map.get("valid_end") - t.getValidRange().getEnd().toEpochSecond() + ((Long) map.get("valid_end") - (Long) map.get("valid_start")) * 0.1) / (Double) map.get("valid_range"));
                m.put("height", Math.abs((t.getValidRange().getStart().toEpochSecond() - t.getValidRange().getEnd().toEpochSecond()) / (Double) map.get("valid_range")));
            }
            children.add(m);
        }
        map.put("children", children);

        return map;
    }
}
