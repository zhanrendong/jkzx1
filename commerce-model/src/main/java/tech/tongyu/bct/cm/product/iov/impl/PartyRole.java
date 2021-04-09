package tech.tongyu.bct.cm.product.iov.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.product.asset.LegalPartyRole;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.reference.elemental.Account;
import tech.tongyu.bct.cm.reference.elemental.Party;

import java.util.Optional;

public class PartyRole implements LegalPartyRole {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public Party party;

    public InstrumentOfValuePartyRoleTypeEnum role;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public Account account;

    public PartyRole() {
    }

    public PartyRole(Party party, InstrumentOfValuePartyRoleTypeEnum role, Account account) {
        this.party = party;
        this.role = role;
        this.account = account;
    }

    public PartyRole(Party party, InstrumentOfValuePartyRoleTypeEnum role) {
        this.party = party;
        this.role = role;
        this.account = null;
    }

    @Override
    public Party party() {
        return party;
    }

    @Override
    public InstrumentOfValuePartyRoleTypeEnum roleType() {
        return role;
    }

    @Override
    public Optional<Account> account() {

        return Optional.ofNullable(account);
    }
}
