package tech.tongyu.bct.report.service;

import tech.tongyu.bct.report.dto.GenericEodReportRowDTO;
import tech.tongyu.bct.report.dto.ReportTypeEnum;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EodReportService {
    String SCHEMA = "reportService";
    ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    void createReport(String reportName, ReportTypeEnum reportType, LocalDate valuationDate, String modifiedBy,
                      List<Map<String, Object>> reportData);

    void deleteByNameAndTypeAndDate(String reportName, ReportTypeEnum reportType, LocalDate valuationDate);

    List<GenericEodReportRowDTO> getReportByNameAndTypeAndDate(String reportName, ReportTypeEnum reportType,
                                                               LocalDate valuationDate);

    List<GenericEodReportRowDTO> findByValuationDate(LocalDate valuationDate);

    GenericEodReportRowDTO saveReportRow(GenericEodReportRowDTO row);

    void deleteReport(String reportId);
}
