package tech.tongyu.bct.trade.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.tongyu.bct.cm.trade.TradeStatusEnum;
import tech.tongyu.bct.trade.dao.dbo.Trade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TradeRepo extends JpaRepository<Trade, UUID> {

    void deleteByTradeId(String tradeId);

    Optional<Trade> findByTradeId(String tradeId);

    List<Trade> findAllByBookName(String bookName);

    List<Trade> findAllByTrader(String trader);

    List<Trade> findAllByTradeIdInOrderByCreatedAtDesc(List<String> tradeIds);

    List<Trade> findAllByTradeIdContaining(String similarTradeId);

    List<Trade> findByTradeStatus(TradeStatusEnum similarTradeId);

    boolean existsByTradeId(String tradeId);
}
