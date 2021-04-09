package tech.tongyu.bct.trade.service;

import tech.tongyu.bct.trade.dto.trade.PortfolioDTO;

import java.util.List;
import java.util.UUID;

public interface PortfolioService {

    PortfolioDTO create(String portfolioName);

    PortfolioDTO update(PortfolioDTO portfolioDto);

    PortfolioDTO findById(UUID uuid);

    Boolean deleteByPortfolioName(String portfolioName);

    Boolean createTradePortfolio(String tradeId, String portfolioName);

    Boolean deleteTradePortfolio(String tradeId, String portfolioName);

    Boolean refreshTradePortfolios(String tradeId, List<String> portfolioNames);

    Boolean createTradePortfolioBatch(String tradeId, List<String> portfolioNames);

}
