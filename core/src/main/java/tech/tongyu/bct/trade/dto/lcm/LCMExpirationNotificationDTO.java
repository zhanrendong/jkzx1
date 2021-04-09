package tech.tongyu.bct.trade.dto.lcm;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.SettlementTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class LCMExpirationNotificationDTO extends LCMNotificationInfoDTO {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public LocalDateTime expirationTime;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public LocalDateTime settlementDate;

    public SettlementTypeEnum settlementType;

    public LCMExpirationNotificationDTO() {
    }

    public LCMExpirationNotificationDTO(Map<String, InstrumentOfValuePartyRoleTypeEnum> partyRoles,
                                        LCMEventTypeEnum notificationEventType, ProductTypeEnum productType,
                                        String underlyerInstrumentId, BigDecimal underlyerPrice, LocalDateTime notificationTime,
                                        LocalDateTime expirationTime, LocalDateTime settlementDate, SettlementTypeEnum settlementType) {
        super(partyRoles, notificationEventType, productType, underlyerInstrumentId, underlyerPrice, notificationTime);
        this.expirationTime = expirationTime;
        this.settlementDate = settlementDate;
        this.settlementType = settlementType;
    }
}
