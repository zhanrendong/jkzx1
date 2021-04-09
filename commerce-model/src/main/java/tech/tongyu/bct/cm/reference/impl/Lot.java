package tech.tongyu.bct.cm.reference.impl;

import tech.tongyu.bct.cm.reference.elemental.Unit;

public class Lot implements Unit {
    public String symbol = "Lot";
    public String name = "LOT";

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public String name() {
        return name;
    }

    private Lot() {
    }

    private static class SingleLot {
        private static final Lot INSTANCE = new Lot();
    }

    public static Lot get() {
        return SingleLot.INSTANCE;
    }

}
