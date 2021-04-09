package tech.tongyu.bct.trade.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.jpa.HasUuid;
import tech.tongyu.bct.trade.service.TradeService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = TradeService.SCHEMA,
        indexes = {@Index(name = "TID_INDEX", columnList = "tradeId"),
                   @Index(name = "PID_INDEX", columnList = "positionId")})
public class TradeCashFlow implements HasUuid {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column
    private String tradeId;

    @Column
    private String positionId;

    @Column(precision=19,scale=4)
    private BigDecimal premium;

    @Column(precision=19,scale=4)
    private BigDecimal cashFlow;

    @Column
    @Enumerated(EnumType.STRING)
    private LCMEventTypeEnum lcmEventType;

    @Column
    @CreationTimestamp
    private Instant createdAt;


    public TradeCashFlow() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }

    public BigDecimal getCashFlow() {
        return cashFlow;
    }

    public void setCashFlow(BigDecimal cashFlow) {
        this.cashFlow = cashFlow;
    }

    public LCMEventTypeEnum getLcmEventType() {
        return lcmEventType;
    }

    public void setLcmEventType(LCMEventTypeEnum lcmEventType) {
        this.lcmEventType = lcmEventType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
