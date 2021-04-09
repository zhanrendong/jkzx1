package tech.tongyu.bct.trade.api;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.ProfilingUtils;
import tech.tongyu.bct.trade.dto.trade.PortfolioDTO;
import tech.tongyu.bct.trade.service.PortfolioSearchService;
import tech.tongyu.bct.trade.service.PortfolioService;

import java.util.List;
import java.util.Map;

@Service
public class PortfolioApi {

    @Autowired
    PortfolioService portfolioService;

    @Autowired
    PortfolioSearchService portfolioSearchService;

    @BctMethodInfo(
            description = "创建投资组合",
            retDescription = "创建的投资组合",
            retName = "PortfolioDTO",
            returnClass = PortfolioDTO.class,
            service = "trade-service"
    )
    public PortfolioDTO trdPortfolioCreate(
            @BctMethodArg(description = "投资组合名称") String portfolioName
    ) {
        if (StringUtils.isBlank(portfolioName)) {
            throw new IllegalArgumentException("请输入投资组合名称portfolioName");
        }
        return portfolioService.create(portfolioName);
    }

    @BctMethodInfo(
            description = "更改投资组合名称",
            retDescription = "更改后的投资组合",
            retName = "PortfolioDTO",
            returnClass = PortfolioDTO.class,
            service = "trade-service"
    )
    public PortfolioDTO trdPortfolioUpdate(
            @BctMethodArg(description = "唯一标识uuid") String uuid,
            @BctMethodArg(description = "投资组合名称") String portfolioName
    ) {
        if (StringUtils.isBlank(uuid)) {
            throw new IllegalArgumentException("请输入修改唯一标识uuid");
        }
        if (StringUtils.isBlank(portfolioName)) {
            throw new IllegalArgumentException("请输入投资组合名称portfolioName");
        }
        PortfolioDTO portfolioDto = new PortfolioDTO();
        portfolioDto.setUuid(uuid);
        portfolioDto.setPortfolioName(portfolioName);
        return portfolioService.update(portfolioDto);
    }

    @BctMethodInfo(
            description = "删除投资组合",
            retDescription = "删除是否成功",
            retName = "true or false",
            service = "trade-service"
    )
    public Boolean trdPortfolioDelete(
            @BctMethodArg(description = "投资组合名称") String portfolioName
    ) {
        if (StringUtils.isBlank(portfolioName)) {
            throw new IllegalArgumentException("请输入投资组合名称portfolioName");
        }
        return portfolioService.deleteByPortfolioName(portfolioName);
    }

    @BctMethodInfo(
            description = "模糊查询投资组合名称",
            retDescription = "符合条件的投资组合名称",
            retName = "list of portfolio names",
            service = "trade-service"
    )
    public List<String> trdPortfolioListBySimilarPortfolioName(
            @BctMethodArg(description = "模糊查询关键词") String similarPortfolioName
    ) {
        return portfolioSearchService.listBySimilarPortfolioName(similarPortfolioName);
    }

    @BctMethodInfo(
            description = "根据投资组合名称搜索投资组合",
            retDescription = "投资组合",
            retName = "list of PortfolioDTOs",
            returnClass = PortfolioDTO.class,
            service = "trade-service"
    )
    public List<PortfolioDTO> trdPortfolioSearch(
            @BctMethodArg(description = "投资组合名称") String portfolioName
    ) {
        return portfolioSearchService.search(portfolioName);
    }

    @BctMethodInfo(
            description = "获取所有的投资组合和相关的交易",
            retDescription = "所有的投资组合和相关的交易",
            retName = "all portfolios and their trades",
            service = "trade-service"
    )
    public Map<String, List<String>> trdPortfolioTradesList() {
        return portfolioSearchService.listAllPortfolioTrades(false);
    }

    @BctMethodInfo(
            description = "关联投资组合和交易",
            retDescription = "关联是否成功",
            retName = "true or false",
            service = "trade-service"
    )
    public Boolean trdTradePortfolioCreate(
            @BctMethodArg(description = "交易编号") String tradeId,
            @BctMethodArg(description = "投资组合名称") String portfolioName
    ) {
        if (StringUtils.isBlank(tradeId)) {
            throw new IllegalArgumentException("请输入交易编号tradeId");
        }
        if (StringUtils.isBlank(portfolioName)) {
            throw new IllegalArgumentException("请输入投资组合名称portfolioName");
        }
        return portfolioService.createTradePortfolio(tradeId, portfolioName);
    }

    @BctMethodInfo(
            description = "交易关联多个投资组合",
            retDescription = "关联是否成功",
            retName = "true or false",
            service = "trade-service"
    )
    public Boolean trdTradePortfolioCreateBatch(
            @BctMethodArg(description = "交易编号") String tradeId,
            @BctMethodArg(description = "投资组合名称列表") List<String> portfolioNames) {
        if (StringUtils.isBlank(tradeId)) {
            throw new IllegalArgumentException("请输入交易编号tradeId");
        }
        if (CollectionUtils.isEmpty(portfolioNames)) {
            throw new IllegalArgumentException("请输入投资组合名称portfolioName");
        }
        return portfolioService.createTradePortfolioBatch(tradeId, portfolioNames);
    }

    @BctMethodInfo(
            description = "删除交易和投资组合的关联",
            retDescription = "删除是否成功",
            retName = "true or false",
            service = "trade-service"
    )
    public Boolean trdTradePortfolioDelete(
            @BctMethodArg(description = "交易编号") String tradeId,
            @BctMethodArg(description = "投资组合名称") String portfolioName
    ) {
        if (StringUtils.isBlank(tradeId)) {
            throw new IllegalArgumentException("请输入交易编号tradeId");
        }
        if (StringUtils.isBlank(portfolioName)) {
            throw new IllegalArgumentException("请输入投资组合名称portfolioName");
        }
        return portfolioService.deleteTradePortfolio(tradeId, portfolioName);
    }

    @BctMethodInfo(
            description = "更改交易和投资组合的关联",
            retDescription = "更改是否成功",
            retName = "true or false",
            service = "trade-service"
    )
    public Boolean trdTradePortfolioRefresh(
            @BctMethodArg(description = "交易编号") String tradeId,
            @BctMethodArg(description = "投资组合名称列表") List<String> portfolioNames
    ) {
        if (StringUtils.isBlank(tradeId)) {
            throw new IllegalArgumentException("请输入交易编号tradeId");
        }
        if (CollectionUtils.isEmpty(portfolioNames)) {
            throw new IllegalArgumentException("请输入投资组合名称portfolioName");
        }
        return portfolioService.refreshTradePortfolios(tradeId, portfolioNames);
    }

}
