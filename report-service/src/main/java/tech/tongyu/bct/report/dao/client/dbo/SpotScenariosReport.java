package tech.tongyu.bct.report.dao.client.dbo;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.common.jpa.JsonConverter;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.report.dto.DataRangeEnum;
import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = EodReportService.SCHEMA, indexes = @Index(name = "spot_scenarios_name_rpt_index",
        columnList = "valuationDate,reportType,contentName,instrumentId"))
public class SpotScenariosReport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    @Column
    private String reportName;
    @Column
    @Enumerated(EnumType.STRING)
    private DataRangeEnum reportType;
    @Column
    /*
     * when reportType is MARKET, contentName is always null
     * when reportType is SUBSIDIARY, contentName represents subsidiary
     * when reportType is PARTY, contentName represents counter-party
     */
    private String contentName;
    @Column
    private String assetClass;
    @Column
    private String instrumentType;
    @Column
    private String instrumentId;
    @Column
    private LocalDate valuationDate;

    @Convert(converter = JsonConverter.class)
    @Column(nullable = false, length = JsonConverter.LENGTH)
    private JsonNode scenarios;

    @Column
    @CreationTimestamp
    private Instant createdAt;
    @Column
    private String exfsid;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
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

    public String getExfsid() {
        return exfsid;
    }

    public void setExfsid(String exfsid) {
        this.exfsid = exfsid;
    }
}
