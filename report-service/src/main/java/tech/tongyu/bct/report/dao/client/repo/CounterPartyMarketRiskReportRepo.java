package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.report.dao.client.dbo.CounterPartyMarketRiskReport;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CounterPartyMarketRiskReportRepo extends JpaRepository<CounterPartyMarketRiskReport, UUID> {

    List<CounterPartyMarketRiskReport> findByValuationDate(LocalDate valuationDate);

    void deleteByValuationDateAndPartyName(LocalDate valuationDate, String partyName);

    void deleteAllByReportNameAndValuationDate(String reportName,LocalDate valuationDate);

    List<CounterPartyMarketRiskReport> findAllByValuationDate(LocalDate valuationDate);
}