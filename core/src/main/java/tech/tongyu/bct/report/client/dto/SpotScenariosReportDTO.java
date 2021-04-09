package tech.tongyu.bct.report.client.dto;

import com.fasterxml.jackson.databind.JsonNode;
import tech.tongyu.bct.report.dto.DataRangeEnum;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class SpotScenariosReportDTO {
    private String uuid;
    private String reportName;
    private DataRangeEnum reportType;
    private String contentName;
    private String assetClass;
    private String instrumentType;
    private String instrumentId;
    private LocalDate valuationDate;
    private JsonNode scenarios;
    private Instant createdAt;

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

    public DataRangeEnum getReportType() {
        return reportType;
    }

    public void setReportType(DataRangeEnum reportType) {
        this.reportType = reportType;
    }

    public String getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(String assetClass) {
        this.assetClass = assetClass;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
    }

    public JsonNode getScenarios() {
        return scenarios;
    }

    public void setScenarios(JsonNode scenarios) {
        this.scenarios = scenarios;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getContentName() {
        return contentName;
    }

    public void setContentName(String contentName) {
        this.contentName = contentName;
    }
}
