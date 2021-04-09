package tech.tongyu.bct.report.client.dto;

import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.report.dto.report.HasBookName;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class RiskReportDTO implements HasBookName {
    @BctField(name = "uuid", description = "唯一标识", type = "String")
    private String uuid;
    @BctField(name = "reportName", description = "报告名称", type = "String")
    private String reportName;
    @BctField(name = "bookName", description = "交易簿", type = "String")
    private String bookName;
    @BctField(name = "underlyerInstrumentId", description = "合约标的", type = "String")
    private String underlyerInstrumentId;
    @BctField(name = "underlyerPrice", description = "标的物价格", type = "BigDecimal")
    private BigDecimal underlyerPrice;
    @BctField(name = "underlyerPriceChangePercent", description = "标的物价格变化", type = "BigDecimal")
    private BigDecimal underlyerPriceChangePercent;
    @BctField(name = "underlyerNetPosition", description = "标的物持仓 (手)", type = "BigDecimal")
    private BigDecimal underlyerNetPosition;
    @BctField(name = "delta", description = "Delta(手)", type = "BigDecimal")
    private BigDecimal delta;
    @BctField(name = "deltaCash", description = "Delta金额", type = "BigDecimal")
    private BigDecimal deltaCash;
    @BctField(name = "netDelta", description = "净Delta(手)", type = "BigDecimal")
    private BigDecimal netDelta;
    @BctField(name = "deltaDecay", description = "Delta Decay (手)", type = "BigDecimal")
    private BigDecimal deltaDecay;
    @BctField(name = "deltaWithDecay", description = "预期Delta(手)", type = "BigDecimal")
    private BigDecimal deltaWithDecay;
    @BctField(name = "gamma", description = "Gamma(手)", type = "BigDecimal")
    private BigDecimal gamma;
    @BctField(name = "gammaCash", description = "Gamma金额", type = "BigDecimal")
    private BigDecimal gammaCash;
    @BctField(name = "vega", description = "Vega/1%", type = "BigDecimal")
    private BigDecimal vega;
    @BctField(name = "theta", description = "Theta/1天", type = "BigDecimal")
    private BigDecimal theta;
    @BctField(name = "rho", description = "Rho/1%", type = "BigDecimal")
    private BigDecimal rho;
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

    public RiskReportDTO() {
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

    public BigDecimal getDeltaCash() {
        return deltaCash;
    }

    public void setDeltaCash(BigDecimal deltaCash) {
        this.deltaCash = deltaCash;
    }
}
