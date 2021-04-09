package tech.tongyu.bct.report.dao.client.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.cm.product.iov.InstrumentAssetClassTypeEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.TradeStatusEnum;
import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table( schema = EodReportService.SCHEMA,
        indexes = {@Index(name = "risk_rpt_index", columnList = "valuationDate,reportName")})
public class RiskReport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    @Column
    private String reportName;
    @Column
    private String bookName;
    @Column
    private String underlyerInstrumentId;
    @Column(precision=19,scale=4)
    private BigDecimal underlyerPrice;
    @Column(precision=19,scale=4)
    private BigDecimal underlyerPriceChangePercent;
    @Column(precision=19,scale=4)
    private BigDecimal underlyerNetPosition;
    @Column(precision=19,scale=4)
    private BigDecimal delta;
    @Column(precision=19,scale=4)
    private BigDecimal deltaCash;
    @Column(precision=19,scale=4)
    private BigDecimal netDelta;
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
    @Column
    private LocalDate valuationDate;
    @Column
    private String pricingEnvironment;
    @Column
    @CreationTimestamp
    private Instant createdAt;

    public RiskReport() {
    }

    public String getPricingEnvironment() {
        return pricingEnvironment;
    }

    public void setPricingEnvironment(String pricingEnvironment) {
        this.pricingEnvironment = pricingEnvironment;
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
