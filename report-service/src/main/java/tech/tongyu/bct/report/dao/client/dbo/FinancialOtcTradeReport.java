package tech.tongyu.bct.report.dao.client.dbo;

import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
/**
 * 场外期权业务交易报表
 */
@Entity
@Table( schema = EodReportService.SCHEMA,
        indexes = {@Index(name = "otc_trade_report_index", columnList = "valuationDate,reportName")})
public class FinancialOtcTradeReport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;//主键
    @Column
    private String masterAgreementId;//SAC主协议编号
    @Column
    private String optionName;//交易确认书编码
    @Column
    private String reportName;//报告名称
    @Column
    private String client;//客户名称
    @Column
    private LocalDate dealStartDate;//开始日期（合同中的开始日）
    @Column
    private LocalDate expiry;//到期日期（合同中的开始日）
    @Column
    private String side;//买卖方向（Long/Short）
    @Column
    private String baseContract;//标的资产名称
    @Column
    private String assetType;//标的资产类型（股票，商品）
    @Column(precision=19,scale=4)
    private BigDecimal nominalPrice;//名义金额
    @Column(precision=19,scale=4)
    private BigDecimal beginPremium;//期权费
    @Column(precision=19,scale=4)
    private BigDecimal endPremium;//市值
    @Column(precision=19,scale=4)
    private BigDecimal totalPremium;//总盈亏（期权费+市值）
    @Column
    private LocalDate endDate;//实际平仓日期
    @Column
    private String status;//存续状态（平仓/存续）
    @Column
    private LocalDate valuationDate;
    @Column
    @CreationTimestamp
    private Instant createdAt;
    @Column
    private String bookName;
    @Column
    private String positionId;//持仓编号

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getMasterAgreementId() {
        return masterAgreementId;
    }

    public void setMasterAgreementId(String masterAgreementId) {
        this.masterAgreementId = masterAgreementId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public LocalDate getDealStartDate() {
        return dealStartDate;
    }

    public void setDealStartDate(LocalDate dealStartDate) {
        this.dealStartDate = dealStartDate;
    }

    public LocalDate getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDate expiry) {
        this.expiry = expiry;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getBaseContract() {
        return baseContract;
    }

    public void setBaseContract(String baseContract) {
        this.baseContract = baseContract;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public BigDecimal getNominalPrice() {
        return nominalPrice;
    }

    public void setNominalPrice(BigDecimal nominalPrice) {
        this.nominalPrice = nominalPrice;
    }

    public BigDecimal getBeginPremium() {
        return beginPremium;
    }

    public void setBeginPremium(BigDecimal beginPremium) {
        this.beginPremium = beginPremium;
    }

    public BigDecimal getEndPremium() {
        return endPremium;
    }

    public void setEndPremium(BigDecimal endPremium) {
        this.endPremium = endPremium;
    }

    public BigDecimal getTotalPremium() {
        return totalPremium;
    }

    public void setTotalPremium(BigDecimal totalPremium) {
        this.totalPremium = totalPremium;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    @Override
    public String toString() {
        return "FinancialOtcTradeReport{" +
                "uuid=" + uuid +
                ", masterAgreementId='" + masterAgreementId + '\'' +
                ", optionName='" + optionName + '\'' +
                ", reportName='" + reportName + '\'' +
                ", client='" + client + '\'' +
                ", dealStartDate=" + dealStartDate +
                ", expiry=" + expiry +
                ", side='" + side + '\'' +
                ", baseContract='" + baseContract + '\'' +
                ", assetType='" + assetType + '\'' +
                ", nominalPrice=" + nominalPrice +
                ", beginPremium=" + beginPremium +
                ", endPremium=" + endPremium +
                ", totalPremium=" + totalPremium +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                ", valuationDate=" + valuationDate +
                ", createdAt=" + createdAt +
                ", bookName='" + bookName + '\'' +
                ", positionId='" + positionId + '\'' +
                '}';
    }

    public FinancialOtcTradeReport() {
    }
}
