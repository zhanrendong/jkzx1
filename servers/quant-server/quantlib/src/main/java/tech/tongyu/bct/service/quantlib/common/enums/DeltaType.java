package tech.tongyu.bct.service.quantlib.common.enums;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantEnum;

/**
 * Delta types. According to which option price to which underlyer price the sensitivity is calculated, there are
 * two types of FX deltas: {@link #SPOT_DELTA} and {@link #FORWARD_DELTA}
 */
@BctQuantEnum
public enum DeltaType {
    /**
     * Sensitivity of discounted option price to underlyer spot
     */
    SPOT_DELTA,
    /**
     * Sensitivity of forward option price to underlyer forward
     */
    FORWARD_DELTA,
    /**
     * Sensitivity of discounted option price to futures price (underlyer is futures)
     */
    FUTURES_DELTA
}
