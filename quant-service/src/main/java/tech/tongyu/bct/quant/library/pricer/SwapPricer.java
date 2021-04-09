package tech.tongyu.bct.quant.library.pricer;

import tech.tongyu.bct.quant.library.market.curve.DiscountingCurve;
import tech.tongyu.bct.quant.library.priceable.ir.leg.FixedLeg;
import tech.tongyu.bct.quant.library.priceable.ir.leg.FloatingLeg;
import tech.tongyu.bct.quant.library.priceable.ir.leg.Leg;
import tech.tongyu.bct.quant.library.priceable.ir.swap.Swap;
import tech.tongyu.bct.quant.library.priceable.ir.swap.impl.FixedFloatingSwap;

public class SwapPricer {
    public static double dv01(Leg leg, DiscountingCurve discountingCurve) {
        return leg.dv01CashFlows(discountingCurve.getValuationDate()).stream()
                .mapToDouble(c -> c.getAmount() * discountingCurve.df(c.getPaymentDate()))
                .sum();
    }

    public static double pv(FixedLeg fixedLeg, DiscountingCurve discountingCurve) {
        return fixedLeg.cashFlows(discountingCurve.getValuationDate()).stream()
                .mapToDouble(c -> c.getAmount() * discountingCurve.df(c.getPaymentDate()))
                .sum();
    }

    public static double pv(FloatingLeg floatingLeg,
                            DiscountingCurve discountingCurve,
                            DiscountingCurve forecastingCurve) {
            return floatingLeg.cashFlows(discountingCurve.getValuationDate(), forecastingCurve).stream()
                    .mapToDouble(c -> c.getAmount() * discountingCurve.df(c.getPaymentDate()))
                    .sum();
    }

    public static double pv(FixedFloatingSwap swap,
                            DiscountingCurve discountingCurve,
                            DiscountingCurve forecastingCurve) {
        double pvFixed = pv(swap.getFixedLeg(), discountingCurve);
        double pvFloating = pv(swap.getFloatingLeg(), forecastingCurve, discountingCurve);
        if (swap.getDirection() == Swap.Direction.RECEIVE) {
            return pvFixed - pvFloating;
        } else {
            return pvFloating - pvFixed;
        }
    }

    public static double dv01(FixedFloatingSwap swap, DiscountingCurve discountingCurve) {
        return dv01(swap.getFixedLeg(), discountingCurve);
    }
}
