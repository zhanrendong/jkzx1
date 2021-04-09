package tech.tongyu.bct.report.service;

import tech.tongyu.bct.report.dto.report.*;

import java.util.List;

public interface IntradayReportService {
    String intradayNotifyChannel = "/topic-report/notify";
    String intradayNotifyTopic = "intraday:notify";
    String intradayTopic = "intraday:queue";

    String tradeExpiringTopic = "tradeExpiring:queue";
    String tradeTopic = "trade:queue";
    String riskTopic = "risk:queue";
    String pnlTopic = "pnl:queue";

    String pnlHedgingTopic = "pnlHedging:queue";
    String pnlOptionTopic = "pnlOption:queue";
    String pnlTotalTopic = "pnlTotal:queue";
    String portfolioRiskTopic = "portfolioRisk:queue";

    List<GenericIntradayReportRowDTO> listAllGenericIntradayReport();

    List<GenericIntradayReportRowDTO> listGenericIntradayReportByName(String reportName);

    List<PositionExpiringReportRowDTO> listAllIntradayTradeExpiringReport();

    List<PositionReportRowDTO> listAllIntradayTradeReport();

    List<RiskReportRowDTO> listAllIntradayRiskReport();

    List<PnlReportRowDTO> listAllIntradayPnlReport();

    List<PortfolioRiskReportRowDTO> listAllPortfolioRiskReport();

    void saveGenericIntradayReport(List<GenericIntradayReportRowDTO> genericIntradayReport);

    void saveIntradayTradeExpiringReport(List<PositionExpiringReportRowDTO> tradeExpiringReports);

    void saveIntradayTradeReport(List<PositionReportRowDTO> tradeReports);

    void saveIntradayRiskReport(List<RiskReportRowDTO> riskReports);

    void saveIntradayPnlReport(List<PnlReportRowDTO> pnlReports);


    void saveIntradayPnlTotalReport(List<PnlReportTotalRowDTO> pnlTotals);

    void saveIntradayPnlOptionReport(List<PnlReportOptionRowDTO> pnlOptions);

    void saveIntradayPnlHedgingReport(List<PnlReportHedgingRowDTO> pnlHedgings);

    void saveIntradayPortfolioRiskReport(List<PortfolioRiskReportRowDTO> portfolioRiskReports);

}
