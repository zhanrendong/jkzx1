package tech.tongyu.bct.report.dto.report;

import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;


public class RiskReportRowDTO implements HasBookName {
    @BctField(name = "bookName", description = "交易簿", type = "String")
    String bookName;
    @BctField(name = "underlyerInstrumentId", description = "合约标的", type = "String")
    String underlyerInstrumentId;
    @BctField(name = "underlyerPrice", description = "标的物价格", type = "BigDecimal")
    BigDecimal underlyerPrice;
    @BctField(name = "underlyerPriceChangePercent", description = "标的物价格变化", type = "BigDecimal")
    BigDecimal underlyerPriceChangePercent;
    @BctField(name = "underlyerNetPosition", description = "标的物持仓 (手)", type = "BigDecimal")
    BigDecimal underlyerNetPosition;
    @BctField(name = "delta", description = "Delta(手)", type = "BigDecimal")
    BigDecimal delta;
    @BctField(name = "deltaCash", description = "Delta金额", type = "BigDecimal")
    BigDecimal deltaCash;
    @BctField(name = "netDelta", description = "净Delta(手)", type = "BigDecimal")
    BigDecimal netDelta;
    @BctField(name = "deltaDecay", description = "Delta Decay (手)", type = "BigDecimal")
    BigDecimal deltaDecay;
    @BctField(name = "deltaWithDecay", description = "预期Delta(手)", type = "BigDecimal")
    BigDecimal deltaWithDecay;
    @BctField(name = "gamma", description = "Gamma(手)", type = "BigDecimal")
    BigDecimal gamma;
    @BctField(name = "gammaCash", description = "Gamma金额", type = "BigDecimal")
    BigDecimal gammaCash;
    @BctField(name = "vega", description = "Vega/1%", type = "BigDecimal")
    BigDecimal vega;
    @BctField(name = "theta", description = "Theta/1天", type = "BigDecimal")
    BigDecimal theta;
    @BctField(name = "rho", description = "Rho/1%", type = "BigDecimal")
    BigDecimal rho;
    @BctField(name = "createdAt", description = "计算时间", type = "String")
    String createdAt;
    @BctField(name = "pricingEnvironment", description = "定价环境", type = "String")
    String pricingEnvironment;

    public String getPricingEnvironment() {
        return pricingEnvironment;
    }

    public void setPricingEnvironment(String pricingEnvironment) {
        this.pricingEnvironment = pricingEnvironment;
    }

    public RiskReportRowDTO() {
    }

    public RiskReportRowDTO(String bookName, String underlyerInstrumentId, BigDecimal underlyerPrice,
                            BigDecimal underlyerPriceChangePercent, BigDecimal underlyerNetPosition, BigDecimal delta,
                            BigDecimal netDelta, BigDecimal deltaDecay, BigDecimal deltaWithDecay, BigDecimal gamma,
                            BigDecimal gammaCash, BigDecimal vega, BigDecimal theta, BigDecimal rho, String createdAt) {
        this.bookName = bookName;
        this.underlyerInstrumentId = underlyerInstrumentId;
        this.underlyerPrice = underlyerPrice;
        this.underlyerPriceChangePercent = underlyerPriceChangePercent;
        this.underlyerNetPosition = underlyerNetPosition;
        this.delta = delta;
        this.netDelta = netDelta;
        this.deltaDecay = deltaDecay;
        this.deltaWithDecay = deltaWithDecay;
        this.gamma = gamma;
        this.gammaCash = gammaCash;
        this.vega = vega;
        this.theta = theta;
        this.rho = rho;
        this.createdAt = createdAt;
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

    public BigDecimal getUnderlyerPrice() {
        return underlyerPrice;
    }

    public void setUnderlyerPrice(BigDecimal underlyerPrice) {
        this.underlyerPrice = underlyerPrice;
    }

    public BigDecimal getUnderlyerPriceChangePercent() {
        return underlyerPriceChangePercent;
    }

    public void setUnderlyerPriceChangePercent(BigDecimal underlyerPriceChangePercent) {
        this.underlyerPriceChangePercent = underlyerPriceChangePercent;
    }

    public BigDecimal getUnderlyerNetPosition() {
        return underlyerNetPosition;
    }

    public void setUnderlyerNetPosition(BigDecimal underlyerNetPosition) {
        this.underlyerNetPosition = underlyerNetPosition;
    }

    public BigDecimal getDelta() {
        return delta;
    }

    public void setDelta(BigDecimal delta) {
        this.delta = delta;
    }

    public BigDecimal getNetDelta() {
        return netDelta;
    }

    public void setNetDelta(BigDecimal netDelta) {
        this.netDelta = netDelta;
    }

    public BigDecimal getDeltaDecay() {
        return deltaDecay;
    }

    public void setDeltaDecay(BigDecimal deltaDecay) {
        this.deltaDecay = deltaDecay;
    }

    public BigDecimal getDeltaWithDecay() {
        return deltaWithDecay;
    }

    public void setDeltaWithDecay(BigDecimal deltaWithDecay) {
        this.deltaWithDecay = deltaWithDecay;
    }

    public BigDecimal getGamma() {
        return gamma;
    }

    public void setGamma(BigDecimal gamma) {
        this.gamma = gamma;
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

    public BigDecimal getDeltaCash() {
        return deltaCash;
    }

    public void setDeltaCash(BigDecimal deltaCash) {
        this.deltaCash = deltaCash;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
