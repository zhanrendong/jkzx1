package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.core.CMEnumeration;
import tech.tongyu.bct.common.api.doc.BctField;

public enum OptionTypeEnum implements CMEnumeration {
    @BctField(description = "看涨")
    CALL("看涨"),
    @BctField(description = "看跌")
    PUT("看跌");

    private String description;

    OptionTypeEnum(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
