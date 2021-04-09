package tech.tongyu.bct.report.client.dto;

import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.report.dto.report.HasBookName;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class PnlHstReportDTO implements HasBookName {
    @BctField(name = "uuid", description = "唯一标识", type = "String")
    private String uuid;
    @BctField(name = "reportName", description = "报告名称", type = "String")
    private String reportName;
    @BctField(name = "bookName", description = "交易簿", type = "String")
    private String bookName;
    @BctField(name = "underlyerInstrumentId", description = "合约标的", type = "String")
    private String underlyerInstrumentId;
    @BctField(name = "underlyerMarketValue", description = "标的物市值", type = "BigDecimal")
    private BigDecimal underlyerMarketValue;
    @BctField(name = "underlyerNetPosition", description = "标的物持仓 (手)", type = "BigDecimal")
    private BigDecimal underlyerNetPosition;
    @BctField(name = "underlyerSellAmount", description = "标的物卖出金额", type = "BigDecimal")
    private BigDecimal underlyerSellAmount;
    @BctField(name = "underlyerBuyAmount", description = "标的物买入金额", type = "BigDecimal")
    private BigDecimal underlyerBuyAmount;
    @BctField(name = "underlyerPrice", description = "标的物价格", type = "BigDecimal")
    private BigDecimal underlyerPrice;
    @BctField(name = "underlyerPnl", description = "标的物盈亏", type = "BigDecimal")
    private BigDecimal underlyerPnl;
    @BctField(name = "optionUnwindAmount", description = "期权平仓金额", type = "BigDecimal")
    private BigDecimal optionUnwindAmount;
    @BctField(name = "optionSettleAmount", description = "期权结算金额", type = "BigDecimal")
    private BigDecimal optionSettleAmount;
    @BctField(name = "optionMarketValue", description = "期权持仓市值", type = "BigDecimal")
    private BigDecimal optionMarketValue;
    @BctField(name = "optionPremium", description = "期权费", type = "BigDecimal")
    private BigDecimal optionPremium;
    @BctField(name = "optionPnl", description = "期权盈亏", type = "BigDecimal")
    private BigDecimal optionPnl;
    @BctField(name = "pnl", description = "盈亏", type = "BigDecimal")
    private BigDecimal pnl;
    @BctField(name = "valuationDate", description = "报告日期", type = "LocalDate")
    private LocalDate valuationDate;
    @BctField(name = "createdAt", description = "计算时间", type = "Instant")
    private Instant createdAt;
    @BctField(name = "pricingEnvironment", description = "定价环境", type = "String")
    private String pricingEnvironment;

    public String getPricingEnvironment() {
        return pricingEnvironment;
    }

    public void setPricingEnvironment(String pricingEnvironment) {
        this.pricingEnvironment = pricingEnvironment;
    }

    public PnlHstReportDTO() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getUnderlyerInstrumentId() {
        return underlyerInstrumentId;
    }

    public void setUnderlyerInstrumentId(String underlyerInstrumentId) {
        this.underlyerInstrumentId = underlyerInstrumentId;
    }

    public BigDecimal getUnderlyerMarketValue() {
        return underlyerMarketValue;
    }

    public void setUnderlyerMarketValue(BigDecimal underlyerMarketValue) {
        this.underlyerMarketValue = underlyerMarketValue;
    }

    public BigDecimal getUnderlyerNetPosition() {
        return underlyerNetPosition;
    }

    public void setUnderlyerNetPosition(BigDecimal underlyerNetPosition) {
        this.underlyerNetPosition = underlyerNetPosition;
    }

    public BigDecimal getUnderlyerSellAmount() {
        return underlyerSellAmount;
    }

    public void setUnderlyerSellAmount(BigDecimal underlyerSellAmount) {
        this.underlyerSellAmount = underlyerSellAmount;
    }

    public BigDecimal getUnderlyerBuyAmount() {
        return underlyerBuyAmount;
    }

    public void setUnderlyerBuyAmount(BigDecimal underlyerBuyAmount) {
        this.underlyerBuyAmount = underlyerBuyAmount;
    }

    public BigDecimal getUnderlyerPrice() {
        return underlyerPrice;
    }

    public void setUnderlyerPrice(BigDecimal underlyerPrice) {
        this.underlyerPrice = underlyerPrice;
    }

    public BigDecimal getUnderlyerPnl() {
        return underlyerPnl;
    }

    public void setUnderlyerPnl(BigDecimal underlyerPnl) {
        this.underlyerPnl = underlyerPnl;
    }

    public BigDecimal getOptionUnwindAmount() {
        return optionUnwindAmount;
    }

    public void setOptionUnwindAmount(BigDecimal optionUnwindAmount) {
        this.optionUnwindAmount = optionUnwindAmount;
    }

    public BigDecimal getOptionSettleAmount() {
        return optionSettleAmount;
    }

    public void setOptionSettleAmount(BigDecimal optionSettleAmount) {
        this.optionSettleAmount = optionSettleAmount;
    }

    public BigDecimal getOptionMarketValue() {
        return optionMarketValue;
    }

    public void setOptionMarketValue(BigDecimal optionMarketValue) {
        this.optionMarketValue = optionMarketValue;
    }

    public BigDecimal getOptionPremium() {
        return optionPremium;
    }

    public void setOptionPremium(BigDecimal optionPremium) {
        this.optionPremium = optionPremium;
    }

    public BigDecimal getOptionPnl() {
        return optionPnl;
    }

    public void setOptionPnl(BigDecimal optionPnl) {
        this.optionPnl = optionPnl;
    }

    public BigDecimal getPnl() {
        return pnl;
    }

    public void setPnl(BigDecimal pnl) {
        this.pnl = pnl;
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
