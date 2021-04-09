package tech.tongyu.bct.reference.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.client.dto.AccountEvent;
import tech.tongyu.bct.client.service.AccountService;
import tech.tongyu.bct.common.exception.CustomException;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(schema = AccountService.SCHEMA)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    @Column(nullable = false, unique = true)
    private String accountId; // 账户ID
    @Column(nullable = false)
    private String legalName;
    @Column(nullable = false)
    private Boolean normalStatus; //
    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
    @Column
    @UpdateTimestamp
    private Instant updatedAt;
    @Column
    private String accountInformation; // 账户信息

    //Asset
    @Column(nullable = false, precision=19,scale=4)
    private BigDecimal margin; //保证金
    @Column(nullable = false, precision=19,scale=4)
    private BigDecimal cash;   // 现金余额
    @Column(nullable = false, precision=19,scale=4)
    private BigDecimal premium;// 存续期权利金 premium of active options
    //Liability
    @Column(nullable = false, precision=19,scale=4)
    private BigDecimal creditUsed; // 已用授信额度
    @Column(nullable = false, precision=19,scale=4)
    private BigDecimal debt; // 负债
    //Equity
    @Column(nullable = false, precision=19,scale=4)
    private BigDecimal netDeposit; // 出入金总额 the final PNL of terminated trade + alive trade cashflow
    @Column(nullable = false, precision=19,scale=4)
    private BigDecimal realizedPnL; //  已实现盈亏 PnL of options, for expired/terminated options, premium is accounted for.
    //Other information
    @Column(nullable = false, precision=19,scale=4)
    private BigDecimal credit; // 授信总额
    @Column(nullable = false, precision=19,scale=4)
    private BigDecimal counterPartyFund;  // 我方可用资金
    @Column(nullable = false, precision=19,scale=4)
    private BigDecimal counterPartyMargin;// 我方冻结保证金
    @Column(nullable = false, precision=19,scale=4)
    private BigDecimal counterPartyCredit;// 我方授信总额
    @Column(nullable = false, precision=19,scale=4)
    private BigDecimal counterPartyCreditBalance; // 我方剩余授信余额


    public Account() {

    }

    public Account(String accountId, BigDecimal margin, BigDecimal cash, BigDecimal premium,
                   BigDecimal creditUsed, BigDecimal debt, BigDecimal netDeposit, BigDecimal realizedPnL, BigDecimal credit,
                   BigDecimal counterPartyCredit, BigDecimal counterPartyCreditBalance, BigDecimal counterPartyFund, BigDecimal counterPartyMargin) {
        this.accountId = accountId;
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
        this.validate();
    }

    public void initDefaltValue(){
        this.cash = BigDecimal.valueOf(0);
        this.debt = BigDecimal.valueOf(0);
        this.margin = BigDecimal.valueOf(0);
        this.credit = BigDecimal.valueOf(0);
        this.premium = BigDecimal.valueOf(0);
        this.creditUsed = BigDecimal.valueOf(0);
        this.netDeposit = BigDecimal.valueOf(0);
        this.realizedPnL = BigDecimal.valueOf(0);
        this.counterPartyFund = BigDecimal.valueOf(0);
        this.counterPartyMargin = BigDecimal.valueOf(0);
        this.counterPartyCredit = BigDecimal.valueOf(0);
        this.counterPartyCreditBalance = BigDecimal.valueOf(0);
        this.validate();
    }

    private void validate() {
        // credit >  usedCredit
        // except AssetValue , realized PNL and book PNL. all other element has to bigger or equal to  0
        if (getMargin().compareTo(BigDecimal.valueOf(0)) < 0) {
            normalStatus = false;
            accountInformation = "保证金信息无效.";
        } else if (getNetDeposit().compareTo(BigDecimal.valueOf(0)) < 0) {
            normalStatus = false;
            accountInformation = "出入金总额信息无效.";

        } else if (getCash().compareTo(BigDecimal.valueOf(0)) < 0) {
            normalStatus = false;
            accountInformation = "现金信息无效.";
        } else if (getCreditUsed().compareTo(BigDecimal.valueOf(0)) < 0) {
            normalStatus = false;
            accountInformation = "已用授信额度无效.";
        } else if (getCreditUsed().compareTo(getCredit()) > 0) {
            normalStatus = false;
            accountInformation = "已用授信额度超出最大授信额度.";
        } else if (getDebt().compareTo(BigDecimal.valueOf(0)) < 0) {
            normalStatus = false;
            accountInformation = "负债信息无效.";
        } else if (getDebt().compareTo(BigDecimal.valueOf(0)) != 0 &&
                getCash().add(getUsableCredit()).compareTo(BigDecimal.valueOf(0)) != 0){
            normalStatus = false;
            accountInformation = "负债和可用资金都不为0.";
        } else if (getCash().add(getMargin().add(getPremium())).compareTo(getNetDeposit().add(getCreditUsed()).add(
                getDebt()).add(getRealizedPnL())) != 0) {
            normalStatus = false;
            accountInformation = String.format(
                    "资金无法配平, 可用资金: %s, 保证金: %s, 出入金: %s, 已用授信: %s, 负债: %s," +
                            "存续期权利金: %s, 已实现盈亏: %s.", getCash(), getMargin(), getNetDeposit(),
                    getCreditUsed(), getDebt(), getPremium(), getRealizedPnL());
        } else {
            accountInformation = "";
            normalStatus = true;
        }
    }


    public AccountOpRecord deposit(BigDecimal number, String information) {
        ZonedDateTime nowTime = ZonedDateTime.now();
        AccountOpRecord record = new AccountOpRecord(getAccountId(), nowTime, AccountEvent.DEPOSIT);
        if (!checkAccountStatus(record)) {
            record.setAccountInformation(this);
            record.setStatusToInvalid();
            return record;
        }
        if (number.compareTo(new BigDecimal(0)) < 0) {
            throw new CustomException(String.format("入金金额[%s]无效", number.toPlainString()));
        }
        record.setInformation(information);
        if (flowIn(number, record)) {
            setNetDeposit(getNetDeposit().add(number), record);
            record.setStatusToNormal();
        }
        record.setAccountInformation(this);
        return record;
    }

    public AccountOpRecord tryWithdraw(BigDecimal number, String information) {
        ZonedDateTime nowTime = ZonedDateTime.now();
        AccountOpRecord record = new AccountOpRecord(getAccountId(), nowTime, AccountEvent.WITHDRAW);
        if (!checkAccountStatus(record)) {
            record.setAccountInformation(this);
            return record;
        }
        if (number.compareTo(new BigDecimal(0)) < 0) {
            throw new CustomException(String.format("出金金额[%s]无效", number.toPlainString()));
        }
        if (getWithdrawableCash().compareTo(number) >= 0) {
            record.setInformation(information);
            if (flowOut(number, false, record)) {
                setNetDeposit(getNetDeposit().subtract(number), record);
                record.setStatusToNormal();
            }
        } else {
            throw new CustomException(String.format("没有足够的可提取资金(现金-已用授信-负债).当前可提取资金[%s]",
                    getWithdrawableCash().toPlainString()));
        }
        record.setAccountInformation(this);
        return record;
    }

    public AccountOpRecord updateCredit(BigDecimal credit, BigDecimal counterPartyCredit, String information){
        ZonedDateTime nowTime = ZonedDateTime.now();
        AccountOpRecord record = new AccountOpRecord(getAccountId(), nowTime, AccountEvent.CHANGE_CREDIT);
        BigDecimal counterPartyCreditBalanceOldValue = getCounterPartyCreditBalance();
        BigDecimal counterPartyCreditChange = counterPartyCredit.subtract(getCounterPartyCredit());
        BigDecimal counterPartyCreditBalance = counterPartyCreditBalanceOldValue.add(counterPartyCreditChange);
        // 我方可用授信余额字段维护
        BigDecimal counterPartyCreditBalanceChange = counterPartyCreditChange;
        if (counterPartyCreditBalance.compareTo(BigDecimal.ZERO) < 0){
            counterPartyCreditBalance = BigDecimal.ZERO;
            counterPartyCreditBalanceChange = counterPartyCreditBalanceOldValue.negate();
        }

        record.setCounterPartyCreditBalanceChange(counterPartyCreditBalanceChange);
        record.setCounterPartyCreditChange(counterPartyCreditChange);
        record.setCreditChange(credit.subtract(getCredit()));
        record.setAccountInformation(this);
        record.setStatusToNormal();


        setCredit(credit);
        setAccountInformation(information);
        setCounterPartyCredit(counterPartyCredit);
        setCounterPartyCreditBalance(counterPartyCreditBalance);
        return record;
    }

    public AccountOpRecord premiumChange(BigDecimal premium, String information){
        ZonedDateTime nowTime = ZonedDateTime.now();
        AccountOpRecord record = new AccountOpRecord(getAccountId(), nowTime, AccountEvent.CHANGE_PREMIUM);
        if (!checkAccountStatus(record)) { // 校验账户是否正常
            record.setAccountInformation(this);
            record.setStatusToInvalid();
            return record;
        }
        if (payOff(premium.negate(), true, record)){
            this.premium = this.premium.add(premium);
            record.setPremiumChange(premium);
        }
        if (this.normalStatus){
            record.setStatusToNormal();
        }else{
            record.setStatusToInvalid();
        }
        record.setInformation(information);
        record.setAccountInformation(this);
        return record;
    }

    public AccountOpRecord settleTrade(BigDecimal amount, BigDecimal premium, AccountEvent event, String information){
        ZonedDateTime nowTime = ZonedDateTime.now();
        AccountOpRecord record = new AccountOpRecord(getAccountId(), nowTime, event);
        if (!checkAccountStatus(record)) {
            record.setAccountInformation(this);
            record.setStatusToInvalid();
            return record;
        }
        if (payOff(amount, true, record)){
            this.premium = this.premium.add(premium);
            this.realizedPnL = this.realizedPnL.add(amount.add(premium));
            record.setPremiumChange(premium);
            record.setRealizedPnLChange(amount.add(premium));
        }
        if (this.normalStatus){
            record.setStatusToNormal();
        }else {
            record.setStatusToInvalid();
        }
        record.setInformation(information);
        record.setAccountInformation(this);
        return record;
    }

    public AccountOpRecord tryInitializeTrade(
            String tradeId, BigDecimal gainOrCost, BigDecimal marginToBePaid) {
        ZonedDateTime nowTime = ZonedDateTime.now();
        AccountOpRecord record = new AccountOpRecord(getAccountId(), nowTime, AccountEvent.START_TRADE, tradeId);
        if (!checkAccountStatus(record)) {
            record.setAccountInformation(this);
            return record;
        }
        if (marginToBePaid.compareTo(BigDecimal.valueOf(0)) < 0) {
            record.setInformation("支付的保证金无效.");
            record.setAccountInformation(this);
            return record;
        }
        BigDecimal totalPayOff = gainOrCost.subtract(marginToBePaid);
        if (totalPayOff.add(getUsableFund()).compareTo(BigDecimal.valueOf(0)) < 0) {
            record.setInformation(String.format("没有足够的资金, 需要 %s, 可用 %s.",
                    totalPayOff.negate(), getUsableFund()));
            record.setAccountInformation(this);
            return record;
        }
        if (payOff(totalPayOff, false, record)) {
            setMargin(getMargin().add(marginToBePaid), record);
            setPermium(getPremium().add(gainOrCost), record);
            record.setStatusToNormal();
        }
        validate();
        record.setAccountInformation(this);
        return record;
    }


    public AccountOpRecord terminateTrade(
            String tradeId, BigDecimal startGainOrCost, BigDecimal endGainOrCost, BigDecimal marginRelease) {
        ZonedDateTime nowTime = ZonedDateTime.now();
        AccountOpRecord record = new AccountOpRecord(getAccountId(), nowTime, AccountEvent.TERMINATE_TRADE, tradeId);
        if (!checkAccountStatus(record)) {
            record.setAccountInformation(this);
            return record;
        }
        if (marginRelease.compareTo(BigDecimal.valueOf(0)) < 0) {
            record.setInformation("保证金释放无效.");
            record.setAccountInformation(this);
            return record;
        }
        if (marginRelease.compareTo(getMargin()) > 0) {
            marginRelease = new BigDecimal(getMargin().toString());
        }
        BigDecimal totalPayOff = endGainOrCost.add(marginRelease);
        if (payOff(totalPayOff, true, record)) {
            setMargin(getMargin().subtract(marginRelease), record);
            BigDecimal realizedPNL = endGainOrCost.add(startGainOrCost);
            setRealizedPNL(getRealizedPnL().add(realizedPNL), record);
            setPermium((getPremium().subtract(startGainOrCost)), record);
            record.setStatusToNormal();
        }
        validate();
        record.setAccountInformation(this);
        return record;
    }

    public AccountOpRecord marginChange(
            BigDecimal minimalMargin, BigDecimal initialMargin, BigDecimal refillMargin) {
        ZonedDateTime nowTime = ZonedDateTime.now();
        AccountOpRecord record = new AccountOpRecord(getAccountId(), nowTime, AccountEvent.REEVALUATE_MARGIN);
        if (!checkAccountStatus(record)) {
            record.setAccountInformation(this);
            return record;
        }
        if (initialMargin.compareTo(getMargin()) < 0) {
            BigDecimal totalPayoff = getMargin().subtract(initialMargin);
            if (payOff(totalPayoff, false, record)) {
                setMargin(initialMargin, record);
                record.setStatusToNormal();
            }
            record.setAccountInformation(this);
            return record;
        } else if (minimalMargin.compareTo(getMargin()) > 0) {
            BigDecimal totalPayOff = getMargin().subtract(refillMargin);
            if (payOff(totalPayOff, true, record)) {
                setMargin(refillMargin, record);
                record.setStatusToNormal();
            }
            record.setAccountInformation(this);
            return record;
        } else {
            record.setInformation("保证金没有变化.");
            record.setAccountInformation(this);
            return record;
        }
    }

    public AccountOpRecord changeCredit(BigDecimal newCredit, String information) {
        ZonedDateTime nowTime = ZonedDateTime.now();
        AccountOpRecord record = new AccountOpRecord(getAccountId(), nowTime, AccountEvent.CHANGE_CREDIT);
        if (!checkAccountStatus(record)) {
            record.setAccountInformation(this);
            record.setStatusToInvalid();
            return record;
        }
        if (getCredit().compareTo(newCredit) <= 0) {
            BigDecimal creditDiff = newCredit.subtract(getCredit());
            if (getDebt().compareTo(BigDecimal.valueOf(0)) > 0) {
                BigDecimal debtChange = creditDiff.min(getDebt());
                setDebt(getDebt().subtract(debtChange), record);
                setCreditUsed(getCreditUsed().add(debtChange), record);
            }
            setCredit(newCredit, record);
            record.setStatusToNormal();
            record.setInformation(information);
            record.setAccountInformation(this);
            return record;
        } else if (getUsableFund().compareTo(getCredit().subtract(newCredit)) >= 0) {
            setCredit(newCredit, record);
            if (getCreditUsed().compareTo(newCredit) > 0) {
                BigDecimal cashDeduct = getCreditUsed().subtract(newCredit);
                setCreditUsed(newCredit, record);
                setCash(getCash().subtract(cashDeduct), record);
            }
            record.setStatusToNormal();
            record.setInformation(information);
            record.setAccountInformation(this);
            return record;
        } else {
            record.setInformation(String.format("没有足够资金来调整授信,需要 %s,可用 %s.",
                    getCredit().subtract(newCredit), getUsableFund()));
            record.setAccountInformation(this);
            return record;
        }
    }

    public AccountOpRecord tradeCashFlowEvent(String tradeId, BigDecimal cashFlow, BigDecimal marginFlow) {
        ZonedDateTime nowTime = ZonedDateTime.now();
        AccountOpRecord record = new AccountOpRecord(getAccountId(), nowTime, AccountEvent.REEVALUATE_MARGIN, tradeId);
        if (!checkAccountStatus(record)) {
            throw new CustomException("操作失败,账户状态异常！");
        }
        if (payOff(cashFlow, true, record)) {
            setRealizedPNL(getRealizedPnL().add(cashFlow), record);
        }
        if (payOffMargin(marginFlow, true, record)){
            setMargin(getMargin().add(marginFlow), record);
        }
        record.setStatusToNormal();
        record.setAccountInformation(this);
        return record;
    }

    private Boolean payOff(BigDecimal totalPayOff, Boolean allowDebt, AccountOpRecord record) {
        if (totalPayOff.compareTo(BigDecimal.valueOf(0)) == 0)
            return true;
        boolean success = totalPayOff.compareTo(BigDecimal.valueOf(0)) < 0 ?
                flowOut(totalPayOff.abs(), allowDebt, record) : flowIn(totalPayOff, record);
        return success;
    }

    private Boolean payOffMargin(BigDecimal totalPayOff, Boolean allowDebt, AccountOpRecord record) {
        if (totalPayOff.compareTo(BigDecimal.valueOf(0)) == 0)
            return true;
        boolean success = totalPayOff.compareTo(BigDecimal.valueOf(0)) > 0 ?
                flowOut(totalPayOff, allowDebt, record) : flowIn(totalPayOff.abs(), record);
        return success;
    }
    //花钱了, 先用现金再用授信,特殊情况下，可以用债务
    private boolean flowOut(BigDecimal number, Boolean allowDebt, AccountOpRecord record) {
        if (number.compareTo(BigDecimal.valueOf(0)) < 0) {
            record.setInformation(String.format("Outgoing fund is negative, %s", number));
            return false;
        }

        //use cash first
        if (getCash().compareTo(number) >= 0) {
            setCash(getCash().subtract(number), record);
            return true;
        } else if (getUsableFund().compareTo(number) >= 0) {
            setCreditUsed(getCreditUsed().add(number.subtract(getCash())), record);
            setCash(BigDecimal.valueOf(0), record);
            return true;
        } else if (allowDebt) {
            setDebt(number.subtract(getUsableFund()), record);
            setCreditUsed(new BigDecimal(getCredit().toString()), record);
            setCash(BigDecimal.valueOf(0), record);
            return true;
        } else {
            record.setInformation(String.format(
                    "没有足够资金，当前可用资金为: %s.", getUsableFund()));
            return false;
        }
    }

    //钱进来了，先填债务，授信再填现金
    private boolean flowIn(BigDecimal number, AccountOpRecord record) {
        if (number.compareTo(BigDecimal.valueOf(0)) < 0) {
            record.setInformation(String.format("流入资金无效, %s.", number));
            return false;
        }

        if (getDebt().compareTo(BigDecimal.valueOf(0)) > 0) {
            if (getDebt().compareTo(number) > 0) {
                setDebt(getDebt().subtract(number), record);
                return true;
            } else {
                number = number.subtract(getDebt());
                setDebt(BigDecimal.valueOf(0), record);
            }
        }

        if (getCreditUsed().compareTo(number) >= 0) {
            setCreditUsed(getCreditUsed().subtract(number), record);
            return true;
        } else {
            BigDecimal toCash = number.subtract(getCreditUsed());
            setCreditUsed(BigDecimal.valueOf(0), record);
            setCash(toCash.add(getCash()), record);
            return true;
        }
    }

    // -----------------自定义相关参数获取方法----------------
    public BigDecimal getUsableCredit() {
        return getCredit().subtract(getCreditUsed());
    }

    public BigDecimal getWithdrawableCash() {
        //可提取资金不大于 max(现金余额 - (授信额度 - 授信余额) - 负债, 0)
        return getCash().subtract(getCreditUsed()).subtract(getDebt()).max(BigDecimal.valueOf(0));
    }

    public BigDecimal getUsableFund() {
        return getCash().add(getUsableCredit()).subtract(getDebt());
    }

    public BigDecimal getAsset() {
        return getPremium().add(getMargin()).add(getCash());
    }

    public BigDecimal getLiability() {
        return getCreditUsed().add(getDebt());
    }

    public BigDecimal getEquity() {
        return getNetDeposit();
    }

    public boolean checkAccountStatus(AccountOpRecord record) {
        if (getNormalStatus()) {
            return true;
        } else {
            record.setInformation(this.accountInformation);
            // TODO(http://jira.tongyu.tech/browse/OTMS-2294): 暂时disable检查
            return false;
        }
    }

    // --------------------属性特殊Set方法--------------------
    public void setMargin(BigDecimal margin, AccountOpRecord record) {
        BigDecimal diff = margin.subtract(getMargin());
        record.addMarginChange(diff);
        this.margin = margin;
    }

    public void setCash(BigDecimal cash, AccountOpRecord record) {
        BigDecimal diff = cash.subtract(getCash());
        record.addCashChange(diff);
        this.cash = cash;
    }

    public void setPermium(BigDecimal premium, AccountOpRecord record) {
        BigDecimal diff = premium.subtract(getPremium());
        record.addPremiumChange(diff);
        this.premium = premium;
    }

    public void setCreditUsed(BigDecimal creditUsed, AccountOpRecord record) {
        BigDecimal diff = creditUsed.subtract(getCreditUsed());
        record.addCreditUsedChange(diff);
        this.creditUsed = creditUsed;
    }

    public void setNetDeposit(BigDecimal netDeposit, AccountOpRecord record) {
        BigDecimal diff = netDeposit.subtract(getNetDeposit());
        record.addNetDepositChange(diff);
        this.netDeposit = netDeposit;

    }

    public void setRealizedPNL(BigDecimal realizedPNL, AccountOpRecord record) {
        BigDecimal diff = realizedPNL.subtract(getRealizedPnL());
        record.addRealizedPnLChange(diff);
        this.realizedPnL = realizedPNL;
    }

    public void setCredit(BigDecimal credit, AccountOpRecord record) {
        BigDecimal diff = credit.subtract(getCredit());
        record.addCreditChange(diff);
        this.credit = credit;
    }

    public void setDebt(BigDecimal debt, AccountOpRecord record) {
        BigDecimal diff = debt.subtract(getDebt());
        record.addDebtChange(diff);
        this.debt = debt;
    }

    public BigDecimal getCounterPartyCreditUsed(){
        return this.counterPartyCredit.subtract(this.counterPartyCreditBalance);
    }

    // ---------------------Property Generate Get/Set Method-----------------

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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
