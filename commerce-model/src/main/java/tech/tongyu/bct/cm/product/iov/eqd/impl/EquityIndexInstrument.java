package tech.tongyu.bct.cm.product.iov.eqd.impl;

import tech.tongyu.bct.cm.core.BusinessCenterEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentAssetClassTypeEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.ListedInstrument;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;

import java.util.List;

public class EquityIndexInstrument implements ListedInstrument {

    public String instrumentId;
    public BusinessCenterEnum venue;
    public InstrumentAssetClassTypeEnum assetClassType;
    public ProductTypeEnum productType;

    public EquityIndexInstrument(String instrumentId, BusinessCenterEnum venue, InstrumentAssetClassTypeEnum assetClassType, ProductTypeEnum productType) {
        this.instrumentId = instrumentId;
        this.venue = venue;
        this.assetClassType = assetClassType;
        this.productType = productType;
    }

    public EquityIndexInstrument() {
    }


    public EquityIndexInstrument(String instrumentId) {
        this.instrumentId = instrumentId;
    }


    @Override
    public String instrumentId() {
        return instrumentId;
    }

    @Override
    public BusinessCenterEnum venue() {
        return venue;
    }


    @Override
    public CurrencyUnit currency() {
        return null;
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public List<InstrumentOfValuePartyRoleTypeEnum> roles() {
        return null;
    }

    @Override
    public InstrumentAssetClassTypeEnum assetClassType() {
        return InstrumentAssetClassTypeEnum.EQUITY;
    }

    @Override
    public ProductTypeEnum productType() {
        return productType;
    }

    @Override
    public List<CurrencyUnit> contractCurrencies() {
        return null;
    }
}
