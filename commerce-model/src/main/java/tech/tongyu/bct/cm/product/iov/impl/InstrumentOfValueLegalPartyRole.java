package tech.tongyu.bct.cm.product.iov.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.reference.elemental.Account;
import tech.tongyu.bct.cm.reference.elemental.Party;

import java.util.Optional;

public class InstrumentOfValueLegalPartyRole<I extends InstrumentOfValue>
        implements tech.tongyu.bct.cm.product.asset.InstrumentOfValueLegalPartyRole<I> {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public I instrument;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public Party party;

    public InstrumentOfValuePartyRoleTypeEnum role;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public Account account;

    public InstrumentOfValueLegalPartyRole() {
    }

    public InstrumentOfValueLegalPartyRole(I instrument, Party party, InstrumentOfValuePartyRoleTypeEnum role, Account account) {
        this.instrument = instrument;
        this.party = party;
        this.role = role;
        this.account = account;
    }

    public InstrumentOfValueLegalPartyRole(I instrument, Party party, InstrumentOfValuePartyRoleTypeEnum role) {
        this.instrument = instrument;
        this.party = party;
        this.role = role;
        this.account = null;
    }

    @Override
    public I instrumentOfValue() {
        return null;
    }

    @Override
    public Party party() {
        return null;
    }

    @Override
    public InstrumentOfValuePartyRoleTypeEnum roleType() {
        return null;
    }

    @Override
    public Optional<Account> account() {
        return Optional.ofNullable(account);
    }
}
