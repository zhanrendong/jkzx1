package tech.tongyu.bct.cm.product.asset;

import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.reference.elemental.Account;
import tech.tongyu.bct.cm.reference.elemental.Party;

import java.util.Optional;

public interface LegalPartyRole {
    Party party();

    InstrumentOfValuePartyRoleTypeEnum roleType();

    Optional<Account> account();


}
