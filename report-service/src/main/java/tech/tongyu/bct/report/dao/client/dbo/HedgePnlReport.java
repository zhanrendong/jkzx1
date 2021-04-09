package tech.tongyu.bct.report.dao.client.dbo;

import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 对冲端盈亏表
 */
@Entity
@Table(schema = EodReportService.SCHEMA)
public class HedgePnlReport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;//主键
    /**
     *个股对冲盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal stockHedgePnl;
    /**
     *商品期货对冲盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal commodityHedgePnl;
    /**
     *场内期权对冲盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal floorOptionsHedgePnl;
    /**
     *对冲总盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal hedgeAllPnl;
    @Column
    private String reportName;
    @Column
    private LocalDate valuationDate;
    @Column
    private Instant createdAt;

    @Override
    public String toString() {
        return "HedgePnlReport{" +
                "uuid=" + uuid +
                ", stockHedgePnl=" + stockHedgePnl +
                ", commodityHedgePnl=" + commodityHedgePnl +
                ", floorOptionsHedgePnl=" + floorOptionsHedgePnl +
                ", hedgeAllPnl=" + hedgeAllPnl +
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
