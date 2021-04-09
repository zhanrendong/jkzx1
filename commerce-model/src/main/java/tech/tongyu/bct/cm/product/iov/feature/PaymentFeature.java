package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;

public interface PaymentFeature extends ParticipationRateFeature {
    UnitOfValue<BigDecimal> payment();

    default BigDecimal paymentValue() {
        if (payment().unit() instanceof Percent) {
            return payment().value().multiply(notionalWithParticipation());
        } else return payment().value();
    }

}