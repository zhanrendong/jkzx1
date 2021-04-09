package tech.tongyu.bct.report.client.dto;


import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.report.dto.report.HasBookName;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class PnlReportDTO  implements HasBookName {
    @BctField(name = "uuid", description = "唯一标识", type = "String")
    private String uuid;
    @BctField(name = "reportName", description = "报告名称", type = "String")
    private String reportName;
    @BctField(name = "bookName", description = "交易簿", type = "String")
    private String bookName;
    @BctField(name = "underlyerInstrumentId", description = "合约标的", type = "String")
    private String underlyerInstrumentId;
    @BctField(name = "dailyPnl", description = "当日总盈亏", type = "BigDecimal")
    private BigDecimal dailyPnl;
    @BctField(name = "dailyOptionPnl", description = "当日期权盈亏", type = "BigDecimal")
    private BigDecimal dailyOptionPnl;
    @BctField(name = "dailyUnderlyerPnl", description = "当日标的物盈亏", type = "BigDecimal")
    private BigDecimal dailyUnderlyerPnl;
    @BctField(name = "pnlContributionNew", description = "新贡献", type = "BigDecimal")
    private BigDecimal pnlContributionNew;
    @BctField(name = "pnlContributionSettled", description = "结算贡献", type = "BigDecimal")
    private BigDecimal pnlContributionSettled;
    @BctField(name = "pnlContributionDelta", description = "Delta 贡献", type = "BigDecimal")
    private BigDecimal pnlContributionDelta;
    @BctField(name = "pnlContributionGamma", description = "Gamma 贡献", type = "BigDecimal")
    private BigDecimal pnlContributionGamma;
    @BctField(name = "pnlContributionVega", description = "Vega 贡献", type = "BigDecimal")
    private BigDecimal pnlContributionVega;
    @BctField(name = "pnlContributionTheta", description = "Theta 贡献", type = "BigDecimal")
    private BigDecimal pnlContributionTheta;
    @BctField(name = "pnlContributionRho", description = "Rho 贡献", type = "BigDecimal")
    private BigDecimal pnlContributionRho;
    @BctField(name = "pnlContributionUnexplained", description = "其它贡献", type = "BigDecimal")
    private BigDecimal pnlContributionUnexplained;
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

    public PnlReportDTO() {
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

    public BigDecimal getDailyPnl() {
        return dailyPnl;
    }

    public void setDailyPnl(BigDecimal dailyPnl) {
        this.dailyPnl = dailyPnl;
    }

    public BigDecimal getDailyOptionPnl() {
        return dailyOptionPnl;
    }

    public void setDailyOptionPnl(BigDecimal dailyOptionPnl) {
        this.dailyOptionPnl = dailyOptionPnl;
    }

    public BigDecimal getDailyUnderlyerPnl() {
        return dailyUnderlyerPnl;
    }

    public void setDailyUnderlyerPnl(BigDecimal dailyUnderlyerPnl) {
        this.dailyUnderlyerPnl = dailyUnderlyerPnl;
    }

    public BigDecimal getPnlContributionDelta() {
        return pnlContributionDelta;
    }

    public void setPnlContributionDelta(BigDecimal pnlContributionDelta) {
        this.pnlContributionDelta = pnlContributionDelta;
    }

    public BigDecimal getPnlContributionGamma() {
        return pnlContributionGamma;
    }

    public void setPnlContributionGamma(BigDecimal pnlContributionGamma) {
        this.pnlContributionGamma = pnlContributionGamma;
    }

    public BigDecimal getPnlContributionVega() {
        return pnlContributionVega;
    }

    public void setPnlContributionVega(BigDecimal pnlContributionVega) {
        this.pnlContributionVega = pnlContributionVega;
    }

    public BigDecimal getPnlContributionTheta() {
        return pnlContributionTheta;
    }

    public void setPnlContributionTheta(BigDecimal pnlContributionTheta) {
        this.pnlContributionTheta = pnlContributionTheta;
    }

    public BigDecimal getPnlContributionRho() {
        return pnlContributionRho;
    }

    public void setPnlContributionRho(BigDecimal pnlContributionRho) {
        this.pnlContributionRho = pnlContributionRho;
    }

    public BigDecimal getPnlContributionUnexplained() {
        return pnlContributionUnexplained;
    }

    public void setPnlContributionUnexplained(BigDecimal pnlContributionUnexplained) {
        this.pnlContributionUnexplained = pnlContributionUnexplained;
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

    public BigDecimal getPnlContributionNew() {
        return pnlContributionNew;
    }

    public void setPnlContributionNew(BigDecimal pnlContributionNew) {
        this.pnlContributionNew = pnlContributionNew;
    }

    public BigDecimal getPnlContributionSettled() {
        return pnlContributionSettled;
    }

    public void setPnlContributionSettled(BigDecimal pnlContributionSettled) {
        this.pnlContributionSettled = pnlContributionSettled;
    }
}
