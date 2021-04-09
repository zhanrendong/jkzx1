package tech.tongyu.bct.cm.reference.impl;

import tech.tongyu.bct.cm.reference.elemental.Party;

import java.util.UUID;

public class SimpleParty implements Party {
    public UUID partyUUID;

    public String partyName;

    public String partyCode;

    public SimpleParty() {
    }

    public SimpleParty(UUID partyUUID, String partyCode, String partyName) {
        this.partyUUID = partyUUID;
        this.partyCode = partyCode;
        this.partyName = partyName;
    }

    @Override
    public UUID partyUUID() {
        return partyUUID;
    }

    @Override
    public String partyName() {
        return partyName;
    }

    @Override
    public String partyCode() {
        return partyCode;
    }
}
