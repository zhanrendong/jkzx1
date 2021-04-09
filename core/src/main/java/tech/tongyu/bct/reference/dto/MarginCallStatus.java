package tech.tongyu.bct.reference.dto;

/**
 * 追保状态
 * @author hangzhi
 */
public enum MarginCallStatus {
    /**
     * 未知．　
     * 缺计算所用的参数．　比如维持保证金没有初始化．
     */
    UNKNOWN,

    /**
     *　正常
     */
    NORMAL,

    /**
     * 保证金不足
     */
    NOT_ENOUGH,

    /**
     * 待追保证金
     */
    PENDING_MARGIN_CALL
}
