package tech.tongyu.bct.cm.trade;

import java.util.Collections;
import java.util.List;

/**
 * A grouping of Firm POSITIONs that enforces control. Some of the controls
 * include:
 * i. All executed Firm POSITIONs will appear in exactly one BOOK. (A
 * prospective POSITION can also be included in at most one BOOK, but does
 * not have to be);
 * ii. A BOOK is the basis for identifying the accountability for the
 * economic performance of the POSITIONs it contains;
 * iii. Each BOOK is small enough that the lowest level of trader signoff is
 * on a whole BOOK (or a collection of whole BOOKS), but not on a subset of
 * POSITIONs in a BOOK.
 */
public interface Book extends PositionSet {
    String name();

    @Override
    default List<PositionSet> children(){
        return Collections.emptyList();
    }
}
