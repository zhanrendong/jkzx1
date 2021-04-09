package tech.tongyu.bct.cm.product.iov.feature;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface AnnualizedFeature extends Feature {
    LocalDate startDate();

    LocalDate endDate();

    // this should be a default method with parameter of startDate and endDate.
    BigDecimal term();

    BigDecimal daysInYear();

    Boolean isAnnualized();

    BigDecimal annValRatio();
}
