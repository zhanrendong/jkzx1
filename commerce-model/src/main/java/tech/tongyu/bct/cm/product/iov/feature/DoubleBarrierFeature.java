package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;

public interface DoubleBarrierFeature extends NotionalFeature{

    UnitOfValue<BigDecimal> lowBarrier();

    UnitOfValue<BigDecimal> highBarrier();

    default BigDecimal lowBarrierValue(){
        if (lowBarrier().unit() instanceof Percent)
            return lowBarrier().value().multiply(initialSpot());
        else {
            return lowBarrier().value();
        }
    }

    default BigDecimal highBarrierValue(){
        if (highBarrier().unit() instanceof Percent)
            return highBarrier().value().multiply(initialSpot());
        else {
            return highBarrier().value();
        }
    }
}
