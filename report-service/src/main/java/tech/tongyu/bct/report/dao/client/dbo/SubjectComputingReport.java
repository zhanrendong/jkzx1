package tech.tongyu.bct.report.dao.client.dbo;

import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 资产统计表
 */
@Entity
@Table( schema = EodReportService.SCHEMA)
public class SubjectComputingReport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;//主键
    /**一般商品期权对应的名义本金*/
    @Column(precision=19,scale=4)
    private BigDecimal commodity;
    /**商品远期对应的名义本金*/
    @Column(precision=19,scale=4)
    private BigDecimal commodityForward;
    /**商品期权标的总资产（名义本金）*/
    @Column(precision=19,scale=4)
    private BigDecimal commodityAll;
    /**一般个股期权标对应的名义本金*/
    @Column(precision=19,scale=4)
    private BigDecimal stock;
    /**互换对应的名义本金*/
    @Column(precision=19,scale=4)
    private BigDecimal stockSwap;
    /**个股期权标的的总资产*/
    @Column(precision=19,scale=4)
    private BigDecimal stockAll;
    /**奇异期权标的总资产（名义本金）*/
    @Column(precision=19,scale=4)
    private BigDecimal exotic;
    /**按净持仓的名义本金*/
    @Column(precision=19,scale=4)
    private BigDecimal netPosition;
    /**按绝对持仓的名本金*/
    @Column(precision=19,scale=4)
    private BigDecimal absolutePosition;
    /**个股对冲的名义本金*/
    @Column(precision=19,scale=4)
    private BigDecimal hedge;
    @Column
    private String reportName;
    @Column
    private LocalDate valuationDate;
    @Column
    private Instant createdAt;
    @Override
    public String toString() {
        return "SubjectComputingReportRepo{" +
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

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
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
