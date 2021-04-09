package tech.tongyu.bct.model.dto;

import com.fasterxml.jackson.databind.JsonNode;
import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.market.dto.InstanceEnum;

import java.time.LocalDate;
import java.time.ZoneId;

public class ModelDataDTO {
    @BctField(
            description = "模型名称",
            type = "String",
            order = 1
    )
    private String modelName;
    @BctField(
            description = "标的物",
            type = "String",
            order = 2
    )
    private String underlyer;
    @BctField(
            description = "定价环境",
            type = "InstanceEnum",
            order = 3,
            componentClass = InstanceEnum.class
    )
    private InstanceEnum instance;
    @BctField(
            description = "估值日",
            type = "LocalDate",
            order = 4
    )
    private LocalDate valuationDate;
    @BctField(
            description = "时区",
            type = "ZoneId",
            order = 5
    )
    private ZoneId modelTimezone;
    @BctField(
            description = "模型数据",
            type = "JsonNode",
            order = 6
    )
    private JsonNode modelData;
    @BctField(
            description = "模型信息",
            type = "JsonNode",
            order = 7
    )
    private JsonNode modelInfo;
    @BctField(
            description = "模型ID",
            type = "String",
            order = 8
    )
    private String modelId;
    @BctField(
            description = "模型类型",
            type = "ModelTypeEnum",
            order = 9,
            componentClass = ModelTypeEnum.class
    )
    private ModelTypeEnum modelType;

    public ModelDataDTO(String modelName, String underlyer,
                        InstanceEnum instance, LocalDate valuationDate,
                        ZoneId modelTimezone,
                        JsonNode modelData, JsonNode modelInfo,
                        String modelId, ModelTypeEnum modelType) {
        this.modelName = modelName;
        this.underlyer = underlyer;
        this.instance = instance;
        this.valuationDate = valuationDate;
        this.modelTimezone = modelTimezone;
        this.modelData = modelData;
        this.modelInfo = modelInfo;
        this.modelId = modelId;
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

    public JsonNode getModelData() {
        return modelData;
    }

    public void setModelData(JsonNode modelData) {
        this.modelData = modelData;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public ModelTypeEnum getModelType() {
        return modelType;
    }

    public void setModelType(ModelTypeEnum modelType) {
        this.modelType = modelType;
    }

    public JsonNode getModelInfo() {
        return modelInfo;
    }

    public void setModelInfo(JsonNode modelInfo) {
        this.modelInfo = modelInfo;
    }
}
