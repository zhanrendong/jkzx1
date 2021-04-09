package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;

public interface SingleStrikeFeature extends Feature, InitialSpotFeature {
    UnitOfValue<BigDecimal> strike();

    void strikeChangeLCMEvent(UnitOfValue<BigDecimal> strike);

    default BigDecimal strikeValue() {
        if (strike().unit() instanceof Percent)
            return strike().value().multiply(initialSpot());
        else {
            return strike().value();
        }
    }

    default BigDecimal strikePercentValue(){
        if (strike().unit() instanceof Percent){
            return strike().value();
        }
        return null;
    }
}
