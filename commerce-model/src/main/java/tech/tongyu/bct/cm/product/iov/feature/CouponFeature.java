package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;

public interface CouponFeature extends InitialSpotFeature{

    BigDecimal couponPayment();

    UnitOfValue<BigDecimal> couponBarrier();

    default BigDecimal couponBarrierValue(){
        if (couponBarrier().unit() instanceof Percent)
            return couponBarrier().value().multiply(initialSpot());
        else {
            return couponBarrier().value();
        }
    }

}
