package tech.tongyu.bct.report.client.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 风险指标统计表
 */
public class StatisticsOfRiskReportDTO {
    @BctField(name = "uuid", description = "唯一标识", type = "String")
    private String uuid;//主键
    /**
     * 个股Delta
     */
    @BctField(name = "stockDelta", description = "个股Delta", type = "BigDecimal")
    private BigDecimal stockDelta;
    /**
     *个股GAMMA
     */
    @BctField(name = "stockGamma", description = "个股GAMMA", type = "BigDecimal")
    private BigDecimal stockGamma;
    /**
     * 个股Vega
     */
    @BctField(name = "stockVega", description = "个股Vega", type = "BigDecimal")
    private BigDecimal stockVega;
    /**
     *商品DELTA
     */
    @BctField(name = "commodityDelta", description = "商品DELTA", type = "BigDecimal")
    private BigDecimal commodityDelta;
    /**
     *商品GAMMA
     */
    @BctField(name = "commodityGamma", description = "商品GAMMA", type = "BigDecimal")
    private BigDecimal commodityGamma;
    /**
     *商品Vega
     */
    @BctField(name = "commodityVega", description = "商品Vega", type = "BigDecimal")
    private BigDecimal commodityVega;
    @BctField(name = "reportName", description = "报告名称", type = "String")
    private String reportName;
    @BctField(name = "valuationDate", description = "报告日期", type = "LocalDate")
    private LocalDate valuationDate;
    @BctField(name = "createdAt", description = "计算时间", type = "Instant")
    private Instant createdAt;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
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

    @Override
    public String toString() {
        return "StatisticsOfRiskReportDTO{" +
                "uuid='" + uuid + '\'' +
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
}
