package tech.tongyu.bct.report.dto.report;

import com.fasterxml.jackson.databind.JsonNode;
import tech.tongyu.bct.common.api.doc.BctField;
import java.time.LocalDateTime;

public class GenericIntradayReportRowDTO implements HasBookName{
    @BctField(name = "reportName", description = "报告名称", type = "String")
    String reportName;
    @BctField(name = "bookName", description = "交易簿", type = "String")
    String bookName;
    @BctField(name = "createdAt", description = "创建时间", type = "LocalDateTime")
    LocalDateTime createdAt;
    @BctField(name = "modifiedBy", description = "修改人", type = "String")
    String modifiedBy;
    @BctField(name = "reportData", description = "报告数据", type = "JsonNode")
    JsonNode reportData;

    public GenericIntradayReportRowDTO(String reportName, String bookName,
                                       LocalDateTime createdAt, String modifiedBy,
                                       JsonNode reportData) {
        this.bookName = bookName;
        this.reportName = reportName;
        this.createdAt = createdAt;
        this.modifiedBy = modifiedBy;
        this.reportData = reportData;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }


}
