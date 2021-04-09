package tech.tongyu.bct.trade.manager;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.trade.NonEconomicPartyRole;
import tech.tongyu.bct.cm.trade.TradeStatusEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.trade.dao.dbo.Trade;
import tech.tongyu.bct.trade.dao.repo.TradeRepo;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class TradeManagerImpl implements TradeManager {

    TradeRepo tradeRepo;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public TradeManagerImpl(TradeRepo tradeRepo) {
        this.tradeRepo = tradeRepo;
    }

    @Override
    public void deleteAll() {
        tradeRepo.deleteAll();
    }

    @Override
    public List<BctTrade> findAll() {
        List<Trade> trades = tradeRepo.findAll();
        entityManager.clear();
        return trades
                .parallelStream()
                .map(this::transToBctTrade)
                .collect(Collectors.toList());
    }

    @Override
    public List<BctTrade> findByTradeStatus(TradeStatusEnum tradeStatus) {
        List<Trade> trades = tradeRepo.findByTradeStatus(tradeStatus);
        entityManager.clear();
        return trades
                .parallelStream()
                .map(this::transToBctTrade)
                .collect(Collectors.toList());
    }

    @Override
    public List<BctTrade> findByTradeIds(List<String> tradeIds) {
        List<Trade> trades = tradeRepo.findAllByTradeIdInOrderByCreatedAtDesc(tradeIds);
        entityManager.clear();
        return trades
                .parallelStream()
                .map(this::transToBctTrade)
                .collect(Collectors.toList());
    }

    @Override
    public List<BctTrade> findByExample(BctTrade bctTrade) {
        Trade trade = new Trade();
        BeanUtils.copyProperties(bctTrade, trade);
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
        if (StringUtils.isBlank(trade.getTradeId())) {
            trade.setTradeId(null);
        } else {
            matcher.withMatcher("tradeId", ExampleMatcher.GenericPropertyMatchers.exact());
        }
        if (StringUtils.isBlank(trade.getBookName())) {
            trade.setBookName(null);
        } else {
            matcher.withMatcher("bookName", ExampleMatcher.GenericPropertyMatchers.exact());
        }
        if (Objects.isNull(trade.getTradeDate())) {
            trade.setTradeDate(null);
        } else {
            matcher.withMatcher("tradeDate", ExampleMatcher.GenericPropertyMatchers.exact());
        }
        Example<Trade> tradeExample = Example.of(trade, matcher);
        List<Trade> trades = tradeRepo.findAll(tradeExample, Sort.by("tradeId"));
        entityManager.clear();
        return trades
                .parallelStream()
                .map(this::transToBctTrade)
                .collect(Collectors.toList());
    }

    @Override
    public List<BctTrade> findBySimilarTradeId(String tradeId) {
        List<Trade> trades = tradeRepo.findAllByTradeIdContaining(tradeId);
        entityManager.clear();
        return trades
                .parallelStream()
                .map(this::transToBctTrade)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void upsert(BctTrade bctTrade, OffsetDateTime validTime) {
        if (Objects.isNull(bctTrade)) {
            throw new IllegalArgumentException("交易数据不存在");
        }
        tradeRepo.save(transToTrade(bctTrade));
    }


    @Override
    public void deleteByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        if (StringUtils.isBlank(tradeId)) {
            throw new IllegalArgumentException("请输入待删除交易编号:tradeId");
        }
        tradeRepo.deleteByTradeId(tradeId);
    }

    @Override
    public Optional<BctTrade> getByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        if (StringUtils.isBlank(tradeId)) {
            throw new IllegalArgumentException("请输入待查询交易编号:tradeId");
        }
        return tradeRepo.findByTradeId(tradeId)
                .map(trade -> {
                    BctTrade bctTrade = transToBctTrade(trade);
                    return Optional.of(bctTrade);
                }).orElse(Optional.empty());
    }

    @Override
    public List<BctTrade> findByBookName(String bookName, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        if (StringUtils.isBlank(bookName)) {
            throw new IllegalArgumentException("请输入待查询交易簿名称:bookName");
        }
        List<Trade> trades = tradeRepo.findAllByBookName(bookName);
        entityManager.clear();
        return trades.parallelStream()
                .map(this::transToBctTrade)
                .collect(Collectors.toList());
    }

    @Override
    public List<BctTrade> findByTrader(String trader, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        List<Trade> trades = tradeRepo.findAllByTrader(trader);
        entityManager.clear();
        return trades
                 .stream()
                 .map(this::transToBctTrade)
                 .collect(Collectors.toList());
    }

    @Override
    public boolean existsByTradeId(String tradeId) {
        return tradeRepo.existsByTradeId(tradeId);
    }


    private BctTrade transToBctTrade(Trade trade) {
        entityManager.detach(trade);
        BctTrade bctTrade = new BctTrade();
        BeanUtils.copyProperties(trade, bctTrade);

        bctTrade.nonEconomicPartyRoles =
                (List<NonEconomicPartyRole>) JsonUtils.jsonNodeWithTypeTagToObject(trade.getNonEconomicPartyRoles());
        List<String> positionIds;
        try {
            positionIds = JsonUtils.mapper.readValue(trade.getPositions(), List.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("类型转换错误");
        }
        bctTrade.setPositionIds(positionIds);
        return bctTrade;
    }

    private Trade transToTrade(BctTrade bctTrade) {
        Trade trade = new Trade();
        BeanUtils.copyProperties(bctTrade, trade);

        List<String> positionIds = bctTrade.getPositionIds();
        Set<String> collect = positionIds.stream().collect(Collectors.toSet());
        trade.setPositionIds(collect);

        JsonNode tradeJson = JsonUtils.mapper.valueToTree(bctTrade);
        String positionsStr = JsonUtils.objectToJsonString(bctTrade.getPositionIds());

        trade.setPositions(positionsStr);
        trade.setNonEconomicPartyRoles(tradeJson.get(BctTrade.nonEconomicPartyRolesFieldName));
        tradeRepo.findByTradeId(bctTrade.getTradeId()).ifPresent(t -> trade.setUuid(t.getUuid()));
        return trade;
    }
}
