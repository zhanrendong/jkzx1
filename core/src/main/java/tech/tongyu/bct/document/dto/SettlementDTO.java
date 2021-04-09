package tech.tongyu.bct.document.dto;


/**
 * 结算通知书
 */
public class SettlementDTO {

    private String tradeId;
    private Integer id;
    private String instrumentName;
    private String underlyerInstrumentId;
    private String direction;
    private String productType;
    private Object optionType;
    private Object strike;
    private String expirationDate;
    private String settlementDate;
    private Object initialValue;
    private Object historyValue;
    private Object remainValue;
    private Object underlyerPrice;
    private Object settleAmount;

    public SettlementDTO() {
    }

    public SettlementDTO(String tradeId, Integer id, String instrumentName, String underlyerInstrumentId, String direction, String productType, Object optionType, Object strike, String expirationDate, String settlementDate, Object initialValue, Object historyValue, Object remainValue, Object underlyerPrice, Object settleAmount) {
        this.tradeId = tradeId;
        this.id = id;
        this.instrumentName = instrumentName;
        this.underlyerInstrumentId = underlyerInstrumentId;
        this.direction = direction;
        this.productType = productType;
        this.optionType = optionType;
        this.strike = strike;
        this.expirationDate = expirationDate;
        this.settlementDate = settlementDate;
        this.initialValue = initialValue;
        this.historyValue = historyValue;
        this.remainValue = remainValue;
        this.underlyerPrice = underlyerPrice;
        this.settleAmount = settleAmount;
    }

    public Object getUnderlyerPrice() {
        return underlyerPrice;
    }

    public void setUnderlyerPrice(Object underlyerPrice) {
        this.underlyerPrice = underlyerPrice;
    }

    public Object getSettleAmount() {
        return settleAmount;
    }

    public void setSettleAmount(Object settleAmount) {
        this.settleAmount = settleAmount;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public String getUnderlyerInstrumentId() {
        return underlyerInstrumentId;
    }

    public void setUnderlyerInstrumentId(String underlyerInstrumentId) {
        this.underlyerInstrumentId = underlyerInstrumentId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public Object getOptionType() {
        return optionType;
    }

    public void setOptionType(Object optionType) {
        this.optionType = optionType;
    }

    public Object getStrike() {
        return strike;
    }

    public void setStrike(Object strike) {
        this.strike = strike;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(String settlementDate) {
        this.settlementDate = settlementDate;
    }

    public Object getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(Object initialValue) {
        this.initialValue = initialValue;
    }

    public Object getHistoryValue() {
        return historyValue;
    }

    public void setHistoryValue(Object historyValue) {
        this.historyValue = historyValue;
    }

    public Object getRemainValue() {
        return remainValue;
    }

    public void setRemainValue(Object remainValue) {
        this.remainValue = remainValue;
    }
}
