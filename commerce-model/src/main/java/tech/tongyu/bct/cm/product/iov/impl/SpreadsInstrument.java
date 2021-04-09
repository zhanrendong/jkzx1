package tech.tongyu.bct.cm.product.iov.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.BasketInstrumentConstituent;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;

import java.util.List;
import java.util.stream.Collectors;

public class SpreadsInstrument implements tech.tongyu.bct.cm.product.iov.BasketInstrument {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public List<BasketInstrumentConstituent> constituents;

    public String basketId;

    public static String INSTRUMENT_ID_KEY = "underlyerInstrumentId";

    public static String MULTIPIER_KEY = "underlyerMultiplier";

    public static String INITIAL_SPOT_KEY = "initialSpot";

    public static String WEIGHT_KEY = "weight";

    public static String BASKET_ID_JOINER = "_";

    public SpreadsInstrument() {
    }

    public SpreadsInstrument(String basketId, List<BasketInstrumentConstituent> constituents) {
        this.basketId = basketId;
        this.constituents = constituents;
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
        return constituents().stream().flatMap(c -> c.instrument().roles().stream())
                .collect(Collectors.toList());
    }

    @Override
    public InstrumentAssetClassTypeEnum assetClassType() {
        return InstrumentAssetClassTypeEnum.OTHER;
    }

    @Override
    public List<CurrencyUnit> contractCurrencies() {
        return constituents().stream().flatMap(c -> c.instrument().contractCurrencies().stream())
                .collect(Collectors.toList());
    }

}
