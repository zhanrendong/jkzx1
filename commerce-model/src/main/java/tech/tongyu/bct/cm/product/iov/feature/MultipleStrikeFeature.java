package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public interface MultipleStrikeFeature extends Feature, InitialSpotFeature {
    List<UnitOfValue<BigDecimal>> strikes();

    default List<BigDecimal> strikeValues() {
        return strikes().stream().map(strike -> {
                    if (strike.unit() instanceof Percent)
                        return strike.value().multiply(initialSpot());
                    else {
                        return strike.value();
                    }
                }
        ).collect(Collectors.toList());
    }
}
