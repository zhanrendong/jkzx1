package tech.tongyu.bct.service.quantlib.market.curve;

import java.time.LocalDateTime;

/**
 * A forward curve gives the underling's forward price at a future time
 */
public interface Forward {
    /**
     * Gives the forward price at a future delivery date
     * <p>
     *     Certan assets have a lag between delivery and its fixing date. Thus to specify the forward
     *     completely either delivery or fixing/expiry date must be given.
     * </p>
     * <p>
     *     FX forward requires two dates to determine a forward: spot date and delivery date.
     *     When setting up the forward curve, a spot date should be given. Then forwards can
     *     calculated in this function with delivery date only (the spot date is implicitly
     *     stored in the curve)
     * </p>
     * @param delivery delivery date
     * @return Forward
     */
    double onDelivery(LocalDateTime delivery);

    /**
     * Gives the forward price at a future delivery date
     * <p>
     *     Certan assets have a lag between delivery and its fixing date. Thus to specify the forward
     *     completely either delivery or fixing/expiry date must be given.
     * </p>
     * <p>
     *     FX forward requires two dates to determine a forward: spot date and delivery date.
     *     When setting up the forward curve, a spot date should be given. When implementing
     *     this function, a mechanism should be provided to calculate delivery dates from expiry
     *     dates.
     * </p>
     * @param expiry Usually option expiry date
     * @return
     */
    double onExpiry(LocalDateTime expiry);

    /**
     * Get curve valuation date. This date is usually considered as the curve start date
     * @return Curve valuation date
     */
    LocalDateTime getVal();
}