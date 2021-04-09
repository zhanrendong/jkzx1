package tech.tongyu.bct.cm.product.iov.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.product.iov.BasketInstrumentConstituent;
import tech.tongyu.bct.cm.product.iov.InstrumentAssetClassTypeEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;

import java.util.List;
import java.util.stream.Collectors;

public class BasketInstrument implements tech.tongyu.bct.cm.product.iov.BasketInstrument {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public List<BasketInstrumentConstituent> constituents;

    public String basketId;

    public BasketInstrument() {
    }

    public BasketInstrument(String basketId, List<BasketInstrumentConstituent> constituents) {
        this.basketId = basketId;
        this.constituents = constituents;
    }

    public static List<BasketInstrumentConstituent> createBasket(List<? extends InstrumentOfValue> instruments) {
        Double multiplier = 1.0 / instruments.size();
        return instruments.stream()
                .map(i ->
                        new tech.tongyu.bct.cm.product.iov.impl.BasketInstrumentConstituent(i, multiplier))
                .collect(Collectors.toList());
    }

    @Override
    public List<BasketInstrumentConstituent> constituents() {
        return constituents;
    }

    @Override
    public String instrumentId() {
        return basketId;
    }

    @Override
    public List<InstrumentOfValuePartyRoleTypeEnum> roles() {
        return constituents()
                .stream()
                .flatMap(c -> c.instrument().roles().stream())
                .collect(Collectors.toList());
    }

    @Override
    public InstrumentAssetClassTypeEnum assetClassType() {
        return InstrumentAssetClassTypeEnum.OTHER;
    }

    @Override
    public List<CurrencyUnit> contractCurrencies() {
        return constituents()
                .stream()
                .flatMap(c -> c.instrument().contractCurrencies().stream())
                .collect(Collectors.toList());
    }
}
