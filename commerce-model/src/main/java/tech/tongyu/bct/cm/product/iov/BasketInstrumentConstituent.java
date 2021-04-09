package tech.tongyu.bct.cm.product.iov;

import java.math.BigDecimal;

public interface BasketInstrumentConstituent<U extends InstrumentOfValue> {
    U instrument();

    BigDecimal multiplier();

    BigDecimal weight();

    BigDecimal initialSpot();
}
