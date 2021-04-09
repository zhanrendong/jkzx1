package tech.tongyu.bct.reference.dao.dbo;

import tech.tongyu.bct.client.dto.ProcessStatusEnum;
import tech.tongyu.bct.client.service.AccountService;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = AccountService.SCHEMA)
public class AccountTradeTask {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    private String lcmUUID;

    private String tradeId;

    private String legalName;

    private String accountId;
    @Column(precision=19,scale=4)
    private BigDecimal premium;
    @Column(precision=19,scale=4)
    private BigDecimal cashFlow;

    private LCMEventTypeEnum lcmEventType;

    private ProcessStatusEnum processStatus;

    private InstrumentOfValuePartyRoleTypeEnum direction;

    private Instant createdAt;

    private Instant updatedAt;

    public AccountTradeTask() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getLcmUUID() {
        return lcmUUID;
    }

    public void setLcmUUID(String lcmUUID) {
        this.lcmUUID = lcmUUID;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
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

    public ProcessStatusEnum getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(ProcessStatusEnum processStatus) {
        this.processStatus = processStatus;
    }

    public InstrumentOfValuePartyRoleTypeEnum getDirection() {
        return direction;
    }

    public void setDirection(InstrumentOfValuePartyRoleTypeEnum direction) {
        this.direction = direction;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
