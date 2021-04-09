package tech.tongyu.bct.model.dto;

import org.springframework.lang.Nullable;
import tech.tongyu.bct.common.Locator;
import tech.tongyu.bct.market.dto.InstanceEnum;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

public class ModelLocator implements Locator {
    private ModelTypeEnum modelType;
    private String modelName;
    @Nullable
    private String underlyer; // discounting curve may not have underlyer
    private InstanceEnum instance;
    private LocalDate valuationDate;
    private ZoneId modelTimezone;

    public ModelLocator() {
    }

    /**
     * 构造函数
     * @param modelType 模型类型{@link ModelTypeEnum}
     * @param modelName 模型名称
     * @param underlyer 模型标的物
     * @param instance 日内/收盘
     * @param valuationDate 估值日
     * @param modelTimezone 时区
     */
    public ModelLocator(ModelTypeEnum modelType, String modelName,
                        @Nullable String underlyer, InstanceEnum instance,
                        LocalDate valuationDate, ZoneId modelTimezone) {
        this.modelType = modelType;
        this.modelName = modelName;
        this.underlyer = underlyer;
        this.instance = instance;
        this.valuationDate = valuationDate;
        this.modelTimezone = modelTimezone;
    }

    public ModelTypeEnum getModelType() {
        return modelType;
    }

    public void setModelType(ModelTypeEnum modelType) {
        this.modelType = modelType;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getUnderlyer() {
        return underlyer;
    }

    public void setUnderlyer(String underlyer) {
        this.underlyer = underlyer;
    }

    public InstanceEnum getInstance() {
        return instance;
    }

    public void setInstance(InstanceEnum instance) {
        this.instance = instance;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
    }

    public ZoneId getModelTimezone() {
        return modelTimezone;
    }

    public void setModelTimezone(ZoneId modelTimezone) {
        this.modelTimezone = modelTimezone;
    }

    @Override
    public String toString() {
        return "ModelLocator{" +
                "modelType=" + modelType +
                ", modelName='" + modelName + '\'' +
                ", underlyer='" + underlyer + '\'' +
                ", instance=" + instance +
                ", valuationDate=" + valuationDate +
                ", modelTimezone=" + modelTimezone +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelLocator)) return false;
        ModelLocator locator = (ModelLocator) o;
        return getModelType() == locator.getModelType() &&
                Objects.equals(getModelName(), locator.getModelName()) &&
                Objects.equals(getUnderlyer(), locator.getUnderlyer()) &&
                getInstance() == locator.getInstance() &&
                Objects.equals(getValuationDate(), locator.getValuationDate()) &&
                Objects.equals(getModelTimezone(), locator.getModelTimezone());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getModelType(), getModelName(), getUnderlyer(), getInstance(),
                getValuationDate(), getModelTimezone());
    }
}
