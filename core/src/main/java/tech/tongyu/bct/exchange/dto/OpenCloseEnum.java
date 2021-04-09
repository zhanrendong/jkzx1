package tech.tongyu.bct.exchange.dto;

import tech.tongyu.bct.common.api.doc.BctField;

public enum OpenCloseEnum {
    @BctField(description = "开")
    OPEN,
    @BctField(description = "平")
    CLOSE
}
