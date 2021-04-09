package tech.tongyu.bct.exchange.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;

public class PositionPortfolioRecordDTO {

    public enum Key {
        BOOK_INSTRUMENT,
        PRICING_OTHERS,
        PORTFOLIO_INSTRUMENT,
        PORTFOLIO_BOOK_INSTRUMENT
    }

    @BctField(name = "portfolioName", description = "投资组合名称", type = "String")
    private String portfolioName;
    @BctField(name = "instrumentId", description = "标的物ID", type = "String")
    private String instrumentId;
    @BctField(name = "bookId", description = "交易簿ID", type = "String")
    private String bookId;
    @BctField(name = "longPosition", description = "多头头寸", type = "BigDecimal")
    private BigDecimal longPosition;
    @BctField(name = "shortPosition", description = "空头头寸", type = "BigDecimal")
    private BigDecimal shortPosition;
    @BctField(name = "netPosition", description = "净头寸", type = "BigDecimal")
    private BigDecimal netPosition;
    @BctField(name = "totalSell", description = "总卖", type = "BigDecimal")
    private BigDecimal totalSell;
    @BctField(name = "totalBuy", description = "总买", type = "BigDecimal")
    private BigDecimal totalBuy;
    @BctField(name = "historyBuyAmount", description = "历史买入量", type = "BigDecimal")
    private BigDecimal historyBuyAmount;
    @BctField(name = "historySellAmount", description = "历史卖出量", type = "BigDecimal")
    private BigDecimal historySellAmount;
    @BctField(name = "marketValue", description = "市值", type = "BigDecimal")
    private BigDecimal marketValue;
    @BctField(name = "totalPnl", description = "总盈亏", type = "BigDecimal")
    private BigDecimal totalPnl;

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
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

    public BigDecimal getTotalSell() {
        return totalSell;
    }

    public void setTotalSell(BigDecimal totalSell) {
        this.totalSell = totalSell;
    }

    public BigDecimal getTotalBuy() {
        return totalBuy;
    }

    public void setTotalBuy(BigDecimal totalBuy) {
        this.totalBuy = totalBuy;
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

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public PositionPortfolioRecordDTO() {
    }

    public PositionPortfolioRecordDTO(String portfolioName, String bookId, String instrumentId, BigDecimal longPosition,
                                      BigDecimal shortPosition, BigDecimal netPosition, BigDecimal totalSell,
                                      BigDecimal totalBuy, BigDecimal historyBuyAmount, BigDecimal historySellAmount,
                                      BigDecimal marketValue, BigDecimal totalPnl) {
        this.portfolioName = portfolioName;
        this.bookId = bookId;
        this.instrumentId = instrumentId;
        this.longPosition = longPosition;
        this.shortPosition = shortPosition;
        this.netPosition = netPosition;
        this.totalSell = totalSell;
        this.totalBuy = totalBuy;
        this.historyBuyAmount = historyBuyAmount;
        this.historySellAmount = historySellAmount;
        this.marketValue = marketValue;
        this.totalPnl = totalPnl;
    }

    public void add(PositionPortfolioRecordDTO that) {
        this.longPosition = longPosition.add(that.getLongPosition());
        this.shortPosition = shortPosition.add(that.getShortPosition());
        this.netPosition = netPosition.add(that.getNetPosition());
        this.totalSell = totalSell.add(that.getTotalSell());
        this.totalBuy = totalBuy.add(that.getTotalBuy());
        this.totalPnl = totalPnl.add(that.getTotalPnl());
        this.historyBuyAmount = historyBuyAmount.add(that.getHistoryBuyAmount());
        this.historySellAmount = historySellAmount.add(that.getHistorySellAmount());
        this.marketValue = marketValue.add(that.getMarketValue());
    }
}
