package tech.tongyu.bct.cm.reference.impl;

import tech.tongyu.bct.cm.reference.elemental.Account;

import java.util.UUID;

public class SimpleAccount implements Account {
    public UUID accountUUID;

    public String accountCode;

    public String accountName;

    public SimpleAccount() {
    }

    public SimpleAccount(UUID accountUUID, String accountCode, String accountName) {
        this.accountUUID = accountUUID;
        this.accountCode = accountCode;
        this.accountName = accountName;
    }

    @Override
    public UUID accountUUID() {
        return accountUUID;
    }

    @Override
    public String accountCode() {
        return accountCode;
    }

    @Override
    public String accountName() {
        return accountName;
    }
}
