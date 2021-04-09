package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.report.dao.client.dbo.MarketRiskDetailReport;
import tech.tongyu.bct.report.dto.DataRangeEnum;
import tech.tongyu.bct.report.dto.report.MarketScenarioType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MarketRiskDetailReportRepo extends JpaRepository<MarketRiskDetailReport, UUID> {

    List<MarketRiskDetailReport> findByValuationDateAndScenarioType(LocalDate valuationDate, MarketScenarioType ScenarioType);

    List<MarketRiskDetailReport> findByValuationDateAndUnderlyerInstrumentId(LocalDate valuationDate, String instrumentId);

    void deleteByValuationDateAndUnderlyerInstrumentIdAndScenarioTypeAndReportTypeAndSubsidiaryAndPartyName(
            LocalDate valuationDate, String UnderlyerInstrumentId, MarketScenarioType ScenarioType, DataRangeEnum reportType, String subsidiary, String partyName);

    void deleteAllByReportNameAndValuationDate(String reportName,LocalDate valuationDate);

    List<MarketRiskDetailReport> findAllByValuationDateAndScenarioType(LocalDate valuationDate, MarketScenarioType scenarioType);
}