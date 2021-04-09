package tech.tongyu.bct.pricing.dao.dbo;

import com.fasterxml.jackson.databind.JsonNode;
import tech.tongyu.bct.common.jpa.JsonConverter;
import tech.tongyu.bct.market.dto.InstanceEnum;
import tech.tongyu.bct.market.dto.QuoteFieldEnum;
import tech.tongyu.bct.pricing.service.PricingService;
import tech.tongyu.bct.quant.library.priceable.AssetClass;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = PricingService.SCHEMA)
public class SingleAssetOptionRule {
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
    private Priceable.PriceableTypeEnum productType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeListed.InstrumentTypeEnum underlyerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstanceEnum underlyerInstance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteFieldEnum underlyerField;

    @Column
    private String positionId;

    @Column(nullable = false)
    private String discountingCurveName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstanceEnum discountingCurveInstance;

    @Column
    private String dividendCurveName;

    @Enumerated(EnumType.STRING)
    @Column
    private InstanceEnum dividendCurveInstance;

    @Column(nullable = false)
    private String volSurfaceName;

    @Enumerated(EnumType.STRING)
    @Column
    private InstanceEnum volSurfaceInstance;

    @Convert(converter = JsonConverter.class)
    @Column(nullable = false, length = JsonConverter.LENGTH)
    private JsonNode pricer;

    public SingleAssetOptionRule() {
    }

    public SingleAssetOptionRule(String pricingEnvironmentId,
                                 AssetClass.AssetClassEnum assetClass, Priceable.PriceableTypeEnum productType,
                                 ExchangeListed.InstrumentTypeEnum underlyerType, InstanceEnum underlyerInstance,
                                 QuoteFieldEnum underlyerField,
                                 String positionId,
                                 String discountingCurveName, InstanceEnum discountingCurveInstance,
                                 String dividendCurveName, InstanceEnum dividendCurveInstance,
                                 String volSurfaceName, InstanceEnum volSurfaceInstance,
                                 JsonNode pricer) {
        this.pricingEnvironmentId = pricingEnvironmentId;
        this.assetClass = assetClass;
        this.productType = productType;
        this.underlyerType = underlyerType;
        this.underlyerInstance = underlyerInstance;
        this.underlyerField = underlyerField;
        this.positionId = positionId;
        this.discountingCurveName = discountingCurveName;
        this.discountingCurveInstance = discountingCurveInstance;
        this.dividendCurveName = dividendCurveName;
        this.dividendCurveInstance = dividendCurveInstance;
        this.volSurfaceName = volSurfaceName;
        this.volSurfaceInstance = volSurfaceInstance;
        this.pricer = pricer;
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

    public Priceable.PriceableTypeEnum getProductType() {
        return productType;
    }

    public void setProductType(Priceable.PriceableTypeEnum productType) {
        this.productType = productType;
    }

    public ExchangeListed.InstrumentTypeEnum getUnderlyerType() {
        return underlyerType;
    }

    public void setUnderlyerType(ExchangeListed.InstrumentTypeEnum underlyerType) {
        this.underlyerType = underlyerType;
    }

    public InstanceEnum getUnderlyerInstance() {
        return underlyerInstance;
    }

    public void setUnderlyerInstance(InstanceEnum underlyerInstance) {
        this.underlyerInstance = underlyerInstance;
    }

    public QuoteFieldEnum getUnderlyerField() {
        return underlyerField;
    }

    public void setUnderlyerField(QuoteFieldEnum underlyerField) {
        this.underlyerField = underlyerField;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getDiscountingCurveName() {
        return discountingCurveName;
    }

    public void setDiscountingCurveName(String discountingCurveName) {
        this.discountingCurveName = discountingCurveName;
    }

    public InstanceEnum getDiscountingCurveInstance() {
        return discountingCurveInstance;
    }

    public void setDiscountingCurveInstance(InstanceEnum discountingCurveInstance) {
        this.discountingCurveInstance = discountingCurveInstance;
    }

    public String getDividendCurveName() {
        return dividendCurveName;
    }

    public void setDividendCurveName(String dividendCurveName) {
        this.dividendCurveName = dividendCurveName;
    }

    public InstanceEnum getDividendCurveInstance() {
        return dividendCurveInstance;
    }

    public void setDividendCurveInstance(InstanceEnum dividendCurveInstance) {
        this.dividendCurveInstance = dividendCurveInstance;
    }

    public String getVolSurfaceName() {
        return volSurfaceName;
    }

    public void setVolSurfaceName(String volSurfaceName) {
        this.volSurfaceName = volSurfaceName;
    }

    public InstanceEnum getVolSurfaceInstance() {
        return volSurfaceInstance;
    }

    public void setVolSurfaceInstance(InstanceEnum volSurfaceInstance) {
        this.volSurfaceInstance = volSurfaceInstance;
    }

    public JsonNode getPricer() {
        return pricer;
    }

    public void setPricer(JsonNode pricer) {
        this.pricer = pricer;
    }
}
