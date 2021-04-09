package tech.tongyu.bct.common.api.annotation;

public enum BctApiTagEnum {
    /**
     * Normal Excel UDF. All Excel exported API should have this tag
     */
    Excel,
    /**
     * Excel UDF that can be delayed. If an Excel exported API should be allowed to be
     * executed later, it should be tagged such.
     */
    ExcelDelayed
}
