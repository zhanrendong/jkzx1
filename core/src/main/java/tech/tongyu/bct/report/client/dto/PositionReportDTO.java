package tech.tongyu.bct.report.client.dto;

import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.report.dto.report.HasBookName;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class PositionReportDTO implements HasBookName {
    @BctField(name = "uuid", description = "唯一标识", type = "String")
    private String uuid;
    @BctField(name = "reportName", description = "报告名称", type = "String")
    private String reportName;
    @BctField(name = "bookName", description = "交易簿", type = "String")
    private String bookName;
    @BctField(name = "tradeId", description = "交易ID", type = "String")
    private String tradeId;
    @BctField(name = "positionId", description = "持仓ID", type = "String")
    private String positionId;
    @BctField(name = "partyName", description = "交易对手", type = "String")
    private String partyName;
    @BctField(name = "underlyerInstrumentId", description = "合约标的", type = "String")
    private String underlyerInstrumentId;
    @BctField(name = "productType", description = "期权类型", type = "ProductTypeEnum")
    private ProductTypeEnum productType;
    @BctField(name = "underlyerMultiplier", description = "合约乘数", type = "BigDecimal")
    private BigDecimal underlyerMultiplier;
    @BctField(name = "underlyerPrice", description = "标的物价格", type = "BigDecimal")
    private BigDecimal underlyerPrice;
    @BctField(name = "initialNumber", description = "期初数量 (手)", type = "BigDecimal")
    private BigDecimal initialNumber;
    @BctField(name = "unwindNumber", description = "平仓数量 (手)", type = "BigDecimal")
    private BigDecimal unwindNumber;
    @BctField(name = "number", description = "持仓数量 (手)", type = "BigDecimal")
    private BigDecimal number;
    @BctField(name = "premium", description = "期权费", type = "BigDecimal")
    private BigDecimal premium;
    @BctField(name = "unwindAmount", description = "平仓金额", type = "BigDecimal")
    private BigDecimal unwindAmount;
    @BctField(name = "marketValue", description = "市值", type = "BigDecimal")
    private BigDecimal marketValue;
    @BctField(name = "pnl", description = "盈亏", type = "BigDecimal")
    private BigDecimal pnl;
    @BctField(name = "delta", description = "Delta(手)", type = "BigDecimal")
    private BigDecimal delta;
    @BctField(name = "deltaCash", description = "Delta金额", type = "BigDecimal")
    private BigDecimal deltaCash;
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
    @BctField(name = "r", description = "r", type = "BigDecimal")
    private BigDecimal r;
    @BctField(name = "q", description = "q", type = "BigDecimal")
    private BigDecimal q;
    @BctField(name = "vol", description = "波动率", type = "BigDecimal")
    private BigDecimal vol;
    @BctField(name = "daysInYear", description = "年度计息天数", type = "BigDecimal")
    private BigDecimal daysInYear;
    @BctField(name = "tradeDate", description = "交易日", type = "LocalDate")
    private LocalDate tradeDate;
    @BctField(name = "expirationDate", description = "过期日", type = "LocalDate")
    private LocalDate expirationDate;
    @BctField(name = "valuationDate", description = "报告日期", type = "LocalDate")
    private LocalDate valuationDate;
    @BctField(name = "createdAt", description = "计算时间", type = "Instant")
    private Instant createdAt;
    @BctField(name = "message", description = "错误信息", type = "String")
    private String message;
    @BctField(name = "pricingEnvironment", description = "定价环境", type = "String")
    private String pricingEnvironment;

    public PositionReportDTO() {
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

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getUnderlyerInstrumentId() {
        return underlyerInstrumentId;
    }

    public void setUnderlyerInstrumentId(String underlyerInstrumentId) {
        this.underlyerInstrumentId = underlyerInstrumentId;
    }

    public ProductTypeEnum getProductType() {
        return productType;
    }

    public void setProductType(ProductTypeEnum productType) {
        this.productType = productType;
    }

    public BigDecimal getUnderlyerMultiplier() {
        return underlyerMultiplier;
    }

    public void setUnderlyerMultiplier(BigDecimal underlyerMultiplier) {
        this.underlyerMultiplier = underlyerMultiplier;
    }

    public BigDecimal getUnderlyerPrice() {
        return underlyerPrice;
    }

    public void setUnderlyerPrice(BigDecimal underlyerPrice) {
        this.underlyerPrice = underlyerPrice;
    }

    public BigDecimal getInitialNumber() {
        return initialNumber;
    }

    public void setInitialNumber(BigDecimal initialNumber) {
        this.initialNumber = initialNumber;
    }

    public BigDecimal getUnwindNumber() {
        return unwindNumber;
    }

    public void setUnwindNumber(BigDecimal unwindNumber) {
        this.unwindNumber = unwindNumber;
    }

    public BigDecimal getNumber() {
        return number;
    }

    public void setNumber(BigDecimal number) {
        this.number = number;
    }

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }

    public BigDecimal getUnwindAmount() {
        return unwindAmount;
    }

    public void setUnwindAmount(BigDecimal unwindAmount) {
        this.unwindAmount = unwindAmount;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }

    public BigDecimal getPnl() {
        return pnl;
    }

    public void setPnl(BigDecimal pnl) {
        this.pnl = pnl;
    }

    public BigDecimal getDelta() {
        return delta;
    }

    public void setDelta(BigDecimal delta) {
        this.delta = delta;
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

    public BigDecimal getR() {
        return r;
    }

    public void setR(BigDecimal r) {
        this.r = r;
    }

    public BigDecimal getQ() {
        return q;
    }

    public void setQ(BigDecimal q) {
        this.q = q;
    }

    public BigDecimal getVol() {
        return vol;
    }

    public void setVol(BigDecimal vol) {
        this.vol = vol;
    }

    public BigDecimal getDaysInYear() {
        return daysInYear;
    }

    public void setDaysInYear(BigDecimal daysInYear) {
        this.daysInYear = daysInYear;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getDeltaCash() {
        return deltaCash;
    }

    public void setDeltaCash(BigDecimal deltaCash) {
        this.deltaCash = deltaCash;
    }

    public String getPricingEnvironment() {
        return pricingEnvironment;
    }

    public void setPricingEnvironment(String pricingEnvironment) {
        this.pricingEnvironment = pricingEnvironment;
    }
}
