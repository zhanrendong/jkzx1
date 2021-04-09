package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.KnockDirectionEnum;
import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface KnockInFeature<IOV extends InstrumentOfValue> extends ParticipationRateFeature {

    Boolean knockedIn();

    LocalDate knockInDate();

    IOV knockInInstrumentOfValue();

    KnockDirectionEnum knockInDirection();

    UnitOfValue<BigDecimal> knockInBarrier();

    default BigDecimal knockInBarrierValue(){
        if (knockInBarrier().unit() instanceof Percent)
            return knockInBarrier().value().multiply(initialSpot());
        else {
            return knockInBarrier().value();
        }
    }

    void updateKnockInDate(LocalDate knockInDate);
}
