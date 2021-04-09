package tech.tongyu.bct.cm.product.iov.eqd;

import tech.tongyu.bct.cm.product.iov.InstrumentAssetClassTypeEnum;
import tech.tongyu.bct.cm.product.iov.ListedInstrument;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;

public interface ListedEquityInstrument extends ListedInstrument {
    @Override
    default InstrumentAssetClassTypeEnum assetClassType() {
        return InstrumentAssetClassTypeEnum.EQUITY;
    }

    @Override
    default ProductTypeEnum productType() {
        return ProductTypeEnum.CASH_PRODUCT;
    }
}
