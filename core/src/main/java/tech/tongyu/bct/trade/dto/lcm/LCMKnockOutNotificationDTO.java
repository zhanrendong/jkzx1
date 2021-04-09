package tech.tongyu.bct.trade.dto.lcm;

import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class LCMKnockOutNotificationDTO extends LCMNotificationInfoDTO {

    public BigDecimal barrier;
    public BigDecimal highBarrier;
    public BigDecimal lowBarrier;

    public LCMKnockOutNotificationDTO(Map<String, InstrumentOfValuePartyRoleTypeEnum> partyRoles,
                                      LCMEventTypeEnum notificationEventType, ProductTypeEnum productType,
                                      String underlyerInstrumentId, BigDecimal underlyerPrice, LocalDateTime notificationTime) {

        super(partyRoles, notificationEventType, productType, underlyerInstrumentId, underlyerPrice, notificationTime);
    }

}
