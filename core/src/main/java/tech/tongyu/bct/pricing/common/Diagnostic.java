package tech.tongyu.bct.pricing.common;

import tech.tongyu.bct.common.api.doc.BctField;

public class Diagnostic {
    public enum Type {
        @BctField(description = "错误")
        ERROR,
        @BctField(description = "警告")
        WARNING
    }

    @BctField(description = "关键词")
    private final String key;
    @BctField(
            description = "类型",
            componentClass = Type.class
    )
    private final Type type;
    @BctField(description = "错误信息")
    private final String message;

    private Diagnostic(String key, Type type, String message) {
        this.key = key;
        this.type = type;
        this.message = message;
    }

    public static Diagnostic of(String key, Type type, String message) {
        return new Diagnostic(key, type, message);
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getKey() {
        return key;
    }
}
