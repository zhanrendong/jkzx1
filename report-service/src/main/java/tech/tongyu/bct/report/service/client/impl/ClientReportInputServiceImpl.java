package tech.tongyu.bct.report.service.client.impl;

import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.report.client.service.ClientReportInputService;
import tech.tongyu.bct.report.dao.client.dbo.*;
import tech.tongyu.bct.report.dao.client.repo.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */

@Service
public class ClientReportInputServiceImpl implements ClientReportInputService {


    private PositionReportRepo positionRepo;

    private PnlHstReportRepo pnlHstRepo;

    private RiskReportRepo riskRepo;

    private PnlReportRepo pnlRepo;

    private FinancialOtcTradeReportRepo otcTradeRepo;

    private FinancialOtcFundDetailReportRepo fodRepo;

    private FinanicalOtcClientFundReportRepo focRepo;

    private SubjectComputingReportRepo subjectComputingReportRepo;

    private ProfitStatisicsReportRepo profitStatisicsReportRepo;

    private StatisticsOfRiskReportRepo statisticsOfRiskReportRepo;

    private HedgePnlReportRepo hedgePnlReportRepo;

    private MarketRiskReportRepo marketRiskReportRepo;

    private MarketRiskDetailReportRepo marketRiskDetailReportRepo;

    private MarketRiskBySubUnderlyerReportRepo marketRiskBySubUnderlyerReportRepo;

    private SpotScenariosReportRepo spotScenariosReportRepo;

    private SubsidiaryMarketRiskReportRepo subsidiaryMarketRiskReportRepo;

    private CounterPartyMarketRiskReportRepo counterPartyMarketRiskReportRepo;

    private CounterPartyMarketRiskByUnderlyerReportRepo counterPartyMarketRiskByUnderlyerReportRepo;

    public ClientReportInputServiceImpl(PositionReportRepo positionRepo
            , PnlHstReportRepo pnlHstRepo
            , RiskReportRepo riskRepo
            , PnlReportRepo pnlRepo, FinancialOtcTradeReportRepo otcTradeRepo
            , FinancialOtcFundDetailReportRepo fodRepo
            , FinanicalOtcClientFundReportRepo focRepo
            , SubjectComputingReportRepo subjectComputingReportRepo
            , ProfitStatisicsReportRepo profitStatisicsReportRepo
            , StatisticsOfRiskReportRepo statisticsOfRiskReportRepo
            , HedgePnlReportRepo hedgePnlReportRepo
            , MarketRiskReportRepo marketRiskReportRepo
            , MarketRiskDetailReportRepo marketRiskDetailReportRepo
            , MarketRiskBySubUnderlyerReportRepo marketRiskBySubUnderlyerReportRepo
            , SpotScenariosReportRepo spotScenariosReportRepo
            , SubsidiaryMarketRiskReportRepo subsidiaryMarketRiskReportRepo
            , CounterPartyMarketRiskReportRepo counterPartyMarketRiskReportRepo
            , CounterPartyMarketRiskByUnderlyerReportRepo counterPartyMarketRiskByUnderlyerReportRepo) {
        this.positionRepo = positionRepo;
        this.pnlHstRepo = pnlHstRepo;
        this.riskRepo = riskRepo;
        this.pnlRepo = pnlRepo;
        this.otcTradeRepo = otcTradeRepo;
        this.fodRepo = fodRepo;
        this.focRepo = focRepo;
        this.subjectComputingReportRepo = subjectComputingReportRepo;
        this.profitStatisicsReportRepo = profitStatisicsReportRepo;
        this.statisticsOfRiskReportRepo = statisticsOfRiskReportRepo;
        this.hedgePnlReportRepo = hedgePnlReportRepo;
        this.marketRiskReportRepo = marketRiskReportRepo;
        this.marketRiskDetailReportRepo = marketRiskDetailReportRepo;
        this.marketRiskBySubUnderlyerReportRepo = marketRiskBySubUnderlyerReportRepo;
        this.spotScenariosReportRepo = spotScenariosReportRepo;
        this.subsidiaryMarketRiskReportRepo = subsidiaryMarketRiskReportRepo;
        this.counterPartyMarketRiskReportRepo = counterPartyMarketRiskReportRepo;
        this.counterPartyMarketRiskByUnderlyerReportRepo = counterPartyMarketRiskByUnderlyerReportRepo;
    }

    @Override
    public void createPnlReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<PnlReport> pnlReports = reports.parallelStream().map(pnl -> {
            PnlReport pnlReport = JsonUtils.mapper.convertValue(pnl, PnlReport.class);
            pnlReport.setReportName(reportName);
            pnlReport.setValuationDate(date);
            return pnlReport;
        }).collect(Collectors.toList());
        pnlRepo.saveAll(pnlReports);
    }

    @Override
    public void createRiskReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<RiskReport> riskReports = reports.parallelStream().map(risk -> {
            RiskReport riskReport = JsonUtils.mapper.convertValue(risk, RiskReport.class);
            riskReport.setReportName(reportName);
            riskReport.setValuationDate(date);
            return riskReport;
        }).collect(Collectors.toList());
        riskRepo.saveAll(riskReports);
    }

    @Override
    public void createPnlHstReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<PnlHstReport> pnlHstReports = reports.parallelStream().map(pnlHst -> {
            PnlHstReport pnlHstReport = JsonUtils.mapper.convertValue(pnlHst, PnlHstReport.class);
            pnlHstReport.setReportName(reportName);
            pnlHstReport.setValuationDate(date);
            return pnlHstReport;
        }).collect(Collectors.toList());
        pnlHstRepo.saveAll(pnlHstReports);
    }

    @Override
    public void createPositionReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<PositionReport> positionReports = reports.parallelStream().map(position -> {
            PositionReport positionReport = JsonUtils.mapper.convertValue(position, PositionReport.class);
            positionReport.setReportName(reportName);
            positionReport.setValuationDate(date);
            return positionReport;
        }).collect(Collectors.toList());
        positionRepo.saveAll(positionReports);
    }

    @Override
    public void createOtcTradeReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<FinancialOtcTradeReport> financialOtcTradeReports = reports.parallelStream().map(otcTrade -> {
            FinancialOtcTradeReport financialOtcTradeReport = JsonUtils.mapper.convertValue(otcTrade, FinancialOtcTradeReport.class);
            financialOtcTradeReport.setReportName(reportName);
            financialOtcTradeReport.setValuationDate(date);
            return financialOtcTradeReport;
        }).collect(Collectors.toList());
        otcTradeRepo.saveAll(financialOtcTradeReports);
    }

    @Override
    public void createFinancialOtcFundDetailReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<FinancialOtcFundDetailReport> financialOtcFundDetailReports = reports.parallelStream().map(otcFund -> {
            FinancialOtcFundDetailReport financialOtcTradeReport = JsonUtils.mapper.convertValue(otcFund, FinancialOtcFundDetailReport.class);
            financialOtcTradeReport.setReportName(reportName);
            financialOtcTradeReport.setValuationDate(date);
            return financialOtcTradeReport;
        }).collect(Collectors.toList());
        fodRepo.saveAll(financialOtcFundDetailReports);
    }

    @Override
    public void createFinanicalOtcClientFundReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<FinanicalOtcClientFundReport> finanicalOtcClientFundReports = reports.parallelStream().map(OtcClient -> {
            FinanicalOtcClientFundReport finanicalOtcClientFundReport = JsonUtils.mapper.convertValue(OtcClient, FinanicalOtcClientFundReport.class);
            finanicalOtcClientFundReport.setReportName(reportName);
            finanicalOtcClientFundReport.setValuationDate(date);
            return finanicalOtcClientFundReport;
        }).collect(Collectors.toList());
        focRepo.saveAll(finanicalOtcClientFundReports);
    }

    @Override
    public void createHedgePnlReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<HedgePnlReport> hedgePnlReports = reports.parallelStream().map(hedgePnl -> {
            HedgePnlReport hedgePnlReport = JsonUtils.mapper.convertValue(hedgePnl, HedgePnlReport.class);
            hedgePnlReport.setReportName(reportName);
            hedgePnlReport.setValuationDate(date);
            return hedgePnlReport;
        }).collect(Collectors.toList());
        hedgePnlReportRepo.saveAll(hedgePnlReports);
    }

    @Override
    public void createProfitStatisicsReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<ProfitStatisicsReport> profitStatisicsReports = reports.parallelStream().map(profit -> {
            ProfitStatisicsReport profitStatisicsReport = JsonUtils.mapper.convertValue(profit, ProfitStatisicsReport.class);
            profitStatisicsReport.setReportName(reportName);
            profitStatisicsReport.setValuationDate(date);
            return profitStatisicsReport;
        }).collect(Collectors.toList());
        profitStatisicsReportRepo.saveAll(profitStatisicsReports);
    }

    @Override
    public void createStatisticsOfRiskReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<StatisticsOfRiskReport> statisticsOfRiskReports = reports.parallelStream().map(statistics -> {
            StatisticsOfRiskReport profitStatisicsReport = JsonUtils.mapper.convertValue(statistics, StatisticsOfRiskReport.class);
            profitStatisicsReport.setReportName(reportName);
            profitStatisicsReport.setValuationDate(date);
            return profitStatisicsReport;
        }).collect(Collectors.toList());
        statisticsOfRiskReportRepo.saveAll(statisticsOfRiskReports);
    }

    @Override
    public void createSubjectComputingReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<SubjectComputingReport> subjectComputingReports = reports.parallelStream().map(subject -> {
            SubjectComputingReport subjectComputingReport = JsonUtils.mapper.convertValue(subject, SubjectComputingReport.class);
            subjectComputingReport.setReportName(reportName);
            subjectComputingReport.setValuationDate(date);
            return subjectComputingReport;
        }).collect(Collectors.toList());
        subjectComputingReportRepo.saveAll(subjectComputingReports);
    }

    @Override
    public void createMarketRiskReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<MarketRiskReport> marketRiskReports = reports.parallelStream().map(market -> {
            MarketRiskReport marketRiskReport = JsonUtils.mapper.convertValue(market, MarketRiskReport.class);
            marketRiskReport.setReportName(reportName);
            marketRiskReport.setValuationDate(date);
            return marketRiskReport;
        }).collect(Collectors.toList());
        marketRiskReportRepo.saveAll(marketRiskReports);
    }

    @Override
    public void createMarketRiskDetailReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<MarketRiskDetailReport> marketRiskDetailReports = reports.parallelStream().map(market -> {
            MarketRiskDetailReport marketRiskDetailReport = JsonUtils.mapper.convertValue(market, MarketRiskDetailReport.class);
            marketRiskDetailReport.setReportName(reportName);
            marketRiskDetailReport.setValuationDate(date);
            return marketRiskDetailReport;
        }).collect(Collectors.toList());
        marketRiskDetailReportRepo.saveAll(marketRiskDetailReports);
    }

    @Override
    public void createSubsidiaryMarketRiskReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<SubsidiaryMarketRiskReport> subsidiaryMarketRiskReports = reports.parallelStream().map(smr -> {
            SubsidiaryMarketRiskReport subsidiaryMarketRiskReport = JsonUtils.mapper.convertValue(smr, SubsidiaryMarketRiskReport.class);
            subsidiaryMarketRiskReport.setReportName(reportName);
            subsidiaryMarketRiskReport.setValuationDate(date);
            return subsidiaryMarketRiskReport;
        }).collect(Collectors.toList());
        subsidiaryMarketRiskReportRepo.saveAll(subsidiaryMarketRiskReports);
    }

    @Override
    public void createCounterPartyMarketRiskReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<CounterPartyMarketRiskReport> counterPartyMarketRiskReports = reports.parallelStream().map(counterParty -> {
            CounterPartyMarketRiskReport counterPartyMarketRiskReport = JsonUtils.mapper.convertValue(counterParty, CounterPartyMarketRiskReport.class);
            counterPartyMarketRiskReport.setReportName(reportName);
            counterPartyMarketRiskReport.setValuationDate(date);
            return counterPartyMarketRiskReport;
        }).collect(Collectors.toList());
        counterPartyMarketRiskReportRepo.saveAll(counterPartyMarketRiskReports);
    }

    @Override
    public void createCounterPartyMarketRiskByUnderlyerReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<CounterPartyMarketRiskByUnderlyerReport> counterPartyMarketRiskByUnderlyerReports = reports.parallelStream().map(counterParty -> {
            CounterPartyMarketRiskByUnderlyerReport counterPartyMarketRiskByUnderlyerReport = JsonUtils.mapper.convertValue(counterParty, CounterPartyMarketRiskByUnderlyerReport.class);
            counterPartyMarketRiskByUnderlyerReport.setReportName(reportName);
            counterPartyMarketRiskByUnderlyerReport.setValuationDate(date);
            return counterPartyMarketRiskByUnderlyerReport;
        }).collect(Collectors.toList());
        counterPartyMarketRiskByUnderlyerReportRepo.saveAll(counterPartyMarketRiskByUnderlyerReports);
    }

    @Override
    public void createMarketRiskBySubUnderlyerReport(List<Map<String, Object>> reports, String reportName, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<MarketRiskBySubUnderlyerReport> marketRiskBySubUnderlyerReports = reports.parallelStream().map(marketRisk -> {
            MarketRiskBySubUnderlyerReport marketRiskBySubUnderlyerReport = JsonUtils.mapper.convertValue(marketRisk, MarketRiskBySubUnderlyerReport.class);
            marketRiskBySubUnderlyerReport.setReportName(reportName);
            marketRiskBySubUnderlyerReport.setValuationDate(date);
            return marketRiskBySubUnderlyerReport;
        }).collect(Collectors.toList());
        marketRiskBySubUnderlyerReportRepo.saveAll(marketRiskBySubUnderlyerReports);
    }

    @Override
    public void createSpotScenariosReport(List<Map<String, Object>> reports, String valuationDate) {
        LocalDate date = LocalDate.parse(valuationDate);
        List<SpotScenariosReport> spotScenariosReports = reports.parallelStream().map(spotScenarios -> {
            SpotScenariosReport spotScenariosReport = JsonUtils.mapper.convertValue(spotScenarios, SpotScenariosReport.class);
            spotScenariosReport.setValuationDate(date);
            return spotScenariosReport;
        }).collect(Collectors.toList());
        spotScenariosReportRepo.saveAll(spotScenariosReports);
    }
}

