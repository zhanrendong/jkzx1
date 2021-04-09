package tech.tongyu.bct.report.dao.client.dbo;

import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 利润统计表
 */
@Entity
@Table(schema = EodReportService.SCHEMA)
public class ProfitStatisicsReport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;//主键
    /**
     * 个股持仓盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal stockHoldPnl;
    /**
     * 商品持仓盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal commodityHoldPnl;
    /**
     * 奇异持仓盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal exoticHoldPnl;
    /**
     * 个股今日了结盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal stockTodayToEndPnl;
    /**
     * 个股历史了结盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal stockHistroyToEndPnl;
    /**
     * 商品今日了结盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal commodityTodayToEndPnl;
    /**
     * 商品历史了结盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal commodityHistroyToEndPnl;
    /**
     * 奇异今日了结盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal exoticTodayToEndPnl;
    /**
     * 奇异今日了结盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal exoticHistroyToEndPnl;
    /**
     * 个股了结盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal stockToEndAllPnl;
    /**
     * 商品了结盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal commodityToEndAllPnl;
    /**
     * 奇异了结盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal exoticToEndAllPnl;
    /**
     * 个股总盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal stockPnl;
    /**
     * 商品总盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal commodityPnl;
    /**
     * 奇异总盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal exoticPnl;
    @Column(precision=19,scale=4)
    private BigDecimal optionTodayToEndPnl;
    /**
     * 期权历史了结盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal optionHistroyToEndPnl;
    /**
     * 期权了结盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal optionToEndAllPnl;
    /**
     * 期权持仓盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal optionHoldPnl;
    /**
     * 期权总盈亏
     */
    @Column(precision=19,scale=4)
    private BigDecimal optionPnl;
    @Column
    private String reportName;
    @Column
    private LocalDate valuationDate;
    @Column
    private Instant createdAt;

    public BigDecimal getStockHoldPnl() {
        return stockHoldPnl;
    }

    public void setStockHoldPnl(BigDecimal stockHoldPnl) {
        this.stockHoldPnl = stockHoldPnl;
    }

    public BigDecimal getCommodityHoldPnl() {
        return commodityHoldPnl;
    }

    public void setCommodityHoldPnl(BigDecimal commodityHoldPnl) {
        this.commodityHoldPnl = commodityHoldPnl;
    }

    public BigDecimal getExoticHoldPnl() {
        return exoticHoldPnl;
    }

    public void setExoticHoldPnl(BigDecimal exoticHoldPnl) {
        this.exoticHoldPnl = exoticHoldPnl;
    }

    public BigDecimal getStockTodayToEndPnl() {
        return stockTodayToEndPnl;
    }

    public void setStockTodayToEndPnl(BigDecimal stockTodayToEndPnl) {
        this.stockTodayToEndPnl = stockTodayToEndPnl;
    }

    public BigDecimal getStockHistroyToEndPnl() {
        return stockHistroyToEndPnl;
    }

    public void setStockHistroyToEndPnl(BigDecimal stockHistroyToEndPnl) {
        this.stockHistroyToEndPnl = stockHistroyToEndPnl;
    }

    public BigDecimal getCommodityTodayToEndPnl() {
        return commodityTodayToEndPnl;
    }

    public void setCommodityTodayToEndPnl(BigDecimal commodityTodayToEndPnl) {
        this.commodityTodayToEndPnl = commodityTodayToEndPnl;
    }

    public BigDecimal getCommodityHistroyToEndPnl() {
        return commodityHistroyToEndPnl;
    }

    public void setCommodityHistroyToEndPnl(BigDecimal commodityHistroyToEndPnl) {
        this.commodityHistroyToEndPnl = commodityHistroyToEndPnl;
    }

    public BigDecimal getExoticTodayToEndPnl() {
        return exoticTodayToEndPnl;
    }

    public void setExoticTodayToEndPnl(BigDecimal exoticTodayToEndPnl) {
        this.exoticTodayToEndPnl = exoticTodayToEndPnl;
    }

    public BigDecimal getExoticHistroyToEndPnl() {
        return exoticHistroyToEndPnl;
    }

    public void setExoticHistroyToEndPnl(BigDecimal exoticHistroyToEndPnl) {
        this.exoticHistroyToEndPnl = exoticHistroyToEndPnl;
    }

    public BigDecimal getStockToEndAllPnl() {
        return stockToEndAllPnl;
    }

    public void setStockToEndAllPnl(BigDecimal stockToEndAllPnl) {
        this.stockToEndAllPnl = stockToEndAllPnl;
    }

    public BigDecimal getCommodityToEndAllPnl() {
        return commodityToEndAllPnl;
    }

    public void setCommodityToEndAllPnl(BigDecimal commodityToEndAllPnl) {
        this.commodityToEndAllPnl = commodityToEndAllPnl;
    }

    public BigDecimal getExoticToEndAllPnl() {
        return exoticToEndAllPnl;
    }

    public void setExoticToEndAllPnl(BigDecimal exoticToEndAllPnl) {
        this.exoticToEndAllPnl = exoticToEndAllPnl;
    }

    public BigDecimal getStockPnl() {
        return stockPnl;
    }

    public void setStockPnl(BigDecimal stockPnl) {
        this.stockPnl = stockPnl;
    }

    public BigDecimal getCommodityPnl() {
        return commodityPnl;
    }

    public void setCommodityPnl(BigDecimal commodityPnl) {
        this.commodityPnl = commodityPnl;
    }

    public BigDecimal getExoticPnl() {
        return exoticPnl;
    }

    public void setExoticPnl(BigDecimal exoticPnl) {
        this.exoticPnl = exoticPnl;
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

    public BigDecimal getOptionTodayToEndPnl() {
        return optionTodayToEndPnl;
    }

    public void setOptionTodayToEndPnl(BigDecimal optionTodayToEndPnl) {
        this.optionTodayToEndPnl = optionTodayToEndPnl;
    }

    public BigDecimal getOptionHistroyToEndPnl() {
        return optionHistroyToEndPnl;
    }

    public void setOptionHistroyToEndPnl(BigDecimal optionHistroyToEndPnl) {
        this.optionHistroyToEndPnl = optionHistroyToEndPnl;
    }

    public BigDecimal getOptionToEndAllPnl() {
        return optionToEndAllPnl;
    }

    public void setOptionToEndAllPnl(BigDecimal optionToEndAllPnl) {
        this.optionToEndAllPnl = optionToEndAllPnl;
    }

    public BigDecimal getOptionHoldPnl() {
        return optionHoldPnl;
    }

    public void setOptionHoldPnl(BigDecimal optionHoldPnl) {
        this.optionHoldPnl = optionHoldPnl;
    }

    public BigDecimal getOptionPnl() {
        return optionPnl;
    }

    public void setOptionPnl(BigDecimal optionPnl) {
        this.optionPnl = optionPnl;
    }
}
