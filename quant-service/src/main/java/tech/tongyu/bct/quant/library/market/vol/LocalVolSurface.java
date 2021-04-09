package tech.tongyu.bct.quant.library.market.vol;

import tech.tongyu.bct.quant.library.common.QuantlibSerializableObject;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A local vol model is specified by the SDE <br>
 *     dS = r * S * dt + vol(S, t) * S * dW <br>
 * where vol(S, t) is the local vol function. Local vol surface returns local vol given the spot of
 * the underlying and a time t (in the future).
 */
public interface LocalVolSurface extends QuantlibSerializableObject {
    /**
     * Calculates the local vol given time t
     * @param S Underlying spot (at a future time t)
     * @param forward (Underlying's forward with expiry t)
     * @param t Time t
     * @return Local vol
     */
    double localVol(double S, double forward, LocalDateTime t);

    /**
     * Calculates the local varialce (localVol * localVol * time delta)
     * @param S Underlying spot (at start)
     * @param forward Underlying forward (at start)
     * @param start Time interval start time
     * @param end Time interval end time
     * @return Local variance between start and time
     */
    double localVariance(double S, double forward, LocalDateTime start, LocalDateTime end);

    /**
     * Get the spot that the vol surface is built with
     * @return Underlying spot
     */
    double getSpot();

    /**
     * Get vol surface valuation date. This date is usually considered as the vol start date
     * @return Vol surface valuation date
     */
    LocalDate getValuationDate();

    LocalDateTime getValuationDateTime();
}
