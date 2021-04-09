package tech.tongyu.bct.cm.product.iov;

import tech.tongyu.bct.cm.core.CMEnumeration;
import tech.tongyu.bct.common.api.doc.BctField;

public enum KnockDirectionEnum implements CMEnumeration {
    @BctField(description = "向下")
    UP("向上"),
    @BctField(description = "向上")
    DOWN("向下");

    KnockDirectionEnum(String description) {
        this.description = description;
    }

    private String description;

    @Override
    public String description() {
        return description;
    }
}
