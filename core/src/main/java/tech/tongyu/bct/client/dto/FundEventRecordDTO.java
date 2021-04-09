package tech.tongyu.bct.client.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class FundEventRecordDTO {
    @BctField(description = "唯一标识")
    private String uuid;

    @BctField(description = "主协议编号")
    private String masterAgreementId;

    @BctField(description = "开户行")
    private String bankName;

    @BctField(description = "银行账户名称")
    private String bankAccountName;

    @BctField(description = "交易对手legalName")
    private String clientId;

    @BctField(description = "银行账户")
    private String bankAccount;

    @BctField(description = "序列编号")
    private Integer serialNumber;

    @BctField(description = "支付金额")
    private BigDecimal paymentAmount;

    @BctField(description = "支付日期")
    private LocalDate paymentDate;

    @BctField(description = "支付方向")
    private PaymentDirectionEnum paymentDirection;

    @BctField(description = "账户方向")
    private AccountDirectionEnum accountDirection;

    @BctField(description = "处理状态")
    private ProcessStatusEnum processStatus;

    @BctField(description = "创建时间")
    private Instant createdAt;

    @BctField(description = "更新时间")
    private Instant updatedAt;

    public FundEventRecordDTO() {
    }

    public FundEventRecordDTO(String uuid, String clientId, String bankAccount, BigDecimal paymentAmount,
                              LocalDate paymentDate, PaymentDirectionEnum paymentDirection, AccountDirectionEnum accountDirection) {
        this.uuid = uuid;
        this.clientId = clientId;
        this.bankAccount = bankAccount;
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
        this.paymentDirection = paymentDirection;
        this.accountDirection = accountDirection;
    }

    public String getMasterAgreementId() {
        return masterAgreementId;
    }

    public void setMasterAgreementId(String masterAgreementId) {
        this.masterAgreementId = masterAgreementId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountName() {
        return bankAccountName;
    }

    public void setBankAccountName(String bankAccountName) {
        this.bankAccountName = bankAccountName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public Integer getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(Integer serialNumber) {
        this.serialNumber = serialNumber;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentDirectionEnum getPaymentDirection() {
        return paymentDirection;
    }

    public void setPaymentDirection(PaymentDirectionEnum paymentDirection) {
        this.paymentDirection = paymentDirection;
    }

    public AccountDirectionEnum getAccountDirection() {
        return accountDirection;
    }

    public void setAccountDirection(AccountDirectionEnum accountDirection) {
        this.accountDirection = accountDirection;
    }

    public ProcessStatusEnum getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(ProcessStatusEnum processStatus) {
        this.processStatus = processStatus;
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
