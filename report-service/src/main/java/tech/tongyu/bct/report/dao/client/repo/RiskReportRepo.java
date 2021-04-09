package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.report.dao.client.dbo.RiskReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiskReportRepo extends JpaRepository<RiskReport, UUID> {

    @Query("select distinct r.reportName from RiskReport r")
    List<String> findAllReportName();

    List<RiskReport> findByValuationDate(LocalDate valuationDate);

    List<RiskReport> findByValuationDateAndReportName(LocalDate valuationDate, String reportName);

    Page<RiskReport> findByReportNameAndValuationDate(String reportName, LocalDate valuationDate, Pageable pageable);

    Page<RiskReport> findByReportNameAndValuationDateAndBookNameIn(String reportName, LocalDate valuationDate,
                                                                 List<String> bookIds, Pageable pageable);

    Optional<RiskReport> findByBookNameAndReportNameAndUnderlyerInstrumentIdAndValuationDate(String bookName,
                                                                                             String reportName,
                                                                                             String instrumentId,
                                                                                             LocalDate valuationDate);

    void deleteByBookNameAndReportNameAndUnderlyerInstrumentIdAndValuationDate(String bookName, String reportName,
                                                                               String instrumentId, LocalDate valuationDate);

    void deleteAllByReportNameAndValuationDate(String reportName,LocalDate valuationDate);
}
