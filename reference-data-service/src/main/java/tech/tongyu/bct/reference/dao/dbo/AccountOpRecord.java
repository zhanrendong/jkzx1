package tech.tongyu.bct.reference.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.client.dto.AccountEvent;
import tech.tongyu.bct.client.dto.AccountOpRecordStatus;
import tech.tongyu.bct.client.service.AccountService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(schema = AccountService.SCHEMA)
public class AccountOpRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    @Column(nullable = false)
    private String accountOpId;
    //Optional
    @Column
    private String tradeId; // NOT KEY
    @Column(nullable = false)
    private String accountId;
    @Column
    private String legalName;
    @Column
    private String information;
    @Column
    @Enumerated(EnumType.STRING)
    private AccountEvent event;
    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
    @Column
    @UpdateTimestamp
    private Instant updatedAt;
    @Column
    @Enumerated(EnumType.STRING)
    private AccountOpRecordStatus status;
    //Asset
    @Column(precision=19,scale=4)
    private BigDecimal marginChange;
    @Column(precision=19,scale=4)
    private BigDecimal cashChange;
    @Column(precision=19,scale=4)
    private BigDecimal premiumChange;

    //Liability
    @Column(precision=19,scale=4)
    private BigDecimal creditUsedChange;
    @Column(precision=19,scale=4)
    private BigDecimal debtChange;

    //Equity
    @Column(precision=19,scale=4)
    private BigDecimal netDepositChange;
    @Column(precision=19,scale=4)
    private BigDecimal realizedPnLChange;
    @Column(precision=19,scale=4)
    private BigDecimal creditChange;

    @Column(precision=19,scale=4)
    private BigDecimal counterPartyCreditChange;// 对我方授总额
    @Column(precision=19,scale=4)
    private BigDecimal counterPartyCreditBalanceChange; // 我方剩余授信余额
    @Column(precision=19,scale=4)
    private BigDecimal counterPartyFundChange; // 我方可用资金
    @Column(precision=19,scale=4)
    private BigDecimal counterPartyMarginChange; // 我方冻结保证金

    //Account status after cashflow
    //Asset
    @Column(precision=19,scale=4)
    private BigDecimal margin;
    @Column(precision=19,scale=4)
    private BigDecimal cash;
    @Column(precision=19,scale=4)
    private BigDecimal premium; // premium of active options
    //Liability
    @Column(precision=19,scale=4)
    private BigDecimal creditUsed;
    @Column(precision=19,scale=4)
    private BigDecimal debt;
    //Equity
    @Column(precision=19,scale=4)
    private BigDecimal netDeposit;
    //the final PNL of terminated trade + alive trade cashflow
    @Column(precision=19,scale=4)
    private BigDecimal realizedPnL; // PnL of options, for expired/terminated options, premium is accounted for.
    //Other information
    @Column(precision=19,scale=4)
    private BigDecimal credit;
    @Column(precision=19,scale=4)
    private BigDecimal counterPartyCredit;// 对我方授总额
    @Column(precision=19,scale=4)
    private BigDecimal counterPartyCreditBalance; // 我方剩余授信余额
    @Column(precision=19,scale=4)
    private BigDecimal counterPartyFund; // 我方可用资金
    @Column(precision=19,scale=4)
    private BigDecimal counterPartyMargin; // 我方冻结保证金

    public static AccountOpRecord InvalidRecord(String information, String accountId) {
        AccountOpRecord record = new AccountOpRecord();
        record.setInformation(information);
        record.setAccountId(accountId);
        record.setStatusToInvalid();
        return record;
    }

    public AccountOpRecord() {}

    public AccountOpRecord(String accountId, ZonedDateTime eventTime, AccountEvent event) {
        this.accountId = accountId;
        this.event = event;
        //optional
        this.tradeId = null;

        //Asset
        this.marginChange = BigDecimal.valueOf(0);
        this.cashChange = BigDecimal.valueOf(0);
        this.premiumChange = BigDecimal.valueOf(0);

        //Liability
        this.creditUsedChange = BigDecimal.valueOf(0);
        this.debtChange = BigDecimal.valueOf(0);

        //Equity
        this.netDepositChange = BigDecimal.valueOf(0);
        this.realizedPnLChange = BigDecimal.valueOf(0);
        this.creditChange = BigDecimal.valueOf(0);
        this.status = AccountOpRecordStatus.INVALID;
        //
        this.counterPartyCreditBalanceChange = BigDecimal.valueOf(0);
        this.counterPartyCreditChange = BigDecimal.valueOf(0);
        this.counterPartyFundChange = BigDecimal.valueOf(0);
        this.counterPartyMarginChange = BigDecimal.valueOf(0);

        this.information = null;
        this.margin = null;
        this.cash = null;
        this.premium = null;
        this.creditUsed = null;
        this.debt = null;
        this.netDeposit = null;
        this.realizedPnL = null;
        this.credit = null;
    }

    public AccountOpRecord(String accountId, ZonedDateTime eventTime, AccountEvent event, String tradeId) {
        this.accountId = accountId;
        this.event = event;
        //optional
        this.tradeId = tradeId;

        //Asset
        this.marginChange = BigDecimal.valueOf(0);
        this.cashChange = BigDecimal.valueOf(0);
        this.premiumChange = BigDecimal.valueOf(0);

        //Liability
        this.creditUsedChange = BigDecimal.valueOf(0);
        this.debtChange = BigDecimal.valueOf(0);

        //Equity
        this.netDepositChange = BigDecimal.valueOf(0);
        this.realizedPnLChange = BigDecimal.valueOf(0);
        this.creditChange = BigDecimal.valueOf(0);
        this.status = AccountOpRecordStatus.INVALID;

        this.counterPartyCreditBalanceChange = BigDecimal.valueOf(0);
        this.counterPartyCreditChange = BigDecimal.valueOf(0);
        this.counterPartyFundChange = BigDecimal.valueOf(0);
        this.counterPartyMarginChange = BigDecimal.valueOf(0);

        this.information = null;
        this.margin = null;
        this.cash = null;
        this.premium = null;
        this.creditUsed = null;
        this.debt = null;
        this.netDeposit = null;
        this.realizedPnL = null;
        this.credit = null;
    }

    public AccountOpRecord(String tradeId, String accountId, AccountEvent event, ZonedDateTime eventTime,
                           AccountOpRecordStatus status, BigDecimal marginChange, BigDecimal cashChange, BigDecimal premiumChange,
                           BigDecimal creditUsedChange, BigDecimal debtChange, BigDecimal netDepositChange, BigDecimal realizedPnLChange,
                           BigDecimal creditChange, BigDecimal counterPartyCreditChange, BigDecimal counterPartyCreditBalanceChange,
                           BigDecimal counterPartyFundChange, BigDecimal counterPartyMarginChange) {
        this.tradeId = tradeId;
        this.accountId = accountId;
        this.event = event;
        this.status = status;
        this.marginChange = marginChange;
        this.cashChange = cashChange;
        this.premiumChange = premiumChange;
        this.creditUsedChange = creditUsedChange;
        this.debtChange = debtChange;
        this.netDepositChange = netDepositChange;
        this.realizedPnLChange = realizedPnLChange;
        this.creditChange = creditChange;
        this.counterPartyCreditChange = counterPartyCreditChange;
        this.counterPartyCreditBalanceChange = counterPartyCreditBalanceChange;
        this.counterPartyFundChange = counterPartyFundChange;
        this.counterPartyMarginChange = counterPartyMarginChange;
        this.information = null;
        this.margin = null;
        this.cash = null;
        this.premium = null;
        this.creditUsed = null;
        this.debt = null;
        this.netDeposit = null;
        this.realizedPnL = null;
        this.credit = null;
    }

    // -----------------自定义相关参数获取方法----------------

    public void addMarginChange(BigDecimal marginChange) {
        this.marginChange = this.marginChange.add(marginChange);
    }

    public void addCashChange(BigDecimal cashChange) {
        this.cashChange = this.cashChange.add(cashChange);
    }

    public void addPremiumChange(BigDecimal assetStartValueChange) {
        this.premiumChange = this.premiumChange.add(assetStartValueChange);
    }

    public void addCreditUsedChange(BigDecimal creditUsedChange) {
        this.creditUsedChange = this.creditUsedChange.add(creditUsedChange);
    }

    public void addDebtChange(BigDecimal debtChange) {
        this.debtChange = this.debtChange.add(debtChange);
    }

    public void addNetDepositChange(BigDecimal netDepositChange) {
        this.netDepositChange = this.netDepositChange.add(netDepositChange);
    }

    public void addRealizedPnLChange(BigDecimal realizedPNL) {
        this.realizedPnLChange = this.realizedPnLChange.add(realizedPNL);
    }

    public void addCreditChange(BigDecimal creditChange) {
        this.creditChange = this.creditChange.add(creditChange);
    }

    public void setStatusToNormal() {
        this.status = AccountOpRecordStatus.NORMAL;
    }

    public void setStatusToInvalid() {
        this.status = AccountOpRecordStatus.INVALID;
    }

    public void setAccountInformation(Account account) {
        margin = account.getMargin();
        cash = account.getCash();
        premium = account.getPremium();

        creditUsed = account.getCreditUsed();
        debt = account.getDebt();

        netDeposit = account.getNetDeposit();
        realizedPnL = account.getRealizedPnL();
        credit = account.getCredit();
    }

    // ---------------------Property Generate Get/Set Method-----------------
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getAccountOpId() {
        return accountOpId;
    }

    public void setAccountOpId(String accountOpId) {
        this.accountOpId = accountOpId;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public AccountEvent getEvent() {
        return event;
    }

    public void setEvent(AccountEvent event) {
        this.event = event;
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

    public AccountOpRecordStatus getStatus() {
        return status;
    }

    public void setStatus(AccountOpRecordStatus status) {
        this.status = status;
    }

    public BigDecimal getMarginChange() {
        return marginChange;
    }

    public void setMarginChange(BigDecimal marginChange) {
        this.marginChange = marginChange;
    }

    public BigDecimal getCashChange() {
        return cashChange;
    }

    public void setCashChange(BigDecimal cashChange) {
        this.cashChange = cashChange;
    }

    public BigDecimal getPremiumChange() {
        return premiumChange;
    }

    public void setPremiumChange(BigDecimal premiumChange) {
        this.premiumChange = premiumChange;
    }

    public BigDecimal getCreditUsedChange() {
        return creditUsedChange;
    }

    public void setCreditUsedChange(BigDecimal creditUsedChange) {
        this.creditUsedChange = creditUsedChange;
    }

    public BigDecimal getDebtChange() {
        return debtChange;
    }

    public void setDebtChange(BigDecimal debtChange) {
        this.debtChange = debtChange;
    }

    public BigDecimal getNetDepositChange() {
        return netDepositChange;
    }

    public void setNetDepositChange(BigDecimal netDepositChange) {
        this.netDepositChange = netDepositChange;
    }

    public BigDecimal getRealizedPnLChange() {
        return realizedPnLChange;
    }

    public void setRealizedPnLChange(BigDecimal realizedPnLChange) {
        this.realizedPnLChange = realizedPnLChange;
    }

    public BigDecimal getCreditChange() {
        return creditChange;
    }

    public void setCreditChange(BigDecimal creditChange) {
        this.creditChange = creditChange;
    }

    public BigDecimal getCounterPartyCreditChange() {
        return counterPartyCreditChange;
    }

    public void setCounterPartyCreditChange(BigDecimal counterPartyCreditChange) {
        this.counterPartyCreditChange = counterPartyCreditChange;
    }

    public BigDecimal getCounterPartyCreditBalanceChange() {
        return counterPartyCreditBalanceChange;
    }

    public void setCounterPartyCreditBalanceChange(BigDecimal counterPartyCreditBalanceChange) {
        this.counterPartyCreditBalanceChange = counterPartyCreditBalanceChange;
    }

    public BigDecimal getCounterPartyFundChange() {
        return counterPartyFundChange;
    }

    public void setCounterPartyFundChange(BigDecimal counterPartyFundChange) {
        this.counterPartyFundChange = counterPartyFundChange;
    }

    public BigDecimal getCounterPartyMarginChange() {
        return counterPartyMarginChange;
    }

    public void setCounterPartyMarginChange(BigDecimal counterPartyMarginChange) {
        this.counterPartyMarginChange = counterPartyMarginChange;
    }

    public BigDecimal getMargin() {
        return margin;
    }

    public void setMargin(BigDecimal margin) {
        this.margin = margin;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }

    public BigDecimal getCreditUsed() {
        return creditUsed;
    }

    public void setCreditUsed(BigDecimal creditUsed) {
        this.creditUsed = creditUsed;
    }

    public BigDecimal getDebt() {
        return debt;
    }

    public void setDebt(BigDecimal debt) {
        this.debt = debt;
    }

    public BigDecimal getNetDeposit() {
        return netDeposit;
    }

    public void setNetDeposit(BigDecimal netDeposit) {
        this.netDeposit = netDeposit;
    }

    public BigDecimal getRealizedPnL() {
        return realizedPnL;
    }

    public void setRealizedPnL(BigDecimal realizedPnL) {
        this.realizedPnL = realizedPnL;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public BigDecimal getCounterPartyCredit() {
        return counterPartyCredit;
    }

    public void setCounterPartyCredit(BigDecimal counterPartyCredit) {
        this.counterPartyCredit = counterPartyCredit;
    }

    public BigDecimal getCounterPartyCreditBalance() {
        return counterPartyCreditBalance;
    }

    public void setCounterPartyCreditBalance(BigDecimal counterPartyCreditBalance) {
        this.counterPartyCreditBalance = counterPartyCreditBalance;
    }

    public BigDecimal getCounterPartyFund() {
        return counterPartyFund;
    }

    public void setCounterPartyFund(BigDecimal counterPartyFund) {
        this.counterPartyFund = counterPartyFund;
    }

    public BigDecimal getCounterPartyMargin() {
        return counterPartyMargin;
    }

    public void setCounterPartyMargin(BigDecimal counterPartyMargin) {
        this.counterPartyMargin = counterPartyMargin;
    }
}
