package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;

public interface DoubleStrikeFeature extends InitialSpotFeature  {

    UnitOfValue<BigDecimal> lowStrike();

    UnitOfValue<BigDecimal> highStrike();

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
}
