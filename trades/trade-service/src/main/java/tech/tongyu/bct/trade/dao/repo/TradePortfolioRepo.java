package tech.tongyu.bct.trade.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.trade.dao.dbo.TradePortfolio;

import java.util.List;
import java.util.UUID;

public interface TradePortfolioRepo extends JpaRepository<TradePortfolio, UUID> {

    void deleteByTradeId(String tradeId);

    void deleteByPortfolioName(String portfolioName);

    List<TradePortfolio> findByTradeId(String tradeId);

    List<TradePortfolio> findByPortfolioName(String portfolioName);

    List<TradePortfolio> findByPortfolioNameIn(List<String> portfolioNames);

    void deleteByTradeIdAndPortfolioName(String tradeId, String portfolioName);

    Boolean existsByTradeIdAndPortfolioName(String tradeId, String portfolioName);
}
