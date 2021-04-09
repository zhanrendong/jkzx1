package tech.tongyu.bct.client.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

public class AccountOpRecordDTO {
    @BctField(description = "唯一标识")
    private UUID uuid;
    //Optional
    @BctField(description = "交易编号")
    private String tradeId;
    @BctField(description = "主协议编号")
    private String masterAgreementId;
    @BctField(description = "账户编号")
    private String accountId;
    @BctField(description = "交易对手")
    private String legalName;
    @BctField(description = "账户信息")
    private String information;
    @BctField(description = "事件类型")
    private String event;
    @BctField(description = "账户状态")
    private String status;

    //Asset
    @BctField(description = "保证金变化")
    private BigDecimal marginChange;
    @BctField(description = "现金变化")
    private BigDecimal cashChange;
    @BctField(description = "权力金变化")
    private BigDecimal premiumChange;
    //Liability

    @BctField(description = "授信余额变化")
    private BigDecimal creditBalanceChange;
    @BctField(description = "已用授信变化")
    private BigDecimal creditUsedChange;
    @BctField(description = "负债变化")
    private BigDecimal debtChange;
    //Equity
    @BctField(description = "出入金总额变化")
    private BigDecimal netDepositChange;
    @BctField(description = "已实现盈亏变化")
    private BigDecimal realizedPnLChange;
    @BctField(description = "授信变化")
    private BigDecimal creditChange;
    // counterParty
    @BctField(description = "我方授信变化")
    private BigDecimal counterPartyCreditChange;// 对我方授总额
    @BctField(description = "我方授信余额变化")
    private BigDecimal counterPartyCreditBalanceChange; // 我方剩余授信余额
    @BctField(description = "我方可用资金变化")
    private BigDecimal counterPartyFundChange; // 我方可用资金
    @BctField(description = "我方冻结保证金变化")
    private BigDecimal counterPartyMarginChange; // 我方冻结保证金

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AccountOpRecordDTO(){

    }

    public AccountOpRecordDTO(UUID uuid, String tradeId, String accountId, String legalName, String information,
                              String event, ZonedDateTime eventTime, String status, BigDecimal marginChange,
                              BigDecimal cashChange, BigDecimal premiumChange, BigDecimal creditUsedChange, BigDecimal debtChange,
                              BigDecimal netDepositChange, BigDecimal realizedPnLChange, BigDecimal creditChange, BigDecimal counterPartyCreditChange,
                              BigDecimal counterPartyCreditBalanceChange, BigDecimal counterPartyFundChange, BigDecimal counterPartyMarginChange,
                              BigDecimal margin, BigDecimal cash, BigDecimal premium, BigDecimal creditUsed, BigDecimal debt, BigDecimal netDeposit,
                              BigDecimal realizedPnL, BigDecimal credit) {
        this.uuid = uuid;
        this.tradeId = tradeId;
        this.accountId = accountId;
        this.legalName = legalName;
        this.information = information;
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

    }

    public void initDefaultValue(){
        this.marginChange = BigDecimal.ZERO;
        this.cashChange = BigDecimal.ZERO;
        this.premiumChange = BigDecimal.ZERO;
        this.creditUsedChange = BigDecimal.ZERO;
        this.debtChange = BigDecimal.ZERO;
        this.netDepositChange = BigDecimal.ZERO;
        this.realizedPnLChange = BigDecimal.ZERO;
        this.creditChange = BigDecimal.ZERO;
        this.creditBalanceChange = BigDecimal.ZERO;
        this.counterPartyCreditChange = BigDecimal.ZERO;
        this.counterPartyCreditBalanceChange = BigDecimal.ZERO;
        this.counterPartyFundChange = BigDecimal.ZERO;
        this.counterPartyMarginChange = BigDecimal.ZERO;
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

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public BigDecimal getCreditBalanceChange() {
        return creditBalanceChange;
    }

    public void setCreditBalanceChange(BigDecimal creditBalanceChange) {
        this.creditBalanceChange = creditBalanceChange;
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
