package tech.tongyu.bct.report.dao.client.dbo;

import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.report.dto.DataRangeEnum;
import tech.tongyu.bct.report.dto.report.MarketScenarioType;
import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = EodReportService.SCHEMA,
        indexes = {@Index(name = "market_risk_detail_rpt_index", columnList = "reportName,valuationDate,underlyerInstrumentId")})
public class MarketRiskDetailReport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    @Column
    private String reportName;
    @Column
    @Enumerated(EnumType.STRING)
    private MarketScenarioType scenarioType;
    @Column
    @Enumerated(EnumType.STRING)
    private DataRangeEnum reportType;
    @Column
    private String underlyerInstrumentId;
    @Column
    private String subsidiary;
    @Column
    private String scenarioName;
    @Column
    private String partyName;
    @Column(precision = 19, scale = 4)
    private BigDecimal delta;
    @Column(precision = 19, scale = 4)
    private BigDecimal deltaCash;
    @Column(precision = 19, scale = 4)
    private BigDecimal gamma;
    @Column(precision = 19, scale = 4)
    private BigDecimal gammaCash;
    @Column(precision = 19, scale = 4)
    private BigDecimal vega;
    @Column(precision = 19, scale = 4)
    private BigDecimal theta;
    @Column(precision = 19, scale = 4)
    private BigDecimal rho;
    @Column(precision = 19, scale = 4)
    private BigDecimal pnlChange;
    @Column
    private LocalDate valuationDate;
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

    public String getUnderlyerInstrumentId() {
        return underlyerInstrumentId;
    }

    public void setUnderlyerInstrumentId(String underlyerInstrumentId) {
        this.underlyerInstrumentId = underlyerInstrumentId;
    }

    public BigDecimal getDelta() {
        return delta;
    }

    public void setDelta(BigDecimal delta) {
        this.delta = delta;
    }

    public BigDecimal getDeltaCash() {
        return deltaCash;
    }

    public void setDeltaCash(BigDecimal deltaCash) {
        this.deltaCash = deltaCash;
    }

    public BigDecimal getGamma() {
        return gamma;
    }

    public void setGamma(BigDecimal gamma) {
        this.gamma = gamma;
    }

    public BigDecimal getGammaCash() {
        return gammaCash;
    }

    public void setGammaCash(BigDecimal gammaCash) {
        this.gammaCash = gammaCash;
    }

    public BigDecimal getVega() {
        return vega;
    }

    public void setVega(BigDecimal vega) {
        this.vega = vega;
    }

    public BigDecimal getTheta() {
        return theta;
    }

    public void setTheta(BigDecimal theta) {
        this.theta = theta;
    }

    public BigDecimal getRho() {
        return rho;
    }

    public void setRho(BigDecimal rho) {
        this.rho = rho;
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

    public MarketScenarioType getScenarioType() {
        return scenarioType;
    }

    public void setScenarioType(MarketScenarioType scenarioType) {
        this.scenarioType = scenarioType;
    }

    public BigDecimal getPnlChange() {
        return pnlChange;
    }

    public void setPnlChange(BigDecimal pnlChange) {
        this.pnlChange = pnlChange;
    }

    public DataRangeEnum getReportType() {
        return reportType;
    }

    public void setReportType(DataRangeEnum reportType) {
        this.reportType = reportType;
    }

    public String getSubsidiary() {
        return subsidiary;
    }

    public void setSubsidiary(String subsidiary) {
        this.subsidiary = subsidiary;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public String getExfsid() {
        return exfsid;
    }

    public void setExfsid(String exfsid) {
        this.exfsid = exfsid;
    }
}
