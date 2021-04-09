package tech.tongyu.bct.pricing.dao.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.lang.Nullable;
import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.market.dto.InstanceEnum;
import tech.tongyu.bct.market.dto.QuoteFieldEnum;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;
import tech.tongyu.bct.quant.library.priceable.AssetClass;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

public class SingleAssetOptionRuleDTO {
    @BctField(description = "资产类别", componentClass = AssetClass.AssetClassEnum.class)
    private AssetClass.AssetClassEnum assetClass;
    @BctField(description = "定价类别", componentClass = Priceable.PriceableTypeEnum.class)
    private Priceable.PriceableTypeEnum productType;
    @BctField(description = "标的物类别", componentClass = ExchangeListed.InstrumentTypeEnum.class)
    private ExchangeListed.InstrumentTypeEnum underlyerType;
    @BctField(description = "标的物行情", componentClass = InstanceEnum.class)
    private InstanceEnum underlyerInstance;
    @BctField(description = "行情字段", componentClass = QuoteFieldEnum.class)
    private QuoteFieldEnum underlyerField;
    @Nullable
    @BctField(description = "唯一持仓ID")
    private String positionId;
    @BctField(description = "无风险利率曲线名称")
    private String discountingCurveName;
    @BctField(description = "无风险利率曲线行情", componentClass = InstanceEnum.class)
    private InstanceEnum discountingCurveInstance;
    @BctField(description = "波动率曲面名称")
    private String volSurfaceName;
    @BctField(description = "波动率曲面行情", componentClass = InstanceEnum.class)
    private InstanceEnum volSurfaceInstance;
    @Nullable
    @BctField(description = "分红曲线名称")
    private String dividendCurveName;
    @Nullable
    @BctField(description = "分红曲线行情", componentClass = InstanceEnum.class)
    private InstanceEnum dividendCurveInstance;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @BctField(description = "定价", componentClass = QuantPricerSpec.class)
    private QuantPricerSpec pricer;

    public SingleAssetOptionRuleDTO() {
    }

    public SingleAssetOptionRuleDTO(AssetClass.AssetClassEnum assetClass, Priceable.PriceableTypeEnum productType,
                                    ExchangeListed.InstrumentTypeEnum underlyerType, InstanceEnum underlyerInstance,
                                    QuoteFieldEnum underlyerField,
                                    @Nullable String positionId,
                                    String discountingCurveName, InstanceEnum discountingCurveInstance,
                                    String volSurfaceName, InstanceEnum volSurfaceInstance,
                                    @Nullable String dividendCurveName, @Nullable InstanceEnum dividendCurveInstance,
                                    QuantPricerSpec pricer) {
        this.assetClass = assetClass;
        this.productType = productType;
        this.underlyerType = underlyerType;
        this.underlyerInstance = underlyerInstance;
        this.underlyerField = underlyerField;
        this.positionId = positionId;
        this.discountingCurveName = discountingCurveName;
        this.discountingCurveInstance = discountingCurveInstance;
        this.volSurfaceName = volSurfaceName;
        this.volSurfaceInstance = volSurfaceInstance;
        this.dividendCurveName = dividendCurveName;
        this.dividendCurveInstance = dividendCurveInstance;
        this.pricer = pricer;
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

    @Nullable
    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(@Nullable String positionId) {
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

    @Nullable
    public String getDividendCurveName() {
        return dividendCurveName;
    }

    public void setDividendCurveName(@Nullable String dividendCurveName) {
        this.dividendCurveName = dividendCurveName;
    }

    @Nullable
    public InstanceEnum getDividendCurveInstance() {
        return dividendCurveInstance;
    }

    public void setDividendCurveInstance(@Nullable InstanceEnum dividendCurveInstance) {
        this.dividendCurveInstance = dividendCurveInstance;
    }

    public QuantPricerSpec getPricer() {
        return pricer;
    }

    public void setPricer(QuantPricerSpec pricer) {
        this.pricer = pricer;
    }
}
