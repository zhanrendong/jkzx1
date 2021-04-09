package tech.tongyu.bct.cm.product.asset;

import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.reference.elemental.Account;
import tech.tongyu.bct.cm.reference.elemental.Party;

import java.util.Optional;

public interface InstrumentOfValueLegalPartyRole<I extends InstrumentOfValue> {
    I instrumentOfValue();

    Party party();

    InstrumentOfValuePartyRoleTypeEnum roleType();

    Optional<Account> account();


}
