package tech.tongyu.bct.cm.trade;

import tech.tongyu.bct.cm.reference.elemental.Account;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * ACCOUNT ALLOCATIONs represent the relationship between POSITIONS and
 * ACCOUNTS. They indicate the scalar quantity of a POSITION allocated to an
 * ACCOUNT on the Books and Records of an Booking Entity. The quantity
 * represented is a real fraction (numerator/denominator).
 */
public interface AccountAllocation {
    /**
     * The account for this position of the allocation
     *
     * @return
     */
    Account account();

    /**
     * The quantity of the Position this Account Allocation makes up
     **/
    BigDecimal positionQuantityMultiplier();

    /**
     * Comment Text applied to a specific version of an ACCOUNT ALLOCATION
     *
     * @return
     */
    Optional<String> comment();
}
