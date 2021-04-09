package tech.tongyu.bct.cm.product.iov.feature;

import java.time.LocalDate;

public interface SettlementFeature extends Feature {
    LocalDate settlementDate();
    SettlementTypeEnum settlementType();
}
