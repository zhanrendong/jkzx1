package tech.tongyu.bct.quant.library.priceable.ir.swap;

import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.ir.leg.Leg;

public interface Swap extends Priceable {
    enum Direction {
        PAY, RECEIVE
    }
    Leg payLeg();
    Leg receiveLeg();
}
