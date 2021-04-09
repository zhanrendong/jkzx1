package tech.tongyu.bct.report.dao.client.dbo;

import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table( schema = EodReportService.SCHEMA,
        indexes = {@Index(name = "market_risk_rpt_index", columnList = "valuationDate")})
public class MarketRiskReport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    @Column
    private String reportName;
    @Column(precision=19,scale=4)
    private BigDecimal deltaCash;
    @Column(precision=19,scale=4)
    private BigDecimal gammaCash;
    @Column(precision=19,scale=4)
    private BigDecimal vega;
    @Column(precision=19,scale=4)
    private BigDecimal theta;
    @Column(precision=19,scale=4)
    private BigDecimal rho;
    @Column
    private LocalDate valuationDate;
    @Column
    private String pricingEnvironment;
    @Column
    @CreationTimestamp
    private Instant createdAt;

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

    public BigDecimal getDeltaCash() {
        return deltaCash;
    }

    public void setDeltaCash(BigDecimal deltaCash) {
        this.deltaCash = deltaCash;
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

    public String getPricingEnvironment() {
        return pricingEnvironment;
    }

    public void setPricingEnvironment(String pricingEnvironment) {
        this.pricingEnvironment = pricingEnvironment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
