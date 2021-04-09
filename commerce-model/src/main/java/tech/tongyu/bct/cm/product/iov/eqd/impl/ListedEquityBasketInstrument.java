package tech.tongyu.bct.cm.product.iov.eqd.impl;

import tech.tongyu.bct.cm.core.BusinessCenterEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.BasketInstrument;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;

import java.util.List;

public class ListedEquityBasketInstrument
        extends BasketInstrument
        implements tech.tongyu.bct.cm.product.iov.eqd.ListedEquityInstrument {

    public CurrencyUnit currency;

    public BusinessCenterEnum venue;

    public ListedEquityBasketInstrument() {

    }

    public ListedEquityBasketInstrument(String basketId, CurrencyUnit currency, BusinessCenterEnum venue,
                                        List<ListedEquityInstrument> instruments) {
        super(basketId, BasketInstrument.createBasket(instruments));
        this.currency = currency;
        this.venue = venue;
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
        StringBuffer sb = new StringBuffer();
        sb.append("Basket of EQUITY:\n");
        constituents.forEach(c -> sb.append("\t").append(c.instrument().instrumentId()).append(":").append(c.multiplier()));
        return sb.toString();
    }

    @Override
    public ProductTypeEnum productType() {
        return ProductTypeEnum.BASKET;
    }
}
