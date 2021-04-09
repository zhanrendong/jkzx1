package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.report.dao.client.dbo.PnlReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PnlReportRepo extends JpaRepository<PnlReport, UUID> {

    @Query("select distinct r.reportName from PnlReport r")
    List<String>  findAllReportName();

    List<PnlReport> findByValuationDate(LocalDate valuationDate);

    List<PnlReport> findByValuationDateAndReportName(LocalDate valuationDate, String reportName);

    Page<PnlReport> findAllByReportNameAndValuationDate(String reportName, LocalDate valuationDate, Pageable pageable);

    Page<PnlReport> findByReportNameAndValuationDateAndBookNameIn(String reportName, LocalDate valuationDate,
                                                                  List<String> bookIds, Pageable pageable);

    Optional<PnlReport> findByBookNameAndReportNameAndUnderlyerInstrumentIdAndValuationDate(String bookName,
                                                                                            String reportName,
                                                                                            String instrumentId,
                                                                                            LocalDate valuationDate);

    void deleteByBookNameAndReportNameAndUnderlyerInstrumentIdAndValuationDate(String bookName, String reportName,
                                                                               String instrumentId, LocalDate valuationDate);

    void deleteAllByReportNameAndValuationDate(String reportName,LocalDate valuationDate);
}
