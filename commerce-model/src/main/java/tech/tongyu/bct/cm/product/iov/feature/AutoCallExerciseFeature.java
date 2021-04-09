package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;

public interface AutoCallExerciseFeature extends AutoCallPaymentFeature, ParticipationRateFeature{

    BigDecimal fixedPayment();

    UnitOfValue<BigDecimal> autoCallStrike();

    default BigDecimal fixedPaymentValue(){
        return fixedPayment().multiply(notionalWithParticipation());
    }
}
