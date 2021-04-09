package tech.tongyu.bct.report.dao.client.repo;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.report.dao.client.dbo.HedgePnlReport;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HedgePnlReportRepo extends JpaRepository<HedgePnlReport, UUID> {
    @Query("select distinct f.reportName from HedgePnlReport f")
    List<String> findAllReportName();

    List<HedgePnlReport> findByValuationDate(LocalDate valuationDate);

    List<HedgePnlReport> findByValuationDateAndReportName(LocalDate valuationDate, String reportName);

    Page<HedgePnlReport> findByReportNameAndValuationDateIn(String reportName, LocalDate valuationDate, Pageable pageable);

    void deleteAllByReportNameAndValuationDate(String reportName,LocalDate valuationDate);
}
