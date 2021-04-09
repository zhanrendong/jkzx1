package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.report.dao.client.dbo.SpotScenariosReport;
import tech.tongyu.bct.report.dto.DataRangeEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SpotScenariosReportRepo extends JpaRepository<SpotScenariosReport, UUID> {
    @Query("select distinct r.reportName from SpotScenariosReport r")
    List<String> findAllReportName();

    void deleteByValuationDateAndInstrumentIdAndReportTypeAndContentName(
            LocalDate valuationDate, String instrumentId, DataRangeEnum reportType, String contentName);

    List<SpotScenariosReport> findAllByValuationDateAndReportTypeAndContentNameAndInstrumentId(
            LocalDate valuationDate, DataRangeEnum reportType, String contentName, String instrumentId);

    void deleteAllByValuationDate(LocalDate valuationDate);
}
