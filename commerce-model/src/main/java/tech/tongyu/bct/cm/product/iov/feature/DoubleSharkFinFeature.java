package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.product.iov.KnockDirectionEnum;
import tech.tongyu.bct.cm.product.iov.RebateTypeEnum;
import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;

public interface DoubleSharkFinFeature extends UnderlyerFeature, DoubleParticipationRateFeature, ObservationFeature {

    KnockDirectionEnum lowDirection();
    KnockDirectionEnum highDirection();

    UnitOfValue<BigDecimal> lowStrike();
    UnitOfValue<BigDecimal> highStrike();

    UnitOfValue<BigDecimal> lowBarrier();
    UnitOfValue<BigDecimal> highBarrier();

    RebateTypeEnum rebateType();
    UnitOfValue<BigDecimal> lowRebate();
    UnitOfValue<BigDecimal> highRebate();

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

    default BigDecimal lowRebateValue() {
        if (lowRebate().unit() instanceof Percent)
            return lowRebate().value().multiply(lowNotionalWithParticipation());
        else return lowRebate().value();
    }

    default BigDecimal highRebateValue(){
        if (highRebate().unit() instanceof Percent)
            return highRebate().value().multiply(highNotionalWithParticipation());
        else {
            return highRebate().value();
        }
    }

}
