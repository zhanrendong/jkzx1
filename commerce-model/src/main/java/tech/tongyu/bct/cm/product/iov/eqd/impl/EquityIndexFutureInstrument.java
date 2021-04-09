package tech.tongyu.bct.cm.product.iov.eqd.impl;

import tech.tongyu.bct.cm.core.BusinessCenterEnum;
import tech.tongyu.bct.cm.product.iov.FutureInstrument;
import tech.tongyu.bct.cm.product.iov.InstrumentAssetClassTypeEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.cmd.impl.CommodityInstrument;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class EquityIndexFutureInstrument implements FutureInstrument<EquityIndexInstrument> {

    public LocalDate issueDate;

    public LocalDate maturityDate;

    public EquityIndexInstrument underlyer;

    public CurrencyUnit currency;

    public String instrumentId;

    public BusinessCenterEnum venue;


    public EquityIndexFutureInstrument() {

    }

    public EquityIndexFutureInstrument(LocalDate maturityDate, EquityIndexInstrument underlyer, String instrumentId) {
        this.currency = CurrencyUnit.CNY;
        this.maturityDate = maturityDate;
        this.underlyer = underlyer;
        this.instrumentId = instrumentId;
        //change venue code then update this:
        this.venue = null;
    }

    public EquityIndexFutureInstrument(LocalDate maturityDate, String underlyerInsturmentId, String instrumentId) {
        this(maturityDate, new EquityIndexInstrument(underlyerInsturmentId), instrumentId);
    }

    public EquityIndexFutureInstrument(String underlyerInsturmentId, String instrumentId) {
        this(null, new EquityIndexInstrument(underlyerInsturmentId), instrumentId);
    }

    public EquityIndexFutureInstrument(String instrumentId) {
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
    public EquityIndexInstrument underlyer() {
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
        return InstrumentAssetClassTypeEnum.EQUITY;
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
