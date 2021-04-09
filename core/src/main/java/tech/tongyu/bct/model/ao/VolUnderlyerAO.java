package tech.tongyu.bct.model.ao;

import tech.tongyu.bct.market.dto.InstanceEnum;
import tech.tongyu.bct.market.dto.QuoteFieldEnum;

/**
 * 波动率曲面标的物信息，包含标的物以及波动率曲面创建所需要的标的物行情
 */
public class VolUnderlyerAO {
    private String instrumentId;
    private Double quote;
    private InstanceEnum instance;
    private QuoteFieldEnum field;

    public VolUnderlyerAO() {
    }

    /**
     * 构造函数
     * @param instrumentId 标的物ID
     * @param quote 标的物行情
     * @param instance 日内/收盘 {@link InstanceEnum}
     * @param field 行情字段
     */
    public VolUnderlyerAO(String instrumentId, Double quote, InstanceEnum instance, QuoteFieldEnum field) {
        this.instrumentId = instrumentId;
        this.quote = quote;
        this.instance = instance;
        this.field = field;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Double getQuote() {
        return quote;
    }

    public void setQuote(Double quote) {
        this.quote = quote;
    }

    public InstanceEnum getInstance() {
        return instance;
    }

    public void setInstance(InstanceEnum instance) {
        this.instance = instance;
    }

    public QuoteFieldEnum getField() {
        return field;
    }

    public void setField(QuoteFieldEnum field) {
        this.field = field;
    }
}
