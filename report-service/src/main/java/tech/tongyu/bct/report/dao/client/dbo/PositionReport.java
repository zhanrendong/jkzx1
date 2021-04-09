package tech.tongyu.bct.report.dao.client.dbo;

import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table( schema = EodReportService.SCHEMA,
        indexes = {@Index(name = "pos_rpt_index", columnList = "valuationDate,reportName")})
public class PositionReport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    @Column
    private String reportName;
    @Column
    private String bookName;
    @Column
    private String tradeId;
    @Column
    private String positionId;
    @Column
    private String partyName;
    @Column
    private String underlyerInstrumentId;
    @Column
    private ProductTypeEnum productType;
    @Column
    private BigDecimal underlyerMultiplier;
    @Column(precision=19,scale=4)
    private BigDecimal underlyerPrice;
    @Column(precision=19,scale=4)
    private BigDecimal initialNumber;
    @Column(precision=19,scale=4)
    private BigDecimal unwindNumber;
    @Column(precision=19,scale=4)
    private BigDecimal number;
    @Column(precision=19,scale=4)
    private BigDecimal premium;
    @Column(precision=19,scale=4)
    private BigDecimal unwindAmount;
    @Column(precision=19,scale=4)
    private BigDecimal marketValue;
    @Column(precision=19,scale=4)
    private BigDecimal pnl;
    @Column(precision=19,scale=4)
    private BigDecimal delta;
    @Column(precision=19,scale=4)
    private BigDecimal deltaCash;
    @Column(precision=19,scale=4)
    private BigDecimal deltaDecay;
    @Column(precision=19,scale=4)
    private BigDecimal deltaWithDecay;
    @Column(precision=19,scale=4)
    private BigDecimal gamma;
    @Column(precision=19,scale=4)
    private BigDecimal gammaCash;
    @Column(precision=19,scale=4)
    private BigDecimal vega;
    @Column(precision=19,scale=4)
    private BigDecimal theta;
    @Column(precision=19,scale=4)
    private BigDecimal rho;
    @Column(precision=19,scale=4)
    private BigDecimal r;
    @Column(precision=19,scale=4)
    private BigDecimal q;
    @Column(precision=19,scale=4)
    private BigDecimal vol;
    @Column(precision=19,scale=4)
    private BigDecimal daysInYear;
    @Column
    private LocalDate tradeDate;
    @Column
    private LocalDate expirationDate;
    @Column
    private LocalDate valuationDate;
    @Column
    private String message;
    @Column
    @CreationTimestamp
    private Instant createdAt;
    @Column
    private String pricingEnvironment;
    @Column(precision=19,scale=4)
    private BigDecimal price;
    @Column
    private Boolean listedOption;

    public PositionReport() {
    }

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

    public BigDecimal getUnderlyerMultiplier() {
        return underlyerMultiplier;
    }

    public void setUnderlyerMultiplier(BigDecimal underlyerMultiplier) {
        this.underlyerMultiplier = underlyerMultiplier;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getListedOption() {
        return listedOption;
    }

    public void setListedOption(Boolean listedOption) {
        this.listedOption = listedOption;
    }
}
