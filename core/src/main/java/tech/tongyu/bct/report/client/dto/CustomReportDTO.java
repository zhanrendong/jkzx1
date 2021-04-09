package tech.tongyu.bct.report.client.dto;

import com.fasterxml.jackson.databind.JsonNode;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.report.dto.ReportTypeEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomReportDTO {

    private String uuid;

    private String reportName;

    private String resourceName;

    private JsonNode reportData;

    private ResourceTypeEnum resourceType;

    private ReportTypeEnum reportType;

    private LocalDate valuationDate;

    private LocalDateTime createdAt;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public JsonNode getReportData() {
        return reportData;
    }

    public void setReportData(JsonNode reportData) {
        this.reportData = reportData;
    }

    public ReportTypeEnum getReportType() {
        return reportType;
    }

    public void setReportType(ReportTypeEnum reportType) {
        this.reportType = reportType;
    }

    public ResourceTypeEnum getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceTypeEnum resourceType) {
        this.resourceType = resourceType;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
