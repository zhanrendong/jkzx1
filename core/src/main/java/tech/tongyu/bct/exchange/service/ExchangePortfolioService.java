package tech.tongyu.bct.exchange.service;

import java.util.List;
import java.util.Map;

public interface ExchangePortfolioService {

    boolean createExchangeTradePortfolioWithNewTransaction(String tradeId, String portfolioName);

    boolean createExchangeTradePortfolioWithoutNewTransaction(String tradeId, String portfolioName);

    boolean deleteExchangeTradePortfolio(String tradeId, String portfolioName);

    boolean createExchangeTradePortfolioBatch(String tradeId, List<String> portfolioNames);

    boolean refreshExchangeTradePortfolios(List<String> tradeIds, List<String> portfolioNames);

    List<String> listExchangeTradeIdsByPortfolioName(String portfolioName);

    boolean deleteByPortfolioName(String portfolioName);

    boolean existsByPortfolioName(String portfolioName);

    Map<String, List<String>> listAllExchangePortfolioTrades();
}
