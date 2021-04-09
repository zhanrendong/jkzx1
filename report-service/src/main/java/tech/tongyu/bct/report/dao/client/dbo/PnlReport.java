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
        indexes = {@Index(name = "pnl_rpt_index", columnList = "valuationDate,reportName")})
public class PnlReport {
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
    private BigDecimal dailyPnl;
    @Column(precision=19,scale=4)
    private BigDecimal dailyOptionPnl;
    @Column(precision=19,scale=4)
    private BigDecimal dailyUnderlyerPnl;
    @Column(precision=19,scale=4)
    private BigDecimal pnlContributionNew;
    @Column(precision=19,scale=4)
    private BigDecimal pnlContributionSettled;
    @Column(precision=19,scale=4)
    private BigDecimal pnlContributionDelta;
    @Column(precision=19,scale=4)
    private BigDecimal pnlContributionGamma;
    @Column(precision=19,scale=4)
    private BigDecimal pnlContributionVega;
    @Column(precision=19,scale=4)
    private BigDecimal pnlContributionTheta;
    @Column(precision=19,scale=4)
    private BigDecimal pnlContributionRho;
    @Column(precision=19,scale=4)
    private BigDecimal pnlContributionUnexplained;
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

    public PnlReport() {
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
