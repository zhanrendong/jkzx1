package tech.tongyu.bct.report.dto.report;

public class PnlReportOptionRowDTO implements HasBookName {

    String bookId;

    String tradeId;

    String instrumentId;

    Integer instrumentMultiplier;

    Double totalBuy;

    Double totalSell;

    Double totalSettle;

    Double marketValue;

    Double totalPnl;

    public String getBookName() {
        return bookId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Integer getInstrumentMultiplier() {
        return instrumentMultiplier;
    }

    public void setInstrumentMultiplier(Integer instrumentMultiplier) {
        this.instrumentMultiplier = instrumentMultiplier;
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

    public Double getTotalSettle() {
        return totalSettle;
    }

    public void setTotalSettle(Double totalSettle) {
        this.totalSettle = totalSettle;
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
