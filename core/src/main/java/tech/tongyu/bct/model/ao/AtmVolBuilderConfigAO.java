package tech.tongyu.bct.model.ao;

import tech.tongyu.bct.market.dto.InstanceEnum;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 创建ATM波动率曲面所需输入
 */
public class AtmVolBuilderConfigAO {
    private String modelName;
    private LocalDate valuationDate;
    private ZoneId timezone;
    private InstanceEnum instance;
    private VolUnderlyerAO underlyer;
    private List<AtmVolInstrumentAO> instruments;

    public AtmVolBuilderConfigAO() {
    }

    /**
     * 构造函数
     * @param modelName 模型名称。同一名称可以按估值日，创建时间有不同版本/实例。
     * @param valuationDate 估值日, 即波动率曲面起始日期
     * @param timezone 时区
     * @param instance 日内/收盘
     * @param underlyer 标的物{@link VolUnderlyerAO}
     * @param instruments 用于创建模型的输入{@link AtmVolInstrumentAO}
     */
    public AtmVolBuilderConfigAO(String modelName, LocalDate valuationDate, ZoneId timezone, InstanceEnum instance,
                                 VolUnderlyerAO underlyer, List<AtmVolInstrumentAO> instruments) {
        this.modelName = modelName;
        this.valuationDate = valuationDate;
        this.timezone = timezone;
        this.instance = instance;
        this.underlyer = underlyer;
        this.instruments = instruments;
    }

    public String getModelId() {
        return modelName + "|VOL_SURFACE|" + instance + "|" + underlyer.getInstrumentId() + "|"
                + valuationDate.format(DateTimeFormatter.BASIC_ISO_DATE) + "|"
                + timezone;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
    }

    public ZoneId getTimezone() {
        return timezone;
    }

    public void setTimezone(ZoneId timezone) {
        this.timezone = timezone;
    }

    public VolUnderlyerAO getUnderlyer() {
        return underlyer;
    }

    public void setUnderlyer(VolUnderlyerAO underlyer) {
        this.underlyer = underlyer;
    }

    public List<AtmVolInstrumentAO> getInstruments() {
        return instruments;
    }

    public void setInstruments(List<AtmVolInstrumentAO> instruments) {
        this.instruments = instruments;
    }

    public InstanceEnum getInstance() {
        return instance;
    }

    public void setInstance(InstanceEnum instance) {
        this.instance = instance;
    }
}
