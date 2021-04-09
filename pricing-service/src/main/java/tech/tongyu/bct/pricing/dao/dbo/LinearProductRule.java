package tech.tongyu.bct.pricing.dao.dbo;

import tech.tongyu.bct.market.dto.InstanceEnum;;
import tech.tongyu.bct.market.dto.QuoteFieldEnum;
import tech.tongyu.bct.pricing.service.PricingService;
import tech.tongyu.bct.quant.library.priceable.AssetClass;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = PricingService.SCHEMA)
public class LinearProductRule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false)
    private String pricingEnvironmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetClass.AssetClassEnum assetClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeListed.InstrumentTypeEnum instrumentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstanceEnum instance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteFieldEnum field;

    public LinearProductRule() {
    }

    public LinearProductRule(String pricingEnvironmentId, AssetClass.AssetClassEnum assetClass,
                             ExchangeListed.InstrumentTypeEnum instrumentType, InstanceEnum instance, QuoteFieldEnum field) {
        this.pricingEnvironmentId = pricingEnvironmentId;
        this.assetClass = assetClass;
        this.instrumentType = instrumentType;
        this.instance = instance;
        this.field = field;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPricingEnvironmentId() {
        return pricingEnvironmentId;
    }

    public void setPricingEnvironmentId(String pricingEnvironmentId) {
        this.pricingEnvironmentId = pricingEnvironmentId;
    }

    public AssetClass.AssetClassEnum getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(AssetClass.AssetClassEnum assetClass) {
        this.assetClass = assetClass;
    }

    public ExchangeListed.InstrumentTypeEnum getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(ExchangeListed.InstrumentTypeEnum instrumentType) {
        this.instrumentType = instrumentType;
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
