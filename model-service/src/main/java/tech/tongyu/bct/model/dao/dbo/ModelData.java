package tech.tongyu.bct.model.dao.dbo;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.common.jpa.JsonConverter;
import tech.tongyu.bct.market.dto.InstanceEnum;
import tech.tongyu.bct.model.dto.ModelTypeEnum;
import tech.tongyu.bct.model.service.ModelService;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(schema = ModelService.SCHEMA)
public class ModelData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String modelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModelTypeEnum modelType;

    @Column(nullable = false)
    private String modelName;

    // JPA enum
    // Will be converted into string. Postgresql has its own enum type, but not used here.
    @Enumerated(EnumType.STRING) // default is ordinal
    @Column(nullable = false)
    private InstanceEnum instance;

    // Json: Stored as string
    @Convert(converter = JsonConverter.class)
    // 常量定义: model, market data. where to put it?
    @Column(nullable = false, length = JsonConverter.LENGTH)
    private JsonNode modelData;

    @Column(nullable = false)
    private LocalDate valuationDate;

    @Column(nullable = false)
    private ZoneId modelTimezone;

    @Column
    private String underlyer;

    @Convert(converter = JsonConverter.class)
    @Column(length = JsonConverter.LENGTH)
    private JsonNode modelInfo;

    // JPA
    // for Spring, see Spring Data JPA Auditing
    @CreationTimestamp
    @Column
    private Instant createdAt;

    @UpdateTimestamp
    @Column
    private Instant updatedAt;

//    @Column
//    private String createdBy;

    public ModelData() {
    }

    public ModelData(String modelName, InstanceEnum instance,
                     JsonNode modelData, LocalDate valuationDate, ZoneId modelTimezone,
                     String underlyer, JsonNode modelInfo,
                     String modelId, ModelTypeEnum modelType) {
        this.modelName = modelName;
        this.instance = instance;
        this.modelData = modelData;
        this.valuationDate = valuationDate;
        this.modelTimezone = modelTimezone;
        this.underlyer = underlyer;
        this.modelInfo = modelInfo;
        this.modelId = modelId;
        this.modelType = modelType;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public InstanceEnum getInstance() {
        return instance;
    }

    public void setInstance(InstanceEnum instance) {
        this.instance = instance;
    }

    public JsonNode getModelData() {
        return modelData;
    }

    public void setModelData(JsonNode modelData) {
        this.modelData = modelData;
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

    public String getUnderlyer() {
        return underlyer;
    }

    public void setUnderlyer(String underlyer) {
        this.underlyer = underlyer;
    }

    public JsonNode getModelInfo() {
        return modelInfo;
    }

    public void setModelInfo(JsonNode modelInfo) {
        this.modelInfo = modelInfo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
