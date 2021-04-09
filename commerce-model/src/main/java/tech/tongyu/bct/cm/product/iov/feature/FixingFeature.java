package tech.tongyu.bct.cm.product.iov.feature;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface FixingFeature extends UnderlyerFeature, Feature{

    Map<LocalDate, BigDecimal> fixingObservations();

    Map<LocalDate, LocalDate> fixingPaymentDates();

    void observationLCMEvent(LocalDate observationDate, BigDecimal observationPrice);

}
