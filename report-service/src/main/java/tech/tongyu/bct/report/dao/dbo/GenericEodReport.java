package tech.tongyu.bct.report.dao.dbo;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.common.jpa.HasUuid;
import tech.tongyu.bct.common.jpa.JsonConverter;
import tech.tongyu.bct.report.dto.ReportTypeEnum;
import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static tech.tongyu.bct.report.service.EodReportService.DEFAULT_ZONE;

/**
 * 这个Entity代表了Report中的一条记录
 */
@Entity
@Table(
        schema = EodReportService.SCHEMA,
        indexes = {@Index(name = "report_index", columnList = "valuationDate,reportName")})
public class GenericEodReport implements HasUuid {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false)
    private LocalDate valuationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTypeEnum reportType;

    @Column(nullable = false)
    private String reportName;

    @Convert(converter = JsonConverter.class)
    @Column(length = JsonConverter.LENGTH)
    private JsonNode reportData;

    @Column(nullable = false)
    private String modifiedBy;

    @CreationTimestamp
    @Column
    private Instant createdAt;

    @UpdateTimestamp
    @Column
    private Instant updatedAt;

    public GenericEodReport(){}

    public GenericEodReport(String reportName, ReportTypeEnum reportType, LocalDate valuationDate, String modifiedBy, JsonNode reportData) {
        this.valuationDate = valuationDate;
        this.reportType = reportType;
        this.reportName = reportName;
        this.reportData = reportData;
        this.modifiedBy = modifiedBy;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public ReportTypeEnum getReportType() {
        return reportType;
    }

    public String getReportName() {
        return reportName;
    }

    public JsonNode getReportData() {

        return reportData;
    }

    public LocalDateTime getCreatedAt() {
        if (createdAt == null) return LocalDateTime.now();
        else return LocalDateTime.ofInstant(createdAt, DEFAULT_ZONE);
    }

    public LocalDateTime getUpdatedAt() {
        if (updatedAt == null) return LocalDateTime.now();
        else return LocalDateTime.ofInstant(updatedAt, DEFAULT_ZONE);
    }

    public void setReportData(JsonNode reportData) {
        this.reportData = reportData;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }
}
