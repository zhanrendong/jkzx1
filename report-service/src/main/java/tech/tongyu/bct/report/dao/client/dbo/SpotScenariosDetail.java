package tech.tongyu.bct.report.dao.client.dbo;

import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


public class SpotScenariosDetail {

    private UUID uuid;
    private String scenarioId;
    private BigDecimal pnlChange;
    private BigDecimal delta;
    private BigDecimal deltaCash;
    private BigDecimal gamma;
    private BigDecimal gammaCash;
    private BigDecimal vega;
    private BigDecimal theta;
    private BigDecimal rhoR;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }

    public BigDecimal getPnlChange() {
        return pnlChange;
    }

    public void setPnlChange(BigDecimal pnlChange) {
        this.pnlChange = pnlChange;
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

    public BigDecimal getRhoR() {
        return rhoR;
    }

    public void setRhoR(BigDecimal rhoR) {
        this.rhoR = rhoR;
    }
}
