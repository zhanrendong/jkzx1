package tech.tongyu.bct.quant.library.priceable.ir.swap.impl;

import tech.tongyu.bct.quant.library.priceable.ir.leg.FixedLeg;
import tech.tongyu.bct.quant.library.priceable.ir.leg.FloatingLeg;
import tech.tongyu.bct.quant.library.priceable.ir.leg.Leg;
import tech.tongyu.bct.quant.library.priceable.ir.swap.Swap;

public class FixedFloatingSwap implements Swap {
    private final FixedLeg fixedLeg;
    private final FloatingLeg floatingLeg;
    private final Swap.Direction direction;

    public FixedFloatingSwap(FixedLeg fixedLeg, FloatingLeg floatingLeg, Direction direction) {
        this.fixedLeg = fixedLeg;
        this.floatingLeg = floatingLeg;
        this.direction = direction;
    }

    @Override
    public Leg payLeg() {
        return direction == Direction.PAY ? fixedLeg : floatingLeg;
    }

    @Override
    public Leg receiveLeg() {
        return direction == Direction.RECEIVE ? fixedLeg : floatingLeg;
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.FIXED_FLOATING_SWAP;
    }

    public FixedLeg getFixedLeg() {
        return fixedLeg;
    }

    public FloatingLeg getFloatingLeg() {
        return floatingLeg;
    }

    public Direction getDirection() {
        return direction;
    }
}
