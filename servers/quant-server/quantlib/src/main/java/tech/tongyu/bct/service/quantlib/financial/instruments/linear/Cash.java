package tech.tongyu.bct.service.quantlib.financial.instruments.linear;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * A single cash payment
 */
public class Cash {
    @JsonProperty("class")
    private final String instrument = Cash.class.getSimpleName();

    private String currency;
    private double amount;
    private LocalDateTime payDate;

    /**
     * Creates a cash payment
     * @param currency Currency
     * @param amount Amount
     * @param payDate Payment time
     */
    @JsonCreator
    public Cash(String currency, double amount, LocalDateTime payDate) {
        this.currency = currency;
        this.amount = amount;
        this.payDate = payDate;
    }

    /**
     * Creates a cash payment in CNY
     * @param amount Amount
     * @param payDate Payment time
     */
    public Cash(double amount, LocalDateTime payDate) {
        this.currency = "CNY";
        this.amount = amount;
        this.payDate = payDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getPayDate() {
        return payDate;
    }

    public void setPayDate(LocalDateTime payDate) {
        this.payDate = payDate;
    }
}