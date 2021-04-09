package tech.tongyu.bct.trade.dto.trade.common;

import tech.tongyu.bct.cm.core.BusinessCenterEnum;
import tech.tongyu.bct.cm.core.CMEnumeration;
import tech.tongyu.bct.cm.product.iov.InstrumentAssetClassTypeEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.util.List;
import java.util.stream.Collectors;

public class TradeReferenceDTO {
    private class EnumData {
        public String name;

        public String description;

        public EnumData() {
        }

        public EnumData(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    @BctField(name = "assetClasses", description = "资产类别列表", type = "List<EnumData>")
    public List<EnumData> assetClasses;
    @BctField(name = "productTypes", description = "合约类型列表", type = "List<EnumData>")
    public List<EnumData> productTypes;
    @BctField(name = "optionTypes", description = "涨/跌类型列表", type = "List<EnumData>")
    public List<EnumData> optionTypes;
    @BctField(name = "unitTypes", description = "单位列表", type = "List<EnumData>")
    public List<EnumData> unitTypes;
    @BctField(name = "lcmEventTypes", description = "生命周期事类型列表", type = "List<EnumData>")
    public List<EnumData> lcmEventTypes;
    @BctField(name = "businessCenters", description = "交易所列表", type = "List<EnumData>")
    public List<EnumData> businessCenters;

    public TradeReferenceDTO() {

    }

    public void setAssetClasses(List<InstrumentAssetClassTypeEnum> assetClasses) {
        this.assetClasses = toEnumData(assetClasses);
    }

    public void setProductTypes(List<ProductTypeEnum> productTypes) {
        this.productTypes = toEnumData(productTypes);
    }

    public void setOptionTypes(List<OptionTypeEnum> optionTypes) {
        this.optionTypes = toEnumData(optionTypes);
    }

    public void setUnitTypes(List<UnitTypeEnum> unitTypes) {
        this.unitTypes = toEnumData(unitTypes);
    }

    public void setLcmEventTypes(List<LCMEventTypeEnum> lcmEventTypes) {
        this.lcmEventTypes = toEnumData(lcmEventTypes);
    }

    public void setBusinessCenters(List<BusinessCenterEnum> businessCenters){
        this.businessCenters = toEnumData(businessCenters);
    }

    private List<EnumData> toEnumData(List<? extends CMEnumeration> enums) {
        return enums.stream()
                .map(c -> new EnumData(c.name(), c.description()))
                .collect(Collectors.toList());
    }
}
