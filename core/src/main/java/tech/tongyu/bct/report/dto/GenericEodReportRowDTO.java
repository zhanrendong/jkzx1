package tech.tongyu.bct.report.dto;

import com.fasterxml.jackson.databind.JsonNode;
import tech.tongyu.bct.common.api.doc.BctField;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GenericEodReportRowDTO {
    @BctField(name = "reportName", description = "报告名称", type = "String")
    String reportName;
    @BctField(name = "reportType", description = "报告种类", type = "ReportTypeEnum")
    ReportTypeEnum reportType;
    @BctField(name = "valuationDate", description = "报告日期", type = "LocalDate")
    LocalDate valuationDate;
    @BctField(name = "reportUuid", description = "报告唯一标识", type = "String")
    String reportUuid;
    @BctField(name = "createdAt", description = "创建时间", type = "LocalDateTime")
    LocalDateTime createdAt;
    @BctField(name = "updatedAt", description = "更新时间", type = "LocalDateTime")
    LocalDateTime updatedAt;
    @BctField(name = "modifiedBy", description = "修改人", type = "String")
    String modifiedBy;
    @BctField(name = "reportData", description = "报告数据", type = "JsonNode")
    JsonNode reportData;

    public GenericEodReportRowDTO(String reportUuid, String reportName, ReportTypeEnum reportType, LocalDate valuationDate,
                                  LocalDateTime createdAt, LocalDateTime updatedAt, String modifiedBy, JsonNode reportData) {
        this.reportName = reportName;
        this.reportType = reportType;
        this.valuationDate = valuationDate;
        this.reportUuid = reportUuid;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.modifiedBy = modifiedBy;
        this.reportData = reportData;
    }

    public JsonNode getReportData() {
        return reportData;
    }

    public void setReportData(JsonNode reportData) {
        this.reportData = reportData;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public ReportTypeEnum getReportType() {
        return reportType;
    }

    public void setReportType(ReportTypeEnum reportType) {
        this.reportType = reportType;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
    }

    public String getReportUuid() {
        return reportUuid;
    }

    public void setReportUuid(String reportUuid) {
        this.reportUuid = reportUuid;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }


}
