package tech.tongyu.bct.cm.product.iov;

/**
 * Option exercise styles. An option can be exercise on a single date {@link #EUROPEAN},
 * on any date (at any time) {@link #AMERICAN} or on a schedule {@link #BERMUDAN}.
 */
public enum ExerciseTypeEnum {
    /**
     * European option can only be exercised on a pre-specified date.
     */
    EUROPEAN,
    /**
     * An American option can be exercised on any day before option expiry.
     * This can be viewed as the continuous version of a Bermudan option {@link #BERMUDAN}
     */
    AMERICAN,
    /**
     * A Bermudan option can be exercised on a pre-defined schedule. For example, monthly.
     * This can be viewed as the discrete version of an American option {@link #AMERICAN}.
     * It is a common practice to approximate an American option with a Bermudan for pricing
     * due to numerical discretization.
     */
    BERMUDAN
}
