package tech.tongyu.bct.pricing.dao.dto;

import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.market.dto.InstanceEnum;
import tech.tongyu.bct.market.dto.QuoteFieldEnum;
import tech.tongyu.bct.quant.library.priceable.AssetClass;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

public class LinearProductRuleDTO {
    @BctField(description = "资产类别", componentClass = AssetClass.AssetClassEnum.class)
    private AssetClass.AssetClassEnum assetClass;
    @BctField(description = "标的物类型", componentClass = ExchangeListed.InstrumentTypeEnum.class)
    private ExchangeListed.InstrumentTypeEnum instrumentType;
    @BctField(description = "行情", componentClass = InstanceEnum.class)
    private InstanceEnum instance;
    @BctField(description = "行情字段", componentClass = QuoteFieldEnum.class)
    private QuoteFieldEnum field;

    public LinearProductRuleDTO() {
    }

    public LinearProductRuleDTO(AssetClass.AssetClassEnum assetClass,
                                ExchangeListed.InstrumentTypeEnum instrumentType,
                                InstanceEnum instance,
                                QuoteFieldEnum field) {
        this.assetClass = assetClass;
        this.instrumentType = instrumentType;
        this.instance = instance;
        this.field = field;
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
