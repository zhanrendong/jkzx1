package tech.tongyu.bct.market.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Intraday and close combined. Before each day's close, close/settle etc. from close instance will be missing.
 * Last instance will only show the lastest quotes. This is consistent with current market data design - only snapshots
 * are persisted.
 */
public class CombinedQuoteDTO {
    @BctField(name = "instrumentId", description = "标的物ID", type = "String")
    private String instrumentId;
    @BctField(name = "instrumentName", description = "标的物名称", type = "String")
    private String instrumentName;
    @BctField(name = "intradayQuoteTimestamp", description = "日间行情时间戳", type = "LocalDateTime")
    private LocalDateTime intradayQuoteTimestamp;
    @BctField(name = "closeValuationDate", description = "收盘日期", type = "LocalDate")
    private LocalDate closeValuationDate;
    // a list of possible quote fields. may expand over time.
    // LAST, BID, ASK, YESTERDAY_CLOSE, OPEN, HIGH, LOW, CLOSE, SETTLE;
    @BctField(name = "last", description = "最新成交价格", type = "Double")
    private Double last;
    @BctField(name = "bid", description = "买价", type = "Double")
    private Double bid;
    @BctField(name = "ask", description = "卖价", type = "Double")
    private Double ask;
    @BctField(name = "close", description = "今收", type = "Double")
    private Double close;
    @BctField(name = "yesterdayClose", description = "昨收", type = "Double")
    private Double yesterdayClose;
    @BctField(name = "settle", description = "结算价", type = "Double")
    private Double settle;
    @BctField(name = "exchange", description = "交易所代码", type = "ExchangeEnum")
    private ExchangeEnum exchange;
    @BctField(name = "multiplier", description = "合约乘数", type = "Integer")
    private Integer multiplier = 1;
    @BctField(name = "assetClass", description = "资产类别", type = "AssetClassEnum")
    private AssetClassEnum assetClass;
    @BctField(name = "instrumentType", description = "标的物合约类型", type = "InstrumentTypeEnum")
    private InstrumentTypeEnum instrumentType;
    @BctField(name = "maturity", description = "合约到期日", type = "LocalDate")
    private LocalDate maturity;
    // timezone
    @BctField(name = "quoteTimezone", description = "时区", type = "LocalDateTime")
    private ZoneId quoteTimezone;

    public CombinedQuoteDTO() {
    }

    public CombinedQuoteDTO(String instrumentId, String instrumentName, LocalDateTime intradayQuoteTimestamp,
                            LocalDate closeValuationDate, Double last, Double bid, Double ask, Double close,
                            Double yesterdayClose, Double settle, ExchangeEnum exchange, Integer multiplier,
                            AssetClassEnum assetClass, InstrumentTypeEnum instrumentType,
                            LocalDate maturity, ZoneId quoteTimezone) {
        this.instrumentId = instrumentId;
        this.instrumentName = instrumentName;
        this.intradayQuoteTimestamp = intradayQuoteTimestamp;
        this.closeValuationDate = closeValuationDate;
        this.last = last;
        this.bid = bid;
        this.ask = ask;
        this.close = close;
        this.yesterdayClose = yesterdayClose;
        this.settle = settle;
        this.exchange = exchange;
        this.multiplier = multiplier;
        this.assetClass = assetClass;
        this.instrumentType = instrumentType;
        this.maturity = maturity;
        this.quoteTimezone = quoteTimezone;
    }

    public void setQuoteField(QuoteFieldEnum field, Double value) {
        if (Objects.isNull(value))
            return;
        switch (field) {
            case CLOSE:
                this.close = value;
                break;
            case BID:
                this.bid = value;
                break;
            case ASK:
                this.ask = value;
                break;
            case LAST:
                this.last = value;
                break;
            case YESTERDAY_CLOSE:
                this.yesterdayClose = value;
                break;
            case SETTLE:
                this.settle = value;
                break;
        }
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public LocalDateTime getIntradayQuoteTimestamp() {
        return intradayQuoteTimestamp;
    }

    public void setIntradayQuoteTimestamp(LocalDateTime intradayQuoteTimestamp) {
        this.intradayQuoteTimestamp = intradayQuoteTimestamp;
    }

    public LocalDate getCloseValuationDate() {
        return closeValuationDate;
    }

    public void setCloseValuationDate(LocalDate closeValuationDate) {
        this.closeValuationDate = closeValuationDate;
    }

    public Double getLast() {
        return last;
    }

    public void setLast(Double last) {
        this.last = last;
    }

    public Double getBid() {
        return bid;
    }

    public void setBid(Double bid) {
        this.bid = bid;
    }

    public Double getAsk() {
        return ask;
    }

    public void setAsk(Double ask) {
        this.ask = ask;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Double getYesterdayClose() {
        return yesterdayClose;
    }

    public void setYesterdayClose(Double yesterdayClose) {
        this.yesterdayClose = yesterdayClose;
    }

    public Double getSettle() {
        return settle;
    }

    public void setSettle(Double settle) {
        this.settle = settle;
    }

    public ExchangeEnum getExchange() {
        return exchange;
    }

    public void setExchange(ExchangeEnum exchange) {
        this.exchange = exchange;
    }

    public Integer getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Integer multiplier) {
        this.multiplier = multiplier;
    }

    public AssetClassEnum getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(AssetClassEnum assetClass) {
        this.assetClass = assetClass;
    }

    public InstrumentTypeEnum getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(InstrumentTypeEnum instrumentType) {
        this.instrumentType = instrumentType;
    }

    public ZoneId getQuoteTimezone() {
        return quoteTimezone;
    }

    public void setQuoteTimezone(ZoneId quoteTimezone) {
        this.quoteTimezone = quoteTimezone;
    }

    public LocalDate getMaturity() {
        return maturity;
    }

    public void setMaturity(LocalDate maturity) {
        this.maturity = maturity;
    }
}
