package tech.tongyu.bct.report.dto.report;

public class PnlReportTotalRowDTO implements HasBookName {

    private String rowId;

    String bookId;

    String instrumentId;

    Double optionPnl;

    Double hedgingPnl;

    Double totalPnl;

    public String getBookName() {
        return bookId;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId() {
        this.rowId = this.bookId + "-" + this.instrumentId;
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

    public Double getOptionPnl() {
        return optionPnl;
    }

    public void setOptionPnl(Double optionPnl) {
        this.optionPnl = optionPnl;
    }

    public Double getHedgingPnl() {
        return hedgingPnl;
    }

    public void setHedgingPnl(Double hedgingPnl) {
        this.hedgingPnl = hedgingPnl;
    }

    public Double getTotalPnl() {
        return totalPnl;
    }

    public void setTotalPnl(Double totalPnl) {
        this.totalPnl = totalPnl;
    }
}
