package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.product.iov.CashFlowDirectionEnum;
import tech.tongyu.bct.cm.product.iov.PaymentStateEnum;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;

import java.math.BigDecimal;

public interface CashPaymentFeature extends Feature {
    CurrencyUnit currency();

    BigDecimal paymentAmount();

    CashFlowDirectionEnum paymentDirection();

    PaymentStateEnum paymentState();
}