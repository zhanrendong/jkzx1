package tech.tongyu.bct.market.dao.dbo;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.common.jpa.JsonConverter;
import tech.tongyu.bct.market.dto.AssetClassEnum;
import tech.tongyu.bct.market.dto.AssetSubClassEnum;
import tech.tongyu.bct.market.dto.InstrumentTypeEnum;
import tech.tongyu.bct.market.service.MarketDataService;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = MarketDataService.SCHEMA)
public class Instrument {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String instrumentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetClassEnum assetClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstrumentTypeEnum instrumentType;

    @Enumerated(EnumType.STRING)
    private AssetSubClassEnum assetSubClass;

    @Convert(converter = JsonConverter.class)
    @Column(nullable = false, length = JsonConverter.LENGTH)
    private JsonNode instrumentInfo;

    @CreationTimestamp
    @Column
    private Instant createdAt;

    @UpdateTimestamp
    @Column
    private Instant updatedAt;

    public Instrument() {
    }

    public Instrument(String instrumentId, AssetClassEnum assetClass, AssetSubClassEnum assetSubClass,
                      InstrumentTypeEnum instrumentType, JsonNode instrumentInfo) {
        this.instrumentId = instrumentId;
        this.assetClass = assetClass;
        this.assetSubClass = assetSubClass;
        this.instrumentType = instrumentType;
        this.instrumentInfo = instrumentInfo;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
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

    public JsonNode getInstrumentInfo() {
        return instrumentInfo;
    }

    public void setInstrumentInfo(JsonNode instrumentInfo) {
        this.instrumentInfo = instrumentInfo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public AssetSubClassEnum getAssetSubClass() {
        return assetSubClass;
    }

    public void setAssetSubClass(AssetSubClassEnum assetSubClass) {
        this.assetSubClass = assetSubClass;
    }
}
