package tech.tongyu.bct.trade.dto.trade.common;

import tech.tongyu.bct.cm.core.CMEnumeration;

public enum UnitTypeEnum implements CMEnumeration {
    CURRENCY("货币单位"),
    PERCENTAGE("百分比");

    private String description;

    UnitTypeEnum(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
