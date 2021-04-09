package tech.tongyu.bct.report.dto.report;

import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;

public class PortfolioRiskReportRowDTO {
    @BctField(name = "portfolioName", description = "投资组合", type = "String")
    private String portfolioName;
    @BctField(name = "deltaCash", description = "Delta 金额", type = "BigDecimal")
    private BigDecimal deltaCash;
    @BctField(name = "gammaCash", description = "Gamma 金额", type = "BigDecimal")
    private BigDecimal gammaCash;
    @BctField(name = "vega", description = "Vega/1%", type = "BigDecimal")
    private BigDecimal vega;
    @BctField(name = "theta", description = "Theta/1天", type = "BigDecimal")
    private BigDecimal theta;
    @BctField(name = "rho", description = "Rho/1%", type = "BigDecimal")
    private BigDecimal rho;
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

    @Override
    public String toString() {
        return "PortfolioRiskReportRowDTO{" +
                "portfolioName='" + portfolioName + '\'' +
                ", deltaCash=" + deltaCash +
                ", gammaCash=" + gammaCash +
                ", vega=" + vega +
                ", theta=" + theta +
                ", rho=" + rho +
                ", pricingEnvironment='" + pricingEnvironment + '\'' +
                '}';
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    public BigDecimal getDeltaCash() {
        return deltaCash;
    }

    public void setDeltaCash(BigDecimal deltaCash) {
        this.deltaCash = deltaCash;
    }

    public BigDecimal getGammaCash() {
        return gammaCash;
    }

    public void setGammaCash(BigDecimal gammaCash) {
        this.gammaCash = gammaCash;
    }

    public BigDecimal getVega() {
        return vega;
    }

    public void setVega(BigDecimal vega) {
        this.vega = vega;
    }

    public BigDecimal getTheta() {
        return theta;
    }

    public void setTheta(BigDecimal theta) {
        this.theta = theta;
    }

    public BigDecimal getRho() {
        return rho;
    }

    public void setRho(BigDecimal rho) {
        this.rho = rho;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
