package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.product.iov.RebateTypeEnum;
import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;

public interface DoubleTouchFeature extends ParticipationRateFeature {

    Boolean touched();

    UnitOfValue<BigDecimal> lowBarrier();

    UnitOfValue<BigDecimal> highBarrier();

    RebateTypeEnum rebateType();
    UnitOfValue<BigDecimal> rebate();

    default BigDecimal lowBarrierValue() {
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

    default BigDecimal rebateValue(){
        if (rebate().unit() instanceof Percent)
            return rebate().value().multiply(notionalWithParticipation());
        else {
            return rebate().value();
        }
    }


}
