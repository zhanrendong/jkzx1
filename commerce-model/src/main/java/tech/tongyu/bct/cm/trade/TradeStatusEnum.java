package tech.tongyu.bct.cm.trade;

import tech.tongyu.bct.common.api.doc.BctField;

public enum TradeStatusEnum {
    @BctField(description = "存续期")
    LIVE,
    @BctField(description = "到期")
    MATURED,
    @BctField(description = "结算")
    CLOSED
}
