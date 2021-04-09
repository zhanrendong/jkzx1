package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.product.iov.RebateTypeEnum;
import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;

public interface RebateFeature extends ParticipationRateFeature {

    RebateTypeEnum rebateType();

    UnitOfValue<BigDecimal> rebate();

    default BigDecimal rebateValue(){
        if(rebate().unit() instanceof Percent)
            return rebate().value().multiply(notionalWithParticipation());
        else return rebate().value();
    }
}
