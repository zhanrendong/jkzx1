package tech.tongyu.bct.cm.product.iov;

import tech.tongyu.bct.cm.core.BusinessCenterEnum;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;

import java.time.LocalDate;

public interface  FutureInstrument<T extends InstrumentOfValue> extends ListedInstrument {
    LocalDate issueDate();

    LocalDate maturityDate();

    T underlyer();

    CurrencyUnit currency();

    String instrumentId();

    BusinessCenterEnum venue();
}
