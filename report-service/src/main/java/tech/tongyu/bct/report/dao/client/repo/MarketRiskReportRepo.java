package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.report.dao.client.dbo.MarketRiskReport;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MarketRiskReportRepo extends JpaRepository<MarketRiskReport, UUID> {

    @Query("select distinct r.reportName from MarketRiskReport r")
    List<String> findAllReportName();

    List<MarketRiskReport> findByValuationDate(LocalDate valuationDate);

    List<MarketRiskReport> findByValuationDateAndReportName(LocalDate valuationDate, String reportName);

    void deleteAllByReportNameAndValuationDate(String reportName, LocalDate valuationDate);
}
