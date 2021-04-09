package tech.tongyu.bct.cm.trade;

import tech.tongyu.bct.cm.reference.elemental.Party;

/**
 * Defines the role a Party plays on an entity that have no bearing on the
 * economics of a trade. An Example is: TRADE CONTRACT NON ECONOMIC PARTY ROLE
 */
public interface NonEconomicPartyRole {
    Party party();

    NonEconomicPartyRoleType roleType();
}
