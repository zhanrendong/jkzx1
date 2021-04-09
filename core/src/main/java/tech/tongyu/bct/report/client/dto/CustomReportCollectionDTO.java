package tech.tongyu.bct.report.client.dto;

import com.fasterxml.jackson.databind.JsonNode;
import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.report.dto.ReportTypeEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class CustomReportCollectionDTO {
    @BctField(name = "uuid", description = "唯一标识", type = "String")
    public String uuid;
    @BctField(name = "reportName", description = "报告名称", type = "String")
    public String reportName;
    @BctField(name = "reportData", description = "报告数据", type = "List<JsonNode>")
    public List<JsonNode> reportData;
    @BctField(name = "reportType", description = "报告种类", type = "ReportTypeEnum")
    public ReportTypeEnum reportType;
    @BctField(name = "valuationDate", description = "报告日期", type = "LocalDate")
    public LocalDate valuationDate;
    @BctField(name = "updateTime", description = "更新时间", type = "LocalDateTime")
    public LocalDateTime updateTime;

    public CustomReportCollectionDTO() {
    }

    public CustomReportCollectionDTO(String reportName, ReportTypeEnum reportType, LocalDate valuationDate) {
        this.reportName = reportName;
        this.reportType = reportType;
        this.valuationDate = valuationDate;
    }

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

    public List<JsonNode> getReportData() {
        return reportData;
    }

    public void setReportData(List<JsonNode> reportData) {
        this.reportData = reportData;
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

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CustomReportCollectionDTO){
            CustomReportCollectionDTO customCollection = (CustomReportCollectionDTO) obj;
            return reportName.equals(customCollection.reportName)
                    && reportType.equals(customCollection.reportType)
                    && valuationDate.equals(customCollection.valuationDate);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportName, reportType, valuationDate);
    }
}
