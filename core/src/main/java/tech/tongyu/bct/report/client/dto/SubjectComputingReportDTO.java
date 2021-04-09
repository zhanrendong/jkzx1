package tech.tongyu.bct.report.client.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 资产统计表
 */
public class SubjectComputingReportDTO {
    @BctField(name = "uuid", description = "唯一标识", type = "String")
    private String uuid;//主键
    /**一般商品期权对应的名义本金*/
    @BctField(name = "commodity", description = "一般商品期权对应的名义本金", type = "BigDecimal")
    private BigDecimal commodity;
    /**商品远期对应的名义本金*/
    @BctField(name = "commodityForward", description = "商品远期对应的名义本金", type = "BigDecimal")
    private BigDecimal commodityForward;
    /**商品期权标的总资产（名义本金）*/
    @BctField(name = "commodityAll", description = "商品期权标的总资产", type = "BigDecimal")
    private BigDecimal commodityAll;
    /**一般个股期权标对应的名义本金*/
    @BctField(name = "stock", description = "一般个股期权标对应的名义本金", type = "BigDecimal")
    private BigDecimal stock;
    /**互换对应的名义本金*/
    @BctField(name = "stockSwap", description = "互换对应的名义本金", type = "BigDecimal")
    private BigDecimal stockSwap;
    /**个股期权标的的总资产*/
    @BctField(name = "stockAll", description = "个股期权标的的总资产", type = "BigDecimal")
    private BigDecimal stockAll;
    /**奇异期权标的总资产（名义本金）*/
    @BctField(name = "exotic", description = "奇异期权标的总资产", type = "BigDecimal")
    private BigDecimal exotic;
    /**按净持仓的名义本金*/
    @BctField(name = "netPosition", description = "按净持仓的名义本金", type = "BigDecimal")
    private BigDecimal netPosition;
    /**按绝对持仓的名本金*/
    @BctField(name = "absolutePosition", description = "按绝对持仓的名本金", type = "BigDecimal")
    private BigDecimal absolutePosition;
    /**个股对冲的名义本金*/
    @BctField(name = "hedge", description = "个股对冲的名义本金", type = "BigDecimal")
    private BigDecimal hedge;
    @BctField(name = "reportName", description = "报告名称", type = "String")
    private String reportName;
    @BctField(name = "valuationDate", description = "报告日期", type = "LocalDate")
    private LocalDate valuationDate;
    @BctField(name = "createdAt", description = "计算时间", type = "Instant")
    private Instant createdAt;
    @Override
    public String toString() {
        return "SubjectComputingReportDTO{" +
                "commodity=" + commodity +
                ", commodityForward=" + commodityForward +
                ", commodityAll=" + commodityAll +
                ", stock=" + stock +
                ", stockSwap=" + stockSwap +
                ", stockAll=" + stockAll +
                ", exotic=" + exotic +
                ", netPosition=" + netPosition +
                ", absolutePosition=" + absolutePosition +
                ", hedge=" + hedge +
                '}';
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public BigDecimal getCommodity() {
        return commodity;
    }

    public void setCommodity(BigDecimal commodity) {
        this.commodity = commodity;
    }

    public BigDecimal getCommodityForward() {
        return commodityForward;
    }

    public void setCommodityForward(BigDecimal commodityForward) {
        this.commodityForward = commodityForward;
    }

    public BigDecimal getCommodityAll() {
        return commodityAll;
    }

    public void setCommodityAll(BigDecimal commodityAll) {
        this.commodityAll = commodityAll;
    }

    public BigDecimal getStock() {
        return stock;
    }

    public void setStock(BigDecimal stock) {
        this.stock = stock;
    }

    public BigDecimal getStockSwap() {
        return stockSwap;
    }

    public void setStockSwap(BigDecimal stockSwap) {
        this.stockSwap = stockSwap;
    }

    public BigDecimal getStockAll() {
        return stockAll;
    }

    public void setStockAll(BigDecimal stockAll) {
        this.stockAll = stockAll;
    }

    public BigDecimal getExotic() {
        return exotic;
    }

    public void setExotic(BigDecimal exotic) {
        this.exotic = exotic;
    }

    public BigDecimal getNetPosition() {
        return netPosition;
    }

    public void setNetPosition(BigDecimal netPosition) {
        this.netPosition = netPosition;
    }

    public BigDecimal getAbsolutePosition() {
        return absolutePosition;
    }

    public void setAbsolutePosition(BigDecimal absolutePosition) {
        this.absolutePosition = absolutePosition;
    }

    public BigDecimal getHedge() {
        return hedge;
    }

    public void setHedge(BigDecimal hedge) {
        this.hedge = hedge;
    }
}
