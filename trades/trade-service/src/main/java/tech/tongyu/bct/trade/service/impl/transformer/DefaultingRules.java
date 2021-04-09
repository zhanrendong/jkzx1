package tech.tongyu.bct.trade.service.impl.transformer;

import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;

import java.time.ZoneOffset;

/**
 * default values for all products
 */
public class DefaultingRules {
    public static final CurrencyUnit defaultCcy = CurrencyUnit.CNY;
    public static final ZoneOffset defaultZone = ZoneOffset.ofHours(8);
}
