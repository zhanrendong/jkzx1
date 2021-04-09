package tech.tongyu.bct.service.quantlib.common.enums;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantEnum;

/**
 * Option types
 * <ul>
 * <li>{@link #CALL}</li>
 * <li>{@link #PUT}</li>
 * </ul>
 * Created by lu on 16-6-15.
 */
@BctQuantEnum
public enum OptionType {
    /**
     * CALL: payoff to benefit from upward price movement,
     * for example the European vanilla call max(spot - strike, 0.0)
     */
    CALL,
    /**
     * Put: payoff to benefit from downward price movement,
     * for example the European vanilla put max(strike - spot, 0.0)
     */
    PUT
}
