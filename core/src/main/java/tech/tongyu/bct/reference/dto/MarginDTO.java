package tech.tongyu.bct.reference.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 保证金
 * @author hangzhi
 */
public class MarginDTO {
    /**
     * 唯一标识
     */
    @BctField(description = "唯一标识")
    private UUID uuid;

    /**
     * 客户唯一标识．
     */
    @BctField(description = "客户唯一标识")
    private UUID partyId;
    @BctField(description = "主协议编号")
    private String masterAgreementId;

    /**
     * 客户名称
     */
    @BctField(description = "客户名称")
    private String legalName;

    /**
     * 帐号标识
     */
    @BctField(description = "账户编号")
    private String accountId;

    /**
     * 可用资金
     */
    @BctField(description = "可用资金")
    private BigDecimal cash;

    /**
     * 保证金
     */
    @BctField(description = "保证金")
    private BigDecimal margin;

    /**
     * 剩余授信
     */
    @BctField(description = "授信")
    private BigDecimal credit;

    /**
     * 维持保证金
     */
    @BctField(description = "维持保证金")
    private BigDecimal maintenanceMargin;

    /**
     * 状态
     */
    @BctField(description = "保证金状态")
    private MarginCallStatus status;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;

    public MarginDTO() {
    }

    public String getMasterAgreementId() {
        return masterAgreementId;
    }

    public void setMasterAgreementId(String masterAgreementId) {
        this.masterAgreementId = masterAgreementId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    public BigDecimal getMargin() {
        return margin;
    }

    public void setMargin(BigDecimal margin) {
        this.margin = margin;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public BigDecimal getMaintenanceMargin() {
        return maintenanceMargin;
    }

    public void setMaintenanceMargin(BigDecimal maintenanceMargin) {
        this.maintenanceMargin = maintenanceMargin;
    }

    public MarginCallStatus getStatus() {
        return status;
    }

    public void setStatus(MarginCallStatus status) {
        this.status = status;
    }

    public UUID getPartyId() {
        return partyId;
    }

    public void setPartyId(UUID partyId) {
        this.partyId = partyId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
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
