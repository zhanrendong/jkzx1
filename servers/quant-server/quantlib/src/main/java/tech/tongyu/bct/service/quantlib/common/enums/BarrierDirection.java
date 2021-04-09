package tech.tongyu.bct.service.quantlib.common.enums;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantEnum;

/**
 * The direction of barrier crossing
 */
@BctQuantEnum
public enum BarrierDirection {
    /**
     * The option is knocked out if the spot goes up and crosses the barrier
     */
    UP_AND_OUT,
    /**
     * The option is knocked out if the spot goes down and crosses the barrier
     */
    DOWN_AND_OUT,
    /**
     * The option is knocked in if the spot goes up and crosses the barrier
     */
    UP_AND_IN,
    /**
     * The option is knocked in if the spot goes down and crosses the barrier
     */
    DOWN_AND_IN
}