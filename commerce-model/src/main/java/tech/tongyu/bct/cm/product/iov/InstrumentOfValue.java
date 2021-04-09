package tech.tongyu.bct.cm.product.iov;

import tech.tongyu.bct.cm.product.iov.feature.ContractCurrencyFeature;

import java.util.List;

public interface InstrumentOfValue extends ContractCurrencyFeature {

    String instrumentId();

    List<InstrumentOfValuePartyRoleTypeEnum> roles();

    InstrumentAssetClassTypeEnum assetClassType();

    ProductTypeEnum productType();
}
