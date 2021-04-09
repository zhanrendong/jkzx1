package tech.tongyu.bct.client.dto;

import tech.tongyu.bct.common.api.doc.BctField;

public enum ProcessStatusEnum {
    @BctField(description = "已处理")
    PROCESSED,
    @BctField(description = "待处理")
    UN_PROCESSED

}
