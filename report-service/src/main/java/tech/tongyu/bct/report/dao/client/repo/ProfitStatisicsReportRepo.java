package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.report.dao.client.dbo.ProfitStatisicsReport;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ProfitStatisicsReportRepo extends JpaRepository<ProfitStatisicsReport, UUID> {
    @Query("select distinct f.reportName from ProfitStatisicsReport f")
    List<String> findAllReportName();
    List<ProfitStatisicsReport> findByValuationDate(LocalDate valuationDate);

    List<ProfitStatisicsReport> findByValuationDateAndReportName(LocalDate valuationDate, String reportName);

    Page<ProfitStatisicsReport> findByReportNameAndValuationDateIn(String reportName, LocalDate valuationDate, Pageable pageable);

    void deleteAllByReportNameAndValuationDate(String reportName,LocalDate valuationDate);
}
