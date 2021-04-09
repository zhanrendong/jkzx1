package tech.tongyu.bct.report.dto.report;

import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;

public class PnlReportRowDTO implements HasBookName {
    @BctField(name = "bookName", description = "交易簿", type = "String")
    String bookName;
    @BctField(name = "underlyerInstrumentId", description = "合约标的", type = "String")
    String underlyerInstrumentId;
    @BctField(name = "dailyPnl", description = "当日总盈亏", type = "BigDecimal")
    BigDecimal dailyPnl;
    @BctField(name = "dailyOptionPnl", description = "当日期权盈亏", type = "BigDecimal")
    BigDecimal dailyOptionPnl;
    @BctField(name = "dailyUnderlyerPnl", description = "当日标的物盈亏", type = "BigDecimal")
    BigDecimal dailyUnderlyerPnl;
    @BctField(name = "pnlContributionNew", description = "新贡献", type = "BigDecimal")
    BigDecimal pnlContributionNew;
    @BctField(name = "pnlContributionSettled", description = "结算贡献", type = "BigDecimal")
    BigDecimal pnlContributionSettled;
    @BctField(name = "pnlContributionDelta", description = "Delta 贡献", type = "BigDecimal")
    BigDecimal pnlContributionDelta;
    @BctField(name = "pnlContributionGamma", description = "Gamma 贡献", type = "BigDecimal")
    BigDecimal pnlContributionGamma;
    @BctField(name = "pnlContributionVega", description = "Vega 贡献", type = "BigDecimal")
    BigDecimal pnlContributionVega;
    @BctField(name = "pnlContributionTheta", description = "Theta 贡献", type = "BigDecimal")
    BigDecimal pnlContributionTheta;
    @BctField(name = "pnlContributionRho", description = "Rho 贡献", type = "BigDecimal")
    BigDecimal pnlContributionRho;
    @BctField(name = "pnlContributionUnexplained", description = "其它贡献", type = "BigDecimal")
    BigDecimal pnlContributionUnexplained;
    @BctField(name = "pricingEnvironment", description = "定价环境", type = "String")
    String pricingEnvironment;
    @BctField(name = "createdAt", description = "计算时间", type = "String")
    String createdAt;

    public String getPricingEnvironment() {
        return pricingEnvironment;
    }

    public void setPricingEnvironment(String pricingEnvironment) {
        this.pricingEnvironment = pricingEnvironment;
    }

    public PnlReportRowDTO() {
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
