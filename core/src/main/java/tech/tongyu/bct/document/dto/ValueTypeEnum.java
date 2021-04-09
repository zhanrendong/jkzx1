package tech.tongyu.bct.document.dto;

import tech.tongyu.bct.common.api.doc.BctField;

/**
 * Value type when preparing data for template.
 * @author hangzhi
 */
public enum ValueTypeEnum {
    /**
     * unknown.
     */
    @BctField(description = "未知")
    UNKNOWN,
    @BctField(description = "字符串")
    STRING,

    /**
     * number: integer, float etc
     */
    @BctField(description = "数字.number: integer, float etc")
    NUMBER,
    @BctField(description = "时间")
    DATE_TIME,
    @BctField(description = "通用")
    OBJECT,
    @BctField(description = "数组")
    ARRAY
}
