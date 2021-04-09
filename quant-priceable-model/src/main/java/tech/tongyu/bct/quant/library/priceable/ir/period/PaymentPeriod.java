package tech.tongyu.bct.quant.library.priceable.ir.period;

import java.time.LocalDate;

public interface PaymentPeriod {
    LocalDate paymentStart();
    LocalDate paymentEnd();
    LocalDate paymentDate();
    double notional();
    double paymentDayCountFraction();
}
