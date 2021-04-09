package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.report.dao.client.dbo.PositionReport;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PositionReportRepo extends JpaRepository<PositionReport, UUID> {

    @Query("select distinct r.reportName from PositionReport r")
    List<String> findAllReportName();

    List<PositionReport> findByValuationDate(LocalDate valuationDate);

    List<PositionReport> findByValuationDateAndReportName(LocalDate valuationDate, String reportName);

    Page<PositionReport> findByReportNameAndValuationDateAndBookNameIn(String reportName, LocalDate valuationDate,
                                                                       List<String> bookIds, Pageable pageable);
    List<PositionReport> findByReportNameAndValuationDateBeforeAndBookNameInOrderByValuationDateDesc
            (String reportName, LocalDate valuationDate, List<String> bookIds);
    List<PositionReport> findByReportNameAndValuationDateAndBookNameIn
            (String reportName, LocalDate valuationDate, List<String> bookIds);

    void deleteByReportNameAndPositionIdAndValuationDate(String reportName, String position, LocalDate valuationDate);

    void deleteAllByReportNameAndValuationDate(String reportName,LocalDate valuationDate);
}
