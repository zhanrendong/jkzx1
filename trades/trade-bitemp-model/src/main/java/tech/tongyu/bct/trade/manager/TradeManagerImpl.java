package tech.tongyu.bct.trade.manager;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.trade.NonEconomicPartyRole;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.trade.dao.dto.TradeEvent;
import tech.tongyu.bct.trade.dao.dto.repo.TradeEventRepo;
import tech.tongyu.bct.trade.dao.schema.model.TradePositionSchema;
import tech.tongyu.bct.trade.dao.schema.model.TradeSchema;
import tech.tongyu.bct.trade.dao.schema.model.TradeSchemaKey;
import tech.tongyu.bct.trade.dao.schema.repository.TradeSchemaKeyRepository;
import tech.tongyu.bct.trade.dao.schema.repository.TradeSchemaRepository;
import tech.tongyu.bct.trade.dto.event.TradeEventDTO;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TradeManagerImpl implements TradeManager {

    private static Logger logger = LoggerFactory.getLogger(TradeManagerImpl.class);

    TradeSchemaRepository tradeRepo;

    TradeSchemaKeyRepository tradeKeyRepo;

    TradePositionManagerImpl positionManager;

    TradeEventRepo tradeEventRepo;

    @Autowired
    public TradeManagerImpl(
            TradeSchemaRepository tradeRepo,
            TradeSchemaKeyRepository tradeKeyRepo,
            TradePositionManagerImpl positionManager,
            TradeEventRepo tradeEventRepo) {
        this.positionManager = positionManager;
        this.tradeRepo = tradeRepo;
        this.tradeKeyRepo = tradeKeyRepo;
        this.tradeEventRepo = tradeEventRepo;
    }

    @Transactional
    public void upsert(TradeEventDTO event, List<BctTrade> trades, OffsetDateTime validTime) {
        List<TradeSchema> schemas = trades.parallelStream()
                .map(trade -> toSchema(trade)).collect(Collectors.toList());
        schemas.forEach(tradeSchema -> {
            try {
                UUID tradeVersionUUID = tradeRepo.append(tradeSchema, UUID.randomUUID(), validTime);
                TradeEvent eventDbo = new TradeEvent();
                eventDbo.setEventType(event.tradeEventType);
                eventDbo.setUserLoginId(event.userLoginId);
                eventDbo.setTradeVersionUUID(tradeVersionUUID);
                tradeEventRepo.save(eventDbo);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    public List<BctTrade> findTradeByTradeId(List<String> tradeIds, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        List<TradeSchema> keys = tradeIds
                .stream()
                .map(id -> {
                    TradeSchema t = new TradeSchema();
                    t.setTradeId(id);
                    return t;
                }).collect(Collectors.toList());
        List<TradeSchemaKey> entityRefs = keys.parallelStream()
                .flatMap(trade -> tradeKeyRepo.key(trade).stream()).collect(Collectors.toList());
        List<TradeSchema> entities = entityRefs.parallelStream()
                .map(key -> tradeRepo.find(key.getEntityId(), validTime, transactionTime))
                .collect(Collectors.toList());
        List<BctTrade> trades = entities.parallelStream()
                .map(tradeSchema -> fromSchema(tradeSchema))
                .collect(Collectors.toList());
        return trades;
    }

    public BctTrade fromSchema(TradeSchema tradeSchema) {
        OffsetDateTime vt = tradeSchema.getValidRange().getStart();
        OffsetDateTime tt = tradeSchema.getSystemRange().getStart();
        List<UUID> posEntityIds = Stream.of(tradeSchema.getPositionIds()).collect(Collectors.toList());
        List<BctTradePosition> positions = positionManager.findPositionByEntityId(posEntityIds, vt, tt);
        try {
            JsonNode nonEconomicPartyRolesJson = tradeSchema.getNonEconomicPartyRoles();
            List<NonEconomicPartyRole> nonEconomicPartyRoles =
                    (List<NonEconomicPartyRole>)
                            JsonUtils.jsonNodeWithTypeTagToObject(nonEconomicPartyRolesJson);
            return new BctTrade(
                    tradeSchema.getBook(),
                    tradeSchema.getTradeId(),
                    tradeSchema.getTrader(),
                    positions,
                    nonEconomicPartyRoles,
                    tradeSchema.getTradeDate(),
                    tradeSchema.getComment());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public TradeSchema toSchema(BctTrade trade) {
        JsonNode tradeJson = JsonUtils.mapper.valueToTree(trade);
        List<TradePositionSchema> positions = trade.positions
                .stream()
                .map(positionManager::toSchema)
                .collect(Collectors.toList());
        TradeSchema tradeSchema = new TradeSchema();
        tradeSchema.setTradeId(trade.tradeId);
        tradeSchema.setPositions(positions);
        tradeSchema.setComment(trade.comment);
        tradeSchema.setTradeDate(trade.tradeDate);
        tradeSchema.setTrader(trade.trader);
        tradeSchema.setNonEconomicPartyRoles(tradeJson.get(BctTrade.nonEconomicPartyRolesFieldName));
        return tradeSchema;
    }
}
