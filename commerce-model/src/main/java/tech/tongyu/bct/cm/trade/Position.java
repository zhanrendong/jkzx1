package tech.tongyu.bct.cm.trade;

import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.reference.elemental.Party;

import java.math.BigDecimal;
import java.util.List;

/**
 * A POSITION records that a particular PARTY is playing a ROLE on an ASSET,
 * holding a particular quantity. The quantity is represented as a real
 * fraction. The Firm maintains a view of its own positions and on occasion
 * the position of clients. A POSITION indicates a particular PARTYs view of
 * the asset (and their legal entitlement to an Asset). However, POSITION
 * in the CM is NOT the aggregated quantity of an ASSET resulting from
 * TRADEs or TRADE CONTRATs; it is purely a multipler of an individual ASSET.
 * <p>
 * An example is where the Firm holds 100 shares in IBM stocks in one
 * particular trade.
 *
 * @tparam A
 */
public interface Position<A extends Asset<InstrumentOfValue>> {
    /**
     * The Asset this position scales
     **/
    A asset();

    /**
     * The parties playing a legal role on the Asset
     */
    Party counterparty();

    /**
     * A multiplier to scale the Asset by for the Party in this Position
     */
    BigDecimal quantity();

    /**
     * The collection of Account Allocations for this Position. In PreTrade
     * this many be an empty collection
     */
    List<AccountAllocation> accountAllocations();

    /**
     * This optionally associates a BOOK to a POSITION.
     *
     * There are 3 possible values this could take right now:
     * 1) PositionBookDirect: a direct reference to Book
     * 2) PositionBookByAccount: an indirect reference to Book, via Account
     * 3) NONE - There's no Book associated with this Position. This may be
     * used more in the pre Trade space when Book are not needed
     *
     */
    //def positionBook: Optional[PositionBook]
}
