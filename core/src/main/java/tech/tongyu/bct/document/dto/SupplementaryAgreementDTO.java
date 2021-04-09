package tech.tongyu.bct.document.dto;

/**
 * 补充协议
 */
public class SupplementaryAgreementDTO {

    private Integer id;
    private String instrumentName;
    private String underlyerInstrumentId;
    private String direction;
    private String productType;
    private Object optionType;
    private String effectiveDate;
    private String expirationDate;
    private Object initialSpot;
    private Object strike;
    private Object expireSettle;
    private Object quantity;
    private Object notionalAmount;
    private Object premium;
    private Double margin;
    private Object maintenanceMargin;

    public SupplementaryAgreementDTO() {
    }

    public SupplementaryAgreementDTO(Integer id, String instrumentName, String underlyerInstrumentId, String direction, String productType, Object optionType, String effectiveDate, String expirationDate, Object initialSpot, Object strike, Object expireSettle, Object quantity, Object notionalAmount, Object premium, Double margin, Object maintenanceMargin) {
        this.id = id;
        this.instrumentName = instrumentName;
        this.underlyerInstrumentId = underlyerInstrumentId;
        this.direction = direction;
        this.productType = productType;
        this.optionType = optionType;
        this.effectiveDate = effectiveDate;
        this.expirationDate = expirationDate;
        this.initialSpot = initialSpot;
        this.strike = strike;
        this.expireSettle = expireSettle;
        this.quantity = quantity;
        this.notionalAmount = notionalAmount;
        this.premium = premium;
        this.margin = margin;
        this.maintenanceMargin = maintenanceMargin;
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

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Object getInitialSpot() {
        return initialSpot;
    }

    public void setInitialSpot(Object initialSpot) {
        this.initialSpot = initialSpot;
    }

    public Object getStrike() {
        return strike;
    }

    public void setStrike(Object strike) {
        this.strike = strike;
    }

    public Object getExpireSettle() {
        return expireSettle;
    }

    public void setExpireSettle(Object expireSettle) {
        this.expireSettle = expireSettle;
    }

    public Object getQuantity() {
        return quantity;
    }

    public void setQuantity(Object quantity) {
        this.quantity = quantity;
    }

    public Object getNotionalAmount() {
        return notionalAmount;
    }

    public void setNotionalAmount(Object notionalAmount) {
        this.notionalAmount = notionalAmount;
    }

    public Object getPremium() {
        return premium;
    }

    public void setPremium(Object premium) {
        this.premium = premium;
    }

    public Double getMargin() {
        return margin;
    }

    public void setMargin(Double margin) {
        this.margin = margin;
    }

    public Object getMaintenanceMargin() {
        return maintenanceMargin;
    }

    public void setMaintenanceMargin(Object maintenanceMargin) {
        this.maintenanceMargin = maintenanceMargin;
    }
}
