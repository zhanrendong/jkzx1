package tech.tongyu.bct.cm.product.iov.cmd.impl;

import tech.tongyu.bct.cm.core.BusinessCenterEnum;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class CommodityFutureInstrument implements FutureInstrument<CommodityInstrument> {

    public LocalDate issueDate;

    public LocalDate maturityDate;

    public CommodityInstrument underlyer;

    public CurrencyUnit currency;

    public InstrumentAssetClassTypeEnum assetClassType;

    public String instrumentId;

    public BusinessCenterEnum venue;


    public CommodityFutureInstrument() {

    }

    public CommodityFutureInstrument(LocalDate maturityDate, CommodityInstrument underlyer, String instrumentId) {
        this.currency = CurrencyUnit.CNY;
        this.maturityDate = maturityDate;
        this.underlyer = underlyer;
        this.assetClassType = InstrumentAssetClassTypeEnum.COMMODITY;
        this.instrumentId = instrumentId;
        //change venue code then update this:
        this.venue = null;
    }

    public CommodityFutureInstrument(LocalDate maturityDate, String underlyerInsturmentId, String instrumentId) {
        this(maturityDate, new CommodityInstrument(underlyerInsturmentId), instrumentId);
    }

    public CommodityFutureInstrument(String underlyerInsturmentId, String instrumentId) {
        this(null, new CommodityInstrument(underlyerInsturmentId), instrumentId);
    }

    public CommodityFutureInstrument( String instrumentId) {
        this(null, instrumentId);
    }

    public String underlyerInstrumentId() {
        return underlyer.instrumentId();
    }

    @Override
    public LocalDate issueDate() {
        return issueDate;
    }

    @Override
    public LocalDate maturityDate() {
        return maturityDate;
    }

    @Override
    public CommodityInstrument underlyer() {
        return underlyer;
    }

    @Override
    public CurrencyUnit currency() {
        return currency;
    }

    @Override
    public String description() {
        return null;
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
    public InstrumentAssetClassTypeEnum assetClassType() {
        return assetClassType;
    }

    @Override
    public List<InstrumentOfValuePartyRoleTypeEnum> roles() {
        return null;
    }

    @Override
    public ProductTypeEnum productType() {
        return ProductTypeEnum.FUTURE;
    }

    @Override
    public List<CurrencyUnit> contractCurrencies() {
        return Arrays.asList(currency);
    }
}
