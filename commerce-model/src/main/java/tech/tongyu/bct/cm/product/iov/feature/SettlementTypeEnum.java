package tech.tongyu.bct.cm.product.iov.feature;

public enum SettlementTypeEnum {
    CASH,
    PHYSICAL,
    ELECTION, //Allow Election of either CASH or PHYSICAL settlement
    CASH_OR_PHYSICAL //Allow use of either CASH or PHYSICAL settlement without prior Election
}
