package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;

public interface TripleDigitalFeature extends ParticipationRateFeature{

    UnitOfValue<BigDecimal> strike1();
    UnitOfValue<BigDecimal> strike2();
    UnitOfValue<BigDecimal> strike3();

    UnitOfValue<BigDecimal> payment1();
    UnitOfValue<BigDecimal> payment2();
    UnitOfValue<BigDecimal> payment3();

    default BigDecimal strike1Value() {
        if (strike1().unit() instanceof Percent)
            return strike1().value().multiply(initialSpot());
        else {
            return strike1().value();
        }
    }

    default BigDecimal strike2Value(){
        if (strike2().unit() instanceof Percent)
            return strike2().value().multiply(initialSpot());
        else {
            return strike2().value();
        }
    }

    default BigDecimal strike3Value(){
        if (strike3().unit() instanceof Percent)
            return strike3().value().multiply(initialSpot());
        else {
            return strike3().value();
        }
    }

    default BigDecimal payment1Value() {
        if (payment1().unit() instanceof Percent) {
            return payment1().value().multiply(notionalWithParticipation());
        } else return payment1().value();
    }

    default BigDecimal payment2Value() {
        if (payment2().unit() instanceof Percent) {
            return payment2().value().multiply(notionalWithParticipation());
        } else return payment2().value();
    }

    default BigDecimal payment3Value() {
        if (payment3().unit() instanceof Percent) {
            return payment3().value().multiply(notionalWithParticipation());
        } else return payment3().value();
    }

}
