package tech.tongyu.bct.report.dao.client.dbo;

import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 风险指标统计表
 */
@Entity
@Table(schema = EodReportService.SCHEMA)
public class StatisticsOfRiskReport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;//主键
    /**
     *个股DELTA
     */
    @Column(precision=19,scale=4)
    private BigDecimal stockDelta;
    /**个股GAMMA
     *
     */
    @Column(precision=19,scale=4)
    private BigDecimal stockGamma;
    /**
     * 个股Vega
     */
    @Column(precision=19,scale=4)
    private BigDecimal stockVega;
    /**
     *商品DELTA
     */
    @Column(precision=19,scale=4)
    private BigDecimal commodityDelta;
    /**
     *商品GAMMA
     */
    @Column(precision=19,scale=4)
    private BigDecimal commodityGamma;
    /**
     *商品Vega
     */
    private BigDecimal commodityVega;
    @Column
    private String reportName;
    @Column
    private LocalDate valuationDate;
    @Column
    private Instant createdAt;

    @Override
    public String toString() {
        return "StatisticsOfRiskReport{" +
                "uuid=" + uuid +
                ", stockDelta=" + stockDelta +
                ", stockGamma=" + stockGamma +
                ", stockVega=" + stockVega +
                ", commodityDelta=" + commodityDelta +
                ", commodityGamma=" + commodityGamma +
                ", commodityVega=" + commodityVega +
                ", reportName='" + reportName + '\'' +
                ", valuationDate=" + valuationDate +
                ", createdAt=" + createdAt +
                '}';
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public BigDecimal getStockDelta() {
        return stockDelta;
    }

    public void setStockDelta(BigDecimal stockDelta) {
        this.stockDelta = stockDelta;
    }

    public BigDecimal getStockGamma() {
        return stockGamma;
    }

    public void setStockGamma(BigDecimal stockGamma) {
        this.stockGamma = stockGamma;
    }

    public BigDecimal getStockVega() {
        return stockVega;
    }

    public void setStockVega(BigDecimal stockVega) {
        this.stockVega = stockVega;
    }

    public BigDecimal getCommodityDelta() {
        return commodityDelta;
    }

    public void setCommodityDelta(BigDecimal commodityDelta) {
        this.commodityDelta = commodityDelta;
    }

    public BigDecimal getCommodityGamma() {
        return commodityGamma;
    }

    public void setCommodityGamma(BigDecimal commodityGamma) {
        this.commodityGamma = commodityGamma;
    }

    public BigDecimal getCommodityVega() {
        return commodityVega;
    }

    public void setCommodityVega(BigDecimal commodityVega) {
        this.commodityVega = commodityVega;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
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
