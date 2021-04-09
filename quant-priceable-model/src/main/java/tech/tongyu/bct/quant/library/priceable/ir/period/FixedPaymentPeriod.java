package tech.tongyu.bct.quant.library.priceable.ir.period;

import com.fasterxml.jackson.annotation.JsonCreator;
import tech.tongyu.bct.quant.library.priceable.cash.CashPayment;

import java.time.LocalDate;

public class FixedPaymentPeriod implements PaymentPeriod {
    private final AccrualPeriod accrualPeriod;
    private final LocalDate paymentDate;
    private final double fixedRate;
    private final double notional;

    @JsonCreator
    public FixedPaymentPeriod(AccrualPeriod accrualPeriod, LocalDate paymentDate, double fixedRate, double notional) {
        this.accrualPeriod = accrualPeriod;
        this.paymentDate = paymentDate;
        this.fixedRate = fixedRate;
        this.notional = notional;
    }

    @Override
    public LocalDate paymentStart() {
        return accrualPeriod.getAccrualStart();
    }

    @Override
    public LocalDate paymentEnd() {
        return accrualPeriod.getAccrualEnd();
    }

    @Override
    public LocalDate paymentDate() {
        return this.paymentDate;
    }

    @Override
    public double paymentDayCountFraction() {
        return accrualPeriod.getDayCountFraction();
    }

    @Override
    public double notional() {
        return this.notional;
    }

    public double payment() {
        return this.notional * this.fixedRate * this.accrualPeriod.getDayCountFraction();
    }

    public CashPayment cashFlow() {
        return new CashPayment(this.paymentDate, payment());
    }

    public AccrualPeriod getAccrualPeriod() {
        return accrualPeriod;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public double getFixedRate() {
        return fixedRate;
    }

    public double getNotional() {
        return notional;
    }
}
