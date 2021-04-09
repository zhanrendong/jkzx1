package tech.tongyu.bct.cm.reference.impl;

import tech.tongyu.bct.cm.core.CMEnumeration;
import tech.tongyu.bct.common.api.doc.BctField;

public enum UnitEnum implements CMEnumeration {
    @BctField(description = "手数")
    LOT("手数"),
    @BctField(description = "人民币")
    CNY("人民币"),
    @BctField(description = "美元")
    USD("美元"),
    @BctField(description = "百分比")
    PERCENT("百分比");

    private String description;

    UnitEnum(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
