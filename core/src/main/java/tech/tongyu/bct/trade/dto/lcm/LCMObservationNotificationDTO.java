package tech.tongyu.bct.trade.dto.lcm;

import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class LCMObservationNotificationDTO extends LCMNotificationInfoDTO{

    LocalDate observationDate;

    public LCMObservationNotificationDTO(Map<String, InstrumentOfValuePartyRoleTypeEnum> partyRoles,
                                         LCMEventTypeEnum notificationEventType, ProductTypeEnum productType,
                                         String underlyerInstrumentId, BigDecimal underlyerPrice, LocalDateTime notificationTime,
                                         LocalDate observationDate) {
        super(partyRoles, notificationEventType, productType, underlyerInstrumentId, underlyerPrice, notificationTime);
        this.observationDate = observationDate;
    }
}
