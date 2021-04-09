package tech.tongyu.bct.service.quantlib.market.vol;

import tech.tongyu.bct.service.quantlib.common.enums.OptionType;

import java.time.LocalDateTime;

/**
 * An implied volatility surface
 * <p>
 *     The main functionality of an implied volatility surface is to provide the implied volatility given an option
 *     expiry. If both forward and strike are given, it is equivalent to providing forward european call and put
 *     prices. Some models, such as SABR and Heston, are more efficient to calculate option prices and volatilities
 *     are derived by inverting Black's formula. This is why two interface functions are required even though one
 *     can be derived from the other.
 * <p>
 *     Implied vol surface is the starting point of other vol models. For example, local volatility surface
 *     can be derived from implied vol surface through Dupire's formula. Marginal distributions of
 *     the underlying can be implied from option prices.
 */
public interface ImpliedVolSurface {
    /**
     * Return the total variance from val date to expiry.
     * This is preferable to implied vol since variance is a dimensionless quantity. When plugging into Black's
     * formula, vol depends on how time to expiry is interpreted, while variance does not.
     * @param forward Forward price of the underlyer
     * @param strike Strike of the option
     * @param expiry Expiry of the option
     * @return
     */
    double variance(double forward, double strike, LocalDateTime expiry);
    /**
     * Get implied vol on an expiry
     * @param forward Underlying's forward
     * @param strike Option strike
     * @param expiry Option expiry
     * @return Implied vol
     */
    double iv(double forward, double strike, LocalDateTime expiry);

    /**
     * Get forward call/put price given option expiry, forward and strike
     * @param forward
     * @param strike
     * @param expiry
     * @param type
     * @return forward option price
     */
    double forwardPrice(double forward, double strike, LocalDateTime expiry, OptionType type);

    /**
     * Increase implied volatility. The increased amount is applied to the implied
     * volatility for each expiries, not the local volatility.
     * @param amount The increased volatility
     * @return A new ImpliedVolSurface instance with the increased implied volatility.
     */
    ImpliedVolSurface bump(double amount);

    /**
     * Get vol surface valuation date. This date is usually considered as the vol start date
     * @return Vol surface valuation date
     */
    LocalDateTime getVal();

    /**
     * Get the spot that the vol surface is built with
     * @return Underlying spot
     */
    double getSpot();

    /**
     * Roll a vol surface to a new valuation date
     * @param newVal The new valuation date to roll to
     * @return A new rolled vol surface (the original one is intact)
     */
    ImpliedVolSurface roll(LocalDateTime newVal);
}