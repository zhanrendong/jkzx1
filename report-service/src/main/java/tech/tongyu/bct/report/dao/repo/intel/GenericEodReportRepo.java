package tech.tongyu.bct.report.dao.repo.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.tongyu.bct.report.dao.dbo.GenericEodReport;
import tech.tongyu.bct.report.dto.ReportTypeEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface GenericEodReportRepo extends JpaRepository<GenericEodReport, UUID> {

    void deleteAllByReportNameAndReportTypeAndValuationDate(
            String reportName, ReportTypeEnum reportType, LocalDate valuationDate);

    Boolean existsByValuationDate(LocalDate valuationDate);

    Boolean existsByReportNameAndReportTypeAndValuationDate(
            String reportName, ReportTypeEnum reportType, LocalDate valuationDate);


    List<GenericEodReport> findAllByValuationDate(LocalDate valuationDate);

    List<GenericEodReport> findAllByReportNameAndReportTypeAndValuationDate(
            String reportName, ReportTypeEnum reportType, LocalDate valuationDate);

}
