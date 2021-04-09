package tech.tongyu.bct.service.quantlib.common.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks the class to be serializable to json.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface BctQuantSerializable {
}