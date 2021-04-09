package tech.tongyu.bct.exchange.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.exchange.dao.dbo.local.ExchangeTradePortfolio;
import tech.tongyu.bct.exchange.dao.repo.intel.local.ExchangeTradePortfolioRepo;
import tech.tongyu.bct.exchange.service.ExchangePortfolioService;
import tech.tongyu.bct.trade.service.PortfolioSearchService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExchangePortfolioServiceImpl implements ExchangePortfolioService {

    private ExchangeTradePortfolioRepo tradePortfolioRepo;

    private PortfolioSearchService portfolioSearchService;

    @Autowired
    public ExchangePortfolioServiceImpl(ExchangeTradePortfolioRepo tradePortfolioRepo, PortfolioSearchService portfolioSearchService) {
        this.tradePortfolioRepo = tradePortfolioRepo;
        this.portfolioSearchService = portfolioSearchService;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean createExchangeTradePortfolioWithNewTransaction(String tradeId, String portfolioName) {
        return createExchangeTradePortfolio(tradeId, portfolioName);
    }

    @Override
    @Transactional
    public boolean createExchangeTradePortfolioWithoutNewTransaction(String tradeId, String portfolioName) {
        return createExchangeTradePortfolio(tradeId, portfolioName);
    }


    @Override
    @Transactional
    public boolean deleteExchangeTradePortfolio(String tradeId, String portfolioName) {
        if (StringUtils.isBlank(portfolioName)) {
            throw new CustomException("请输入投资组合名称portfolioName");
        }
        if (StringUtils.isBlank(tradeId)) {
            throw new CustomException("请输入交易编号tradeId");
        }
        if (tradePortfolioRepo.existsByTradeIdAndPortfolioName(tradeId, portfolioName)) {
            tradePortfolioRepo.deleteByTradeIdAndPortfolioName(tradeId, portfolioName);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean createExchangeTradePortfolioBatch(String tradeId, List<String> portfolioNames) {
        ExchangePortfolioServiceImpl portfolioService = (ExchangePortfolioServiceImpl) AopContext.currentProxy();
        portfolioNames.forEach(portfolioName -> portfolioService.createExchangeTradePortfolioWithoutNewTransaction(tradeId, portfolioName));
        return true;
    }

    @Override
    @Transactional
    public boolean refreshExchangeTradePortfolios(List<String> tradeIds, List<String> portfolioNames) {
        if (CollectionUtils.isEmpty(tradeIds)) {
            throw new CustomException("请输入交易编号tradeId");
        }
        ExchangePortfolioServiceImpl portfolioService = (ExchangePortfolioServiceImpl) AopContext.currentProxy();

        tradeIds.stream().distinct().forEach(tradeId -> {
            tradePortfolioRepo.deleteByTradeId(tradeId);
            portfolioNames.stream().distinct().forEach(
                    portfolioName -> portfolioService.createExchangeTradePortfolioWithoutNewTransaction(tradeId, portfolioName));
        });
        return true;
    }

    @Override
    public List<String> listExchangeTradeIdsByPortfolioName(String portfolioName) {
        if (StringUtils.isBlank(portfolioName)) {
            throw new CustomException("请输入投资组合名称portfolioName");
        }
        return tradePortfolioRepo.findByPortfolioName(portfolioName)
                .stream()
                .map(ExchangeTradePortfolio::getTradeId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteByPortfolioName(String portfolioName) {
        if (StringUtils.isBlank(portfolioName)) {
            throw new CustomException("请输入投资组合名称");
        }
        if (!portfolioSearchService.existsByPortfolioName(portfolioName)) {
            throw new CustomException(String.format("投资组合名称不存在,portfolioName:%s", portfolioName));
        }
        tradePortfolioRepo.deleteByPortfolioName(portfolioName);
        return true;
    }

    @Override
    public boolean existsByPortfolioName(String portfolioName) {
        return portfolioSearchService.existsByPortfolioName(portfolioName);
    }

    @Override
    public Map<String, List<String>> listAllExchangePortfolioTrades() {
        Map<String, List<String>> portfolioTrades = new HashMap<>();
        tradePortfolioRepo.findAll().forEach(tradePortfolio -> {
            String portfolioName = tradePortfolio.getPortfolioName();
            List<String> tradeIds = portfolioTrades.getOrDefault(portfolioName, new ArrayList<>());
            tradeIds.add(tradePortfolio.getTradeId());
            portfolioTrades.put(portfolioName, tradeIds);
        });
        return portfolioTrades;
    }

    private boolean createExchangeTradePortfolio(String tradeId, String portfolioName) {
        if (StringUtils.isBlank(tradeId)) {
            throw new CustomException("请输入交易编号tradeId");
        }
        if (StringUtils.isBlank(portfolioName)) {
            throw new CustomException("请输入投资组合名称portfolioName");
        }
        if (!portfolioSearchService.existsByPortfolioName(portfolioName)) {
            throw new CustomException(String.format("投资组合不存在:{%s}", portfolioName));
        }
        if (!tradePortfolioRepo.existsByTradeIdAndPortfolioName(tradeId, portfolioName)) {
            ExchangeTradePortfolio tradePortfolio = new ExchangeTradePortfolio(tradeId, portfolioName);
            tradePortfolioRepo.save(tradePortfolio);
        }
        return true;
    }
}
