package tech.tongyu.bct.cm.product.iov;

public enum BarrierObservationTypeEnum {
    DAILY("每日观察"),
    TERMINAL("到期观察"),
    DISCRETE("离散观察"),
    CONTINUOUS("连续观察");

    private String description;

    BarrierObservationTypeEnum(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
