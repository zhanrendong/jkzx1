package tech.tongyu.bct.exchange.dao.repo.intel.local;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.exchange.dao.dbo.local.ExchangeTradePortfolio;

import java.util.List;
import java.util.UUID;

public interface ExchangeTradePortfolioRepo extends JpaRepository<ExchangeTradePortfolio, UUID> {

    void deleteByTradeId(String tradeId);

    void deleteByPortfolioName(String portfolioName);

    List<ExchangeTradePortfolio> findByTradeId(String tradeId);

    List<ExchangeTradePortfolio> findByPortfolioName(String portfolioName);

    void deleteByTradeIdAndPortfolioName(String tradeId, String portfolioName);

    Boolean existsByTradeIdAndPortfolioName(String tradeId, String portfolioName);
}
