package tech.tongyu.bct.client.dto;

public enum AccountOpRecordStatus {

    NORMAL("NORMAL"),
    INVALID("INVALID");

    private final String strVal;

    AccountOpRecordStatus(String strVal) {
        this.strVal = strVal;
    }

    public static AccountOpRecordStatus fromString(String text) {
        for (AccountOpRecordStatus b : AccountOpRecordStatus.values()) {
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
