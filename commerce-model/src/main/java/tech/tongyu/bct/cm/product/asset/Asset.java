package tech.tongyu.bct.cm.product.asset;

import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;

/**
 * Assets define a set of Roles on Instruments of Value for the purpose of
 * Position keeping
 */
public interface Asset<I extends InstrumentOfValue> {
    /**
     * A multiplier used to scale the quantity of an Asset. A factor of 1
     * represents 100%, whereas a factory of .8 represents a 20% unwind.
     *
     * @return
     */
    SortedMap<LocalDate, BigDecimal> quantityFactors();

    /**
     * An INSTRUMENT OF VALUE LEGAL PARTY ROLE binds an IOV to an Asset in the
     * context of parties playing a particular role or roles. It represents
     * the legal entitlement of a PARTY to rights and obligations as defined
     * by the PAYOFF term. Examples include MSCO playing the Role of 'Payer of
     * FIXED' on an IRS; Company A as 'owner' of gold; Company B as 'Holder'
     * of a fixed income security etc.
     *
     * @return
     */
    List<InstrumentOfValueLegalPartyRole<I>> instrumentOfValueLegalPartyRoles();

    I instrumentOfValue();

    List<LegalPartyRole> legalPartyRoles();
}
