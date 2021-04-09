package tech.tongyu.bct.report.service.impl;

import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tech.tongyu.bct.report.dto.report.*;
import tech.tongyu.bct.report.service.IntradayReportService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class IntradayReportServiceImpl implements IntradayReportService {
    private ConcurrentHashMap<String, Map<String, List<GenericIntradayReportRowDTO>>> cachedGenericReports =
            new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, List<PositionExpiringReportRowDTO>> cachedTradeExpirings = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<PositionReportRowDTO>> cachedTrades = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<RiskReportRowDTO>> cachedRisks = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<PnlReportRowDTO>> cachedPnls = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, List<PnlReportHedgingRowDTO>> cachedPnlHedgings = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<PnlReportOptionRowDTO>> cachedPnlOptions = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<PnlReportTotalRowDTO>> cachedPnlTotals = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,List<PortfolioRiskReportRowDTO>> cachedPortfolioRisks = new ConcurrentHashMap<>();


    @Override
    public void saveIntradayTradeExpiringReport(List<PositionExpiringReportRowDTO> tradeExpiringReports) {
        cachedTradeExpirings.put(tradeExpiringTopic, tradeExpiringReports);
    }

    @Override
    public void saveIntradayTradeReport(List<PositionReportRowDTO> tradeReports) {
        cachedTrades.put(tradeTopic, tradeReports);
    }

    @Override
    public void saveIntradayRiskReport(List<RiskReportRowDTO> riskReports) {
        cachedRisks.put(riskTopic, riskReports);
    }

    @Override
    public void saveIntradayPnlReport(List<PnlReportRowDTO> pnlReports) {
        cachedPnls.put(pnlTopic, pnlReports);
    }

    @Override
    public List<GenericIntradayReportRowDTO> listAllGenericIntradayReport() {
        return cachedGenericReports.getOrDefault(intradayTopic, Maps.newHashMap())
                .entrySet()
                .stream()
                .flatMap(m -> m.getValue().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<GenericIntradayReportRowDTO> listGenericIntradayReportByName(String reportName) {
        Map<String, List<GenericIntradayReportRowDTO>> cache = cachedGenericReports.get(intradayTopic);
        return cache.getOrDefault(reportName, Arrays.asList());
    }

    @Override
    public List<PositionExpiringReportRowDTO> listAllIntradayTradeExpiringReport() {
        List<PositionExpiringReportRowDTO> reports = cachedTradeExpirings.get(tradeExpiringTopic);
        if (CollectionUtils.isEmpty(reports)) {
            return new ArrayList<>();
        }
        return reports;
    }

    @Override
    public List<PositionReportRowDTO> listAllIntradayTradeReport() {
        List<PositionReportRowDTO> reports = cachedTrades.get(tradeTopic);
        if (CollectionUtils.isEmpty(reports)) {
            return new ArrayList<>();
        }
        return reports;
    }

    @Override
    public List<RiskReportRowDTO> listAllIntradayRiskReport() {
        List<RiskReportRowDTO> reports = cachedRisks.get(riskTopic);
        if (CollectionUtils.isEmpty(reports)) {
            return new ArrayList<>();
        }
        return reports;
    }

    @Override
    public List<PnlReportRowDTO> listAllIntradayPnlReport() {
        List<PnlReportRowDTO> reports = cachedPnls.get(pnlTopic);
        if (CollectionUtils.isEmpty(reports)) {
            return new ArrayList<>();
        }
        return reports;
    }

    @Override
    public List<PortfolioRiskReportRowDTO> listAllPortfolioRiskReport() {
        List<PortfolioRiskReportRowDTO> reports = cachedPortfolioRisks.get(portfolioRiskTopic);
        if (CollectionUtils.isEmpty(reports)) {
            return new ArrayList<>();
        }
        return reports;
    }

    @Override
    public void saveGenericIntradayReport(List<GenericIntradayReportRowDTO> genericIntradayReport) {
        Map<String, List<GenericIntradayReportRowDTO>> newValues =
                genericIntradayReport
                        .stream()
                        .collect(Collectors.groupingBy(r -> r.getReportName()))
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));
        if(cachedGenericReports.containsKey(intradayTopic)){
            Map<String, List<GenericIntradayReportRowDTO>> results = new HashMap<>(cachedGenericReports.get(intradayTopic));
            results.putAll(newValues);
            cachedGenericReports.put(intradayTopic, results);

        } else {
            cachedGenericReports.put(intradayTopic, newValues);
        }
    }

    @Override
    public void saveIntradayPnlTotalReport(List<PnlReportTotalRowDTO> pnlTotals) {
        cachedPnlTotals.put(pnlTotalTopic, pnlTotals);
    }

    @Override
    public void saveIntradayPnlOptionReport(List<PnlReportOptionRowDTO> pnlOptions) {
        cachedPnlOptions.put(pnlOptionTopic, pnlOptions);
    }

    @Override
    public void saveIntradayPnlHedgingReport(List<PnlReportHedgingRowDTO> pnlHedgings) {
        cachedPnlHedgings.put(pnlHedgingTopic, pnlHedgings);
    }

    @Override
    public void saveIntradayPortfolioRiskReport(List<PortfolioRiskReportRowDTO> portfolioRiskReports) {
        cachedPortfolioRisks.put(portfolioRiskTopic,portfolioRiskReports);
    }
}
