package tech.tongyu.bct.market.dto;

public abstract class InstrumentCommonInfo {
    /**
     * 合约名称
     */
    protected String name;
    /**
     * 交易所代码
     */
    protected ExchangeEnum exchange;
    /**
     * 单位
     */
    protected String unit;
    /**
     * 交易单位
     */
    protected String tradeUnit;
    /**
     * 交易品种
     */
    protected String tradeCategory;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getTradeUnit() {
        return tradeUnit;
    }

    public void setTradeUnit(String tradeUnit) {
        this.tradeUnit = tradeUnit;
    }

    public String getTradeCategory() {
        return tradeCategory;
    }

    public void setTradeCategory(String tradeCategory) {
        this.tradeCategory = tradeCategory;
    }

    public ExchangeEnum getExchange() {
        return exchange;
    }

    public void setExchange(ExchangeEnum exchange) {
        this.exchange = exchange;
    }
}
