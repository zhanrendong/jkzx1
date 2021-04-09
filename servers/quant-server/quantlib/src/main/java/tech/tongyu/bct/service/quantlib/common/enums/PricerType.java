package tech.tongyu.bct.service.quantlib.common.enums;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantEnum;

/**
 * Pricers
 * The generic pricer will take in a pricer type and its parameters to set up the pricing.
 * <ul>
 *     <li>{@link #BLACK_ANALYTIC}</li>
 *     <li>{@link #BLACK76_ANALYTIC}</li>
 *     <li>{@link #BLACK_PDE}</li>
 *     <li>{@link #BLACK76_PDE}</li>
 *     <li>{@link #BLACK_BINOMIAL_TREE}</li>
 *     <li>{@link #BLACK76_BINOMIAL_TREE}</li>
 *     <li>{@link #BLACK_MC}</li>
 *     <li>{@link #BLACK76_MC}</li>
 *     <li>{@link #LOCAL_VOL_MC}</li>
 * </ul>
 * Created by lu on 5/3/17.
 */
@BctQuantEnum
public enum PricerType {
    /**
     * Cash flow pricer by discounting
     */
    DISCOUNT,
    /**
     * Black model (semi-)analytic pricer
     *
     */
    BLACK_ANALYTIC,
    /**
     * Black76 model (semi-)analytic pricer
     *
     */
    BLACK76_ANALYTIC,
    /**
     * PDE solver based on black model (flat vol or atm vol with term structure)
     */
    BLACK_PDE,
    /**
     * PDE solver based on black76 model (flat vol or atm vol with term structure)
     */
    BLACK76_PDE,
    /**
     * Binomial tree solver based on black model (only flat vol is supported)
     */
    BLACK_BINOMIAL_TREE,
    /**
     * Binomial tree solver based on black76 model (only flat vol is supported)
     */
    BLACK76_BINOMIAL_TREE,
    /**
     * Monte Carlo pricer based on black model (flat vol or atm vol with term structure)
     */
    BLACK_MC,
    /**
     * Monte Carlo pricer based on black76 model (flat vol or atm vol with term structure)
     */
    BLACK76_MC,
    /**
     * Monte Carlo pricer based on local vol model
     */
    LOCAL_VOL_MC
}
