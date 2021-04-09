package tech.tongyu.bct.report.client.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 对冲端盈亏表
 */
public class HedgePnlReportDTO {
    @BctField(name = "uuid", description = "唯一标识", type = "String")
    private String uuid;//主键
    /**
     *个股对冲盈亏
     */
    @BctField(name = "stockHedgePnl", description = "个股对冲盈亏", type = "BigDecimal")
    private BigDecimal stockHedgePnl;
    /**
     *商品期货对冲盈亏
     */
    @BctField(name = "commodityHedgePnl", description = "商品期货对冲盈亏", type = "BigDecimal")
    private BigDecimal commodityHedgePnl;
    /**
     *场内期权对冲盈亏
     */
    @BctField(name = "floorOptionsHedgePnl", description = "场内期权对冲盈亏", type = "BigDecimal")
    private BigDecimal floorOptionsHedgePnl;
    /**
     *对冲总盈亏
     */
    @BctField(name = "hedgeAllPnl", description = "总对冲盈亏", type = "BigDecimal")
    private BigDecimal hedgeAllPnl;
    @BctField(name = "reportName", description = "报告名称", type = "String")
    private String reportName;
    @BctField(name = "valuationDate", description = "报告日期", type = "LocalDate")
    private LocalDate valuationDate;
    @BctField(name = "createdAt", description = "计算时间", type = "Instant")
    private Instant createdAt;

    @Override
    public String toString() {
        return "HedgePnlReportDTO{" +
                "uuid='" + uuid + '\'' +
                ", stockHedgePnl=" + stockHedgePnl +
                ", commodityHedgePnl=" + commodityHedgePnl +
                ", floorOptionsHedgePnl=" + floorOptionsHedgePnl +
                ", hedgeAllPnl=" + hedgeAllPnl +
                ", reportName='" + reportName + '\'' +
                ", valuationDate=" + valuationDate +
                ", createdAt=" + createdAt +
                '}';
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public BigDecimal getStockHedgePnl() {
        return stockHedgePnl;
    }

    public void setStockHedgePnl(BigDecimal stockHedgePnl) {
        this.stockHedgePnl = stockHedgePnl;
    }

    public BigDecimal getCommodityHedgePnl() {
        return commodityHedgePnl;
    }

    public void setCommodityHedgePnl(BigDecimal commodityHedgePnl) {
        this.commodityHedgePnl = commodityHedgePnl;
    }

    public BigDecimal getFloorOptionsHedgePnl() {
        return floorOptionsHedgePnl;
    }

    public void setFloorOptionsHedgePnl(BigDecimal floorOptionsHedgePnl) {
        this.floorOptionsHedgePnl = floorOptionsHedgePnl;
    }

    public BigDecimal getHedgeAllPnl() {
        return hedgeAllPnl;
    }

    public void setHedgeAllPnl(BigDecimal hedgeAllPnl) {
        this.hedgeAllPnl = hedgeAllPnl;
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
