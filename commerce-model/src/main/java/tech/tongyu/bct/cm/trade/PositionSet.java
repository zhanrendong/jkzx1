package tech.tongyu.bct.cm.trade;

import io.vavr.collection.Seq;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;

import java.util.List;

/**
 * A collection of Position where it is required that any position be
 * included at most once. A set of Position may produce netting benefits
 * that are recorded against the PositionSet
 */
public interface PositionSet {

    List<Position<Asset<InstrumentOfValue>>> positions();

    /**
     * A sequence of child PositionSet for this PositionSet. The Parent always
     * owns this relationship. This is designed to represent hierarchies in
     * PositionSet. For example, between Book and BookRollUpNode,
     * BookRollUpNodes, see below:
     * <pre>
     * A    <--  Book Roll Up Node
     * / \
     * /   \
     * Book Roll Up Node -->   B   C  <-- Book
     * /
     * /
     * Book --> D
     * </pre>
     * '''A''' represents the top level a leaf node in a Book Roll Up hierarchy
     * '''B''' is a child of '''A''' and represents a leaf node in a Book Roll
     * Up hierarchy, i.e a BookRollUpNode
     * '''C''' is a child of '''A''' and represents a leaf node in a Book Roll
     * Up hierarchy, i.e a Book
     * '''D''' is a child of '''B''' (B plays parent in this relationship) and
     * represents a leaf node in a Book Roll Up hierarchy, i.e. a Book
     *
     * @return
     */
    List<PositionSet> children();
}

