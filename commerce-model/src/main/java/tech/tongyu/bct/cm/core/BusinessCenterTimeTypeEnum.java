package tech.tongyu.bct.cm.core;

import tech.tongyu.bct.common.api.doc.BctField;

public enum BusinessCenterTimeTypeEnum implements CMEnumeration {
    @BctField(description = "收盘价")
    CLOSE("收盘价"),
    @BctField(description = "开盘价")
    OPEN("开盘价"),
    @BctField(description = "交割价")
    OSP("交割价"), //The time at which the official settlement price is determined
    @BctField(description = "固定时间")
    SPECIFIC_TIME("固定时间"),
    @BctField(description = "交易所交割价")
    XETRA("交易所交割价"), //The time at which the official settlement price (following the auction by the exchange) is determined by the exchange
    @BctField(description = "标的收盘价")
    DERIVATIVES_CLOSE("标的收盘价"), //The official closing time of the derivatives exchange on which a derivative contract is listed on that security underlyer.
    @BctField(description = "平均价")
    TWAP("平均价");

    private String description;

    BusinessCenterTimeTypeEnum(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
