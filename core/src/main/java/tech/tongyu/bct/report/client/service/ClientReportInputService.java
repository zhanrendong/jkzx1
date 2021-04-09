package tech.tongyu.bct.report.client.service;

import java.util.List;
import java.util.Map;

public interface ClientReportInputService {

    void createPnlReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createRiskReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createPnlHstReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createPositionReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createOtcTradeReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createFinancialOtcFundDetailReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createFinanicalOtcClientFundReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createHedgePnlReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createProfitStatisicsReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createStatisticsOfRiskReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createSubjectComputingReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createMarketRiskReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createMarketRiskDetailReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createSubsidiaryMarketRiskReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createCounterPartyMarketRiskReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createCounterPartyMarketRiskByUnderlyerReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createMarketRiskBySubUnderlyerReport(List<Map<String, Object>> reports, String reportName, String valuationDate);

    void createSpotScenariosReport(List<Map<String, Object>> reports, String valuationDate);
}