package tech.tongyu.bct.report.dto.report;

import java.time.LocalDate;

public class PnlReportHedgingRowDTO implements HasBookName {

    String bookId;

    String instrumentId;

    Integer multiplier;

    LocalDate dealDate;

    Double totalBuy;

    Double totalSell;

    Double totalBuyInLot;

    Double totalSellInLot;

    Double totalPositionInLot;

    Double marketValue;

    Double totalPnl;

    public String getBookId() {
        return bookId;
    }

    public String getBookName() {
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

    public Integer getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Integer multiplier) {
        this.multiplier = multiplier;
    }

    public LocalDate getDealDate() {
        return dealDate;
    }

    public void setDealDate(LocalDate dealDate) {
        this.dealDate = dealDate;
    }

    public Double getTotalBuy() {
        return totalBuy;
    }

    public void setTotalBuy(Double totalBuy) {
        this.totalBuy = totalBuy;
    }

    public Double getTotalSell() {
        return totalSell;
    }

    public void setTotalSell(Double totalSell) {
        this.totalSell = totalSell;
    }

    public Double getTotalBuyInLot() {
        return totalBuyInLot;
    }

    public void setTotalBuyInLot(Double totalBuyInLot) {
        this.totalBuyInLot = totalBuyInLot;
    }

    public Double getTotalSellInLot() {
        return totalSellInLot;
    }

    public void setTotalSellInLot(Double totalSellInLot) {
        this.totalSellInLot = totalSellInLot;
    }

    public Double getTotalPositionInLot() {
        return totalPositionInLot;
    }

    public void setTotalPositionInLot(Double totalPositionInLot) {
        this.totalPositionInLot = totalPositionInLot;
    }

    public Double getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(Double marketValue) {
        this.marketValue = marketValue;
    }

    public Double getTotalPnl() {
        return totalPnl;
    }

    public void setTotalPnl(Double totalPnl) {
        this.totalPnl = totalPnl;
    }
}
