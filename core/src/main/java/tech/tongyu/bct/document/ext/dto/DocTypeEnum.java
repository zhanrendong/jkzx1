package tech.tongyu.bct.document.ext.dto;

import tech.tongyu.bct.common.api.doc.BctField;

public enum DocTypeEnum {

    /**
     * 结算通知书
     */
    @BctField(description = "结算通知书")
    SETTLE_NOTIFICATION,
    /**
     * 平仓确认书
     */
    @BctField(description = "平仓确认书")
    CLOSE_CONFIRMATION,
    /**
     * 交易确认书
     */
    @BctField(description = "交易确认书")
    TRADE_CONFIRMATION,
    /**
     * 估值报告
     */
    @BctField(description = "估值报告")
    VALUATION_REPORT,
    /**
     * 追保函
     */
    @BctField(description = "追保函")
    MARGIN_CALL,
    /**
     * 补充协议
     */
    @BctField(description = "补充协议")
    SUPPLEMENTARY_AGREEMENT
}
