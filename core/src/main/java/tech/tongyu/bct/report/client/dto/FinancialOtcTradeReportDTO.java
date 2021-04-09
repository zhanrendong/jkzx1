package tech.tongyu.bct.report.client.dto;


import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.report.dto.report.HasBookName;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 场外期权业务交易报表DTO
 */
public class FinancialOtcTradeReportDTO implements HasBookName {
    @BctField(name = "masterAgreementId", description = "SAC协议编码", type = "String")
    private String masterAgreementId;
    @BctField(name = "uuid", description = "唯一标识", type = "String")
    private String uuid;//主键
    @BctField(name = "reportName", description = "报告名称", type = "String")
    private String reportName;//报告名称
    @BctField(name = "optionName", description = "交易确认书编码", type = "String")
    private String optionName;
    @BctField(name = "client", description = "客户名称", type = "String")
    private String client;//客户名称
    @BctField(name = "dealStartDate", description = "开始日期", type = "LocalDate")
    private LocalDate dealStartDate;//开始日期（合同中的开始日）
    @BctField(name = "expiry", description = "到期日期", type = "LocalDate")
    private LocalDate expiry;//到期日期（合同中的开始日）
    @BctField(name = "side", description = "买卖方向", type = "String")
    private String side;//买卖方向（Long/Short）
    @BctField(name = "baseContract", description = "标的资产名称", type = "String")
    private String baseContract;//标的资产名称
    @BctField(name = "assetType", description = "标的资产类型", type = "String")
    private String assetType; //标的资产类型（股票，商品）
    @BctField(name = "nominalPrice", description = "名义金额", type = "BigDecimal")
    private BigDecimal nominalPrice;//名义金额
    @BctField(name = "beginPremium", description = "期权费", type = "BigDecimal")
    private BigDecimal beginPremium;//期权费
    @BctField(name = "endPremium", description = "市值", type = "BigDecimal")
    private BigDecimal endPremium;//市值
    @BctField(name = "totalPremium", description = "总盈亏", type = "BigDecimal")
    private BigDecimal totalPremium;//总盈亏（期权费+市值）
    @BctField(name = "endDate", description = "实际平仓日期", type = "LocalDate")
    private LocalDate endDate;//实际平仓日期
    @BctField(name = "status", description = "存续状态", type = "String")
    private String status;//存续状态（平仓/存续）
    @BctField(name = "valuationDate", description = "报告日期", type = "LocalDate")
    private LocalDate valuationDate;
    @BctField(name = "createdAt", description = "计算时间", type = "Instant")
    private Instant createdAt;
    @BctField(name = "positionId", description = "持仓ID", type = "String")
    private String positionId;
    @BctField(name = "bookName", description = "交易簿", type = "String")
    private String bookName;

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getMasterAgreementId() {
        return masterAgreementId;
    }

    public void setMasterAgreementId(String masterAgreementId) {
        this.masterAgreementId = masterAgreementId;
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

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
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

    @Override
    public String toString() {
        return "FinancialOtcTradeReportDTO{" +
                "masterAgreementId='" + masterAgreementId + '\'' +
                ", uuid='" + uuid + '\'' +
                ", reportName='" + reportName + '\'' +
                ", optionName='" + optionName + '\'' +
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
                '}';
    }
}
