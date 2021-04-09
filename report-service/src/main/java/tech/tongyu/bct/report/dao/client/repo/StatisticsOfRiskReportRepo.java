package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.report.dao.client.dbo.StatisticsOfRiskReport;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface StatisticsOfRiskReportRepo extends JpaRepository<StatisticsOfRiskReport, UUID> {
    @Query("select distinct f.reportName from StatisticsOfRiskReport f")
    List<String> findAllReportName();
    List<StatisticsOfRiskReport> findByValuationDate(LocalDate valuationDate);

    List<StatisticsOfRiskReport> findByValuationDateAndReportName(LocalDate valuationDate, String reportName);

    Page<StatisticsOfRiskReport> findByReportNameAndValuationDateIn(String reportName, LocalDate valuationDate, Pageable pageable);

    void deleteAllByReportNameAndValuationDate(String reportName,LocalDate valuationDate);
}
