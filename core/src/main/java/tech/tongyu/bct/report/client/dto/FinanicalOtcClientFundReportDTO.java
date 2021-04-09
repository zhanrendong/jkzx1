package tech.tongyu.bct.report.client.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class FinanicalOtcClientFundReportDTO {
    @BctField(name = "uuid", description = "唯一标识", type = "String")
    private String uuid;//主键
    @BctField(name = "reportName", description = "报告名称", type = "String")
    private String reportName;
    @BctField(name = "clientName", description = "客户名称", type = "String")
    private String clientName;//客户名称
    @BctField(name = "masterAgreementId", description = "SAC协议编码", type = "String")
    private String masterAgreementId;//SAC主协议编码
    @BctField(name = "paymentIn", description = "入金", type = "BigDecimal")
    private BigDecimal paymentIn;//入金
    @BctField(name = "paymentOut", description = "出金", type = "BigDecimal")
    private BigDecimal paymentOut;//出金
    @BctField(name = "premiumBuy", description = "期权收取权利金", type = "BigDecimal")
    private BigDecimal premiumBuy;//期权收取权利金（客户买期权）
    @BctField(name = "premiumSell", description = "期权支出权利金", type = "BigDecimal")
    private BigDecimal premiumSell;//期权支出权利金（客户卖期权）
    @BctField(name = "profitAmount", description = "期权了结盈利", type = "BigDecimal")
    private BigDecimal profitAmount;//期权了结盈利
    @BctField(name = "lossAmount", description = "期权了结亏损", type = "BigDecimal")
    private BigDecimal lossAmount;//期权了结亏损
    @BctField(name = "fundTotal", description = "场外预付金金额", type = "BigDecimal")
    private BigDecimal fundTotal;//
    @BctField(name = "valuationDate", description = "报告日期", type = "LocalDate")
    private LocalDate valuationDate;
    @BctField(name = "createdAt", description = "计算时间", type = "Instant")
    private Instant createdAt;

    public BigDecimal getFundTotal() {
        return fundTotal;
    }

    public void setFundTotal(BigDecimal fundTotal) {
        this.fundTotal = fundTotal;
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

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getMasterAgreementId() {
        return masterAgreementId;
    }

    public void setMasterAgreementId(String masterAgreementId) {
        this.masterAgreementId = masterAgreementId;
    }

    public BigDecimal getPaymentIn() {
        return paymentIn;
    }

    public void setPaymentIn(BigDecimal paymentIn) {
        this.paymentIn = paymentIn;
    }

    public BigDecimal getPaymentOut() {
        return paymentOut;
    }

    public void setPaymentOut(BigDecimal paymentOut) {
        this.paymentOut = paymentOut;
    }

    public BigDecimal getPremiumBuy() {
        return premiumBuy;
    }

    public void setPremiumBuy(BigDecimal premiumBuy) {
        this.premiumBuy = premiumBuy;
    }

    public BigDecimal getPremiumSell() {
        return premiumSell;
    }

    public void setPremiumSell(BigDecimal premiumSell) {
        this.premiumSell = premiumSell;
    }

    public BigDecimal getProfitAmount() {
        return profitAmount;
    }

    public void setProfitAmount(BigDecimal profitAmount) {
        this.profitAmount = profitAmount;
    }

    public BigDecimal getLossAmount() {
        return lossAmount;
    }

    public void setLossAmount(BigDecimal lossAmount) {
        this.lossAmount = lossAmount;
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
        return "FinanicalOtcClientFundReportDTO{" +
                "uuid='" + uuid + '\'' +
                ", reportName='" + reportName + '\'' +
                ", clientName='" + clientName + '\'' +
                ", masterAgreementId='" + masterAgreementId + '\'' +
                ", paymentIn=" + paymentIn +
                ", paymentOut=" + paymentOut +
                ", premiumBuy=" + premiumBuy +
                ", premiumSell=" + premiumSell +
                ", profitAmount=" + profitAmount +
                ", lossAmount=" + lossAmount +
                ", fundTotal=" + fundTotal +
                ", valuationDate=" + valuationDate +
                ", createdAt=" + createdAt +
                '}';
    }
}
