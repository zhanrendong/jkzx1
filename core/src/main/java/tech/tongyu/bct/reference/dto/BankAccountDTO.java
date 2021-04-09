package tech.tongyu.bct.reference.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.time.Instant;

public class BankAccountDTO {
    @BctField(description = "唯一标识")
    private String uuid;
    @BctField(description = "开户行")
    private String bankName;
    @BctField(description = "交易对手")
    private String legalName;
    @BctField(description = "银行账户")
    private String bankAccount;
    @BctField(description = "银行账户名称")
    private String bankAccountName;
    @BctField(description = "系统支付编号")
    private String paymentSystemCode;

    private Instant createdAt;
    private Instant updatedAt;

    public BankAccountDTO() {
    }

    public BankAccountDTO(String uuid, String bankName, String legalName, String bankAccount, String bankAccountName,
                          String paymentSystemCode) {
        this.uuid = uuid;
        this.bankName = bankName;
        this.legalName = legalName;
        this.bankAccount = bankAccount;
        this.bankAccountName = bankAccountName;
        this.paymentSystemCode = paymentSystemCode;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getBankAccountName() {
        return bankAccountName;
    }

    public void setBankAccountName(String bankAccountName) {
        this.bankAccountName = bankAccountName;
    }

    public String getPaymentSystemCode() {
        return paymentSystemCode;
    }

    public void setPaymentSystemCode(String paymentSystemCode) {
        this.paymentSystemCode = paymentSystemCode;
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
