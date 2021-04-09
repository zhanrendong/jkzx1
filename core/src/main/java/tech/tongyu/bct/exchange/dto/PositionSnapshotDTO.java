package tech.tongyu.bct.exchange.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.Instant;

public class PositionSnapshotDTO {
    @BctField(name = "uuid", description = "唯一标识", type = "String")
    private String uuid;
    @BctField(name = "bookId", description = "交易簿ID", type = "String")
    private String bookId;
    @BctField(name = "instrumentId", description = "标的物ID", type = "String")
    private String instrumentId;
    @BctField(name = "longPosition", description = "多头头寸", type = "BigDecimal")
    private BigDecimal longPosition;
    @BctField(name = "shortPosition", description = "空头头寸", type = "BigDecimal")
    private BigDecimal shortPosition;
    @BctField(name = "netPosition", description = "净头寸", type = "BigDecimal")
    private BigDecimal netPosition;
    @BctField(name = "historyBuyAmount", description = "历史买入量", type = "BigDecimal")
    private BigDecimal historyBuyAmount;
    @BctField(name = "historySellAmount", description = "历史卖出量", type = "BigDecimal")
    private BigDecimal historySellAmount;
    @BctField(name = "marketValue", description = "市值", type = "BigDecimal")
    private BigDecimal marketValue;
    @BctField(name = "totalPnl", description = "总盈亏", type = "BigDecimal")
    private BigDecimal totalPnl;
    @BctField(name = "createdAt", description = "创建时间", type = "Instant")
    private Instant createdAt;
    @BctField(name = "updatedAt", description = "更新时间", type = "Instant")
    private Instant updatedAt;

    public PositionSnapshotDTO() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public BigDecimal getLongPosition() {
        return longPosition;
    }

    public void setLongPosition(BigDecimal longPosition) {
        this.longPosition = longPosition;
    }

    public BigDecimal getShortPosition() {
        return shortPosition;
    }

    public void setShortPosition(BigDecimal shortPosition) {
        this.shortPosition = shortPosition;
    }

    public BigDecimal getNetPosition() {
        return netPosition;
    }

    public void setNetPosition(BigDecimal netPosition) {
        this.netPosition = netPosition;
    }

    public BigDecimal getHistoryBuyAmount() {
        return historyBuyAmount;
    }

    public void setHistoryBuyAmount(BigDecimal historyBuyAmount) {
        this.historyBuyAmount = historyBuyAmount;
    }

    public BigDecimal getHistorySellAmount() {
        return historySellAmount;
    }

    public void setHistorySellAmount(BigDecimal historySellAmount) {
        this.historySellAmount = historySellAmount;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }

    public BigDecimal getTotalPnl() {
        return totalPnl;
    }

    public void setTotalPnl(BigDecimal totalPnl) {
        this.totalPnl = totalPnl;
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