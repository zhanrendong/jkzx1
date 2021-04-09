package tech.tongyu.bct.cm.product.iov;

/**
 * 障碍方向
 */
public enum BarrierDirectionEnum {
    /**
     * 向上敲出
     * The option is knocked out if the spot goes up and crosses the barrier
     */
    UP_AND_OUT,
    /**
     * 向下敲出
     * The option is knocked out if the spot goes down and crosses the barrier
     */
    DOWN_AND_OUT,
    /**
     * 向上敲入
     * The option is knocked in if the spot goes up and crosses the barrier
     */
    UP_AND_IN,
    /**
     * 向下敲入
     * The option is knocked in if the spot goes down and crosses the barrier
     */
    DOWN_AND_IN
}
