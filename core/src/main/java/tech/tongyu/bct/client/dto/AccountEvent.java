package tech.tongyu.bct.client.dto;

public enum AccountEvent {

    DEPOSIT("DEPOSIT"),
    WITHDRAW("WITHDRAW"),
    START_TRADE("START_TRADE"),
    UNWIND_TRADE("UNWIND_TRADE"),
    SETTLE_TRADE("SETTLE_TRADE"),
    CHANGE_CREDIT("CHANGE_CREDIT"),
    CHANGE_PREMIUM("CHANGE_PREMIUM"),
    TRADE_CASH_FLOW("TRADE_CASH_FLOW"),
    TERMINATE_TRADE("TERMINATE_TRADE"),
    REEVALUATE_MARGIN("REEVALUATE_MARGIN"),
    COUNTER_PARTY_CHANGE("COUNTER_PARTY_CHANGE");

    private final String strVal;

    AccountEvent(String strVal) {
        this.strVal = strVal;
    }

    public static AccountEvent fromString(String text) {
        for (AccountEvent b : AccountEvent.values()) {
            if (b.strVal.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return strVal;
    }
}
