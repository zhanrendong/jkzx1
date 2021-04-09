package tech.tongyu.bct.market.dto;

import tech.tongyu.bct.common.api.doc.BctField;

/**
 * 行情字段
 */
public enum QuoteFieldEnum {
    /**
     * 最新成交价
     */
    @BctField(description = "最新成交价")
    LAST,
    /**
     * 买入价
     */
    @BctField(description = "买入价")
    BID,
    /**
     * 卖出价
     */
    @BctField(description = "卖出价")
    ASK,
    /**
     * 昨收
     */
    @BctField(description = "昨收")
    YESTERDAY_CLOSE,
    /**
     * 今开
     */
    @BctField(description = "今开")
    OPEN,
    /**
     * 最高价
     */
    @BctField(description = "最高价")
    HIGH,
    /**
     * 最低价
     */
    @BctField(description = "最低价")
    LOW,
    /**
     * 收盘价
     */
    @BctField(description = "收盘价")
    CLOSE,
    /**
     * 结算价
     */
    @BctField(description = "结算价")
    SETTLE;
}
