package tech.tongyu.bct.report.client.dto;

import tech.tongyu.bct.report.dto.DataRangeEnum;
import tech.tongyu.bct.report.dto.report.MarketScenarioType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 全市场分品种风险报告
 */
public class MarketRiskDetailReportDTO {

    private String uuid;
    private String underlyerInstrumentId;
    private String reportName;
    private MarketScenarioType scenarioType;
    private DataRangeEnum reportType;
    private String subsidiary;
    private String partyName;
    private String scenarioName;
    private BigDecimal delta;
    private BigDecimal deltaCash;
    private BigDecimal gamma;
    private BigDecimal gammaCash;
    private BigDecimal vega;
    private BigDecimal theta;
    private BigDecimal rho;
    private BigDecimal pnlChange;
    private LocalDate valuationDate;
    private Instant createdAt;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUnderlyerInstrumentId() {
        return underlyerInstrumentId;
    }

    public void setUnderlyerInstrumentId(String underlyerInstrumentId) {
        this.underlyerInstrumentId = underlyerInstrumentId;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
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

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
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
}
