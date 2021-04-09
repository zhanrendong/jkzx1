package tech.tongyu.bct.service.quantlib.common.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// TODO (http://10.1.2.16:8080/browse/OTMS-233) : BctQuantApi2 Rename and replace BctQuantApi annotation
@Retention(RetentionPolicy.RUNTIME)
public @interface BctQuantApi2 {
    String name();
    String description();

    BctQuantApiArg[] args();

    String retName();
    String retType();
    String retDescription();

    boolean addIdInput() default true;
}
