package tech.tongyu.bct.report.dao.client.dbo;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.common.jpa.JsonConverter;
import tech.tongyu.bct.report.dto.ReportTypeEnum;
import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table( schema = EodReportService.SCHEMA,
        indexes = {@Index(name = "custom_report_index", columnList = "valuationDate, reportType, reportName")})
public class CustomReport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    @Column
    private String bookName;
    @Column
    private String reportName;
    @Column
    private String resourceName;
    @Convert(converter = JsonConverter.class)
    @Column(nullable = false, length = JsonConverter.LENGTH)
    private JsonNode reportData;
    @Column
    @Enumerated(EnumType.STRING)
    private ResourceTypeEnum resourceType;
    @Column
    @Enumerated(EnumType.STRING)
    private ReportTypeEnum reportType;
    @Column
    private LocalDate valuationDate;
    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    public CustomReport() {

    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
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

    public ResourceTypeEnum getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceTypeEnum resourceType) {
        this.resourceType = resourceType;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
