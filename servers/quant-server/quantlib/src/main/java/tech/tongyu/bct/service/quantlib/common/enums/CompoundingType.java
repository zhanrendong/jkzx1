package tech.tongyu.bct.service.quantlib.common.enums;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantEnum;

@BctQuantEnum
public enum CompoundingType {
    /**
     * Continuous compounding: df = exp(-rt)
     */
    CONTINUOUS_COMPOUNDING,
    /**
     * Simple compounding: df = 1/(1+day count fraction * r)
     */
    SIMPLE_COMPOUNDING
}
