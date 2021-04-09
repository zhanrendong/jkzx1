package tech.tongyu.bct.trade.dto.lcm;

import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public abstract class LCMNotificationInfoDTO {
    public Map<String, InstrumentOfValuePartyRoleTypeEnum> partyRoles;

    public LCMEventTypeEnum notificationEventType;

    public ProductTypeEnum productType;

    public String underlyerInstrumentId;

    public BigDecimal underlyerPrice;

    public LocalDateTime notificationTime;


    public LCMNotificationInfoDTO() {
    }

    public LCMNotificationInfoDTO(Map<String, InstrumentOfValuePartyRoleTypeEnum> partyRoles,
                                  LCMEventTypeEnum notificationEventType, ProductTypeEnum productType,
                                  String underlyerInstrumentId, BigDecimal underlyerPrice, LocalDateTime notificationTime) {
        this.partyRoles = partyRoles;
        this.notificationEventType = notificationEventType;
        this.productType = productType;
        this.underlyerInstrumentId = underlyerInstrumentId;
        this.underlyerPrice = underlyerPrice;
        this.notificationTime = notificationTime;
    }
}
