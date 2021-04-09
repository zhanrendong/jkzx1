package tech.tongyu.bct.service.quantlib.common.enums;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantEnum;

/**
 * Calculation types shared by all pricers
 * <ul>
 * <li>{@link #PRICE}</li>
 * <li>{@link #DELTA}</li>
 * <li>{@link #DUAL_DELTA}</li>
 * <li>{@link #GAMMA}</li>
 * <li>{@link #VEGA}</li>
 * <li>{@link #THETA}</li>
 * <li>{@link #RHO_R}</li>
 * <li>{@link #VERA_R}</li>
 * <li>{@link #RHO_Q}</li>
 * <li>{@link #VERA_Q}</li>
 * <li>{@link #VANNA}</li>
 * <li>{@link #VETA}</li>
 * <li>{@link #CHARM}</li>
 * <li>{@link #COLOR}</li>
 * <li>{@link #SPEED}</li>
 * <li>{@link #ULTIMA}</li>
 * </ul>
 * @author Lu Lu
 * @since 2016-06-15
 */
@BctQuantEnum
public enum CalcType {
    /**
     * Option price discounted to valuation date
     */
    PRICE,
    /**
     * Option delta (dPrice/dS)
     */
    DELTA,
    /**
     * Option gamma (d^2Price/dS^2)
     */
    GAMMA,
    /**
     * Option dual delta (dPrice/dK)
     */
    DUAL_DELTA,
    /**
     * Option dual gamma (d^2Price/dK^2)
     */
    DUAL_GAMMA,
    /**
     * Option vega (dPrice/dVol)
     */
    VEGA,
    /**
     * Option theta (dPrice/dTimeToExpiry)
     */
    THETA,
    /**
     * Option rho to r (dPrice/dRiskFreeRate)
     */
    RHO_R,
    /**
     * Option rho to q (dPrice/dBorrowCost)
     */
    RHO_Q,
    /**
     * Option vanna (d^2Price/dSdVol)
     */
    VANNA,
    /**
     * Option volga (d^2Price/dVol^2)
     */
    VOLGA,
    /**
     * Option veta (d^2Price/dVoldTau)
     */
    VETA,
    /**
     * Option vera to r (d^2Price/dVoldR)
     */
    VERA_R,
    /**
     * Option vera to q (d^2Price/dVoldQ
     */
    VERA_Q,
    /**
     * Option charm (d^2Price/dSdTau)
     */
    CHARM,
    /**
     * Option color (d^3Price/dS^2dTau)
     */
    COLOR,
    /**
     * Option speed (d^3Price/dS^3)
     */
    SPEED,
    /**
     * Option ultima (d^3Price/dVol^3
     */
    ULTIMA,
    /**
     * option zomma (d^3Price/dS^2dVol)
     */
    ZOMMA
}