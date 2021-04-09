package tech.tongyu.bct.report.client.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class FinancialOtcFundDetailReportDTO {
    @BctField(name = "uuid", description = "唯一标识", type = "String")
    private String uuid;//主键
    @BctField(name = "reportName", description = "报告名称", type = "String")
    private String reportName;
    @BctField(name = "clientName", description = "客户名称", type = "String")
    private String clientName;//客户名称
    @BctField(name = "masterAgreementId", description = "SAC协议编码", type = "String")
    private String masterAgreementId;//SAC主协议编码
    @BctField(name = "paymentDate", description = "出入金日期", type = "LocalDate")
    private LocalDate paymentDate;//出入金日期
    @BctField(name = "paymentIn", description = "入金", type = "BigDecimal")
    private BigDecimal paymentIn;//入金
    @BctField(name = "paymentOut", description = "出金", type = "BigDecimal")
    private BigDecimal paymentOut;//出金
    @BctField(name = "paymentAmount", description = "出入金净额", type = "BigDecimal")
    private BigDecimal paymentAmount;//出入金净额（出金为负数）
    @BctField(name = "valuationDate", description = "报告日期", type = "LocalDate")
    private LocalDate valuationDate;
    @BctField(name = "createdAt", description = "计算时间", type = "Instant")
    private Instant createdAt;

    @Override
    public String toString() {
        return "FinancialOtcFundDetailReportDTO{" +
                "uuid=" + uuid +
                ", reportName='" + reportName + '\'' +
                ", clientName='" + clientName + '\'' +
                ", masterAgreementId='" + masterAgreementId + '\'' +
                ", paymentDate=" + paymentDate +
                ", paymentIn=" + paymentIn +
                ", paymentOut=" + paymentOut +
                ", paymentAmount=" + paymentAmount +
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

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
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

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
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
