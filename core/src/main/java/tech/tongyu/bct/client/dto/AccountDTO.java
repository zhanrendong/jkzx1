package tech.tongyu.bct.client.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class AccountDTO {

    @BctField(description = "账户编号")
    private String accountId; // 账户ID
    @BctField(description = "交易对手")
    private String legalName; // 交易对手
    @BctField(description = "开户销售")
    private String salesName; // 开户销售
    @BctField(description = "主协议编号")
    private String masterAgreementId; //协议编号
    @BctField(description = "账户状态")
    private Boolean normalStatus; //
    @BctField(description = "账户信息")
    private String accountInformation; // 账户信息
    //Asset
    @BctField(description = "保证金")
    private BigDecimal margin; //保证金
    @BctField(description = "现金余额")
    private BigDecimal cash;   // 现金余额
    @BctField(description = "存续期权力金")
    private BigDecimal premium;// 存续期权利金 premium of active options
    //Liability
    @BctField(description = "授信余额")
    private BigDecimal creditBalance;
    @BctField(description = "已用授信")
    private BigDecimal creditUsed; // 已用授信额度
    @BctField(description = "负债")
    private BigDecimal debt; // 负债
    //Equity
    @BctField(description = "出入金总额")
    private BigDecimal netDeposit; // 出入金总额 the final PNL of terminated trade + alive trade cashflow
    @BctField(description = "已实现盈亏")
    private BigDecimal realizedPnL; //  已实现盈亏 PnL of options, for expired/terminated options, premium is accounted for.
    //Other information
    @BctField(description = "授信总额")
    private BigDecimal credit; // 授信总额
    @BctField(description = "我方授信")
    private BigDecimal counterPartyCredit;// 对我方授总额
    @BctField(description = "我方授信余额")
    private BigDecimal counterPartyCreditBalance; // 我方剩余授信余额
    @BctField(description = "我方可用资金")
    private BigDecimal counterPartyFund; // 我方可用资金
    @BctField(description = "我方冻结保证金")
    private BigDecimal counterPartyMargin; // 我方冻结保证金

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AccountDTO() {
    }

    public AccountDTO(String accountId, String legalName, Boolean normalStatus, ZonedDateTime updateTime, String accountInformation,
                      BigDecimal margin, BigDecimal cash, BigDecimal premium, BigDecimal creditUsed, BigDecimal debt, BigDecimal netDeposit,
                      BigDecimal realizedPnL, BigDecimal credit, BigDecimal counterPartyCredit, BigDecimal counterPartyCreditBalance,
                      BigDecimal counterPartyFund, BigDecimal counterPartyMargin) {
        this.accountId = accountId;
        this.legalName = legalName;
        this.normalStatus = normalStatus;
        this.accountInformation = accountInformation;
        this.margin = margin;
        this.cash = cash;
        this.premium = premium;
        this.creditUsed = creditUsed;
        this.debt = debt;
        this.netDeposit = netDeposit;
        this.realizedPnL = realizedPnL;
        this.credit = credit;
        this.counterPartyCredit = counterPartyCredit;
        this.counterPartyCreditBalance = counterPartyCreditBalance;
        this.counterPartyFund = counterPartyFund;
        this.counterPartyMargin = counterPartyMargin;
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

    public String getSalesName() {
        return salesName;
    }

    public void setSalesName(String salesName) {
        this.salesName = salesName;
    }

    public String getMasterAgreementId() {
        return masterAgreementId;
    }

    public void setMasterAgreementId(String masterAgreementId) {
        this.masterAgreementId = masterAgreementId;
    }

    public Boolean getNormalStatus() {
        return normalStatus;
    }

    public void setNormalStatus(Boolean normalStatus) {
        this.normalStatus = normalStatus;
    }

    public String getAccountInformation() {
        return accountInformation;
    }

    public void setAccountInformation(String accountInformation) {
        this.accountInformation = accountInformation;
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

    public BigDecimal getCreditBalance() {
        return creditBalance;
    }

    public void setCreditBalance(BigDecimal creditBalance) {
        this.creditBalance = creditBalance;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
