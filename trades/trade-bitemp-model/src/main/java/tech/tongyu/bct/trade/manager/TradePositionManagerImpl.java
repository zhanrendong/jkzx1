package tech.tongyu.bct.trade.manager;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.reference.elemental.Account;
import tech.tongyu.bct.cm.reference.elemental.Party;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.trade.dao.schema.model.TradePositionSchema;
import tech.tongyu.bct.trade.dao.schema.repository.TradePositionSchemaKeyRepository;
import tech.tongyu.bct.trade.dao.schema.repository.TradePositionSchemaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TradePositionManagerImpl implements TradePositionManager {

    private static Logger logger = LoggerFactory.getLogger(TradePositionManagerImpl.class);

    @Autowired
    TradePositionSchemaKeyRepository positionKeyRepo;

    @Autowired
    TradePositionSchemaRepository positionRepo;

    public List<BctTradePosition> findPositionByPositionId(List<String> positionIds, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        return positionIds
                .stream()
                .map(id -> {
                    TradePositionSchema p = new TradePositionSchema();
                    p.setPositionId(id);
                    return p;
                })
                .flatMap(pos -> positionKeyRepo.key(pos).stream())
                .map(key -> positionRepo.find(key.getEntityId(), validTime, transactionTime))
                .map(this::fromSchema)
                .collect(Collectors.toList());
    }

    public List<BctTradePosition> findPositionByEntityId(List<UUID> entityIds, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        return entityIds.stream()
                .map(eid -> positionRepo.find(eid, validTime, transactionTime))
                .map(p -> fromSchema(p))
                .collect(Collectors.toList());
    }

    @Override
    public void upsert(List<BctTradePosition> positions, OffsetDateTime validTime) {
        positions.stream().forEach(position -> {
            TradePositionSchema positionSchema = toSchema(position);
            try {
                positionRepo.append(positionSchema, UUID.randomUUID(), validTime);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    public BctTradePosition fromSchema(TradePositionSchema schema) {
        JsonNode assetJson = schema.getAsset();
        JsonNode counterpartyJson = schema.getCounterparty();
        JsonNode positionAccountJson = schema.getPositionAccount();
        JsonNode counterpartyAccountJson = schema.getCounterpartyAccount();
        Asset<InstrumentOfValue> asset =
                (Asset<InstrumentOfValue>) JsonUtils.jsonNodeWithTypeTagToObject(assetJson);
        Party counterparty = (Party) JsonUtils.jsonNodeWithTypeTagToObject(counterpartyJson);
        Account posAcct = (Account) JsonUtils.jsonNodeWithTypeTagToObject(positionAccountJson);
        Account counterpartyAcct = (Account) JsonUtils.jsonNodeWithTypeTagToObject(counterpartyAccountJson);
        return new BctTradePosition(
                schema.getBookName(),
                schema.getPositionId(),
                counterparty,
                schema.getQuantity(),
                asset,
                posAcct,
                counterpartyAcct);
    }

    public TradePositionSchema toSchema(BctTradePosition position) {
        JsonNode posJson = JsonUtils.mapper.valueToTree(position);
        TradePositionSchema schema = new TradePositionSchema();
        schema.setPositionId(position.positionId);
        schema.setBookName(position.bookName);
        schema.setQuantity(position.quantity);
        schema.setAsset(posJson.get(BctTradePosition.assetFieldName));
        schema.setCounterparty(posJson.get(BctTradePosition.counterpartyFieldName));
        schema.setCounterpartyAccount(posJson.get(BctTradePosition.counterpartyAccountFieldName));
        schema.setPositionAccount(posJson.get(BctTradePosition.positionAccountFieldName));
        return schema;
    }
}
