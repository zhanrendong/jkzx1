package tech.tongyu.bct.trade.manager;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.reference.elemental.Account;
import tech.tongyu.bct.cm.reference.elemental.Party;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.trade.dao.dbo.Position;
import tech.tongyu.bct.trade.dao.repo.PositionRepo;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PositionManagerImpl implements PositionManager {

    PositionRepo positionRepo;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public PositionManagerImpl(PositionRepo positionRepo) {
        this.positionRepo = positionRepo;
    }

    @Override
    public void deleteAll() {
        positionRepo.deleteAll();
    }

    @Override
    @Transactional
    public void upsert(BctTradePosition bctPosition, OffsetDateTime validTime) {
        if (Objects.isNull(bctPosition)) {
            throw new IllegalArgumentException("持仓数据不存在");
        }
        Position tradePosition = transToTradePosition(bctPosition);
        positionRepo.save(tradePosition);
    }

    @Override
    public void deleteByPositionId(String positionId, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        if (StringUtils.isBlank(positionId)) {
            throw new IllegalArgumentException("请输入待删除持仓编号:positionId");
        }
        positionRepo.deleteByPositionId(positionId);
    }

    @Override
    public Optional<BctTradePosition> getByPositionId(String positionId, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        if (StringUtils.isBlank(positionId)) {
            throw new IllegalArgumentException("请输入待查询持仓编号:positionId");
        }
        return positionRepo.findByPositionId(positionId)
                .map(position -> {
                    entityManager.detach(position);
                    BctTradePosition bctPosition = transToBctPosition(position);
                    return Optional.of(bctPosition);
                }).orElse(Optional.empty());
    }

    @Override
    public Optional<List<BctTradePosition>> getByPositionIds(List<String> positionIds, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        if (CollectionUtils.isEmpty(positionIds)) {
            throw new IllegalArgumentException("请输入待查询持仓编号:positionId");
        }
        return positionRepo.findByPositionIdIn(positionIds).map(positions -> positions.stream().map(position -> {
            entityManager.detach(position);
            return transToBctPosition(position);
        }).collect(Collectors.toList()));
    }

    @Override
    public boolean existByPositionId(String positionId) {
        return positionRepo.existsByPositionId(positionId);
    }

    private BctTradePosition transToBctPosition(Position tradePosition) {
        BctTradePosition bctTradePosition = new BctTradePosition();
        BeanUtils.copyProperties(tradePosition, bctTradePosition);
        if (Objects.nonNull(tradePosition.getCounterparty())) {
            bctTradePosition.counterparty =
                    (Party) JsonUtils.jsonNodeWithTypeTagToObject(tradePosition.getCounterparty());
        }
        if (Objects.nonNull(tradePosition.getPositionAccount())) {
            bctTradePosition.positionAccount =
                    (Account) JsonUtils.jsonNodeWithTypeTagToObject(tradePosition.getPositionAccount());
        }
        if (Objects.nonNull(tradePosition.getCounterpartyAccount())) {
            bctTradePosition.counterpartyAccount =
                    (Account) JsonUtils.jsonNodeWithTypeTagToObject(tradePosition.getCounterpartyAccount());
        }
        if (Objects.nonNull(tradePosition.getAsset())) {
            bctTradePosition.asset =
                    (Asset<InstrumentOfValue>) JsonUtils.jsonNodeWithTypeTagToObject(tradePosition.getAsset());
        }
        return bctTradePosition;
    }

    private Position transToTradePosition(BctTradePosition bctPosition) {
        JsonNode posJson = JsonUtils.mapper.valueToTree(bctPosition);
        Position position = new Position();
        BeanUtils.copyProperties(bctPosition, position);
        position.setAsset(posJson.get(BctTradePosition.assetFieldName));
        position.setCounterparty(posJson.get(BctTradePosition.counterpartyFieldName));
        position.setPositionAccount(posJson.get(BctTradePosition.positionAccountFieldName));
        position.setCounterpartyAccount(posJson.get(BctTradePosition.counterpartyAccountFieldName));

        positionRepo.findByPositionId(bctPosition.getPositionId()).ifPresent(p -> position.setUuid(p.getUuid()));
        return position;
    }

}
