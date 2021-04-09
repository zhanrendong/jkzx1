package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.report.dao.client.dbo.FinanicalOtcClientFundReport;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FinanicalOtcClientFundReportRepo extends JpaRepository<FinanicalOtcClientFundReport, UUID> {
    @Query("select distinct f.reportName from FinanicalOtcClientFundReport f")
    List<String> findAllReportName();

    List<FinanicalOtcClientFundReport> findByValuationDate(LocalDate valuationDate);

    List<FinanicalOtcClientFundReport> findByValuationDateAndReportName(LocalDate valuationDate, String reportName);

    Page<FinanicalOtcClientFundReport> findByReportNameAndValuationDateIn(String reportName, LocalDate valuationDate, Pageable pageable);

    void deleteByReportNameAndClientNameAndValuationDate(String reportName,String clientName,LocalDate valuationDate);

    void deleteAllByReportNameAndValuationDate(String reportName,LocalDate valuationDate);
}
