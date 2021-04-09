package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.report.dao.client.dbo.CustomReport;
import tech.tongyu.bct.report.dto.ReportTypeEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CustomReportRepo extends JpaRepository<CustomReport, UUID> {

    @Query("select distinct r.reportName from CustomReport r")
    List<String> findAllReportName();

    void deleteByReportNameAndReportTypeAndValuationDate(String reportName, ReportTypeEnum reportType,
                                                         LocalDate valuationDate);

}
