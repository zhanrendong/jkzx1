package tech.tongyu.bct.cm.product.iov;

import tech.tongyu.bct.cm.core.CMEnumeration;

public enum InstrumentAssetClassTypeEnum implements CMEnumeration {
    EQUITY("权益"),
    COMMODITY("商品"),
    RATES("利率"),
    FX("外汇"),
    CREDIT("信用"),
    OTHER("其他");

    private String description;

    InstrumentAssetClassTypeEnum(String description) {
        this.description = description;
    }

    @Override
    public String description() {
        return description;
    }
}
