package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.report.dao.client.dbo.FinancialOtcTradeReport;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FinancialOtcTradeReportRepo extends JpaRepository<FinancialOtcTradeReport, UUID> {
    @Query("select distinct o.reportName from FinancialOtcTradeReport o")
    List<String> findAllReportName();

    List<FinancialOtcTradeReport> findByValuationDate(LocalDate valuationDate);

    List<FinancialOtcTradeReport> findByValuationDateAndReportName(LocalDate valuationDate, String reportName);

    Page<FinancialOtcTradeReport> findByReportNameAndValuationDateAndBookNameIn(String reportName, LocalDate valuationDate, List<String> bookIds, Pageable pageable);


    void deleteByReportNameAndValuationDateAndOptionName(String reportName,LocalDate valuationDate,String optionName);

    void deleteAllByReportNameAndValuationDate(String reportName,LocalDate valuationDate);
}
