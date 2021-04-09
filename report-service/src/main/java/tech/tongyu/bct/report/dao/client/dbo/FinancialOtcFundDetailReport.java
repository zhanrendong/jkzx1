package tech.tongyu.bct.report.dao.client.dbo;

import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 场外期权业务资金明细报表
 */
@Entity
@Table( schema = EodReportService.SCHEMA,
        indexes = {@Index(name = "financial_Otc_fund_detail_report_index", columnList = "valuationDate,reportName")})
public class FinancialOtcFundDetailReport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;//主键
    @Column
    private String reportName;
    @Column
    private String clientName;//客户名称
    @Column
    private String masterAgreementId;//SAC主协议编码
    @Column
    private LocalDate paymentDate;//出入金日期
    @Column(precision=19,scale=4)
    private BigDecimal paymentIn;//入金
    @Column(precision=19,scale=4)
    private BigDecimal paymentOut;//出金
    @Column(precision=19,scale=4)
    private BigDecimal paymentAmount;//出入金净额（出金为负数）
    @Column
    private LocalDate valuationDate;
    @Column
    @CreationTimestamp
    private Instant createdAt;
    @Column
    private String paymentDirection;

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

    public String getPaymentDirection() {
        return paymentDirection;
    }

    public void setPaymentDirection(String paymentDirection) {
        this.paymentDirection = paymentDirection;
    }

    @Override
    public String toString() {
        return "FinancialOtcFundDetailReport{" +
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
                ", paymentDirection='" + paymentDirection + '\'' +
                '}';
    }
}
