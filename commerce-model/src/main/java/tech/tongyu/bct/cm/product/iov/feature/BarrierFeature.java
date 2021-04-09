package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.product.iov.KnockDirectionEnum;
import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;

public interface BarrierFeature extends UnderlyerFeature, ParticipationRateFeature, ObservationFeature{

    KnockDirectionEnum knockDirection();

    UnitOfValue<BigDecimal> barrier();

    default BigDecimal barrierValue(){
        if (barrier().unit() instanceof Percent)
            return barrier().value().multiply(initialSpot());
        else {
            return barrier().value();
        }
    }
}
