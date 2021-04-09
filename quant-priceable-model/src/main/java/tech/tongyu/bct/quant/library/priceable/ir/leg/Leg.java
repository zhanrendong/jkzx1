package tech.tongyu.bct.quant.library.priceable.ir.leg;

import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.cash.CashPayment;

import java.time.LocalDate;
import java.util.List;


public interface Leg extends Priceable {
    List<CashPayment> dv01CashFlows(LocalDate asOf);
}
