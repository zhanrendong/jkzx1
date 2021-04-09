package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.report.dao.client.dbo.PnlHstReport;

import java.time.LocalDate;
import java.util.*;

public interface PnlHstReportRepo extends JpaRepository<PnlHstReport, UUID> {

    @Query("select distinct r.reportName from PnlHstReport r")
    List<String> findAllReportName();

    List<PnlHstReport> findByValuationDate(LocalDate valuationDate);

    List<PnlHstReport> findByValuationDateAndReportName(LocalDate valuationDate, String reportName);

    Page<PnlHstReport> findByReportNameAndValuationDateAndBookNameIn(String reportName, LocalDate valuationDate,
                                                                     List<String> bookIds, Pageable pageable);

    List<PnlHstReport> findByReportNameAndValuationDateBeforeAndBookNameInOrderByValuationDateDesc
            (String reportName, LocalDate valuationDate, List<String> bookIds);
    List<PnlHstReport> findByReportNameAndValuationDateAndBookNameIn
            (String reportName, LocalDate valuationDate, List<String> bookIds);

    Optional<PnlHstReport> findByBookNameAndReportNameAndUnderlyerInstrumentIdAndValuationDate(String bookName,
                                                                                               String reportName,
                                                                                               String instrumentId,
                                                                                               LocalDate valuationDate);

    void deleteByBookNameAndReportNameAndUnderlyerInstrumentIdAndValuationDate(String bookName, String reportName,
                                                                               String instrumentId, LocalDate valuationDate);

    void deleteAllByReportNameAndValuationDate(String reportName,LocalDate valuationDate);
}

