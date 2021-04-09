package tech.tongyu.bct.document.ext.dto;

import tech.tongyu.bct.common.api.doc.BctField;

public enum CategoryEnum {
    /**
     * 交易模板
     */
    @BctField(description = "交易模板")
    TRADE_TEMPLATE,
    /**
     * 客户模板
     */
    @BctField(description = "客户模板")
    CLIENT_TEMPLATE
}
