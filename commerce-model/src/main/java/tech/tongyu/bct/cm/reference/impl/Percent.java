package tech.tongyu.bct.cm.reference.impl;

import tech.tongyu.bct.cm.reference.elemental.Unit;

public class Percent implements Unit {
    public String symbol = "%";
    public String name = "PERCENT";

    private Percent() {
    }

    private static class SinglePercent {
        private static final Percent INSTANCE = new Percent();
    }

    public static Percent get() {
        return SinglePercent.INSTANCE;
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
