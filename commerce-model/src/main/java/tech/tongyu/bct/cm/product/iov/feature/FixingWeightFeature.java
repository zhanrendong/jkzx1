package tech.tongyu.bct.cm.product.iov.feature;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface FixingWeightFeature extends Feature{

    Map<LocalDate, BigDecimal> fixingWeights();
}
