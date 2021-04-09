package tech.tongyu.bct.client.dto;

import tech.tongyu.bct.common.api.doc.BctField;

public enum PaymentDirectionEnum {
    @BctField(description = "出金")
    OUT,
    @BctField(description = "入金")
    IN
}
