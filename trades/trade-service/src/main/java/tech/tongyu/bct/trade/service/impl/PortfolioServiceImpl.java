package tech.tongyu.bct.trade.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.exchange.service.ExchangePortfolioService;
import tech.tongyu.bct.trade.dao.dbo.Portfolio;
import tech.tongyu.bct.trade.dao.dbo.TradePortfolio;
import tech.tongyu.bct.trade.dao.repo.PortfolioRepo;
import tech.tongyu.bct.trade.dao.repo.TradePortfolioRepo;
import tech.tongyu.bct.trade.dto.trade.PortfolioDTO;
import tech.tongyu.bct.trade.service.PortfolioService;
import tech.tongyu.bct.trade.service.PortfolioSearchService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    private PortfolioRepo portfolioRepo;

    private TradePortfolioRepo tradePortfolioRepo;

    private ExchangePortfolioService exchangePortfolioService;

    private PortfolioSearchService portfolioSearchService;

    @Autowired
    public PortfolioServiceImpl(PortfolioRepo portfolioRepo, TradePortfolioRepo tradePortfolioRepo,
                                ExchangePortfolioService exchangePortfolioService,
                                PortfolioSearchService portfolioSearchService) {
        this.portfolioRepo = portfolioRepo;
        this.tradePortfolioRepo = tradePortfolioRepo;
        this.exchangePortfolioService = exchangePortfolioService;
        this.portfolioSearchService = portfolioSearchService;
    }

    @Override
    @Transactional
    public PortfolioDTO create(String portfolioName) {
        if (StringUtils.isBlank(portfolioName)) {
            throw new CustomException("请输入投资组合名称");
        }
        if (portfolioRepo.existsByPortfolioName(portfolioName)) {
            throw new CustomException(String.format("投资组合名称已经存在,portfolioName:%s", portfolioName));
        }
        Portfolio portfolio = new Portfolio(portfolioName);
        return transToDto(portfolioRepo.save(portfolio));
    }

    @Override
    public PortfolioDTO findById(UUID uuid) {
        Portfolio portfolio = portfolioRepo.findById(uuid)
                .orElseThrow(() -> new CustomException(String.format("投资组合数据不存在,uuid:%s", uuid.toString())));
        return transToDto(portfolio);
    }

    @Override
    @Transactional
    public PortfolioDTO update(PortfolioDTO portfolioDto) {
        Portfolio portfolio = transToDbo(portfolioDto);
        // 根据uuid查找原有数据的投资组合名称portfolioName
        Portfolio oldPortfolio = portfolioRepo.findById(portfolio.getUuid())
                .orElseThrow(() -> new CustomException(String.format("投资组合数据不存在,uuid:%s", portfolioDto.getUuid())));
        String oldPortfolioName = oldPortfolio.getPortfolioName();
        if (oldPortfolioName.equals(portfolioDto.getPortfolioName())) {
            throw new CustomException(String.format("投资组合名称[%s]没有变化,无效操作", oldPortfolioName));
        }
        if (portfolioRepo.existsByPortfolioName(portfolioDto.getPortfolioName())) {
            throw new CustomException(String.format("投资组合名称[%s]已经存在,不能修改", portfolioDto.getPortfolioName()));
        }
        // 查找原有portfolioName绑定的交易编号,删除原有绑定,替换更改后投资组合和交易绑定
        List<String> tradeIds = portfolioSearchService.listTradeIdsByPortfolioName(oldPortfolioName);
        List<String> exchangeTradeIds = exchangePortfolioService.listExchangeTradeIdsByPortfolioName(oldPortfolioName);
        tradePortfolioRepo.deleteByPortfolioName(oldPortfolioName);
        exchangePortfolioService.deleteByPortfolioName(oldPortfolioName);

        Portfolio saved = portfolioRepo.save(portfolio);
        tradeIds.forEach(tradeId -> {
            TradePortfolio tradePortfolio = new TradePortfolio(tradeId, portfolioDto.getPortfolioName());
            tradePortfolioRepo.save(tradePortfolio);
        });
        exchangeTradeIds.forEach(tradeId -> exchangePortfolioService
                .createExchangeTradePortfolioWithoutNewTransaction(tradeId, portfolioDto.getPortfolioName()));

        return transToDto(saved);
    }


    @Override
    @Transactional
    public Boolean deleteByPortfolioName(String portfolioName) {
        if (StringUtils.isBlank(portfolioName)) {
            throw new CustomException("请输入投资组合名称");
        }
        if (!portfolioRepo.existsByPortfolioName(portfolioName)) {
            throw new CustomException(String.format("投资组合名称不存在,portfolioName:%s", portfolioName));
        }
        portfolioRepo.deleteByPortfolioName(portfolioName);
        tradePortfolioRepo.deleteByPortfolioName(portfolioName);
        return true;
    }


    @Override
    @Transactional
    public Boolean createTradePortfolio(String tradeId, String portfolioName) {
        if (StringUtils.isBlank(tradeId)) {
            throw new CustomException("请输入交易编号tradeId");
        }
        if (StringUtils.isBlank(portfolioName)) {
            throw new CustomException("请输入投资组合名称portfolioName");
        }
        if (!portfolioRepo.existsByPortfolioName(portfolioName)) {
            PortfolioServiceImpl portfolioService = (PortfolioServiceImpl) AopContext.currentProxy();
            portfolioService.create(portfolioName);
        }
        if (!tradePortfolioRepo.existsByTradeIdAndPortfolioName(tradeId, portfolioName)) {
            TradePortfolio tradePortfolio = new TradePortfolio(tradeId, portfolioName);
            tradePortfolioRepo.save(tradePortfolio);
        }

        return true;
    }

    @Override
    @Transactional
    public Boolean deleteTradePortfolio(String tradeId, String portfolioName) {
        if (StringUtils.isBlank(tradeId)) {
            throw new CustomException("请输入交易编号tradeId");
        }
        if (StringUtils.isBlank(portfolioName)) {
            throw new CustomException("请输入投资组合名称portfolioName");
        }
        if (tradePortfolioRepo.existsByTradeIdAndPortfolioName(tradeId, portfolioName)) {
            tradePortfolioRepo.deleteByTradeIdAndPortfolioName(tradeId, portfolioName);
        }
        return true;
    }

    @Override
    @Transactional
    public Boolean createTradePortfolioBatch(String tradeId, List<String> portfolioNames) {
        PortfolioServiceImpl portfolioService = (PortfolioServiceImpl) AopContext.currentProxy();
        portfolioNames.forEach(portfolioName -> portfolioService.createTradePortfolio(tradeId, portfolioName));
        return true;
    }

    @Override
    @Transactional
    public Boolean refreshTradePortfolios(String tradeId, List<String> portfolioNames) {
        if (StringUtils.isBlank(tradeId)) {
            throw new CustomException("请输入交易编号tradeId");
        }
        if (CollectionUtils.isEmpty(portfolioNames)) {
            throw new CustomException("请输入投资组合名称portfolioName");
        }
        tradePortfolioRepo.deleteByTradeId(tradeId);
        PortfolioServiceImpl portfolioService = (PortfolioServiceImpl) AopContext.currentProxy();
        portfolioNames.forEach(portfolioName -> portfolioService.createTradePortfolio(tradeId, portfolioName));
        return true;
    }

    private Boolean checkTradeHasPortfolios(String tradeId, List<String> portfolioNames) {
        return portfolioNames
                .stream()
                .filter(portfolioName -> !tradePortfolioRepo.existsByTradeIdAndPortfolioName(tradeId, portfolioName))
                .collect(Collectors.toList())
                .isEmpty();
    }

    private PortfolioDTO transToDto(Portfolio portfolio) {
        UUID uuid = portfolio.getUuid();
        PortfolioDTO portfolioDto = new PortfolioDTO();
        BeanUtils.copyProperties(portfolio, portfolioDto);
        portfolioDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());

        return portfolioDto;
    }

    private Portfolio transToDbo(PortfolioDTO portfolioDto) {
        String uuid = portfolioDto.getUuid();
        Portfolio portfolio = new Portfolio();
        BeanUtils.copyProperties(portfolioDto, portfolio);
        portfolio.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));

        return portfolio;
    }
}
