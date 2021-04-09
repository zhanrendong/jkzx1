package tech.tongyu.bct.service.quantlib.common.enums;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantEnum;

@BctQuantEnum
public enum AtmVolType {
    ATM_FORWARD,
    ATM_SPOT,
    DELTA_NEUTRAL
}
