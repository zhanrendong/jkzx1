package tech.tongyu.bct.cm.product.iov.cmd.impl;


import tech.tongyu.bct.cm.core.BusinessCenterEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentAssetClassTypeEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;

import java.util.Arrays;
import java.util.List;

public class CommodityInstrument implements InstrumentOfValue {
    public String instrumentId;

    public String description;

    public CurrencyUnit currency;

    public BusinessCenterEnum venue;

    public CommodityInstrument() {
    }

    public CommodityInstrument(String instrumentId, String description, CurrencyUnit currency, BusinessCenterEnum venue) {
        this.instrumentId = instrumentId;
        this.description = description;
        this.currency = currency;
        this.venue = venue;
    }

    public CommodityInstrument(String instrumentId, String description, BusinessCenterEnum venue) {
        this.instrumentId = instrumentId;
        this.description = description;
        this.currency = CurrencyUnit.CNY;
        this.venue = venue;
    }

    public CommodityInstrument(String instrumentId) {
        this.description = instrumentId;
        this.instrumentId = instrumentId;
        this.currency  = CurrencyUnit.CNY;
    }

    @Override
    public InstrumentAssetClassTypeEnum assetClassType() {
        return InstrumentAssetClassTypeEnum.COMMODITY;
    }

    @Override
    public String instrumentId() {
        return instrumentId;
    }

    public CurrencyUnit currency() {
        return currency;
    }


    @Override
    public List<InstrumentOfValuePartyRoleTypeEnum> roles() {
        return Arrays.asList(InstrumentOfValuePartyRoleTypeEnum.BUYER, InstrumentOfValuePartyRoleTypeEnum.SELLER);
    }

    @Override
    public ProductTypeEnum productType() {
        return null;
    }

    @Override
    public List<CurrencyUnit> contractCurrencies() {
        return Arrays.asList(currency());
    }

    private Boolean isNumeric(String str) {
        try {
            Integer.valueOf(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private BusinessCenterEnum fromInstrument(String code) {
        if (code.equalsIgnoreCase("sh")) {
            return BusinessCenterEnum.SSE;
        } else if (code.equalsIgnoreCase("SZ")) {
            return BusinessCenterEnum.SZSE;
        } else {
            throw new IllegalArgumentException(String.format("不支持的交易所代码缩写:%s", code));
        }
    }
}
