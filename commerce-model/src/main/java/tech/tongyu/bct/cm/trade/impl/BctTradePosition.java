package tech.tongyu.bct.cm.trade.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.reference.elemental.Party;
import tech.tongyu.bct.cm.reference.elemental.Account;
import tech.tongyu.bct.cm.trade.AccountAllocation;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.Position;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class BctTradePosition implements Position<Asset<InstrumentOfValue>> {
    public static final String assetFieldName = "asset";
    public static final String positionAccountFieldName = "positionAccount";
    public static final String counterpartyAccountFieldName = "counterpartyAccount";
    public static final String counterpartyFieldName = "counterparty";

    public String bookName;

    public String positionId;

    public String userLoginId;

    public BigDecimal quantity;

    public LCMEventTypeEnum lcmEventType;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @JsonProperty(counterpartyFieldName)
    public Party counterparty;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @JsonProperty(assetFieldName)
    public Asset<InstrumentOfValue> asset;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @JsonProperty(positionAccountFieldName)
    public Account positionAccount;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @JsonProperty(counterpartyAccountFieldName)
    public Account counterpartyAccount;

    public BctTradePosition() {
    }

    public BctTradePosition(String positionId){
        this.positionId = positionId;
    }

    public BctTradePosition(String bookName, String positionId, Party counterparty, BigDecimal quantity,
                            Asset<InstrumentOfValue> asset, Account positionAccount, Account counterpartyAccount) {
        this.bookName = bookName;
        this.positionId = positionId;
        this.counterparty = counterparty;
        this.quantity = quantity;
        this.asset = asset;
        this.positionAccount = positionAccount;
        this.counterpartyAccount = counterpartyAccount;
    }

    public BctTradePosition(String bookName, String positionId, Party counterparty, Double quantity,
                            Asset<InstrumentOfValue> asset, Account positionAccount, Account counterpartyAccount) {
        this.bookName = bookName;
        this.positionId = positionId;
        this.counterparty = counterparty;
        this.quantity = BigDecimal.valueOf(quantity);
        this.asset = asset;
        this.positionAccount = positionAccount;
        this.counterpartyAccount = counterpartyAccount;
    }

    @Override
    public Asset<InstrumentOfValue> asset() {
        return asset;
    }

    @Override
    public Party counterparty() {
        return counterparty;
    }

    @Override
    public BigDecimal quantity() {
        return quantity;
    }

    @Override
    public List<AccountAllocation> accountAllocations() {
        return Arrays.asList(new tech.tongyu.bct.cm.trade.impl.AccountAllocation(counterpartyAccount, 1.0));
    }

    // TODO (http://jira.tongyu.tech:8080/browse/OTMS-1402): Asset.legalPartyRoles中每个Party是否只有1个Role?
    public InstrumentOfValuePartyRoleTypeEnum counterpartyRole() {
        return asset.legalPartyRoles().stream()
                .filter(pr -> pr.party().partyCode().equals(counterparty.partyCode()))
                .map(pr -> pr.roleType())
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "无法从Asset的LegalPartyRoles中找到counterparty（partyCode=%s,partyName=%s）",
                        counterparty().partyCode(), counterparty().partyName())));
    }

    public InstrumentOfValuePartyRoleTypeEnum partyRole() {
        InstrumentOfValuePartyRoleTypeEnum counterpartyRole = counterpartyRole();
        return asset.legalPartyRoles().stream()
                .filter(pr -> !pr.roleType().equals(counterpartyRole))
                .map(pr -> pr.roleType())
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "无法从Asset的LegalPartyRoles中找到counterparty（partyCode=%s,partyName=%s）",
                        counterparty().partyCode(), counterparty().partyName())));
    }

    public Party party() {
        return asset.legalPartyRoles().stream()
                .filter(pr -> !pr.party().partyCode().equals(counterparty.partyCode()))
                .map(pr -> pr.party())
                .findAny()
                .orElseThrow(() -> new IllegalStateException("无法从Asset的LegalPartyRoles中找到Party,该交易只定义了交易对手方"));
    }


    // ---------------------Property Generate Get/Set Method-----------------

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getUserLoginId() {
        return userLoginId;
    }

    public void setUserLoginId(String userLoginId) {
        this.userLoginId = userLoginId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public LCMEventTypeEnum getLcmEventType() {
        return lcmEventType;
    }

    public void setLcmEventType(LCMEventTypeEnum lcmEventType) {
        this.lcmEventType = lcmEventType;
    }

    public Party getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(Party counterparty) {
        this.counterparty = counterparty;
    }

    public Asset<InstrumentOfValue> getAsset() {
        return asset;
    }

    public void setAsset(Asset<InstrumentOfValue> asset) {
        this.asset = asset;
    }

    public Account getPositionAccount() {
        return positionAccount;
    }

    public void setPositionAccount(Account positionAccount) {
        this.positionAccount = positionAccount;
    }

    public Account getCounterpartyAccount() {
        return counterpartyAccount;
    }

    public void setCounterpartyAccount(Account counterpartyAccount) {
        this.counterpartyAccount = counterpartyAccount;
    }
}
