package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.report.dao.client.dbo.SubjectComputingReport;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SubjectComputingReportRepo extends JpaRepository<SubjectComputingReport, UUID> {
    @Query("select distinct f.reportName from SubjectComputingReport f")
    List<String> findAllReportName();
    List<SubjectComputingReport> findByValuationDate(LocalDate valuationDate);

    List<SubjectComputingReport> findByValuationDateAndReportName(LocalDate valuationDate, String reportName);

    Page<SubjectComputingReport> findByReportNameAndValuationDateIn(String reportName, LocalDate valuationDate, Pageable pageable);

    void deleteAllByReportNameAndValuationDate(String reportNameLocalDate,LocalDate valuationDate);
}
