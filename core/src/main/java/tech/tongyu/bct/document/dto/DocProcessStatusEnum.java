package tech.tongyu.bct.document.dto;

import tech.tongyu.bct.common.api.doc.BctField;

public enum DocProcessStatusEnum {

    @BctField(description = "未处理")
    UN_PROCESSED,
    @BctField(description = "已下载")
    DOWNLOADED,
    @BctField(description = "已发送")
    SENT
}
