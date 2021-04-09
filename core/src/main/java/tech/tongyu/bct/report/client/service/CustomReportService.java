package tech.tongyu.bct.report.client.service;

import tech.tongyu.bct.report.client.dto.CustomReportDTO;

import java.time.LocalDate;
import java.util.List;

public interface CustomReportService {

    List<String> listReportName();

    void save(CustomReportDTO customReportDto);

    void deleteAndSaveBatch(List<CustomReportDTO> customReports);

    List<CustomReportDTO> search(CustomReportDTO searchReportDto, LocalDate startDate, LocalDate endDate);

}
