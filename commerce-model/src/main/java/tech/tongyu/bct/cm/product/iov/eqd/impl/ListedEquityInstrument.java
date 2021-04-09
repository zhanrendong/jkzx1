package tech.tongyu.bct.cm.product.iov.eqd.impl;

import tech.tongyu.bct.cm.core.BusinessCenterEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;

import java.util.Arrays;
import java.util.List;

public class ListedEquityInstrument implements tech.tongyu.bct.cm.product.iov.eqd.ListedEquityInstrument {

    public String instrumentId;

    public String description;

    public CurrencyUnit currency;

    public BusinessCenterEnum venue;

    public ListedEquityInstrument() {
    }

    public ListedEquityInstrument(String instrumentId, String description, CurrencyUnit currency, BusinessCenterEnum venue) {
        this.instrumentId = instrumentId;
        this.description = description;
        this.currency = currency;
        this.venue = venue;
    }

    public ListedEquityInstrument(String instrumentId, String description, BusinessCenterEnum venue) {
        this.instrumentId = instrumentId;
        this.description = description;
        this.currency = CurrencyUnit.CNY;
        this.venue = venue;
    }

    public ListedEquityInstrument(String instrumentId) {
        this.description = instrumentId;
        this.instrumentId = instrumentId;
        String[] parts = instrumentId.split(".");
        if (parts.length == 2) {
            String first = parts[0];
            String second = parts[1];
            if (isNumeric(first) && (!isNumeric(second))) {
                this.venue = fromInstrument(second);
            } else if (isNumeric(second) && (!isNumeric(first))) {
                this.venue = fromInstrument(first);
            } else {
                throw new IllegalArgumentException(String.format("instrument id的格式错误:%s", instrumentId));
            }
        }
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
        return currency;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public List<InstrumentOfValuePartyRoleTypeEnum> roles() {
        return Arrays.asList(InstrumentOfValuePartyRoleTypeEnum.BUYER, InstrumentOfValuePartyRoleTypeEnum.SELLER);
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
        } else if (code.equalsIgnoreCase("sz")) {
            return BusinessCenterEnum.SZSE;
        } else {
            throw new IllegalArgumentException(String.format("不支持的交易所代码缩写:%s", code));
        }
    }
}
