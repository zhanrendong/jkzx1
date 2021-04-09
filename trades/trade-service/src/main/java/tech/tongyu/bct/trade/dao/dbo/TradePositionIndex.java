package tech.tongyu.bct.trade.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.jpa.HasUuid;
import tech.tongyu.bct.trade.service.TradeService;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table( schema = TradeService.SCHEMA,
        indexes = {@Index(columnList = "tradeId"), @Index(columnList = "bookName"), @Index(columnList = "salesName"),
        @Index(columnList = "positionId"), @Index(columnList = "instrumentId"), @Index(columnList = "counterPartyName"),
        @Index(columnList = "tradeDate"), @Index(columnList = "expirationDate"), @Index(columnList = "productType"),
        @Index(columnList = "lcmEventType")})
public class TradePositionIndex implements HasUuid {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    @Column
    private String tradeId;
    @Column
    private String bookName;
    @Column
    private String salesName;
    @Column(unique = true)
    private String positionId;
    @Column
    private String instrumentId;
    @Column
    private String counterPartyName;
    @Column
    private LocalDate tradeDate;
    @Column
    private LocalDate effectiveDate;
    @Column
    private LocalDate expirationDate;
    @Column
    @Enumerated(EnumType.STRING)
    private ProductTypeEnum productType;
    @Column
    @Enumerated(EnumType.STRING)
    private LCMEventTypeEnum lcmEventType;
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdAt;
    @Column
    @UpdateTimestamp
    private Instant updatedAt;

    public TradePositionIndex() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getSalesName() {
        return salesName;
    }

    public void setSalesName(String salesName) {
        this.salesName = salesName;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public String getCounterPartyName() {
        return counterPartyName;
    }

    public void setCounterPartyName(String counterPartyName) {
        this.counterPartyName = counterPartyName;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public ProductTypeEnum getProductType() {
        return productType;
    }

    public void setProductType(ProductTypeEnum productType) {
        this.productType = productType;
    }

    public LCMEventTypeEnum getLcmEventType() {
        return lcmEventType;
    }

    public void setLcmEventType(LCMEventTypeEnum lcmEventType) {
        this.lcmEventType = lcmEventType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
