package tech.tongyu.bct.cm.product.iov;

import tech.tongyu.bct.cm.core.BusinessCenterEnum;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;

public interface ListedInstrument extends InstrumentOfValue {


    String instrumentId();


    BusinessCenterEnum venue();

    CurrencyUnit currency();

    String description();

    InstrumentAssetClassTypeEnum assetClassType();
}
