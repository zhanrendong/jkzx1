package tech.tongyu.bct.report.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.report.dao.dbo.GenericEodReport;
import tech.tongyu.bct.report.dao.repo.intel.GenericEodReportRepo;
import tech.tongyu.bct.report.dto.GenericEodReportRowDTO;
import tech.tongyu.bct.report.dto.ReportTypeEnum;
import tech.tongyu.bct.report.dto.report.PnlReportRowDTO;
import tech.tongyu.bct.report.dto.report.PositionReportRowDTO;
import tech.tongyu.bct.report.dto.report.RiskReportRowDTO;
import tech.tongyu.bct.report.service.EodReportService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class EodReportServiceImpl implements EodReportService {
    GenericEodReportRepo eodReportRepo;

    @Autowired
    public EodReportServiceImpl(GenericEodReportRepo eodReportRepo) {
        this.eodReportRepo = eodReportRepo;
    }

    @Override
    public void createReport(String reportName, ReportTypeEnum reportType, LocalDate valuationDate, String modifiedBy,
                             List<Map<String, Object>> reportData) {
        Boolean reportExists = eodReportRepo.existsByReportNameAndReportTypeAndValuationDate(
                reportName, reportType, valuationDate);
        if (reportExists) {
            throw new IllegalStateException(
                    String.format("报告%s,%s,%s已经存在", reportName, reportType, valuationDate));
        } else {
            List<GenericEodReport> rows = reportData.stream()
                    .map(data -> new GenericEodReport(
                            reportName, reportType, valuationDate, modifiedBy, reportRowToJson(reportType, data)))
                    .collect(Collectors.toList());
            eodReportRepo.saveAll(rows);
        }
    }

    @Override
    @Transactional
    public void deleteByNameAndTypeAndDate(String reportName, ReportTypeEnum reportType, LocalDate valuationDate) {
        Boolean reportExists = eodReportRepo.existsByReportNameAndReportTypeAndValuationDate(
                reportName, reportType, valuationDate);
        if (!reportExists) {
            throw new IllegalStateException(
                    String.format("报告名称:%s,报告类型:%s,计算日期:%s---不存在", reportName, reportType, valuationDate));
        }
        eodReportRepo.deleteAllByReportNameAndReportTypeAndValuationDate(reportName, reportType, valuationDate);
    }

    @Override
    public List<GenericEodReportRowDTO> findByValuationDate(LocalDate valuationDate) {
        Boolean reportExists = eodReportRepo.existsByValuationDate(valuationDate);
        if (!reportExists) {
            throw new IllegalStateException(
                    String.format("计算日期:%s---报告不存在", valuationDate));
        }
        List<GenericEodReport> reports = eodReportRepo.findAllByValuationDate(valuationDate);
        return reports.stream()
                .map(r -> new GenericEodReportRowDTO(
                        r.getUuid().toString(),r.getReportName(),r.getReportType(),r.getValuationDate(),
                        r.getCreatedAt(), r.getUpdatedAt(), r.getModifiedBy(),r.getReportData()))
                .collect(Collectors.toList());
    }

    @Override
    public List<GenericEodReportRowDTO> getReportByNameAndTypeAndDate(String reportName, ReportTypeEnum reportType,
                                                                      LocalDate valuationDate) {
        List<GenericEodReport> rows = eodReportRepo.findAllByReportNameAndReportTypeAndValuationDate(reportName, reportType, valuationDate);
        return rows.stream()
                .map(r-> new GenericEodReportRowDTO(
                        r.getUuid().toString(),r.getReportName(),r.getReportType(),r.getValuationDate(),
                        r.getCreatedAt(), r.getUpdatedAt(), r.getModifiedBy(),r.getReportData()))
                .collect(Collectors.toList());
    }

    @Override
    public GenericEodReportRowDTO saveReportRow(GenericEodReportRowDTO row) {
        if (row.getReportUuid() == null) throw new IllegalArgumentException("缺少待更新报告记录的ID");
        return eodReportRepo.findById(UUID.fromString(row.getReportUuid())).map(oldRow -> {
            if (row.getModifiedBy() != null) {
                oldRow.setReportData(row.getReportData());
                eodReportRepo.save(oldRow);
                return row;
            } else {
                throw new IllegalArgumentException("未提供修改报告记录的用户ID");
            }
        }).orElseThrow(() -> new IllegalArgumentException(String.format("无法找到ID为%s的报告记录", row.getReportUuid())));
    }

    @Override
    public void deleteReport(String reportId) {
        if (StringUtils.isBlank(reportId)) throw new IllegalArgumentException("缺少待删除报告记录的ID");
        UUID uuid = UUID.fromString(reportId);
        eodReportRepo.findById(uuid).
                orElseThrow(() -> new IllegalArgumentException(String.format("无法找到ID为%s的报告记录", reportId)));
        eodReportRepo.deleteById(uuid);
    }


    private JsonNode reportRowToJson(ReportTypeEnum reportType, Map<String, Object> data) {
        Object row = null;
        switch (reportType) {
            case LIVE_POSITION_INFO:
                row = JsonUtils.mapper.convertValue(data, PositionReportRowDTO.class);
                break;
            case RISK:
                row = JsonUtils.mapper.convertValue(data, RiskReportRowDTO.class);
                break;
            case PNL:
                row = JsonUtils.mapper.convertValue(data, PnlReportRowDTO.class);
                break;
        }
        return JsonUtils.mapper.valueToTree(row);
    }
}
