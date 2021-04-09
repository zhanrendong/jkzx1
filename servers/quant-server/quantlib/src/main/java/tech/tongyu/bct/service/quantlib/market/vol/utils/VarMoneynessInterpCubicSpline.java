package tech.tongyu.bct.service.quantlib.market.vol.utils;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.enums.ExtrapType;
import tech.tongyu.bct.service.quantlib.common.numerics.interp.Interpolator1DCubicSpline;

public class VarMoneynessInterpCubicSpline {
    private final Interpolator1DCubicSpline interp;

    VarMoneynessInterpCubicSpline(double[] forwards, double[] strikes, double[] vols, double timeToExpiry,
                                  ExtrapType extrapTypeLow, ExtrapType extrapTypeHigh) {
        double[] vars = new double[forwards.length];
        double[] moneyness = new double[forwards.length];
        for (int i = 0; i < forwards.length; ++i) {
            vars[i] = vols[i] * vols[i] * timeToExpiry;
            moneyness[i] = FastMath.log(forwards[i] / strikes[i]);
        }
        this.interp = new Interpolator1DCubicSpline(moneyness, vars, extrapTypeLow, extrapTypeHigh);
    }

    double var(double forward, double strike) {
        double m = FastMath.log(forward / strike);
        return interp.value(m);
    }
}
