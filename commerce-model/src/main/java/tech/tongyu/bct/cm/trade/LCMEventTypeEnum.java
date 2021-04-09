package tech.tongyu.bct.cm.trade;

import tech.tongyu.bct.cm.core.CMEnumeration;
import tech.tongyu.bct.common.api.doc.BctField;

public enum LCMEventTypeEnum implements CMEnumeration {
    @BctField(description = "开仓")
    OPEN("开仓"),
    @BctField(description = "展期")
    ROLL("展期"),
    @BctField(description = "更新")
    AMEND("更新"),
    @BctField(description = "结算")
    SETTLE("结算"),
    @BctField(description = "平仓")
    UNWIND("平仓"),
    @BctField(description = "部分平仓")
    UNWIND_PARTIAL("部分平仓"),
    @BctField(description = "到期")
    EXPIRATION("到期"),
    @BctField(description = "行权")
    EXERCISE("行权"),
    @BctField(description = "敲出")
    KNOCK_OUT("敲出"),
    @BctField(description = "敲入")
    KNOCK_IN("敲入"),
    @BctField(description = "分红")
    DIVIDEND("分红"),
    @BctField(description = "支付")
    PAYMENT("支付"),
    @BctField(description = "观察")
    OBSERVE("观察"),
    @BctField(description = "雪球到期行权")
    SNOW_BALL_EXERCISE("雪球到期行权"),
    @BctField(description = "凤凰到期行权")
    PHOENIX_EXERCISE("凤凰到期行权");

    private String description;

    LCMEventTypeEnum(String description) {
        this.description = description;
    }

    @Override
    public String description() {
        return description;
    }
}
