package tech.tongyu.bct.trade.dto.event;

import tech.tongyu.bct.trade.dto.TradeEventTypeEnum;

import java.time.Instant;

public class TradeEventDTO {

    public String tradeId;

    public String userLoginId;

    public TradeEventTypeEnum eventType;

    public Instant createdAt;


    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getUserLoginId() {
        return userLoginId;
    }

    public void setUserLoginId(String userLoginId) {
        this.userLoginId = userLoginId;
    }

    public TradeEventTypeEnum getEventType() {
        return eventType;
    }

    public void setEventType(TradeEventTypeEnum eventType) {
        this.eventType = eventType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
