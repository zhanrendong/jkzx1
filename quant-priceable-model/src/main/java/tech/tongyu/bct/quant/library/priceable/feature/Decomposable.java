package tech.tongyu.bct.quant.library.priceable.feature;

import tech.tongyu.bct.quant.library.priceable.Position;

import java.util.List;

public interface Decomposable {
    List<Position> decompose(String positionId);
}
