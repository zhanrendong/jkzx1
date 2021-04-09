package tech.tongyu.bct.trade.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.trade.dao.dbo.Portfolio;
import tech.tongyu.bct.trade.dao.dbo.TradePortfolio;
import tech.tongyu.bct.trade.dao.repo.PortfolioRepo;
import tech.tongyu.bct.trade.dao.repo.TradePortfolioRepo;
import tech.tongyu.bct.trade.dto.trade.PortfolioDTO;
import tech.tongyu.bct.trade.service.PortfolioSearchService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PortfolioSearchServiceImpl implements PortfolioSearchService {

    private PortfolioRepo portfolioRepo;

    private TradePortfolioRepo tradePortfolioRepo;

    @Autowired
    public PortfolioSearchServiceImpl(PortfolioRepo portfolioRepo, TradePortfolioRepo tradePortfolioRepo) {
        this.portfolioRepo = portfolioRepo;
        this.tradePortfolioRepo = tradePortfolioRepo;
    }

    @Override
    public List<PortfolioDTO> search(String portfolioName) {
        return portfolioRepo.findByPortfolioNameContaining(portfolioName)
                .stream()
                .map(this::transToDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<String> listBySimilarPortfolioName(String similarPortfolioName) {
        return portfolioRepo.findByPortfolioNameContaining(similarPortfolioName)
                .stream()
                .map(Portfolio::getPortfolioName)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<String>> listAllPortfolioTrades(boolean groupByTradeId) {
        return groupByTradeId ?
                tradePortfolioRepo.findAll().stream()
                        .collect(Collectors.groupingBy(TradePortfolio::getTradeId, HashMap::new,
                                Collectors.mapping(TradePortfolio::getPortfolioName, Collectors.toList()))) :
                tradePortfolioRepo.findAll().stream()
                        .collect(Collectors.groupingBy(TradePortfolio::getPortfolioName, HashMap::new,
                                Collectors.mapping(TradePortfolio::getTradeId, Collectors.toList())));
    }

    @Override
    public List<String> listPortfolioNamesByTradeId(String tradeId) {
        if (StringUtils.isBlank(tradeId)) {
            throw new CustomException("请输入交易编号tradeId");
        }
        return tradePortfolioRepo.findByTradeId(tradeId)
                .stream()
                .map(TradePortfolio::getPortfolioName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listTradeIdsByPortfolioName(String portfolioName) {
        if (StringUtils.isBlank(portfolioName)) {
            throw new CustomException("请输入投资组合名称portfolioName");
        }
        return tradePortfolioRepo.findByPortfolioName(portfolioName)
                .stream()
                .map(TradePortfolio::getTradeId)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listTradeIdsByPortfolioNames(List<String> portfolioNames) {
        if (CollectionUtils.isEmpty(portfolioNames)) {
            throw new CustomException("请输入投资组合名称portfolioName");
        }
        return tradePortfolioRepo.findByPortfolioNameIn(portfolioNames)
                .stream()
                .map(TradePortfolio::getTradeId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByPortfolioName(String portfolioName) {
        return portfolioRepo.existsByPortfolioName(portfolioName);
    }

    private Boolean checkTradeHasPortfolios(String tradeId, List<String> portfolioNames) {
        for (String portfolioName : portfolioNames) {
            if (!tradePortfolioRepo.existsByTradeIdAndPortfolioName(tradeId, portfolioName)) {
                return false;
            }
        }
        return true;
    }

    private PortfolioDTO transToDto(Portfolio portfolio) {
        UUID uuid = portfolio.getUuid();
        PortfolioDTO portfolioDto = new PortfolioDTO();
        BeanUtils.copyProperties(portfolio, portfolioDto);
        portfolioDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        return portfolioDto;
    }

}
