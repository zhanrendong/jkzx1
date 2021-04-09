package tech.tongyu.bct.report.client.service;

import tech.tongyu.bct.common.api.response.RpcResponseListPaged;
import tech.tongyu.bct.report.client.dto.*;
import tech.tongyu.bct.report.dto.DataRangeEnum;
import tech.tongyu.bct.report.dto.ReportTypeEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ClientReportService {

    List<String> findReportNamesByType(ReportTypeEnum reportType);

    void createPnlReport(PnlReportDTO pnlReportDto);

    void createRiskReport(RiskReportDTO riskReportDto);

    void createPnlHstReport(PnlHstReportDTO pnlHstReportDto);

    void createPositionReport(PositionReportDTO positionReportDto);

    void createOtcTradeReport(FinancialOtcTradeReportDTO FinancialOTCTradeReportDTO);

    void createFinancialOtcFundDetailReport(FinancialOtcFundDetailReportDTO financialOtcFundDetailReportDTO);

    void createFinanicalOtcClientFundReport(FinanicalOtcClientFundReportDTO finanicalOtcClientFundReportDTO);

    void createHedgePnlReport(HedgePnlReportDTO hedgePnlReportDTO);

    void createProfitStatisicsReport(ProfitStatisicsReportDTO profitStatisicsReportDTO);

    void createStatisticsOfRiskReport(StatisticsOfRiskReportDTO statisticsOfRiskReportDTO);

    void createSubjectComputingReport(SubjectComputingReportDTO subjectComputingReportDTO);

    void createMarketRiskReport(MarketRiskReportDTO marketRiskReportDTO);

    void createMarketRiskBySubUnderlyerReport(MarketRiskBySubUnderlyerReportDTO riskReportDto);

    void createSpotScenariosReport(SpotScenariosReportDTO dto);

    PnlReportDTO updatePnlById(PnlReportDTO pnlReportDto);

    RiskReportDTO updateRiskById(RiskReportDTO riskReportDto);

    PnlHstReportDTO updatePnlHstById(PnlHstReportDTO pnlHstReportDto);

    PositionReportDTO updatePositionById(PositionReportDTO positionReportDto);

    FinancialOtcTradeReportDTO updateOtcTradeById(FinancialOtcTradeReportDTO FinancialOTCTradeReportDTO);

    FinancialOtcFundDetailReportDTO updateFinancialOtcFundDetailById(FinancialOtcFundDetailReportDTO financialOtcFundDetailReportDTO);

    FinanicalOtcClientFundReportDTO updateFinanicalOtcClientFundById(FinanicalOtcClientFundReportDTO finanicalOtcClientFundReportDTO);

    List<PnlReportDTO> findAllPnlReport();

    List<RiskReportDTO> findAllRiskReport();

    List<PnlHstReportDTO> findAllPnlHstReport();

    List<PositionReportDTO> findAllPositionReport();

    List<FinancialOtcTradeReportDTO> findAllOtcTradeReport();

    List<FinancialOtcFundDetailReportDTO> findAllFinancialOtcFundDetailReport();

    List<FinanicalOtcClientFundReportDTO> findAllFinanicalOtcClientFundReport();

    List<HedgePnlReportDTO> findAllHedgePnlReport();

    List<ProfitStatisicsReportDTO> findAllProfitStatisicsReport();

    List<StatisticsOfRiskReportDTO> findAllStatisticsOfRiskReport();

    List<SubjectComputingReportDTO> findAllSubjectComputingReport();


    List<PnlReportDTO> findPnlReportByValuationDate(LocalDate valuationDate);

    List<PnlReportDTO> findPnlReportByValuationDateAndName(LocalDate valuationDate, String reportName);

    List<RiskReportDTO> findRiskReportByValuationDate(LocalDate valuationDate);

    List<RiskReportDTO> findRiskReportByValuationDateAndName(LocalDate valuationDate, String reportName);

    List<PnlHstReportDTO> findPnlHstReportByValuationDate(LocalDate valuationDate);

    List<PnlHstReportDTO> findPnlHstReportByValuationDateAndName(LocalDate valuationDate, String reportName);

    List<PositionReportDTO> findPositionReportByValuationDate(LocalDate valuationDate);

    List<PositionReportDTO> findPositionReportByValuationDateAndName(LocalDate valuationDate, String reportName);

    List<FinancialOtcTradeReportDTO> findOtcTradeReportByValuationDate(LocalDate valuationDate);

    List<FinancialOtcTradeReportDTO> findOtcTradeReportByValuationDateAndName(LocalDate valuationDate, String reportName);

    List<FinancialOtcFundDetailReportDTO> findFinancialOtcFundDetailReportByValuationDate(LocalDate valuationDate);

    List<FinancialOtcFundDetailReportDTO> findFinancialOtcFundDetailReportByValuationDateAndName(LocalDate valuationDate, String reportName);

    List<FinanicalOtcClientFundReportDTO> findFinanicalOtcClientFundReportByValuationDate(LocalDate valuationDate);

    List<FinanicalOtcClientFundReportDTO> findFinanicalOtcClientFundReportByValuationDateAndName(LocalDate valuationDate, String reportName);

    List<HedgePnlReportDTO> findAllHedgePnlReportByValuationDate(LocalDate valuationDate);

    List<HedgePnlReportDTO> findAllHedgePnlReportByValuationDateAndName(LocalDate valuationDate, String reportName);

    List<ProfitStatisicsReportDTO> findAllProfitStatisicsReportByValuationDate(LocalDate valuationDate);

    List<ProfitStatisicsReportDTO> findAllProfitStatisicsReportByValuationDateAndName(LocalDate valuationDate, String reportName);

    List<StatisticsOfRiskReportDTO> findAllStatisticsOfRiskReportByValuationDate(LocalDate valuationDate);

    List<StatisticsOfRiskReportDTO> findAllStatisticsOfRiskReportByValuationDateAndName(LocalDate valuationDate, String reportName);

    List<SubjectComputingReportDTO> findAllSubjectComputingReportByValuationDate(LocalDate valuationDate);

    List<SubjectComputingReportDTO> findAllSubjectComputingReportByValuationDateAndName(LocalDate valuationDate, String reportName);

    List<MarketRiskReportDTO> findAllMarketRiskReportByDateAndName(LocalDate valuationDate, String reportName);

    List<MarketRiskReportDTO> findAllMarketRiskReportByDate(LocalDate valuationDate);


    RpcResponseListPaged<PnlReportDTO> findPnlByReportNameAndValuationDateAndBookIdPaged(
            String reportName, List<String> bookIds, LocalDate valuationDate, Integer page, Integer pageSize);

    List<PnlReportDTO> findPnlReportByTemplate(PnlReportDTO pnlReportDTO, List<String> bookIds);

    RpcResponseListPaged<RiskReportDTO> findRiskByReportNameAndValuationDateAndBookIdPaged(
            String reportName, List<String> bookIds, LocalDate valuationDate, Integer page, Integer pageSize);

    List<RiskReportDTO> findRiskReportByTemplate(RiskReportDTO riskReportDTO, List<String> bookIds);

    List<ValuationReportDTO> findValuationReportByLegalNames(List<String> legalNames);

    RpcResponseListPaged<PnlHstReportDTO> findPnlHstReportNameAndValuationDateAndBookIdPaged(
            String reportName, List<String> bookIds, LocalDate valuationDate, Integer page, Integer pageSize);

    List<PnlHstReportDTO> findPnlHstReportByTemplate(PnlHstReportDTO pnlReportDTO, List<String> bookIds);

    List<PnlHstReportDTO> findLatestPnlHstReportNameAndValuationDateAndBookId(
            String reportName, List<String> bookIds, LocalDate valuationDate);

    RpcResponseListPaged<PositionReportDTO> findPositionReportNameAndValuationDateAndBookIdPaged(
            String reportName, List<String> bookIds, LocalDate valuationDate, Integer page, Integer pageSize);

    List<PositionReportDTO> findLatestPositionReportNameAndValuationDateAndBookId(
            String reportName, List<String> bookIds, LocalDate valuationDate);

    List<PositionReportDTO> findPositionReportByTemplate(PositionReportDTO positionReportDTO,
                                                         List<String> bookIds);

    RpcResponseListPaged<FinancialOtcTradeReportDTO> findOtcTradeReportNameAndValuationDatePaged(
            String reportName, List<String> bookIds, LocalDate valuationDate, Integer page, Integer pageSize);

    List<FinancialOtcTradeReportDTO> findOtcTradeReportByTemplate(
            FinancialOtcTradeReportDTO financialOtcTradeReportDTO, List<String> bookIds);

    RpcResponseListPaged<FinancialOtcFundDetailReportDTO> findFinancialOtcFundDetailReportNameAndValuationDatePaged(
            String reportName, LocalDate valuationDate, Integer page, Integer pageSize);

    List<FinancialOtcFundDetailReportDTO> findFinancialOtcFundDetailReportByTemplate(
            FinancialOtcFundDetailReportDTO financialOtcFundDetailReportDTO);

    RpcResponseListPaged<FinanicalOtcClientFundReportDTO> findFinanicalOtcClientFundReportNameAndValuationDatePaged(
            String reportName, LocalDate valuationDate, Integer page, Integer pageSize);

    List<FinanicalOtcClientFundReportDTO> findFinanicalOtcClientFundReportByTemplate(
            FinanicalOtcClientFundReportDTO finanicalOtcClientFundReportDTO);

    RpcResponseListPaged<HedgePnlReportDTO> findHedgePnlReportNameAndValuationDatePaged(
            String reportName, LocalDate valuationDate, Integer page, Integer pageSize);

    RpcResponseListPaged<ProfitStatisicsReportDTO> findProfitStatisicsReportNameAndValuationDatePaged(
            String reportName, LocalDate valuationDate, Integer page, Integer pageSize);

    RpcResponseListPaged<StatisticsOfRiskReportDTO> findStatisticsOfRiskReportNameAndValuationDatePaged(
            String reportName, LocalDate valuationDate, Integer page, Integer pageSize);

    RpcResponseListPaged<SubjectComputingReportDTO> findSubjectComputingReportNameAndValuationDatePaged(
            String reportName, LocalDate valuationDate, Integer page, Integer pageSize);

    void createOrUpdateValuationReport(ValuationReportDTO dto);

    ValuationReportDTO getValuationReport(UUID valuationReportId);

    ValuationReportDTO findValuationReportByLegalNameAndValuationDate(String legalName, LocalDate valuationDate);

    List<ValuationReportDTO> valuationReportSearch(ValuationReportDTO reportDto);

    void createMarketRiskDetailReport(MarketRiskDetailReportDTO marketRiskDetailReportDTO);

    List<MarketRiskDetailReportDTO> findAllMarketDetailRiskReportByDateAndInstrument(
            LocalDate valuationDate, String instrument);

    List<MarketRiskDetailReportDTO> findClassicScenarioMarketRiskReportByDateAndInstrument(
            LocalDate valuationDate, String instrument, DataRangeEnum reportType, String subsidiary, String partyName);

    void createSubsidiaryMarketRiskReport(SubsidiaryMarketRiskReportDTO subsidiaryMarketRiskReportDTO);

    List<SubsidiaryMarketRiskReportDTO> findAllSubsidiaryMarketRiskReportByDateAndSubsidiary(
            LocalDate valuationDate, String subsidiary);

    void createCounterPartyMarketRiskReport(CounterPartyMarketRiskReportDTO counterPartyMarketRiskReportDTO);

    List<CounterPartyMarketRiskReportDTO> findAllCounterPartyMarketRiskReportByDateAndPartyName(
            LocalDate valuationDate, String partyName);

    List<MarketRiskBySubUnderlyerReportDTO> findAllMarketRiskSubUnderlyerReportByDateAndInstrumentAndBook(LocalDate valuationDate, List<String> books, String instrument);

    List<SpotScenariosReportDTO> findSpotScenariosReportByDateAndTypeAndInstrument(DataRangeEnum reportType, String contentName, LocalDate valuationDate, String underlyer);

    void createCounterPartyMarketRiskByUnderlyerReport(CounterPartyMarketRiskByUnderlyerReportDTO reportDTO);

    List<CounterPartyMarketRiskByUnderlyerReportDTO> findAllCounterPartyMarketRiskByUnderlyerReportByDateAndInstrumentAndPartyName(LocalDate valuationDate, String partyNamePart, String instrumentIdPart);

    void deleteFinancialOtcFundDetailReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deletePnlReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deleteRiskReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deletePnlHstReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deletePositionReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deleteOtcTradeReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deleteFinanicalOtcClientFundReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deleteEodHedgePnlReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deleteEodProfitStatisicsReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deleteEodStatisticsOfRiskReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deleteEodSubjectComputingReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deleteMarketRiskReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deleteMarketRiskDetailReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deleteSubsidiaryMarketRiskReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deleteCounterPartyMarketRiskReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deleteCounterPartyMarketRiskByUnderlyerReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deleteMarketRiskBySubUnderlyerReportByReportNameAndValuationDate(String reportName, String valuationDate);

    void deleteSpotScenariosReportByValuationDate(String valuationDate);

    List<String> findReportInstrumentListByValuationDate(LocalDate valuationDate, ReportTypeEnum reportType);
    List<String> findReportSubsidiaryListByValuationDate(LocalDate valuationDate, ReportTypeEnum reportType);
    List<String> findReportCounterPartyListByValuationDate(LocalDate valuationDate, ReportTypeEnum reportType);
}
