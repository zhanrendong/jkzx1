package tech.tongyu.bct.service.quantlib.common.enums;

/**
 * Option styles, i.e. vanilla, knock in/out etc. The style generally determines the payoff structure.
 * See also {@link ExerciseType}, {@link PayoffType} and {@link OptionType}. Combination of these
 * types determine fully when and how the option pays off.
 */
public enum OptionStyle {
    /**
     * Payoff min(S-K, 0.0) ({@link OptionType#CALL}) or min(K-S, 0.0) ({@link OptionType#PUT}).
     */
    VANILLA,
    /**
     * Pays spot - strike at a future time
     */
    FORWARD,
    /**
     * Single barrier knock-out option
     */
    KNOCKOUT,
    /**
     * Single barrier knock-in option
     */
    KNOCKIN,
    /**
     * Double barrier knock-out option
     */
    DOUBLE_KNOCKOUT,
    /**
     * Double barrier knock-in option
     */
    DOUBLE_KNOCKIN,
    /**
     * Single barrier no-touch option
     */
    NO_TOUCH,
    /**
     * Single barrier one-touch option
     */
    ONE_TOUCH,
    /**
     * Double barrier no-touch option
     */
    DOUBLE_NO_TOUCH,
    /**
     * Double barrier one-touch option
     */
    DOUBLE_ONE_TOUCH,
    /**
     * Digital option that pays 1 unit (either in cash or asset)
     */
    DIGITAL,
    /**
     * Single barrier knock-out digital option
     */
    DIGITAL_KNOCKTOUT,
    /**
     * Single barrier knock-in option
     */
    DIGITAL_KNOCKIN,
    /**
     * Double barrier knock-out option
     */
    DIGITAL_DOUBLE_KNOCKOUT,
    /**
     * Double barrier knock-in option
     */
    DIGITAL_DOUBLE_KNOCKIN,
    /**
     * Averaging option (arithmatic/geometric averaging of rates or strikes)
     */
    AVERAGE,
    /**
     * Spot trade
     */
    SPOT
}