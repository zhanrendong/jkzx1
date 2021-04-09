package tech.tongyu.bct.cm.trade.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.trade.Book;
import tech.tongyu.bct.cm.trade.Position;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Book does not support hierarchy
 */
public class BctBook implements Book {
    public String name;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public List<BctTradePosition> positions;

    public BctBook(String name, List<BctTradePosition> positions) {
        this.name = name;
        this.positions = positions;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<Position<Asset<InstrumentOfValue>>> positions() {
        return positions.stream().map(p -> (Position<Asset<InstrumentOfValue>>) p).collect(Collectors.toList());
    }
}
