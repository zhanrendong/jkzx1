package tech.tongyu.bct.client.dto;

import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountTradeTaskDTO {
    @BctField(description = "唯一标识")
    private String uuid;
    @BctField(description = "生命周期事件唯一标识")
    private String lcmUUID;
    @BctField(description = "交易编号")
    private String tradeId;
    @BctField(description = "交易对手")
    private String legalName;
    @BctField(description = "账户编号")
    private String accountId;
    @BctField(description = "权力金变化")
    private BigDecimal premium;
    @BctField(description = "现金流变化")
    private BigDecimal cashFlow;
    @BctField(description = "生命周期事件")
    private LCMEventTypeEnum lcmEventType;
    @BctField(description = "处理状态", componentClass = ProcessStatusEnum.class)
    private ProcessStatusEnum processStatus;
    @BctField(description = "买卖方向", componentClass = InstrumentOfValuePartyRoleTypeEnum.class)
    private InstrumentOfValuePartyRoleTypeEnum direction;
    @BctField(description = "创建时间")
    private Instant createdAt;
    @BctField(description = "更新时间")
    private Instant updatedAt;

    public AccountTradeTaskDTO() {
    }

    public AccountTradeTaskDTO(String uuid, String tradeId, String legalName, BigDecimal cashFlow, LCMEventTypeEnum lcmEventType) {
        this.uuid = uuid;
        this.tradeId = tradeId;
        this.legalName = legalName;
        this.cashFlow = cashFlow;
        this.lcmEventType = lcmEventType;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
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
