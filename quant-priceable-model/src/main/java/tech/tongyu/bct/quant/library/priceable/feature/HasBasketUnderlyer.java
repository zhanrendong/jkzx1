package tech.tongyu.bct.quant.library.priceable.feature;

import tech.tongyu.bct.quant.library.priceable.Priceable;

import java.util.List;

public interface HasBasketUnderlyer {
    List<Priceable> underlyers();
}
