package tech.tongyu.bct.cm.reference.impl;

import tech.tongyu.bct.cm.reference.elemental.Unit;

public class CurrencyUnit implements Unit {


    public static CurrencyUnit USD = new CurrencyUnit("$", "USD");

    public static CurrencyUnit CNY = new CurrencyUnit("￥", "CNY");


    public String symbol;

    public String name;

    public CurrencyUnit() {
    }

    private CurrencyUnit(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public String name() {
        return name;
    }
}
