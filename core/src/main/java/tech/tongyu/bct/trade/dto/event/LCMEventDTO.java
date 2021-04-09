package tech.tongyu.bct.trade.dto.event;

import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class LCMEventDTO {
    @BctField(name = "uuid", description = "唯一标识", type = "String")
    public String uuid;
    @BctField(name = "tradeId", description = "交易ID", type = "String")
    public String tradeId;
    @BctField(name = "positionId", description = "持仓ID", type = "String")
    public String positionId;
    @BctField(name = "userLoginId", description = "用户ID", type = "String")
    public String userLoginId;
    @BctField(name = "premium", description = "权利金", type = "BigDecimal")
    public BigDecimal premium;
    @BctField(name = "cashFlow", description = "现金流", type = "BigDecimal")
    public BigDecimal cashFlow;
    @BctField(name = "lcmEventType", description = "生命周期事件类型", type = "LCMEventTypeEnum")
    public LCMEventTypeEnum lcmEventType;
    @BctField(name = "eventDetail", description = "生命周期事件细节", type = "Map")
    public Map<String, Object> eventDetail;
    @BctField(name = "paymentDate", description = "支付日", type = "LocalDate")
    public LocalDate paymentDate;
    @BctField(name = "createdAt", description = "创建日期", type = "LocalDateTime")
    public LocalDateTime createdAt;

    public LCMEventDTO() {

    }

    // ---------------------Property Generate Get/Set Method-----------------


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getUserLoginId() {
        return userLoginId;
    }

    public void setUserLoginId(String userLoginId) {
        this.userLoginId = userLoginId;
    }

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }

    public BigDecimal getCashFlow() {
        return cashFlow;
    }

    public void setCashFlow(BigDecimal cashFlow) {
        this.cashFlow = cashFlow;
    }

    public LCMEventTypeEnum getLcmEventType() {
        return lcmEventType;
    }

    public void setLcmEventType(LCMEventTypeEnum lcmEventType) {
        this.lcmEventType = lcmEventType;
    }

    public Map<String, Object> getEventDetail() {
        return eventDetail;
    }

    public void setEventDetail(Map<String, Object> eventDetail) {
        this.eventDetail = eventDetail;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
