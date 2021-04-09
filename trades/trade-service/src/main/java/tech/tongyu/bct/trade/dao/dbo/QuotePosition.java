package tech.tongyu.bct.trade.dao.dbo;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.cm.product.iov.CashFlowDirectionEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.common.jpa.JsonConverter;
import tech.tongyu.bct.trade.service.TradeService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = TradeService.SCHEMA)
public class QuotePosition {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column
    private String userName;

    @Column
    private String quotePrcId;

    @Column
    private String counterPartyCode;

    @Column
    private String pricingEnvironmentId;

    @Column
    private String underlyerInstrumentId;

    @Column
    @Enumerated(EnumType.STRING)
    private OptionTypeEnum optionType;

    @Column
    @Enumerated(EnumType.STRING)
    private ProductTypeEnum productType;

    @Column
    @Enumerated(EnumType.STRING)
    private InstrumentOfValuePartyRoleTypeEnum direction;

    @Column
    @Enumerated(EnumType.STRING)
    private CashFlowDirectionEnum paymentDirection;

    @Convert(converter = JsonConverter.class)
    @Column(nullable = false, length = JsonConverter.LENGTH)
    private JsonNode asset;

    @Column(precision=19,scale=4)
    private BigDecimal underlyerPrice;

    @Column(precision=19,scale=4)
    private BigDecimal vol;

    @Column(precision=19,scale=4)
    private BigDecimal r;

    @Column(precision=19,scale=4)
    private BigDecimal q;

    @Convert(converter = JsonConverter.class)
    @Column(length = JsonConverter.LENGTH)
    private JsonNode prcResult;

    @Column
    private String comment;

    @Column
    private LocalDate expirationDate;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    @Column
    private Instant updatedAt;

    public QuotePosition() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getQuotePrcId() {
        return quotePrcId;
    }

    public void setQuotePrcId(String quotePrcId) {
        this.quotePrcId = quotePrcId;
    }

    public String getCounterPartyCode() {
        return counterPartyCode;
    }

    public void setCounterPartyCode(String counterPartyCode) {
        this.counterPartyCode = counterPartyCode;
    }

    public String getPricingEnvironmentId() {
        return pricingEnvironmentId;
    }

    public void setPricingEnvironmentId(String pricingEnvironmentId) {
        this.pricingEnvironmentId = pricingEnvironmentId;
    }

    public String getUnderlyerInstrumentId() {
        return underlyerInstrumentId;
    }

    public void setUnderlyerInstrumentId(String underlyerInstrumentId) {
        this.underlyerInstrumentId = underlyerInstrumentId;
    }

    public OptionTypeEnum getOptionType() {
        return optionType;
    }

    public void setOptionType(OptionTypeEnum optionType) {
        this.optionType = optionType;
    }

    public ProductTypeEnum getProductType() {
        return productType;
    }

    public void setProductType(ProductTypeEnum productType) {
        this.productType = productType;
    }

    public InstrumentOfValuePartyRoleTypeEnum getDirection() {
        return direction;
    }

    public void setDirection(InstrumentOfValuePartyRoleTypeEnum direction) {
        this.direction = direction;
    }

    public JsonNode getAsset() {
        return asset;
    }

    public void setAsset(JsonNode asset) {
        this.asset = asset;
    }

    public BigDecimal getUnderlyerPrice() {
        return underlyerPrice;
    }

    public void setUnderlyerPrice(BigDecimal underlyerPrice) {
        this.underlyerPrice = underlyerPrice;
    }

    public BigDecimal getVol() {
        return vol;
    }

    public void setVol(BigDecimal vol) {
        this.vol = vol;
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

    public JsonNode getPrcResult() {
        return prcResult;
    }

    public void setPrcResult(JsonNode prcResult) {
        this.prcResult = prcResult;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
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

    public CashFlowDirectionEnum getPaymentDirection() {
        return paymentDirection;
    }

    public void setPaymentDirection(CashFlowDirectionEnum paymentDirection) {
        this.paymentDirection = paymentDirection;
    }
}
