package tech.tongyu.bct.service.quantlib.market.vol.utils;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.enums.DeltaType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;

public class VolQuoteToStrikeConverter {
    private final static NormalDistribution N = new NormalDistribution();

    public static double atmDeltaNeutral(double forward, double vol, double timeToExpiry) {
        return forward * FastMath.exp(-0.5 * vol * vol * timeToExpiry);
    }

    public static double strike(OptionType optionType, double delta, DeltaType deltaType, double vol,
                                double timeToExpiry,
                                double underlyerPrice, double dfDom, double dfFor) {
        double Nd = 0.0;
        double forward = underlyerPrice;
        switch (deltaType) {
            case SPOT_DELTA:
                Nd = delta / dfFor;
                forward *= dfFor / dfDom;
                break;
            case FORWARD_DELTA:
                Nd = delta;
                break;
            case FUTURES_DELTA:
                Nd = delta / dfDom;
                break;
        }
        if (optionType == OptionType.PUT)
            Nd += 1.0;
        double var = vol * vol * FastMath.sqrt(timeToExpiry);
        return forward * FastMath.exp(0.5 * var - FastMath.sqrt(var) * N.inverseCumulativeProbability(Nd));
    }

    public static double strike(OptionType optionType, double delta, DeltaType deltaType,
                                double atmVol, double rrVol, double bfVol,
                                double timeToExpiry,
                                double underlyerPrice, double dfDom, double dfFor) {
        double vol = optionType == OptionType.CALL ?
                0.5 * rrVol + bfVol + atmVol :
                -0.5 * rrVol + bfVol + atmVol;
        return strike(optionType, delta, deltaType, vol, timeToExpiry, underlyerPrice, dfDom, dfFor);
    }
}
