package tech.tongyu.bct.cm.trade.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.reference.elemental.Account;

import java.math.BigDecimal;
import java.util.Optional;

public class AccountAllocation implements tech.tongyu.bct.cm.trade.AccountAllocation {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public Account account;

    public BigDecimal positionQuantityMultiplier;

    public String comment;

    public AccountAllocation() {
    }

    public AccountAllocation(Account account, Double positionQuantityMultiplier, String comment) {
        this.account = account;
        this.positionQuantityMultiplier = BigDecimal.valueOf(positionQuantityMultiplier);
        this.comment = comment;
    }

    public AccountAllocation(Account account, Double positionQuantityMultiplier) {
        this.account = account;
        this.positionQuantityMultiplier = BigDecimal.valueOf(positionQuantityMultiplier);
        this.comment = null;
    }

    public AccountAllocation(Account account) {
        this.account = account;
        this.positionQuantityMultiplier = BigDecimal.valueOf(1.0);
        this.comment = null;
    }

    @Override
    public Account account() {
        return account;
    }

    @Override
    public BigDecimal positionQuantityMultiplier() {
        return positionQuantityMultiplier;
    }

    @Override
    public Optional<String> comment() {
        return Optional.ofNullable(comment);
    }
}
