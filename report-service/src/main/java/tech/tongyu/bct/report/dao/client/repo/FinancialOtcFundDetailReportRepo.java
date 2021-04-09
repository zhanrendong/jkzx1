package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.report.dao.client.dbo.FinancialOtcFundDetailReport;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FinancialOtcFundDetailReportRepo extends JpaRepository<FinancialOtcFundDetailReport, UUID> {
    @Query("select distinct f.reportName from FinancialOtcFundDetailReport f")
    List<String> findAllReportName();

    List<FinancialOtcFundDetailReport> findByValuationDate(LocalDate valuationDate);

    List<FinancialOtcFundDetailReport> findByValuationDateAndReportName(LocalDate valuationDate, String reportName);

    Page<FinancialOtcFundDetailReport> findByReportNameAndValuationDateIn(String reportName, LocalDate valuationDate, Pageable pageable);

    void deleteAllByReportNameAndValuationDate(String reportName,LocalDate valuationDate);
}
