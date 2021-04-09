package tech.tongyu.bct.report.dto.report;

import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PositionReportRowDTO implements HasBookName {
    @BctField(name = "bookName", description = "交易簿", type = "String")
    String bookName;
    @BctField(name = "tradeId", description = "交易ID", type = "String")
    String tradeId;
    @BctField(name = "positionId", description = "持仓ID", type = "String")
    String positionId;
    @BctField(name = "partyName", description = "交易对手", type = "String")
    String partyName;
    @BctField(name = "underlyerInstrumentId", description = "合约标的", type = "String")
    String underlyerInstrumentId;
    @BctField(name = "underlyerMultiplier", description = "合约乘数", type = "BigDecimal")
    BigDecimal underlyerMultiplier;
    @BctField(name = "productType", description = "期权类型", type = "ProductTypeEnum")
    ProductTypeEnum productType;
    @BctField(name = "initialNumber", description = "期初数量(手)", type = "BigDecimal")
    BigDecimal initialNumber;
    @BctField(name = "unwindNumber", description = "平仓数量(手)", type = "BigDecimal")
    BigDecimal unwindNumber;
    @BctField(name = "number", description = "持仓数量(手)", type = "BigDecimal")
    BigDecimal number;
    @BctField(name = "premium", description = "期权费", type = "BigDecimal")
    BigDecimal premium;
    @BctField(name = "unwindAmount", description = "平仓金额", type = "BigDecimal")
    BigDecimal unwindAmount;
    @BctField(name = "marketValue", description = "市值", type = "BigDecimal")
    BigDecimal marketValue;
    @BctField(name = "pnl", description = "盈亏", type = "BigDecimal")
    BigDecimal pnl;
    @BctField(name = "delta", description = "Delta(手)", type = "BigDecimal")
    BigDecimal delta;
    @BctField(name = "deltaCash", description = "Delta金额", type = "BigDecimal")
    BigDecimal deltaCash;
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
    @BctField(name = "tradeDate", description = "交易日", type = "LocalDate")
    LocalDate tradeDate;
    @BctField(name = "expirationDate", description = "过期日", type = "LocalDate")
    LocalDate expirationDate;
    @BctField(name = "message", description = "错误信息", type = "String")
    String message;
    @BctField(name = "createdAt", description = "计算时间", type = "String")
    String createdAt;
    @BctField(name = "pricingEnvironment", description = "定价环境", type = "String")
    private String pricingEnvironment;
    @BctField(name = "r", description = "r", type = "BigDecimal")
    private BigDecimal r;
    @BctField(name = "q", description = "q", type = "BigDecimal")
    private BigDecimal q;
    @BctField(name = "vol", description = "波动率", type = "BigDecimal")
    private BigDecimal vol;

    public PositionReportRowDTO() {
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

    public BigDecimal getUnderlyerMultiplier() {
        return underlyerMultiplier;
    }

    public void setUnderlyerMultiplier(BigDecimal underlyerMultiplier) {
        this.underlyerMultiplier = underlyerMultiplier;
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

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
