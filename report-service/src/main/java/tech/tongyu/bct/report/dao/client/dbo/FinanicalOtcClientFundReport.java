package tech.tongyu.bct.report.dao.client.dbo;

import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 场外期权业务客户资金汇总报表
 */
@Entity
@Table( schema = EodReportService.SCHEMA,
        indexes = {@Index(name = "financial_Otc_client_fund_report_index", columnList = "valuationDate,reportName")})
public class FinanicalOtcClientFundReport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;//主键
    @Column
    private String reportName;
    @Column
    private String clientName;//客户名称
    @Column
    private String masterAgreementId;//SAC主协议编码
    @Column(precision=19,scale=4)
    private BigDecimal paymentIn;//入金
    @Column(precision=19,scale=4)
    private BigDecimal paymentOut;//出金
    @Column(precision=19,scale=4)
    private BigDecimal premiumBuy;//期权收取权利金（客户买期权）
    @Column(precision=19,scale=4)
    private BigDecimal premiumSell;//期权支出权利金（客户卖期权）
    @Column(precision=19,scale=4)
    private BigDecimal profitAmount;//期权了结盈利
    @Column(precision=19,scale=4)
    private BigDecimal lossAmount;//期权了结亏损
    @Column(precision=19,scale=4)
    private BigDecimal fundTotal;
    @Column
    private LocalDate valuationDate;
    @Column
    @CreationTimestamp
    private Instant createdAt;

    public BigDecimal getFundTotal() {
        return fundTotal;
    }

    public void setFundTotal(BigDecimal fundTotal) {
        this.fundTotal = fundTotal;
    }

    @Override
    public String toString() {
        return "FinanicalOtcClientFundReport{" +
                "uuid=" + uuid +
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

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
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
}
