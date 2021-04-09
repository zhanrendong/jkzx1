package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;

public interface DoubleDigitalFeature extends ParticipationRateFeature{

    UnitOfValue<BigDecimal> lowStrike();
    UnitOfValue<BigDecimal> highStrike();

    UnitOfValue<BigDecimal> lowPayment();
    UnitOfValue<BigDecimal> highPayment();

    default BigDecimal lowStrikeValue() {
        if (lowStrike().unit() instanceof Percent)
            return lowStrike().value().multiply(initialSpot());
        else {
            return lowStrike().value();
        }
    }

    default BigDecimal highStrikeValue(){
        if (highStrike().unit() instanceof Percent)
            return highStrike().value().multiply(initialSpot());
        else {
            return highStrike().value();
        }
    }

    default BigDecimal lowPaymentValue() {
        if (lowPayment().unit() instanceof Percent) {
            return lowPayment().value().multiply(notionalWithParticipation());
        } else return lowPayment().value();
    }

    default BigDecimal highPaymentValue() {
        if (highPayment().unit() instanceof Percent) {
            return highPayment().value().multiply(notionalWithParticipation());
        } else return highPayment().value();
    }

}
