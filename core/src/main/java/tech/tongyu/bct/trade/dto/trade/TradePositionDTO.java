package tech.tongyu.bct.trade.dto.trade;

import com.fasterxml.jackson.databind.JsonNode;
import tech.tongyu.bct.cm.product.iov.InstrumentAssetClassTypeEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.Instant;

public class TradePositionDTO {
    @BctField(name = "bookName", description = "交易簿名称", type = "String")
    public String bookName;
    @BctField(name = "positionId", description = "持仓ID", type = "String")
    public String positionId;
    @BctField(name = "userLoginId", description = "用户ID", type = "String")
    public String userLoginId;
    @BctField(name = "quantity", description = "数量", type = "BigDecimal")
    public BigDecimal quantity;
    @BctField(name = "lcmEventType", description = "生命周期类型", type = "LCMEventTypeEnum")
    public LCMEventTypeEnum lcmEventType;
    @BctField(name = "productType", description = "期权类型", type = "ProductTypeEnum")
    public ProductTypeEnum productType;
    @BctField(name = "assetClass", description = "资产类别", type = "InstrumentAssetClassTypeEnum")
    public InstrumentAssetClassTypeEnum assetClass;
    @BctField(name = "asset", description = "合约内容", type = "JsonNode")
    public JsonNode asset;
    @BctField(name = "counterPartyCode", description = "交易对手代码", type = "String")
    public String counterPartyCode;
    @BctField(name = "counterPartyName", description = "交易对手", type = "String")
    public String counterPartyName;
    @BctField(name = "positionAccountCode", description = "交易账户代码", type = "String")
    public String positionAccountCode;
    @BctField(name = "positionAccountName", description = "交易账户", type = "String")
    public String positionAccountName;
    @BctField(name = "counterPartyAccountCode", description = "交易对手账户代码", type = "String")
    public String counterPartyAccountCode;
    @BctField(name = "counterPartyAccountName", description = "交易对手账户", type = "String")
    public String counterPartyAccountName;
    @BctField(name = "createdAt", description = "创建时间", type = "Instant")
    public Instant createdAt;
    @BctField(name = "updatedAt", description = "更新时间", type = "Instant")
    public Instant updatedAt;

    public TradePositionDTO() {
    }

    public TradePositionDTO(String bookName, String positionId, BigDecimal quantity, JsonNode asset,
                            ProductTypeEnum productType, InstrumentAssetClassTypeEnum assetClass,
                            String counterPartyCode, String counterPartyName,
                            String positionAccountCode, String counterpartyAccountCode) {
        this.bookName = bookName;
        this.positionId = positionId;
        this.quantity = quantity;
        this.asset = asset;
        this.productType = productType;
        this.assetClass = assetClass;
        this.counterPartyCode = counterPartyCode;
        this.counterPartyName = counterPartyName;
        this.positionAccountCode = positionAccountCode;
        this.counterPartyAccountCode = counterpartyAccountCode;
    }


    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getUserLoginId() {
        return userLoginId;
    }

    public void setUserLoginId(String userLoginId) {
        this.userLoginId = userLoginId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public LCMEventTypeEnum getLcmEventType() {
        return lcmEventType;
    }

    public void setLcmEventType(LCMEventTypeEnum lcmEventType) {
        this.lcmEventType = lcmEventType;
    }

    public ProductTypeEnum getProductType() {
        return productType;
    }

    public void setProductType(ProductTypeEnum productType) {
        this.productType = productType;
    }

    public InstrumentAssetClassTypeEnum getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(InstrumentAssetClassTypeEnum assetClass) {
        this.assetClass = assetClass;
    }

    public JsonNode getAsset() {
        return asset;
    }

    public void setAsset(JsonNode asset) {
        this.asset = asset;
    }

    public String getCounterPartyCode() {
        return counterPartyCode;
    }

    public void setCounterPartyCode(String counterPartyCode) {
        this.counterPartyCode = counterPartyCode;
    }

    public String getCounterPartyName() {
        return counterPartyName;
    }

    public void setCounterPartyName(String counterPartyName) {
        this.counterPartyName = counterPartyName;
    }

    public String getPositionAccountCode() {
        return positionAccountCode;
    }

    public void setPositionAccountCode(String positionAccountCode) {
        this.positionAccountCode = positionAccountCode;
    }

    public String getPositionAccountName() {
        return positionAccountName;
    }

    public void setPositionAccountName(String positionAccountName) {
        this.positionAccountName = positionAccountName;
    }

    public String getCounterPartyAccountCode() {
        return counterPartyAccountCode;
    }

    public void setCounterPartyAccountCode(String counterPartyAccountCode) {
        this.counterPartyAccountCode = counterPartyAccountCode;
    }

    public String getCounterPartyAccountName() {
        return counterPartyAccountName;
    }

    public void setCounterPartyAccountName(String counterPartyAccountName) {
        this.counterPartyAccountName = counterPartyAccountName;
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
