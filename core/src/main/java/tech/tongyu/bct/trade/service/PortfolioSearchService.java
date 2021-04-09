package tech.tongyu.bct.trade.service;

import tech.tongyu.bct.trade.dto.trade.PortfolioDTO;

import java.util.List;
import java.util.Map;

public interface PortfolioSearchService {

    List<PortfolioDTO> search(String portfolioName);

    List<String> listBySimilarPortfolioName(String similarPortfolioName);

    Map<String, List<String>> listAllPortfolioTrades(boolean groupByTradeId);

    List<String> listPortfolioNamesByTradeId(String tradeId);

    List<String> listTradeIdsByPortfolioName(String portfolioName);

    List<String> listTradeIdsByPortfolioNames(List<String> portfolioNames);

    boolean existsByPortfolioName(String portfolioNames);

}
