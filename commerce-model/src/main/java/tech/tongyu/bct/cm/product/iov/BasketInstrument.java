package tech.tongyu.bct.cm.product.iov;

import java.util.List;

public interface BasketInstrument extends InstrumentOfValue {
    List<BasketInstrumentConstituent> constituents();

    default ProductTypeEnum productType() {
        return ProductTypeEnum.BASKET;
    }
}
