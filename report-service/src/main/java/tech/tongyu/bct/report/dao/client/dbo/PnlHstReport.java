package tech.tongyu.bct.report.dao.client.dbo;

import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table( schema = EodReportService.SCHEMA,
        indexes = {@Index(name = "pnl_hst_rpt_index", columnList = "valuationDate,reportName")})
public class PnlHstReport {
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
    private BigDecimal underlyerMarketValue;
    @Column(precision=19,scale=4)
    private BigDecimal underlyerNetPosition;
    @Column(precision=19,scale=4)
    private BigDecimal underlyerSellAmount;
    @Column(precision=19,scale=4)
    private BigDecimal underlyerBuyAmount;
    @Column(precision=19,scale=4)
    private BigDecimal underlyerPrice;
    @Column(precision=19,scale=4)
    private BigDecimal underlyerPnl;
    @Column(precision=19,scale=4)
    private BigDecimal optionUnwindAmount;
    @Column(precision=19,scale=4)
    private BigDecimal optionSettleAmount;
    @Column(precision=19,scale=4)
    private BigDecimal optionMarketValue;
    @Column(precision=19,scale=4)
    private BigDecimal optionPremium;
    @Column(precision=19,scale=4)
    private BigDecimal optionPnl;
    @Column(precision=19,scale=4)
    private BigDecimal pnl;
    @Column
    private LocalDate valuationDate;
    @Column
    @CreationTimestamp
    private Instant createdAt;
    @Column
    private String pricingEnvironment;

    public String getPricingEnvironment() {
        return pricingEnvironment;
    }

    public void setPricingEnvironment(String pricingEnvironment) {
        this.pricingEnvironment = pricingEnvironment;
    }

    public PnlHstReport() {
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

    public BigDecimal getUnderlyerMarketValue() {
        return underlyerMarketValue;
    }

    public void setUnderlyerMarketValue(BigDecimal underlyerMarketValue) {
        this.underlyerMarketValue = underlyerMarketValue;
    }

    public BigDecimal getUnderlyerNetPosition() {
        return underlyerNetPosition;
    }

    public void setUnderlyerNetPosition(BigDecimal underlyerNetPosition) {
        this.underlyerNetPosition = underlyerNetPosition;
    }

    public BigDecimal getUnderlyerSellAmount() {
        return underlyerSellAmount;
    }

    public void setUnderlyerSellAmount(BigDecimal underlyerSellAmount) {
        this.underlyerSellAmount = underlyerSellAmount;
    }

    public BigDecimal getUnderlyerBuyAmount() {
        return underlyerBuyAmount;
    }

    public void setUnderlyerBuyAmount(BigDecimal underlyerBuyAmount) {
        this.underlyerBuyAmount = underlyerBuyAmount;
    }

    public BigDecimal getUnderlyerPrice() {
        return underlyerPrice;
    }

    public void setUnderlyerPrice(BigDecimal underlyerPrice) {
        this.underlyerPrice = underlyerPrice;
    }

    public BigDecimal getUnderlyerPnl() {
        return underlyerPnl;
    }

    public void setUnderlyerPnl(BigDecimal underlyerPnl) {
        this.underlyerPnl = underlyerPnl;
    }

    public BigDecimal getOptionUnwindAmount() {
        return optionUnwindAmount;
    }

    public void setOptionUnwindAmount(BigDecimal optionUnwindAmount) {
        this.optionUnwindAmount = optionUnwindAmount;
    }

    public BigDecimal getOptionSettleAmount() {
        return optionSettleAmount;
    }

    public void setOptionSettleAmount(BigDecimal optionSettleAmount) {
        this.optionSettleAmount = optionSettleAmount;
    }

    public BigDecimal getOptionMarketValue() {
        return optionMarketValue;
    }

    public void setOptionMarketValue(BigDecimal optionMarketValue) {
        this.optionMarketValue = optionMarketValue;
    }

    public BigDecimal getOptionPremium() {
        return optionPremium;
    }

    public void setOptionPremium(BigDecimal optionPremium) {
        this.optionPremium = optionPremium;
    }

    public BigDecimal getOptionPnl() {
        return optionPnl;
    }

    public void setOptionPnl(BigDecimal optionPnl) {
        this.optionPnl = optionPnl;
    }

    public BigDecimal getPnl() {
        return pnl;
    }

    public void setPnl(BigDecimal pnl) {
        this.pnl = pnl;
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
}
