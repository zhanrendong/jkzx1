package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.report.dao.client.dbo.CounterPartyMarketRiskByUnderlyerReport;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CounterPartyMarketRiskByUnderlyerReportRepo extends JpaRepository<CounterPartyMarketRiskByUnderlyerReport, UUID> {

    List<CounterPartyMarketRiskByUnderlyerReport> findByValuationDate(LocalDate valuationDate);

    void deleteByValuationDateAndPartyNameAndUnderlyerInstrumentId(LocalDate valuationDate, String partyName, String underlyerInstrumentId);

    void deleteAllByReportNameAndValuationDate(String reportName,LocalDate valuationDate);

    List<CounterPartyMarketRiskByUnderlyerReport> findAllByValuationDate(LocalDate valuationDate);
}