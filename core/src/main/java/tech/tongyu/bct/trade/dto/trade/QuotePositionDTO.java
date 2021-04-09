package tech.tongyu.bct.trade.dto.trade;

import com.fasterxml.jackson.databind.JsonNode;
import tech.tongyu.bct.client.dto.PaymentDirectionEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class QuotePositionDTO {
    @BctField(name = "uuid", description = "唯一标识", type = "String")
    private String uuid;
    @BctField(name = "userName", description = "用户名", type = "String")
    private String userName;
    @BctField(name = "quotePrcId", description = "试定价结果集ID", type = "String")
    private String quotePrcId;
    @BctField(name = "counterPartyCode", description = "交易对手", type = "String")
    private String counterPartyCode;
    @BctField(name = "pricingEnvironmentId", description = "定价环境ID", type = "String")
    private String pricingEnvironmentId;
    @BctField(name = "underlyerInstrumentId", description = "标的物ID", type = "String")
    private String underlyerInstrumentId;
    @BctField(name = "optionType", description = "看涨/跌", type = "OptionTypeEnum")
    private OptionTypeEnum optionType;
    @BctField(name = "productType", description = "期权类型", type = "ProductTypeEnum")
    private ProductTypeEnum productType;
    @BctField(name = "direction", description = "买卖方向", type = "InstrumentOfValuePartyRoleTypeEnum")
    private InstrumentOfValuePartyRoleTypeEnum direction;
    @BctField(name = "paymentDirection", description = "付款方向", type = "PaymentDirectionEnum")
    private PaymentDirectionEnum paymentDirection;
    @BctField(name = "asset", description = "合约内容", type = "JsonNode")
    private JsonNode asset;
    @BctField(name = "underlyerPrice", description = "标的物价格", type = "BigDecimal")
    private BigDecimal underlyerPrice;
    @BctField(name = "vol", description = "波动率", type = "BigDecimal")
    private BigDecimal vol;
    @BctField(name = "r", description = "r", type = "BigDecimal")
    private BigDecimal r;
    @BctField(name = "q", description = "q", type = "BigDecimal")
    private BigDecimal q;
    @BctField(name = "JsonNode", description = "定价结果", type = "prcResult")
    private JsonNode prcResult;
    @BctField(name = "expirationDate", description = "到期日", type = "LocalDate")
    private LocalDate expirationDate;
    @BctField(name = "createdAt", description = "创建日期", type = "LocalDate")
    private LocalDateTime createdAt;
    @BctField(name = "updatedAt", description = "更新日期", type = "LocalDate")
    private LocalDateTime updatedAt;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
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

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public PaymentDirectionEnum getPaymentDirection() {
        return paymentDirection;
    }

    public void setPaymentDirection(PaymentDirectionEnum paymentDirection) {
        this.paymentDirection = paymentDirection;
    }
}
