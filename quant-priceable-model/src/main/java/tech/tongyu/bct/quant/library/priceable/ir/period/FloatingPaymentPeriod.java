package tech.tongyu.bct.quant.library.priceable.ir.period;

import tech.tongyu.bct.quant.library.priceable.cash.CashPayment;

import java.time.LocalDate;
import java.util.function.Function;

public interface FloatingPaymentPeriod extends PaymentPeriod{
    double payment(Function<LocalDate, Double> curve);
    default CashPayment cashFlow(Function<LocalDate, Double> curve) {
        return new CashPayment(paymentDate(), payment(curve));
    }
}
