package tech.tongyu.bct.service.quantlib.common.enums;

/**
 * Averaging types for Asian options.
 * <ul>
 * <li>{@link #ARITHMATIC_RATE}</li>
 * <li>{@link #ARITHMATIC_STRIKE}</li>
 * <li>{@link #GEOMETRIC_RATE}</li>
 * <li>{@link #GEOMETRIC_STRIKE}</li>
 * </ul>
 */
public enum AverageType {
    /**
     * Payoff is of type \sum_1^N spot_i / N - strike
     */
    ARITHMATIC_RATE,
    /**
     * Strike is \sum_1^N spot_i / N
     */
    ARITHMATIC_STRIKE,
    /**
     * Payoff is of type pow(\product_1^N spot_i, 1/N) - strike
     */
    GEOMETRIC_RATE,
    /**
     * Strike is pow(\product spot_i, 1/N)
     */
    GEOMETRIC_STRIKE
}
