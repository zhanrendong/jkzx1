package tech.tongyu.bct.reference.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.client.dto.AccountDirectionEnum;
import tech.tongyu.bct.client.dto.PaymentDirectionEnum;
import tech.tongyu.bct.client.dto.ProcessStatusEnum;
import tech.tongyu.bct.client.service.AccountService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = AccountService.SCHEMA)
public class FundEventRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column
    private String clientId;

    @Column
    private String bankAccount;

    @Column
    private Integer serialNumber;

    @Column(precision=19,scale=4)
    private BigDecimal paymentAmount;

    @Column
    private LocalDate paymentDate;

    @Column
    @Enumerated(EnumType.STRING)
    private PaymentDirectionEnum paymentDirection;

    @Column
    @Enumerated(EnumType.STRING)
    private AccountDirectionEnum accountDirection;

    @Column
    @Enumerated(EnumType.STRING)
    private ProcessStatusEnum processStatus;

    @Column
    @CreationTimestamp
    private Instant createdAt;

    @Column
    @UpdateTimestamp
    private Instant updatedAt;

    public FundEventRecord() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
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
